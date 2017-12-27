/*******************************************************************************
 * Copyright 2017 jamietech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package ch.jamiete.hilda.moderatortools.runnables;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import ch.jamiete.hilda.Hilda;
import ch.jamiete.hilda.moderatortools.commands.ArchiveCommand;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageHistory;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

public class ArchiveTask implements Runnable {
    private final ArchiveCommand command;
    private final TextChannel channel;
    private final User user;
    private final ArrayList<String> files = new ArrayList<String>();
    private final SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd kk:mm:ss");

    public ArchiveTask(final ArchiveCommand command, final TextChannel channel) {
        this(command, channel, null);
    }

    public ArchiveTask(final ArchiveCommand command, final TextChannel channel, final User user) {
        this.command = command;
        this.channel = channel;
        this.user = user;
    }

    public TextChannel getChannel() {
        return this.channel;
    }

    @Override
    public void run() {
        try {
            Hilda.getLogger().info("Started archiving " + this.channel.getName() + " in " + this.channel.getGuild().getName());

            final MessageHistory history = this.channel.getHistory();

            while (true) {
                Hilda.getLogger().fine("Getting 100 messages");
                final List<Message> messages = history.retrievePast(100).complete();

                if (messages.isEmpty()) {
                    break;
                }

                try {
                    final File temporary = File.createTempFile("archive-" + this.channel.getId(), null);
                    final BufferedWriter out = new BufferedWriter(new FileWriter(temporary));

                    this.files.add(temporary.getAbsolutePath());

                    final ArrayList<String> lines = new ArrayList<String>();

                    for (final Message message : messages) {
                        final StringBuilder line = new StringBuilder();

                        line.append("[").append(this.sdf.format(Date.from(message.getCreationTime().toInstant()))).append("] ");

                        line.append("<").append(message.getAuthor().getName()).append("#").append(message.getAuthor().getDiscriminator()).append(">");
                        line.append(" ").append(message.getContentDisplay());

                        if (message.isEdited()) {
                            line.append(" (edited)");
                        }

                        lines.add(line.toString());
                    }

                    Collections.reverse(lines);

                    for (final String line : lines) {
                        out.write(line);
                        out.newLine();
                    }

                    out.close();
                } catch (final Exception e) {
                    Hilda.getLogger().log(Level.SEVERE, "Failed to save archive part", e);
                }

                if (messages.size() < 100) {
                    break;
                }
            }

            Hilda.getLogger().fine("Creating full archive file...");

            File archive;

            try {
                final File directory = new File("archives");

                if (!directory.exists()) {
                    directory.mkdir();
                }

                archive = new File(directory, this.channel.getGuild().getId() + "-" + this.channel.getName() + ".log");
                final BufferedWriter out = new BufferedWriter(new FileWriter(archive, false));

                final String[] headers = new String[] { "----------------------------------", "- L O G                  F I L E", "-               for", "- " + this.channel.getId(), "- " + this.channel.getName(), "- on " + this.channel.getGuild().getId(), "- " + this.channel.getGuild().getName(), "-", "- Generated on " + this.sdf.format(new Date(System.currentTimeMillis())), "----------------------------------" };

                for (final String header : headers) {
                    out.write(header);
                    out.newLine();
                }

                out.newLine();

                Collections.reverse(this.files);

                for (final String path : this.files) {
                    final File temp = new File(path);

                    if (!temp.exists()) {
                        Hilda.getLogger().severe("File " + path + " gone missing!");
                        continue;
                    }

                    final BufferedReader in = new BufferedReader(new FileReader(temp));

                    String line;

                    while ((line = in.readLine()) != null) {
                        out.write(line);
                        out.newLine();
                    }

                    out.flush();
                    in.close();
                    temp.delete();
                }

                out.close();
            } catch (final Exception e) {
                Hilda.getLogger().log(Level.SEVERE, "Something went wrong while making archive file", e);
                this.channel.sendMessage("Something went wrong while archiving the channel. Talk to an administrator.").queue();
                this.command.remove(this);
                return;
            }

            Hilda.getLogger().info("Finished archiving " + this.channel.getName() + " in " + this.channel.getGuild().getName());
            this.channel.sendMessage("Finished archiving channel!").queue();

            if (this.user != null) {
                final PrivateChannel channel = this.user.openPrivateChannel().complete();

                if (archive.length() > 5000000) {
                    channel.sendMessage("The archive file was too large to send. Talk to an administrator for a copy.").queue();
                } else {
                    channel.sendFile(archive, new MessageBuilder().append("Here is your archive!").build()).queue();
                }
            }
        } catch (final Exception e) {
            Hilda.getLogger().log(Level.SEVERE, "Something went wrong while archiving", e);
        } finally {
            this.command.remove(this);
        }
    }

}
