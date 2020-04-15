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
import java.util.List;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import org.apache.commons.lang3.text.WordUtils;
import ch.jamiete.hilda.Hilda;
import ch.jamiete.hilda.Util;
import ch.jamiete.hilda.commands.ChannelCommand;

public class UserinfoCommand extends ChannelCommand {
    private final DateTimeFormatter dtf;

    public UserinfoCommand(final Hilda hilda) {
        super(hilda);

        this.dtf = DateTimeFormatter.ofPattern("EEEE d MMMM yyyy hh:mm a");

        this.setName("userinfo");
        this.setDescription("Displays information about the user.");
    }

    @Override
    public void execute(final Message message, final String[] arguments, final String label) {
        final List<User> tocheck = new ArrayList<>();

        if (arguments.length == 0) {
            tocheck.add(message.getAuthor());
        } else if (message.getMentionedUsers().isEmpty()) {
            this.usage(message, "[@user...]", label);
            return;
        } else {
            tocheck.addAll(message.getMentionedUsers());
        }

        for (final User user : tocheck) {
            final Member member = message.getGuild().getMember(user);
            final EmbedBuilder eb = new EmbedBuilder();

            if (user.isBot()) {
                eb.setTitle(Util.getName(user) + " (Bot account)");
            } else {
                eb.setTitle(Util.getName(user));
            }

            eb.setThumbnail(user.getEffectiveAvatarUrl());

            switch (member.getOnlineStatus()) {
                case ONLINE:
                    eb.setColor(Color.decode("#008E09"));
                    break;

                case DO_NOT_DISTURB:
                    eb.setColor(Color.decode("#FF0303"));
                    break;

                case IDLE:
                    eb.setColor(Color.decode("#FFBF00"));
                    break;

                case INVISIBLE:
                case OFFLINE:
                case UNKNOWN:
                default:
                    eb.setColor(Color.decode("#C2CBCE"));
                    break;

            }

            eb.addField("User ID", user.getId(), false);

            if (member.getNickname() != null) {
                eb.addField("Nickname", member.getNickname(), false);
            }

            eb.addField("Shared servers", String.valueOf(user.getMutualGuilds().size()), false);
            eb.addField("Online status", WordUtils.capitalize(member.getOnlineStatus().toString().toLowerCase().replace("_", "")), false);

            List<Activity> activities = member.getActivities();
            if (!activities.isEmpty()) {
                int size = activities.size();

                for (int i = 0; i < size; i++) {
                    Activity activity = activities.get(i);
                    String value = activity.getName();

                    if (activity.getUrl() != null)
                        value = "[" + value + "](" + activity.getUrl() + ")";

                    String name = "Currently ";

                    switch (activity.getType()) {
                        case CUSTOM_STATUS:
                            // Don't add any text
                            break;

                        case DEFAULT:
                            name += "playing";
                            break;

                        default:
                            name += activity.getType().name().toLowerCase();
                            break;
                    }

                    if (size > 1)
                        name += " [" + (i + 1) + "]";

                    eb.addField(name, value, false);
                }
            }

            eb.addField("Account created on", user.getTimeCreated().format(this.dtf) + " GMT", false);
            eb.addField("Joined server on", member.getTimeJoined().format(this.dtf) + " GMT", false);

            final long existence = System.currentTimeMillis() - message.getGuild().getTimeCreated().toInstant().toEpochMilli();
            final long usertime = System.currentTimeMillis() - member.getTimeJoined().toInstant().toEpochMilli();
            final double percentage = (double) usertime / (double) existence * 100D;

            String value = Util.getFriendlyTime(usertime);
            value += " or \u2248" + Math.round(percentage) + "% of the server's existence.";
            eb.addField("Has been a member for", value, false);

            if (!member.getRoles().isEmpty()) {
                eb.addField("Roles (" + member.getRoles().size() + ")", Util.getRolesAsString(member.getRoles()), false);
            }

            this.reply(message, eb.build());
        }
    }

}
