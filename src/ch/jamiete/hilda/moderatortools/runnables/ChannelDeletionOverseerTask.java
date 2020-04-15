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

import java.util.ArrayList;
import ch.jamiete.hilda.Hilda;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

public class ChannelDeletionOverseerTask implements Runnable {
    private final Hilda hilda;

    public ChannelDeletionOverseerTask(final Hilda hilda) {
        this.hilda = hilda;
    }

    @Override
    public void run() {
        Hilda.getLogger().info("Automatically purging channels...");

        final ArrayList<TextChannel> channels = new ArrayList<TextChannel>();
        for (final Guild guild : this.hilda.getBot().getGuilds()) {
            for (final TextChannel channel : guild.getTextChannels()) {
                if (channel.getTopic() != null && channel.getTopic().toLowerCase().contains("[autoclear]")) {
                    channels.add(channel);
                }
            }
        }

        int count = 0;

        for (final TextChannel channel : channels) {
            count++;
            this.hilda.getExecutor().execute(new ChannelDeletionTask(channel, true));
        }

        Hilda.getLogger().info("Finished queing the purging of " + count + " channels!");
    }

}
