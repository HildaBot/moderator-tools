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
import java.util.Collections;
import java.util.List;
import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;
import ch.jamiete.hilda.Hilda;
import ch.jamiete.hilda.Util;
import ch.jamiete.hilda.commands.ChannelCommand;
import ch.jamiete.hilda.commands.CommandTranscendLevel;
import ch.jamiete.hilda.configuration.Configuration;
import ch.jamiete.hilda.moderatortools.ModeratorToolsPlugin;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public class IgnoreCommand extends ChannelCommand {
    private enum IgnoreDirection {
        IGNORE, UNIGNORE
    }

    private final ModeratorToolsPlugin plugin;

    public IgnoreCommand(final Hilda hilda, final ModeratorToolsPlugin plugin) {
        super(hilda);

        this.plugin = plugin;

        this.setName("ignore");
        this.setAliases(Collections.singletonList("unignore"));
        this.setDescription("Manages ignored channels.");
        this.setMinimumPermission(Permission.ADMINISTRATOR);
        this.setTranscend(CommandTranscendLevel.PERMISSION);
    }

    @Override
    public void execute(final Message message, final String[] arguments, final String label) {
        if (arguments.length == 1 && arguments[0].equalsIgnoreCase("list")) {
            final MessageBuilder mb = new MessageBuilder();
            final List<String> strings = this.hilda.getCommandManager().getIgnoredChannels();
            final List<TextChannel> ignored = new ArrayList<>();

            for (final String s : strings) {
                final TextChannel c = message.getGuild().getTextChannelById(s);

                if (c != null) {
                    ignored.add(c);
                }
            }

            if (ignored.isEmpty()) {
                this.reply(message, "I'm not ignoring any channels!");
            } else {
                mb.append("I'm currently ignoring ");

                for (final TextChannel c : ignored) {
                    mb.append(c.getAsMention());
                    mb.append(", ");
                }

                mb.replaceLast(", ", "");
                mb.append(".");
                this.reply(message, mb.build());
            }

            return;
        }

        final IgnoreDirection direction = IgnoreDirection.valueOf(label.toUpperCase());
        final List<TextChannel> channels = new ArrayList<>();

        if (arguments.length == 0) {
            channels.add(message.getTextChannel());
        }

        if (!message.getMentionedChannels().isEmpty()) {
            channels.addAll(message.getMentionedChannels());
        }

        final Configuration cfg = this.hilda.getConfigurationManager().getConfiguration(this.plugin, "ignore-" + message.getGuild().getId());
        JsonArray array = cfg.get().getAsJsonArray("channels");

        if (array == null) {
            array = new JsonArray();
        }

        for (final TextChannel channel : channels) {
            if (direction == IgnoreDirection.IGNORE) {
                this.hilda.getCommandManager().addIgnoredChannel(channel.getId());

                if (!array.contains(new JsonPrimitive(channel.getId()))) {
                    array.add(channel.getId());
                }
            }

            if (direction == IgnoreDirection.UNIGNORE) {
                this.hilda.getCommandManager().removeIgnoredChannel(channel.getId());
                array.remove(new JsonPrimitive(channel.getId()));
            }
        }

        cfg.get().add("channels", array);
        cfg.save();

        final MessageBuilder mb = new MessageBuilder();

        mb.append("OK, I'm ").append(direction == IgnoreDirection.IGNORE ? "now" : "no longer").append(" ignoring ");
        mb.append(Util.getChannelsAsString(channels));
        mb.append(".");

        mb.buildAll().forEach(m -> message.getChannel().sendMessage(m).queue());
    }

}
