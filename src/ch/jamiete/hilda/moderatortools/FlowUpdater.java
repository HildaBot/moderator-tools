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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import ch.jamiete.hilda.Hilda;
import ch.jamiete.hilda.configuration.Configuration;
import ch.jamiete.hilda.moderatortools.listeners.FlowListener;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

public class FlowUpdater {
    private final Hilda hilda;
    private final ModeratorToolsPlugin plugin;

    public FlowUpdater(final Hilda hilda, final ModeratorToolsPlugin plugin) {
        this.hilda = hilda;
        this.plugin = plugin;
    }

    public void check(final Guild guild) {
        if (!this.shouldCheck(guild)) {
            return;
        }

        final File list = new File("data/flow-" + guild.getId() + ".hildausers");
        final Configuration cfg = this.hilda.getConfigurationManager().getConfiguration(this.plugin, "flow-" + guild.getId());

        if (!list.exists()) {
            return;
        }

        try {
            final FileInputStream stream = new FileInputStream(list);
            final ObjectInputStream obj = new ObjectInputStream(stream);

            int differences = 0;

            @SuppressWarnings("unchecked")
            final ArrayList<FlowMember> members = (ArrayList<FlowMember>) obj.readObject();

            for (final FlowMember putative : members) {
                Member member = null;

                try {
                    member = guild.getMemberById(putative.id);
                } catch (final Exception e) {
                    // Ignore
                }

                if (member == null) {
                    differences++;
                    FlowListener.sendMessage(guild, FlowListener.compute(cfg.getString("leave", FlowListener.DEFAULT_LEAVE), putative, guild));
                }
            }

            for (final Member member : guild.getMembers()) {
                final boolean waspresent = members.stream().anyMatch(m -> m.id.equals(member.getUser().getId()));

                if (!waspresent) {
                    differences++;
                    FlowListener.sendMessage(guild, FlowListener.compute(cfg.getString("join", FlowListener.DEFAULT_JOIN), member));
                }
            }

            obj.close();
            stream.close();

            list.delete();

            Hilda.getLogger().info("Loaded flow information for " + guild.getName() + " (" + guild.getId() + ") and found " + differences + " differences");
        } catch (final Exception e) {
            Hilda.getLogger().log(Level.WARNING, "Failed to check flow of " + guild.getName() + " (" + guild.getId() + ").", e);
        }
    }

    public void save(final Guild guild) {
        if (!this.shouldCheck(guild)) {
            return;
        }

        try {
            final List<FlowMember> members = new ArrayList<>();

            for (final Member member : guild.getMembers()) {
                members.add(new FlowMember(member));
            }

            final File folder = new File("data");

            if (!folder.isDirectory()) {
                folder.mkdir();
            }

            final File file = new File(folder, "flow-" + guild.getId() + ".hildausers");

            if (!file.exists()) {
                file.createNewFile();
            }

            final FileOutputStream stream = new FileOutputStream(file, false);
            final ObjectOutputStream obj = new ObjectOutputStream(stream);

            obj.writeObject(members);

            Hilda.getLogger().fine("Saved " + members.size() + " members to disk for " + guild.getId());

            obj.close();
            stream.close();
        } catch (final Exception e) {
            Hilda.getLogger().log(Level.WARNING, "Failed to save flow members to disk for " + guild.getName() + " (" + guild.getId() + ").", e);
        }
    }

    public boolean shouldCheck(final Guild guild) {
        return guild.getTextChannels().stream().anyMatch(c -> c.getTopic() != null && c.getTopic().toLowerCase().contains("[flow]"));
    }

}
