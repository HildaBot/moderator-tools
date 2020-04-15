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
import java.util.List;
import java.util.stream.Collectors;
import ch.jamiete.hilda.Hilda;
import ch.jamiete.hilda.Util;
import ch.jamiete.hilda.commands.ChannelCommand;
import ch.jamiete.hilda.commands.CommandTranscendLevel;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.PermissionOverride;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

public class MuteListCommand extends ChannelCommand {

    public MuteListCommand(final Hilda hilda) {
        super(hilda);

        this.setName("mutelist");
        this.setDescription("Lists the outstanding mutes for a user or server.");
        this.setMinimumPermission(Permission.MESSAGE_MANAGE);
        this.setTranscend(CommandTranscendLevel.PERMISSION);
    }

    @Override
    public void execute(final Message message, final String[] arguments, final String label) {
        final List<TextChannel> channels = new ArrayList<TextChannel>();

        if (!message.getMentionedChannels().isEmpty()) {
            channels.addAll(message.getMentionedChannels());
        } else {
            channels.addAll(message.getGuild().getTextChannels());
        }

        final EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Outstanding mute results");

        if (message.getMentionedUsers().isEmpty()) {
            for (final TextChannel channel : channels) {
                final List<User> affected = new ArrayList<>();

                for (final PermissionOverride override : channel.getMemberPermissionOverrides()) {
                    if (override.getDenied().contains(Permission.MESSAGE_WRITE)) {
                        affected.add(override.getMember().getUser());
                    }
                }

                if (!affected.isEmpty()) {
                    eb.addField("#" + channel.getName(), Util.getUsersAsString(affected), false);
                }
            }

            if (eb.getFields().isEmpty()) {
                eb.setDescription("There are no outstanding mutes on the server.");
            }
        } else {
            for (final User user : message.getMentionedUsers()) {
                final List<TextChannel> affected = new ArrayList<>();

                for (final TextChannel channel : channels) {
                    for (final PermissionOverride override : channel.getMemberPermissionOverrides().stream().filter(o -> o.getMember().getUser().equals(user)).collect(Collectors.toList())) {
                        if (override.getDenied().contains(Permission.MESSAGE_WRITE)) {
                            affected.add(channel);
                        }
                    }
                }

                eb.addField(Util.getName(user), affected.isEmpty() ? "No outstanding mutes." : Util.getChannelsAsString(affected), false);
            }
        }

        this.reply(message, eb.build());
    }

}
