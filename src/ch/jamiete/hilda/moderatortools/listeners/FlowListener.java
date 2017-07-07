package ch.jamiete.hilda.moderatortools.listeners;

import ch.jamiete.hilda.Hilda;
import ch.jamiete.hilda.configuration.Configuration;
import ch.jamiete.hilda.events.EventHandler;
import ch.jamiete.hilda.moderatortools.ModeratorToolsPlugin;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent;

public class FlowListener {
    private final Hilda hilda;
    private final ModeratorToolsPlugin plugin;
    private static final String DEFAULT_JOIN = ":arrow_forward: $mention ($username#$discriminator) just joined the server!";
    private static final String DEFAULT_LEAVE = ":arrow_backward: $mention ($username#$discriminator) just left the server!";

    public FlowListener(final Hilda hilda, final ModeratorToolsPlugin plugin) {
        this.hilda = hilda;
        this.plugin = plugin;
    }

    @EventHandler
    public void onGuildMemberJoin(final GuildMemberJoinEvent event) {
        Configuration cfg = this.hilda.getConfigurationManager().getConfiguration(this.plugin, "flow-" + event.getGuild().getId());
        String message = this.compute(cfg.getString("join", FlowListener.DEFAULT_JOIN), event.getMember());

        for (final TextChannel channel : event.getGuild().getTextChannels()) {
            if (channel.getTopic() != null && channel.getTopic().toLowerCase().contains("[flow]")) {
                channel.sendMessage(message).queue();
            }
        }
    }

    @EventHandler
    public void onGuildMemberLeave(final GuildMemberLeaveEvent event) {
        Configuration cfg = this.hilda.getConfigurationManager().getConfiguration(this.plugin, "flow-" + event.getGuild().getId());
        String message = this.compute(cfg.getString("leave", FlowListener.DEFAULT_LEAVE), event.getMember());

        for (final TextChannel channel : event.getGuild().getTextChannels()) {
            if (channel.getTopic() != null && channel.getTopic().toLowerCase().contains("[flow]")) {
                channel.sendMessage(message).queue();
            }
        }
    }

    private String compute(String message, final Member member) {
        message = message.replaceAll("\\$mention", member.getAsMention());
        message = message.replaceAll("\\$username", member.getUser().getName());
        message = message.replaceAll("\\$effective", member.getEffectiveName());
        message = message.replaceAll("\\$discriminator", member.getUser().getDiscriminator());
        message = message.replaceAll("\\$id", member.getUser().getId());

        return message;
    }

}
