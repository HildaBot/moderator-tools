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
import ch.jamiete.hilda.commands.ChannelSeniorCommand;
import ch.jamiete.hilda.commands.CommandTranscendLevel;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;

public class CheckRolesCommand extends ChannelSeniorCommand {

    public CheckRolesCommand(final Hilda hilda) {
        super(hilda);

        this.setName("checkroles");
        this.setDescription("Checks whether any roles have incorrect permission settings.");
        this.setMinimumPermission(Permission.MANAGE_ROLES);
        this.setTranscend(CommandTranscendLevel.PERMISSION);

        this.registerSubcommand(new CheckRolesListCommand(this.hilda, this));
        this.registerSubcommand(new CheckRolesFixCommand(this.hilda, this));
    }

    @Override
    public void execute(final Message message, final String[] arguments, final String label) {
        if (!message.getGuild().getSelfMember().hasPermission(Permission.MANAGE_ROLES)) {
            this.reply(message, "Oops! It looks like I don't have permission to manage roles in this server. If you change that you'll be able to use this command.");
            return;
        }

        super.execute(message, arguments, label);
    }

}
