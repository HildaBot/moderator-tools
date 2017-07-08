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

import java.awt.Color;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.text.WordUtils;
import ch.jamiete.hilda.Hilda;
import ch.jamiete.hilda.Util;
import ch.jamiete.hilda.commands.ChannelCommand;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;

public class ServerinfoCommand extends ChannelCommand {
    private final DateTimeFormatter dtf;

    public ServerinfoCommand(final Hilda hilda) {
        super(hilda);

        this.dtf = DateTimeFormatter.ofPattern("EEEE d MMMM yyyy hh:mm a");

        this.setName("serverinfo");
        this.setDescription("Displays information about the server.");
    }

    @Override
    public void execute(final Message message, final String[] arguments, final String label) {
        final Guild guild = message.getGuild();
        final EmbedBuilder eb = new EmbedBuilder();

        eb.setTitle(guild.getName());
        eb.setThumbnail(guild.getIconUrl());
        eb.setColor(Color.decode("#008E09"));

        eb.addField("Server ID", guild.getId(), false);
        eb.addField("Server created on", guild.getCreationTime().format(this.dtf), false);
        eb.addField("Default text channel", guild.getPublicChannel().getAsMention(), false);

        String channels = "";
        channels += guild.getTextChannels().size() + " text ";
        channels += guild.getTextChannels().size() == 1 ? "channel" : "channels";
        channels += " and ";
        channels += guild.getVoiceChannels().size() + " voice ";
        channels += guild.getVoiceChannels().size() == 1 ? "channel" : "channels";
        eb.addField("Channels", channels, false);

        eb.addField("Voice region", guild.getRegion().getName(), false);

        if (guild.getAfkChannel() != null) {
            eb.addField("Voice AFK channel", "**" + guild.getAfkChannel().getName() + "** will be joined after " + Util.getFriendlyTime(guild.getAfkTimeout().getSeconds() * 1000L), false);
        }

        String members = "";
        members += guild.getMembers().size() + " ";
        members += guild.getMembers().size() == 1 ? "member" : "members";
        eb.addField("Members", members, false);

        if (guild.getMembers().size() > 50) {
            List<Member> oldest = guild.getMembers().stream().sorted(new Comparator<Member>() {

                @Override
                public int compare(Member one, Member two) {
                    long onetime = System.currentTimeMillis() - one.getJoinDate().toInstant().toEpochMilli();
                    long twotime = System.currentTimeMillis() - two.getJoinDate().toInstant().toEpochMilli();

                    if (onetime == twotime) {
                        return 0;
                    } else if (onetime > twotime) {
                        return -1;
                    } else {
                        return 1;
                    }
                }

            }).limit(10).collect(Collectors.toList());
            List<String> oldestnames = new ArrayList<String>(oldest.size());
            oldest.forEach(m -> oldestnames.add(Util.getName(m)));
            eb.addField("Ten oldest members", Util.getAsList(oldestnames), false);

            List<Member> newest = guild.getMembers().stream().sorted(new Comparator<Member>() {

                @Override
                public int compare(Member one, Member two) {
                    long onetime = System.currentTimeMillis() - one.getJoinDate().toInstant().toEpochMilli();
                    long twotime = System.currentTimeMillis() - two.getJoinDate().toInstant().toEpochMilli();

                    if (onetime == twotime) {
                        return 0;
                    } else if (onetime > twotime) {
                        return 1;
                    } else {
                        return -1;
                    }
                }

            }).limit(10).collect(Collectors.toList());
            List<String> newestnames = new ArrayList<String>(newest.size());
            newest.forEach(m -> newestnames.add(Util.getName(m)));
            eb.addField("Ten newest members", Util.getAsList(newestnames), false);
        }

        String verification = WordUtils.capitalize(guild.getVerificationLevel().name().toLowerCase());
        verification += " (";
        switch (guild.getVerificationLevel()) {
            case HIGH:
                verification += "Users must be members for 10 minutes before talking.";
                break;

            case LOW:
                verification += "Users must have a verified email address.";
                break;

            case MEDIUM:
                verification += "Users must be members for 5 minutes before talking.";
                break;

            case NONE:
                verification += "All users can talk.";
                break;

            case VERY_HIGH:
                verification += "Users must have a verified phone number.";
                break;

            case UNKNOWN:
                verification += "Discord error.";
                break;
        }
        verification += ")";
        eb.addField("Verification level", verification, false);

        eb.addField("Explicit content filter level", WordUtils.capitalize(guild.getExplicitContentLevel().name().toLowerCase()) + " (" + guild.getExplicitContentLevel().getDescription() + ")", false);
        eb.addField("Owner", Util.getName(guild.getOwner()), false);

        this.reply(message, eb.build());
    }

}
