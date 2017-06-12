package ch.jamiete.hilda.moderatortools.listeners;

import ch.jamiete.hilda.events.EventHandler;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent;

public class FlowListener {

    @EventHandler
    public void onGuildMemberJoin(final GuildMemberJoinEvent event) {
        for (final TextChannel channel : event.getGuild().getTextChannels()) {
            if (channel.getTopic() != null && channel.getTopic().toLowerCase().contains("[flow]")) {
                channel.sendMessage(":arrow_forward: " + event.getMember().getEffectiveName() + " joined the server!").queue();
            }
        }
    }

    @EventHandler
    public void onGuildMemberLeave(final GuildMemberLeaveEvent event) {
        for (final TextChannel channel : event.getGuild().getTextChannels()) {
            if (channel.getTopic() != null && channel.getTopic().toLowerCase().contains("[flow]")) {
                channel.sendMessage(":arrow_backward: " + event.getMember().getEffectiveName() + " left the server!").queue();
            }
        }
    }

}
