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
package ch.jamiete.hilda.moderatortools.commands;

import java.util.ArrayList;
import ch.jamiete.hilda.Hilda;
import ch.jamiete.hilda.commands.ChannelCommand;
import ch.jamiete.hilda.moderatortools.runnables.ArchiveTask;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public class ArchiveCommand extends ChannelCommand {
    private final ArrayList<ArchiveTask> tasks = new ArrayList<ArchiveTask>();

    public ArchiveCommand(final Hilda hilda) {
        super(hilda);

        this.setName("archive");
        this.setMinimumPermission(Permission.ADMINISTRATOR);
        this.setDescription("Archives the channel to disk.");
    }

    @Override
    public void execute(final Message message, final String[] arguments, final String label) {
        if (!message.getGuild().getSelfMember().hasPermission(message.getTextChannel(), Permission.MESSAGE_HISTORY)) {
            this.reply(message, "I do not have permission to view this channel's message history.");
            return;
        }

        if (this.hasTask(message.getTextChannel())) {
            this.reply(message, "I'm already archiving this channel.");
            return;
        }

        final ArchiveTask task = new ArchiveTask(this, message.getTextChannel(), message.getAuthor());
        this.tasks.add(task);
        this.hilda.getExecutor().submit(task);

        this.reply(message, "OK, started archiving all messages in this channel before this message...");
    }

    public boolean hasTask(final TextChannel channel) {
        return this.tasks.stream().filter(task -> task.getChannel() == channel).findAny().isPresent();
    }

    public void remove(final ArchiveTask task) {
        this.tasks.remove(task);
    }

}
