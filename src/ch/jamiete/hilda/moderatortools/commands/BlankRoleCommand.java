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
import ch.jamiete.hilda.Util;
import ch.jamiete.hilda.commands.ChannelCommand;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Message;

public class BlankRoleCommand extends ChannelCommand {

    public BlankRoleCommand(final Hilda hilda) {
        super(hilda);

        this.setName("blankrole");
        this.setMinimumPermission(Permission.MANAGE_ROLES);
        this.setDescription("Creates a new blank role.");
    }

    @Override
    public void execute(final Message message, final String[] arguments, final String label) {
        if (!message.getGuild().getSelfMember().hasPermission(Permission.MANAGE_ROLES)) {
            this.reply(message, "Oops! It looks like I don't have permission to manage roles in this server. If you change that you'll be able to use this command.");
            return;
        }

        if (arguments.length == 0) {
            this.usage(message, "<role name>", label);
            return;
        }

        String name = Util.combineSplit(0, arguments, " ");
        message.getGuild().getController().createRole().setName(name).setPermissions(0L).reason("Created role at request of " + Util.getName(message.getAuthor()) + " (" + message.getAuthor().getId() + ")").queue();
        this.reply(message, "OK, I've just created a role with no permissions named " + name + "!");
    }

}
