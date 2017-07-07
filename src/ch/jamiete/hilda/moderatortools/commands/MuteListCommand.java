package ch.jamiete.hilda.moderatortools.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import ch.jamiete.hilda.Hilda;
import ch.jamiete.hilda.Util;
import ch.jamiete.hilda.commands.ChannelCommand;
import ch.jamiete.hilda.commands.CommandTranscendLevel;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.PermissionOverride;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

public class MuteListCommand extends ChannelCommand {

    public MuteListCommand(Hilda hilda) {
        super(hilda);

        this.setName("mutelist");
        this.setDescription("Lists the outstanding mutes for a user or server.");
        this.setMinimumPermission(Permission.MESSAGE_MANAGE);
        this.setTranscend(CommandTranscendLevel.PERMISSION);
    }

    @Override
    public void execute(Message message, String[] arguments, String label) {
        final List<TextChannel> channels = new ArrayList<TextChannel>();

        if (!message.getMentionedChannels().isEmpty()) {
            channels.addAll(message.getMentionedChannels());
        } else {
            channels.addAll(message.getGuild().getTextChannels());
        }

        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Outstanding mute results");

        if (message.getMentionedUsers().isEmpty()) {
            for (TextChannel channel : channels) {
                List<User> affected = new ArrayList<User>();

                for (PermissionOverride override : channel.getMemberPermissionOverrides()) {
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
            for (User user : message.getMentionedUsers()) {
                List<TextChannel> affected = new ArrayList<TextChannel>();

                for (TextChannel channel : channels) {
                    for (PermissionOverride override : channel.getMemberPermissionOverrides().stream().filter(o -> o.getMember().getUser().equals(user)).collect(Collectors.toList())) {
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