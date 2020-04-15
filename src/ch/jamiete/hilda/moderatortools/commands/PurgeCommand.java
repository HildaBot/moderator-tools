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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import ch.jamiete.hilda.Hilda;
import ch.jamiete.hilda.commands.ChannelCommand;
import ch.jamiete.hilda.commands.CommandManager;
import ch.jamiete.hilda.moderatortools.runnables.ChannelDeletionTask;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;

public class PurgeCommand extends ChannelCommand {
    private final Map<Long, String> keys = Collections.synchronizedMap(new HashMap<>());

    public PurgeCommand(final Hilda hilda) {
        super(hilda);

        this.setName("purge");
        this.setMinimumPermission(Permission.ADMINISTRATOR);
        this.setDescription("Purges the channel by deleting all messages.");
    }

    @Override
    public void execute(final Message message, final String[] arguments, final String label) {
        if (!message.getGuild().getSelfMember().hasPermission(message.getTextChannel(), Permission.MESSAGE_MANAGE)) {
            this.reply(message, "I do not have permission to delete messages in this channel.");
            return;
        }

        if (arguments.length == 0) {
            final String id = this.getFreshID();
            this.keys.put(message.getAuthor().getIdLong(), id);
            this.reply(message, "Are you sure you want to purge **the entire history** of this channel? To continue, say `" + CommandManager.PREFIX + label + " " + id + "`");
            return;
        }

        if (!this.keys.containsKey(message.getAuthor().getIdLong())) {
            this.reply(message, "You must first run the command without arguments to generate a confirmation key.");
            return;
        }

        if (!this.keys.get(message.getAuthor().getIdLong()).equalsIgnoreCase(arguments[0])) {
            this.reply(message, "I don't recognise that confirmation key. Please use the confirmation key you were given or generate a new one.");
            return;
        }

        this.keys.remove(message.getAuthor().getIdLong());
        this.hilda.getExecutor().execute(new ChannelDeletionTask(message.getTextChannel()));
    }

    private String getFreshID() {
        final String alphabet = "abcdefghijkmnpqrstuvwxyz";
        final String numbers = "23456789";
        final Random random = new Random();

        StringBuilder possibleid = new StringBuilder();

        for (int i = 0; i < 8; i++) {
            if (random.nextBoolean()) {
                possibleid.append(alphabet.charAt(random.nextInt(alphabet.length())));
            } else {
                possibleid.append(numbers.charAt(random.nextInt(numbers.length())));
            }
        }

        synchronized (this.keys) {
            for (final Entry<Long, String> entry : this.keys.entrySet()) {
                if (entry.getValue().equals(possibleid.toString())) {
                    possibleid = null;
                    break;
                }
            }
        }

        return possibleid == null ? this.getFreshID() : possibleid.toString();
    }

}
