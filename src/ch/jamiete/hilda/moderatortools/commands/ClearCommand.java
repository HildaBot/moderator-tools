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

import org.apache.commons.lang3.StringUtils;
import ch.jamiete.hilda.Hilda;
import ch.jamiete.hilda.commands.ChannelCommand;
import ch.jamiete.hilda.commands.CommandTranscendLevel;
import ch.jamiete.hilda.moderatortools.runnables.ChannelClearTask;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;

public class ClearCommand extends ChannelCommand {

    public ClearCommand(final Hilda hilda) {
        super(hilda);

        this.setName("clear");
        this.setMinimumPermission(Permission.MESSAGE_MANAGE);
        this.setDescription("Clear messages from the channel.");
        this.setTranscend(CommandTranscendLevel.PERMISSION);
    }

    @Override
    public void execute(final Message message, final String[] args, final String label) {
        final Member member = message.getGuild().getMember(message.getAuthor());

        if (!member.hasPermission(message.getTextChannel(), Permission.MESSAGE_MANAGE)) {
            this.reply(message, "You don't have permission.");
            return;
        }

        if (args.length < 1 || args.length > 2 || args.length == 1 && !StringUtils.isNumeric(args[0]) || args.length == 2 && !StringUtils.isNumeric(args[1])) {
            this.usage(message, "[username] <number>");
            return;
        }

        if (!message.getGuild().getSelfMember().hasPermission(message.getTextChannel(), Permission.MESSAGE_HISTORY)) {
            this.reply(message, "I do not have permission to view the channel history.");
            return;
        }

        if (!message.getGuild().getSelfMember().hasPermission(message.getTextChannel(), Permission.MESSAGE_MANAGE)) {
            this.reply(message, "I do not have permission to delete messages.");
            return;
        }

        final int amount = Integer.valueOf(args.length == 2 ? args[1] : args[0]);
        final User user = message.getMentionedUsers().stream().findFirst().orElse(null);

        if (amount < 2) {
            this.reply(message, "You must delete at least 2 messages.");
            return;
        }

        if (amount > 1000) {
            this.reply(message, "You cannot bulk delete that many messages! Use purge instead.");
            return;
        }

        message.delete().queue();

        this.hilda.getExecutor().execute(new ChannelClearTask(this.hilda, message.getTextChannel(), amount, user));
    }

}
