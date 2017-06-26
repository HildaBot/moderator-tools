package ch.jamiete.hilda.moderatortools.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import ch.jamiete.hilda.Hilda;
import ch.jamiete.hilda.commands.ChannelCommand;
import ch.jamiete.hilda.commands.CommandTranscendLevel;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.PermissionOverride;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

public class MuteCommand extends ChannelCommand {

    public MuteCommand(Hilda hilda) {
        super(hilda);

        this.setName("mute");
        this.setAliases(Arrays.asList(new String[] { "unmute" }));
        this.setDescription("Mutes and unmutes users.");
        this.setMinimumPermission(Permission.MESSAGE_MANAGE);
        this.setTranscend(CommandTranscendLevel.PERMISSION);
    }

    @Override
    public void execute(Message message, String[] arguments, String label) {
        if (arguments.length == 0) {
            this.usage(message, "[server/channel] <user...>", label);
            return;
        }

        Guild guild = message.getGuild();
        Member member = guild.getMember(message.getAuthor());
        MuteScope scope = MuteScope.CHANNEL;
        MuteDirection direction = MuteDirection.valueOf(label.toUpperCase());

        if (!arguments[0].startsWith("<@")) {
            try {
                scope = MuteScope.valueOf(arguments[0].toUpperCase());
            } catch (Exception e) {
                this.reply(message, "You can only mute in the **server** or the **channel**. Please try again.");
                return;
            }
        }

        if (scope == MuteScope.SERVER && !member.hasPermission(Permission.MANAGE_SERVER)) {
            this.reply(message, "You don't have permission to server mute.");
            return;
        }

        List<User> affected = new ArrayList<User>();
        affected.addAll(message.getMentionedUsers().stream().filter(u -> member.canInteract(guild.getMember(u)) && u != member.getUser()).collect(Collectors.toList()));

        List<TextChannel> channels = new ArrayList<TextChannel>();
        List<Permission> deny = Arrays.asList(new Permission[] { Permission.MESSAGE_WRITE, Permission.MESSAGE_ADD_REACTION });

        if (scope == MuteScope.SERVER) {
            channels.addAll(guild.getTextChannels());
        }

        if (scope == MuteScope.CHANNEL) {
            channels.add(message.getTextChannel());
        }

        for (User user : affected) {
            for (TextChannel channel : channels) {
                if (!guild.getSelfMember().hasPermission(message.getTextChannel(), Permission.MANAGE_PERMISSIONS)) {
                    this.reply(message, "Aborting execution; I need permission to manage permissions in " + message.getTextChannel().getName() + ".");
                    return;
                }

                PermissionOverride override = channel.getPermissionOverride(guild.getMember(user));

                if (direction == MuteDirection.MUTE && channel.canTalk(guild.getMember(user))) {
                    if (override == null) {
                        channel.createPermissionOverride(guild.getMember(user)).setDeny(deny).queue();
                    } else {
                        override.getManager().deny(deny).queue();
                    }
                }

                if (direction == MuteDirection.UNMUTE && override != null) {
                    List<Permission> denied = override.getDenied();

                    if (denied.size() == 2 && denied.containsAll(deny) && override.getAllowed().size() == 0) {
                        override.delete().queue();
                    } else {
                        override.getManager().clear(deny).queue();
                    }
                }
            }
        }

        MessageBuilder mb = new MessageBuilder();

        mb.append(":speak_no_evil: I've ").append(direction.name().toLowerCase()).append("d ");
        mb.append(this.getUsersAsString(affected));
        mb.append(" from the ").append(scope.name().toLowerCase()).append("!");

        if (message.getMentionedUsers().size() > affected.size()) {
            int unaffected = message.getMentionedUsers().size() - affected.size();
            mb.append(" I didn't ").append(direction.name().toLowerCase()).append(" ");
            mb.append(unaffected).append(" ").append(unaffected == 1 ? "user" : "users");
            mb.append(" because you don't have permission. ");
            mb.append(this.getUsersAsString(message.getMentionedUsers().stream().filter(u -> !affected.contains(u)).collect(Collectors.toList())));
            mb.append(" ").append(unaffected == 1 ? "remains" : "remain").append(" ");
            mb.append(direction.name().toLowerCase()).append("d.");
        }

        this.reply(message, mb.build());
    }

    private String getUsersAsString(final List<User> users) {
        StringBuilder sb = new StringBuilder();

        if (users.size() == 0) {
            return "";
        }

        if (users.size() == 1) {
            sb.append(users.get(0).getAsMention());
        } else {
            for (User user : users.subList(0, users.size() - 1)) {
                sb.append(user.getAsMention()).append(", ");
            }

            sb.setLength(sb.length() - 2);
            sb.append(" and ").append(users.get(users.size() - 1).getAsMention());
        }

        return sb.toString();
    }

    private enum MuteDirection {
        MUTE, UNMUTE;
    }

    private enum MuteScope {
        CHANNEL, SERVER;
    }

}
