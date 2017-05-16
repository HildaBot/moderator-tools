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

import ch.jamiete.hilda.Hilda;
import ch.jamiete.hilda.commands.ChannelCommand;
import ch.jamiete.hilda.commands.CommandTranscendLevel;
import ch.jamiete.hilda.moderatortools.runnables.ChannelDeletionTask;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Message;

public class PurgeCommand extends ChannelCommand {

    public PurgeCommand(final Hilda hilda) {
        super(hilda);

        this.setName("purge");
        this.setMinimumPermission(Permission.ADMINISTRATOR);
        this.setDescription("Purges the channel by deleting all messages.");
        this.setTranscend(CommandTranscendLevel.PERMISSION);
    }

    @Override
    public void execute(final Message message, final String[] arguments, final String label) {
        if (!message.getGuild().getSelfMember().hasPermission(message.getTextChannel(), Permission.MESSAGE_MANAGE)) {
            this.reply(message, "I do not have permission to delete messages in this channel.");
            return;
        }

        this.hilda.getExecutor().execute(new ChannelDeletionTask(message.getTextChannel()));
    }

}
