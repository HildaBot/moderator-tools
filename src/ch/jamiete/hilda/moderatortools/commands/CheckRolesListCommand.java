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
import ch.jamiete.hilda.commands.ChannelSeniorCommand;
import ch.jamiete.hilda.commands.ChannelSubCommand;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.MessageBuilder.Formatting;
import net.dv8tion.jda.core.MessageBuilder.SplitPolicy;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import java.util.ArrayList;
import java.util.List;

public class CheckRolesListCommand extends ChannelSubCommand {

    public CheckRolesListCommand(final Hilda hilda, ChannelSeniorCommand senior) {
        super(hilda, senior);

        this.setName("list");
        this.setDescription("Checks whether any roles have incorrect permission settings.");
    }

    @Override
    public void execute(final Message message, final String[] arguments, final String label) {
        List<Role> roles = message.getGuild().getRoles();

        MessageBuilder mb = new MessageBuilder();
        mb.append("Roles for " + message.getGuild().getName(), Formatting.BOLD).append("\n");

        for (int i = 0; i < roles.size(); i++) {
            Role role = roles.get(i);

            mb.append("\n").append("[" + i + "]", Formatting.BLOCK).append(" ");
            mb.append(Util.sanitise(role.getName())).append(": ");

            boolean permissions = role.getPermissionsRaw() == 0;

            if (permissions) {
                mb.append("No permissions");
            } else {
                mb.append("Has permissions", Formatting.UNDERLINE);
            }

            List<String> abornmal = new ArrayList<>();

            for (TextChannel channel : message.getGuild().getTextChannels()) {
                if (channel.getPermissionOverride(role) != null) {
                    abornmal.add("#" + channel.getName());
                }
            }

            if (!abornmal.isEmpty()) {
                mb.append(permissions ? " but " : " and ");
                mb.append("has override in ").append(Util.getAsList(abornmal));
            }
        }

        mb.buildAll(SplitPolicy.NEWLINE).forEach(m -> this.reply(message, m));
    }

}
