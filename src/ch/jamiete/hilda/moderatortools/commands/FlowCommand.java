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

import ch.jamiete.hilda.Hilda;
import ch.jamiete.hilda.Util;
import ch.jamiete.hilda.commands.ChannelCommand;
import ch.jamiete.hilda.commands.CommandManager;
import ch.jamiete.hilda.commands.CommandTranscendLevel;
import ch.jamiete.hilda.configuration.Configuration;
import ch.jamiete.hilda.moderatortools.ModeratorToolsPlugin;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;

public class FlowCommand extends ChannelCommand {
    private final ModeratorToolsPlugin plugin;

    public FlowCommand(final Hilda hilda, final ModeratorToolsPlugin plugin) {
        super(hilda);

        this.plugin = plugin;

        this.setName("flow");
        this.setDescription("Manages the content of flow messages.");
        this.setMinimumPermission(Permission.MANAGE_SERVER);
        this.setTranscend(CommandTranscendLevel.PERMISSION);
    }

    @Override
    public void execute(final Message message, final String[] arguments, final String label) {
        final Configuration cfg = this.hilda.getConfigurationManager().getConfiguration(this.plugin, "flow-" + message.getGuild().getId());

        if (arguments.length == 0) {
            final MessageBuilder mb = new MessageBuilder();

            mb.append("Flow messages", MessageBuilder.Formatting.UNDERLINE).append("\n");
            mb.append("Current flow message configuration", MessageBuilder.Formatting.ITALICS).append("\n\n");

            mb.append("Join:", MessageBuilder.Formatting.BOLD).append(" ").append(cfg.getString("join", "Default.")).append("\n");
            mb.append("Leave:", MessageBuilder.Formatting.BOLD).append(" ").append(cfg.getString("leave", "Default.")).append("\n\n");

            mb.append("Change these with ").append(CommandManager.PREFIX + label + " <join/leave> <message>", MessageBuilder.Formatting.BOLD);
            mb.append(". You can use the following substitutions: $mention $username $effective $discriminator $id $count. ");
            mb.append("The default use `$mention ($username#$discriminator)`.");

            this.reply(message, mb.build());
            return;
        }

        String direction = null;
        String to = null;

        if (arguments[0].equalsIgnoreCase("join") || arguments[0].equalsIgnoreCase("leave")) {
            direction = arguments[0];

            if (arguments.length > 1) {
                to = Util.combineSplit(1, arguments, " ");
            }
        }

        if (direction == null) {
            this.usage(message, "<join/leave> <message> OR with no arguments to view current", label);
            return;
        }

        cfg.get().addProperty(direction, to);
        cfg.save();

        this.reply(message, "OK, I've set the " + direction + " message to " + (to == null ? "the default" : to));
    }

}
