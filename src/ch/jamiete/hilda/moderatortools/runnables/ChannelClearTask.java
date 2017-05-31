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

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import ch.jamiete.hilda.Hilda;
import ch.jamiete.hilda.runnables.MessageDeletionTask;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.exceptions.ErrorResponseException;

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

        while (this.amount > 0) {
            int run = this.user == null ? this.amount : 100;

            if (run > 100) {
                run = 100;
            }

            Hilda.getLogger().fine("Deleting " + run + " messages this run...");

            final List<Message> messages = this.channel.getHistory().retrievePast(run).complete();

            if (messages.isEmpty()) {
                break;
            }

            Hilda.getLogger().fine("Deleting...");

            try {
                if (this.user != null) {
                    final List<Message> relevant = messages.stream().filter(message -> message.getAuthor() == this.user).collect(Collectors.toList());
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
            } catch (final ErrorResponseException e) {
                this.channel.sendMessage("I couldn't clear the messages in this channel as they're more than two weeks old.").queue();
                Hilda.getLogger().info("Given up deleting because channel messages are too old.");
                return;
            }

            if (messages.size() < 100) { // Channel now empty
                break;
            }

            Hilda.getLogger().fine("Looping with " + this.amount + " left");
        }

        Hilda.getLogger().fine("Finished deleting!");

        this.channel.sendMessage("Deleted messages.").queue(sent -> {
            this.hilda.getExecutor().schedule(new MessageDeletionTask(sent), 5, TimeUnit.SECONDS);
        });
    }

}
