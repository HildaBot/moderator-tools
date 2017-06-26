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
    private static final String[] EMOJI = new String[] { "328541211562475521", "328541279526977536", "328541313802698763", "328541333218394122", "328541347776561174", "328541378315419648" };

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
            for (String emoji : AnnouncementsListener.EMOJI) {
                event.getMessage().addReaction(guild.getEmoteById(emoji)).queue();
            }
        } else { // Generic yellow thumbs up and down
            event.getMessage().addReaction("\uD83D\uDC4D").queue();
            event.getMessage().addReaction("\uD83D\uDC4E").queue();
        }
    }

    private void trySendPrivately(User user, String message) {
        try {
            user.openPrivateChannel().queue(new Consumer<PrivateChannel>() {

                @Override
                public void accept(PrivateChannel channel) {
                    channel.sendMessage(message).queue();
                }

            });
        } catch (Exception e) {
            return;
        }
    }

}
