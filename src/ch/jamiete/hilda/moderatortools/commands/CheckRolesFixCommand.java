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

import java.util.ArrayList;
import java.util.List;
import ch.jamiete.hilda.Hilda;
import ch.jamiete.hilda.Util;
import ch.jamiete.hilda.commands.ChannelSeniorCommand;
import ch.jamiete.hilda.commands.ChannelSubCommand;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.MessageBuilder.Formatting;
import net.dv8tion.jda.core.MessageBuilder.SplitPolicy;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.Role;

public class CheckRolesFixCommand extends ChannelSubCommand {
    private final String USAGE = "You must specify the role Hilda IDs to null permissions for. These can be obtained with the `list` subcommand. You can give a single role, roles separated by commas (without spaces) or a range. For example, `1,3-5,7`.";

    public CheckRolesFixCommand(final Hilda hilda, final ChannelSeniorCommand senior) {
        super(hilda, senior);

        this.setName("fix");
        this.setDescription("Removes all permissions for certain roles.");
    }

    private void error(final Message message, final String text) {
        this.reply(message, "I couldn't interpret your numbers. " + text + "\n" + this.USAGE);
    }

    @Override
    public void execute(final Message message, final String[] arguments, final String label) {
        if (arguments.length == 0) {
            this.reply(message, this.USAGE);
            return;
        }

        final List<Integer> sought = new ArrayList<Integer>();

        for (final String bit : arguments[0].split(",")) {
            if (bit.equals("")) {
                continue;
            }

            Integer num = null;

            try {
                num = Integer.valueOf(bit);
            } catch (final Exception e) {
                // Ignore
            }

            if (num != null) {
                if (num < 0) {
                    this.error(message, "One of your single numbers was less than zero.");
                    return;
                }

                sought.add(num);
                continue;
            }

            if (!bit.contains("-")) {
                this.error(message, "You must provide a number or a range of numbers.");
                return;
            }

            final String[] range = bit.split("-");

            if (range.length != 2) {
                this.error(message, "A range of numbers may only have one hyphen.");
                return;
            }

            Integer a, b = null;

            try {
                a = Integer.valueOf(range[0]);
                b = Integer.valueOf(range[1]);
            } catch (final Exception e) {
                this.error(message, "One of your ranges did not have two numbers in it.");
                return;
            }

            if (a < 0 || b < 0) {
                this.error(message, "One of your ranges had numbers less than zero.");
                return;
            }

            if (b < a) {
                this.error(message, "One of your ranges has a second value less than the first, but the second value must be greater.");
                return;
            }

            for (int i = a; i <= b; i++) {
                sought.add(i);
            }
        }

        if (sought.isEmpty()) {
            this.error(message, "Incorrect usage.");
            return;
        }

        final List<Role> roles = message.getGuild().getRoles();

        sought.forEach(i -> {
            if (roles.get(i) == null) {
                this.reply(message, "The role with Hilda ID " + i + " you sought to change does not exist.");
                return;
            }
        });

        final MessageBuilder mb = new MessageBuilder();
        mb.append("Roles for " + message.getGuild().getName(), Formatting.BOLD).append("\n");

        for (final Integer i : sought) {
            final Role role = roles.get(i);

            role.getManager().setPermissions(0).reason("Removed permissions at request of " + Util.getName(message.getAuthor()) + " (" + message.getAuthor().getId() + ")").queue();

            mb.append("\n");
            mb.append(Util.sanitise(role.getName())).append(" now has no permissions.");
        }

        mb.buildAll(SplitPolicy.NEWLINE).forEach(m -> this.reply(message, m));
    }

}
