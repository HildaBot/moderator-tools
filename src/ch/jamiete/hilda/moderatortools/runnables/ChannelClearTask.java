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
package ch.jamiete.hilda.moderatortools.runnables;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import ch.jamiete.hilda.Hilda;
import ch.jamiete.hilda.Start;
import ch.jamiete.hilda.runnables.MessageDeletionTask;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.requests.restaction.pagination.MessagePaginationAction;

public class ChannelClearTask implements Runnable {
    private final Hilda hilda;
    private final TextChannel channel;
    private int amount;
    private final User user;

    public ChannelClearTask(final Hilda hilda, final TextChannel channel, final int amount) {
        this(hilda, channel, amount, null);
    }

    public ChannelClearTask(final Hilda hilda, final TextChannel channel, final int amount, final User user) {
        this.hilda = hilda;
        this.channel = channel;
        this.amount = amount;
        this.user = user;
    }

    @Override
    public void run() {
        Hilda.getLogger().fine("Starting to delete " + this.amount + " messages...");

        this.channel.sendTyping().queue();

        final MessagePaginationAction action = this.channel.getIterableHistory().cache(false);
        boolean old = false;

        while (this.amount > 0) {
            int run = this.user == null ? this.amount : 100;

            if (run > 100) {
                run = 100;
            }

            Hilda.getLogger().fine("Deleting " + run + " messages this run...");

            List<Message> messages = action.complete().stream().limit(run).collect(Collectors.toList());

            if (Start.DEBUG)
                Hilda.getLogger().fine("Received list of " + messages.size() + " messages.");

            Instant too_old = Instant.now().minus(14, ChronoUnit.DAYS).minus(5, ChronoUnit.MINUTES);
            if (messages.stream().anyMatch(m -> m.getCreationTime().toInstant().isBefore(too_old))) {
                Hilda.getLogger().fine("Messages too old!");
                old = true;
                messages = messages.stream().filter(m -> m.getCreationTime().toInstant().isAfter(too_old)).collect(Collectors.toList());
            }

            if (messages.isEmpty()) {
                Hilda.getLogger().fine("Messages already empty.");
                break;
            }

            Hilda.getLogger().fine("Deleting...");

            if (this.user != null) {
                List<Message> relevant = messages.stream().filter(message -> message.getAuthor() == this.user).collect(Collectors.toList());

                if (relevant.size() > this.amount) {
                    relevant = relevant.subList(0, this.amount - 1);
                }

                this.channel.deleteMessages(relevant).complete();
                this.amount -= relevant.size();

                if (relevant.size() == 0) {
                    Hilda.getLogger().fine("Gave up deleting user's messages; there's " + this.amount + " left");
                    break;
                }
            } else {
                this.channel.deleteMessages(messages).complete();
                this.amount -= run;
            }

            if (messages.size() < 100 || old) { // Channel now empty
                break;
            }

            Hilda.getLogger().fine("Looping with " + this.amount + " left");
        }

        Hilda.getLogger().fine("Finished deleting!");

        String temp = "Deleted messages.";

        if (old) {
            temp += " Some messages were more than two weeks old and I can't delete any messages older than that. I've deleted everything I can, but I'm otherwise stopping deletions now.";
        }

        String response = temp;

        this.channel.sendMessage(response).queue(sent -> {
            if (response.length() > 32) {
                this.hilda.getExecutor().schedule(new MessageDeletionTask(sent), 5, TimeUnit.SECONDS);
            }
        });
    }

}
