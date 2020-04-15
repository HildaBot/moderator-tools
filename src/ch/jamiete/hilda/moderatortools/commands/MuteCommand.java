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
import ch.jamiete.hilda.commands.CommandTranscendLevel;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;

import java.util.*;
import java.util.stream.Collectors;

public class MuteCommand extends ChannelCommand {

    private enum MuteDirection {
        MUTE, UNMUTE;

        public MuteDirection inverse() {
            switch (this) {
                case MUTE:
                    return UNMUTE;

                case UNMUTE:
                    return MUTE;
            }

            return null;
        }
    }

    private enum MuteScope {
        CHANNEL, SERVER
    }

    public MuteCommand(final Hilda hilda) {
        super(hilda);

        this.setName("mute");
        this.setAliases(Collections.singletonList("unmute"));
        this.setDescription("Mutes and unmutes users.");
        this.setMinimumPermission(Permission.MESSAGE_MANAGE);
        this.setTranscend(CommandTranscendLevel.PERMISSION);
    }

    @Override
    public void execute(final Message message, final String[] arguments, final String label) {
        if (arguments.length == 0) {
            this.usage(message, "[server/channel] <user...>", label);
            return;
        }

        if (arguments[0].equalsIgnoreCase("list")) {
            this.hilda.getCommandManager().getChannelCommand("mutelist").execute(message, Arrays.copyOfRange(arguments, 1, arguments.length), "mute list");
            return;
        }

        final Guild guild = message.getGuild();
        final Member member = guild.getMember(message.getAuthor());
        MuteScope scope = MuteScope.CHANNEL;
        final MuteDirection direction = MuteDirection.valueOf(label.toUpperCase());

        if (!arguments[0].startsWith("<@")) {
            try {
                scope = MuteScope.valueOf(arguments[0].toUpperCase());
            } catch (final Exception e) {
                this.reply(message, "You can only mute in the **server** or the **channel**. Please try again.");
                return;
            }
        }

        if (scope == MuteScope.SERVER && !member.hasPermission(Permission.MANAGE_SERVER)) {
            this.reply(message, "You don't have permission to server mute.");
            return;
        }

        final List<User> affected = message.getMentionedUsers().stream().filter(u -> member.canInteract(guild.getMember(u)) && u != member.getUser()).collect(Collectors.toList());

        final List<TextChannel> channels = new ArrayList<>();
        final List<Permission> deny = Arrays.asList(Permission.MESSAGE_WRITE, Permission.MESSAGE_ADD_REACTION);

        if (scope == MuteScope.SERVER) {
            channels.addAll(guild.getTextChannels());
        }

        if (scope == MuteScope.CHANNEL) {
            channels.add(message.getTextChannel());
        }

        for (final User user : affected) {
            for (final TextChannel channel : channels) {
                if (!guild.getSelfMember().hasPermission(channel, Permission.MANAGE_PERMISSIONS)) {
                    this.reply(message, "Aborting execution; I need permission to manage permissions in " + channel.getAsMention() + ".");
                    return;
                }

                final PermissionOverride override = channel.getPermissionOverride(guild.getMember(user));

                if (direction == MuteDirection.MUTE && channel.canTalk(guild.getMember(user))) {
                    if (override == null) {
                        channel.createPermissionOverride(guild.getMember(user)).setDeny(deny).reason("I created a mute on " + Util.getName(user) + " (" + user.getId() + ") at the request of " + Util.getName(message.getAuthor()) + " (" + message.getAuthor().getId() + ").").queue();
                    } else {
                        override.getManager().deny(deny).reason("I created a mute on " + Util.getName(user) + " (" + user.getId() + ") at the request of " + Util.getName(message.getAuthor()) + " (" + message.getAuthor().getId() + ").").queue();
                    }
                }

                if (direction == MuteDirection.UNMUTE && override != null) {
                    final EnumSet<Permission> denied = override.getDenied();

                    if (denied.size() == 2 && denied.containsAll(deny) && override.getAllowed().size() == 0) {
                        override.delete().reason("I removed a mute on " + Util.getName(user) + " (" + user.getId() + ") at the request of " + Util.getName(message.getAuthor()) + " (" + message.getAuthor().getId() + ").").queue();
                    } else {
                        override.getManager().clear(deny).reason("I removed the permission overrides that were effecting a mute on " + Util.getName(user) + " (" + user.getId() + ") at the request of " + Util.getName(message.getAuthor()) + " (" + message.getAuthor().getId() + ").").queue();
                    }
                }
            }
        }

        final MessageBuilder mb = new MessageBuilder();

        mb.append(":speak_no_evil:");

        if (affected.size() > 0) {
            mb.append(" I've ").append(direction.name().toLowerCase()).append("d ");
            mb.append(this.getUsersAsString(affected));
            mb.append(" from the ").append(scope.name().toLowerCase()).append("!");
        }

        if (message.getMentionedUsers().size() > affected.size()) {
            final int unaffected = message.getMentionedUsers().size() - affected.size();
            mb.append(" I didn't ").append(direction.name().toLowerCase()).append(" ");
            mb.append(unaffected).append(" ").append(unaffected == 1 ? "user" : "users");
            mb.append(" because you don't have permission. ");
            mb.append(this.getUsersAsString(message.getMentionedUsers().stream().filter(u -> !affected.contains(u)).collect(Collectors.toList())));
            mb.append(" ").append(unaffected == 1 ? "remains" : "remain").append(" ");
            mb.append(direction.inverse().name().toLowerCase()).append("d.");
        }

        this.reply(message, mb.build());
    }

    private String getUsersAsString(final List<User> users) {
        final StringBuilder sb = new StringBuilder();

        if (users.size() == 0) {
            return "";
        }

        if (users.size() == 1) {
            sb.append(users.get(0).getAsMention());
        } else {
            for (final User user : users.subList(0, users.size() - 1)) {
                sb.append(user.getAsMention()).append(", ");
            }

            sb.setLength(sb.length() - 2);
            sb.append(" and ").append(users.get(users.size() - 1).getAsMention());
        }

        return sb.toString();
    }

}
