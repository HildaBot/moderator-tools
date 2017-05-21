package ch.jamiete.hilda.moderatortools.commands;

import java.util.Arrays;
import ch.jamiete.hilda.Hilda;
import ch.jamiete.hilda.commands.ChannelCommand;
import ch.jamiete.hilda.commands.CommandTranscendLevel;
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
            this.usage(message, "[server/channel] <users>", label);
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

        if (scope == MuteScope.SERVER) {
            for (User user : message.getMentionedUsers()) {
                for (TextChannel channel : guild.getTextChannels()) {
                    if (!guild.getSelfMember().hasPermission(message.getTextChannel(), Permission.MANAGE_PERMISSIONS)) {
                        this.reply(message, "Aborting execution; I need permission to manage permissions in " + message.getTextChannel().getName() + ".");
                        return;
                    }

                    PermissionOverride override = channel.getPermissionOverride(guild.getMember(user));

                    if (direction == MuteDirection.MUTE) {
                        if (override == null) {
                            channel.createPermissionOverride(guild.getMember(user)).setDeny(Permission.MESSAGE_WRITE).queue();
                        } else {
                            if (!override.getDenied().contains(Permission.MESSAGE_WRITE)) {
                                override.getManager().deny(Permission.MESSAGE_WRITE).queue();
                            }
                        }
                    }

                    if (direction == MuteDirection.UNMUTE) {
                        if (override != null) {
                            if (override.getDenied().contains(Permission.MESSAGE_WRITE)) {
                                if (override.getDenied().size() == 1 && override.getAllowed().size() == 0) {
                                    override.delete().queue();
                                } else {
                                    override.getManager().clear(Permission.MESSAGE_WRITE).queue();
                                }
                            }
                        }
                    }
                }
            }
        }

        if (scope == MuteScope.CHANNEL) {
            if (!guild.getSelfMember().hasPermission(message.getTextChannel(), Permission.MANAGE_PERMISSIONS)) {
                this.reply(message, "Aborting execution; I need permission to manage permissions in " + message.getTextChannel().getName() + ".");
                return;
            }

            for (User user : message.getMentionedUsers()) {
                PermissionOverride override = message.getTextChannel().getPermissionOverride(guild.getMember(user));

                if (direction == MuteDirection.MUTE) {
                    if (override == null) {
                        message.getTextChannel().createPermissionOverride(guild.getMember(user)).setDeny(Permission.MESSAGE_WRITE).queue();
                    } else {
                        if (!override.getDenied().contains(Permission.MESSAGE_WRITE)) {
                            override.getManager().deny(Permission.MESSAGE_WRITE).queue();
                        }
                    }
                }

                if (direction == MuteDirection.UNMUTE) {
                    if (override != null) {
                        if (override.getDenied().contains(Permission.MESSAGE_WRITE)) {
                            if (override.getDenied().size() == 1 && override.getAllowed().size() == 0) {
                                override.delete().queue();
                            } else {
                                override.getManager().clear(Permission.MESSAGE_WRITE).queue();
                            }
                        }
                    }
                }
            }
        }

        this.reply(message, ":speak_no_evil: I've " + direction.name().toLowerCase() + "d " + (message.getMentionedUsers().size() == 1 ? "that user" : "those users") + " from the " + scope.name().toLowerCase() + "!");
    }

    private enum MuteDirection {
        MUTE, UNMUTE;
    }

    private enum MuteScope {
        CHANNEL, SERVER;
    }

}
