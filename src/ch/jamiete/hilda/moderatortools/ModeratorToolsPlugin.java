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
package ch.jamiete.hilda.moderatortools;

import java.util.concurrent.TimeUnit;
import ch.jamiete.hilda.Hilda;
import ch.jamiete.hilda.Util;
import ch.jamiete.hilda.moderatortools.commands.ArchiveCommand;
import ch.jamiete.hilda.moderatortools.commands.ClearCommand;
import ch.jamiete.hilda.moderatortools.commands.MuteCommand;
import ch.jamiete.hilda.moderatortools.commands.PurgeCommand;
import ch.jamiete.hilda.moderatortools.listeners.FlowListener;
import ch.jamiete.hilda.moderatortools.runnables.ChannelDeletionOverseerTask;
import ch.jamiete.hilda.plugins.HildaPlugin;

public class ModeratorToolsPlugin extends HildaPlugin {

    public ModeratorToolsPlugin(final Hilda hilda) {
        super(hilda);
    }

    @Override
    public void onEnable() {
        this.getHilda().getCommandManager().registerChannelCommand(new ArchiveCommand(this.getHilda()));
        this.getHilda().getCommandManager().registerChannelCommand(new ClearCommand(this.getHilda()));
        this.getHilda().getCommandManager().registerChannelCommand(new MuteCommand(this.getHilda()));
        this.getHilda().getCommandManager().registerChannelCommand(new PurgeCommand(this.getHilda()));

        this.getHilda().getBot().addEventListener(new FlowListener());

        final long first = Util.getNextMidnightInMillis("UTC") - System.currentTimeMillis();
        this.getHilda().getExecutor().scheduleAtFixedRate(new ChannelDeletionOverseerTask(this.getHilda()), first, 86400000, TimeUnit.MILLISECONDS); // At midnight then every 24 hours
        Hilda.getLogger().info("Purging channel messages in " + Util.getFriendlyTime(first));
    }

}
