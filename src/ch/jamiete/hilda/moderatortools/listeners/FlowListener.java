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
package ch.jamiete.hilda.moderatortools.listeners;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import ch.jamiete.hilda.Hilda;
import ch.jamiete.hilda.Util;
import ch.jamiete.hilda.configuration.Configuration;
import ch.jamiete.hilda.events.EventHandler;
import ch.jamiete.hilda.moderatortools.FlowMember;
import ch.jamiete.hilda.moderatortools.ModeratorToolsPlugin;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent;

public class FlowListener {
    public static final String DEFAULT_JOIN = ":arrow_forward: $mention ($username#$discriminator) just joined the server!";
    public static final String DEFAULT_LEAVE = ":arrow_backward: $mention ($username#$discriminator) just left the server!";

    public static String compute(String message, final FlowMember member) {
        String mention;

        if (member.nickname != null) {
            mention = "<@!" + member.id + ">";
        } else {
            mention = "<@" + member.id + ">";
        }

        message = message.replaceAll("\\$mention", mention);
        message = message.replaceAll("\\$username", Matcher.quoteReplacement(member.username));
        message = message.replaceAll("\\$effective", Matcher.quoteReplacement(member.getEffectiveName()));
        message = message.replaceAll("\\$discriminator", member.discriminator);
        message = message.replaceAll("\\$id", member.id);

        return message;
    }

    public static String compute(String message, final Member member) {
        message = message.replaceAll("\\$mention", member.getAsMention());
        message = message.replaceAll("\\$username", Matcher.quoteReplacement(member.getUser().getName()));
        message = message.replaceAll("\\$effective", Matcher.quoteReplacement(member.getEffectiveName()));
        message = message.replaceAll("\\$discriminator", member.getUser().getDiscriminator());
        message = message.replaceAll("\\$id", member.getUser().getId());

        return message;
    }

    public static void sendMessage(final Guild guild, final String message) {
        final List<String> failures = new ArrayList<>();

        for (final TextChannel channel : guild.getTextChannels()) {
            if (channel.getTopic() != null && channel.getTopic().toLowerCase().contains("[flow]")) {
                if (!channel.canTalk()) {
                    failures.add("#" + channel.getName());
                    continue;
                }

                channel.sendMessage(message).queue();
            }
        }

        if (!failures.isEmpty()) {
            guild.getOwner().getUser().openPrivateChannel().queue(ch -> {
                ch.sendMessage("I tried to send a flow message to " + Util.getAsList(failures) + " but did not have permission.").queue();
            }, ignore -> {
            });
        }
    }

    private final Hilda hilda;

    private final ModeratorToolsPlugin plugin;

    public FlowListener(final Hilda hilda, final ModeratorToolsPlugin plugin) {
        this.hilda = hilda;
        this.plugin = plugin;
    }

    @EventHandler
    public void onGuildMemberJoin(final GuildMemberJoinEvent event) {
        final Configuration cfg = this.hilda.getConfigurationManager().getConfiguration(this.plugin, "flow-" + event.getGuild().getId());
        FlowListener.sendMessage(event.getGuild(), FlowListener.compute(cfg.getString("join", FlowListener.DEFAULT_JOIN), event.getMember()));
    }

    @EventHandler
    public void onGuildMemberLeave(final GuildMemberLeaveEvent event) {
        final Configuration cfg = this.hilda.getConfigurationManager().getConfiguration(this.plugin, "flow-" + event.getGuild().getId());
        FlowListener.sendMessage(event.getGuild(), FlowListener.compute(cfg.getString("leave", FlowListener.DEFAULT_LEAVE), event.getMember()));
    }

}
