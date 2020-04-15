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

import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import ch.jamiete.hilda.Hilda;
import ch.jamiete.hilda.Util;
import ch.jamiete.hilda.configuration.Configuration;
import ch.jamiete.hilda.moderatortools.commands.ArchiveCommand;
import ch.jamiete.hilda.moderatortools.commands.BlankRoleCommand;
import ch.jamiete.hilda.moderatortools.commands.CheckRolesCommand;
import ch.jamiete.hilda.moderatortools.commands.ClearCommand;
import ch.jamiete.hilda.moderatortools.commands.FlowCommand;
import ch.jamiete.hilda.moderatortools.commands.IgnoreCommand;
import ch.jamiete.hilda.moderatortools.commands.MuteCommand;
import ch.jamiete.hilda.moderatortools.commands.MuteListCommand;
import ch.jamiete.hilda.moderatortools.commands.PurgeCommand;
import ch.jamiete.hilda.moderatortools.commands.ServerinfoCommand;
import ch.jamiete.hilda.moderatortools.commands.UserinfoCommand;
import ch.jamiete.hilda.moderatortools.listeners.AnnouncementsListener;
import ch.jamiete.hilda.moderatortools.listeners.FlowListener;
import ch.jamiete.hilda.moderatortools.runnables.ChannelDeletionOverseerTask;
import ch.jamiete.hilda.plugins.HildaPlugin;
import net.dv8tion.jda.api.entities.Guild;

public class ModeratorToolsPlugin extends HildaPlugin {
    private final FlowUpdater updater;

    public ModeratorToolsPlugin(final Hilda hilda) {
        super(hilda);

        this.updater = new FlowUpdater(hilda, this);
    }

    @Override
    public void onDisable() {
        for (final Guild guild : this.getHilda().getBot().getGuilds()) {
            this.updater.save(guild);
        }
    }

    @Override
    public void onEnable() {
        this.getHilda().getCommandManager().registerChannelCommand(new ArchiveCommand(this.getHilda()));
        this.getHilda().getCommandManager().registerChannelCommand(new BlankRoleCommand(this.getHilda()));
        this.getHilda().getCommandManager().registerChannelCommand(new CheckRolesCommand(this.getHilda()));
        this.getHilda().getCommandManager().registerChannelCommand(new ClearCommand(this.getHilda()));
        this.getHilda().getCommandManager().registerChannelCommand(new FlowCommand(this.getHilda(), this));
        this.getHilda().getCommandManager().registerChannelCommand(new IgnoreCommand(this.getHilda(), this));
        this.getHilda().getCommandManager().registerChannelCommand(new MuteCommand(this.getHilda()));
        this.getHilda().getCommandManager().registerChannelCommand(new MuteListCommand(this.getHilda()));
        this.getHilda().getCommandManager().registerChannelCommand(new PurgeCommand(this.getHilda()));
        this.getHilda().getCommandManager().registerChannelCommand(new ServerinfoCommand(this.getHilda()));
        this.getHilda().getCommandManager().registerChannelCommand(new UserinfoCommand(this.getHilda()));

        this.getHilda().getBot().addEventListener(new AnnouncementsListener());
        this.getHilda().getBot().addEventListener(new FlowListener(this.getHilda(), this));

        final long first = Util.getNextMidnightInMillis("UTC") - System.currentTimeMillis();
        this.getHilda().getExecutor().scheduleAtFixedRate(new ChannelDeletionOverseerTask(this.getHilda()), first, 86400000, TimeUnit.MILLISECONDS); // At midnight then every 24 hours
        Hilda.getLogger().info("Purging channel messages in " + Util.getFriendlyTime(first));

        for (final Guild guild : this.getHilda().getBot().getGuilds()) {
            final Configuration cfg = this.getHilda().getConfigurationManager().getConfiguration(this, "ignore-" + guild.getId());
            final JsonArray array = cfg.get().getAsJsonArray("channels");

            if (array != null) {
                for (JsonElement jsonElement : array) {
                    this.getHilda().getCommandManager().addIgnoredChannel(jsonElement.getAsString());
                }

                Hilda.getLogger().info("Ignored " + array.size() + " channels in " + guild.getName());
            }

            this.updater.check(guild);
        }
    }

    @Override
    public void save() {
        for (final Guild guild : this.getHilda().getBot().getGuilds()) {
            this.updater.save(guild);
        }
    }

}
