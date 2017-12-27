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

import java.util.function.Consumer;
import ch.jamiete.hilda.Start;
import ch.jamiete.hilda.events.EventHandler;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

public class AnnouncementsListener {
    private static final String[] EMOJI = new String[] { "395223528342814732", "395223530637361152", "395223536471638016", "395220773683462145", "395220769174585348", "395223551084331008" };

    @EventHandler
    public void onGuildMessageReceived(final GuildMessageReceivedEvent event) {
        if (event.getChannel().getTopic() == null || !event.getChannel().getTopic().toLowerCase().contains("[announcements]") || Start.DEBUG) {
            return;
        }

        if (!event.getGuild().getSelfMember().hasPermission(event.getChannel(), Permission.MESSAGE_ADD_REACTION)) {
            this.trySendPrivately(event.getAuthor(), "I do not have permission to react to messages in " + event.getChannel().getName() + " in " + event.getGuild().getName() + ", but I've been asked to give announcement reacts. Please either give me permission or remove [announcements] from the channel topic.");
            return;
        }

        final Guild guild = event.getJDA().getGuildById("283920447219826688");

        if (event.getGuild().getSelfMember().hasPermission(event.getChannel(), Permission.MESSAGE_EXT_EMOJI)) {
            for (final String emoji : AnnouncementsListener.EMOJI) {
                event.getMessage().addReaction(guild.getEmoteById(emoji)).queue();
            }
        } else { // Generic yellow thumbs up and down
            event.getMessage().addReaction("\uD83D\uDC4D").queue();
            event.getMessage().addReaction("\uD83D\uDC4E").queue();
        }
    }

    private void trySendPrivately(final User user, final String message) {
        try {
            user.openPrivateChannel().queue(new Consumer<PrivateChannel>() {

                @Override
                public void accept(final PrivateChannel channel) {
                    channel.sendMessage(message).queue();
                }

            });
        } catch (final Exception e) {
            return;
        }
    }

}
