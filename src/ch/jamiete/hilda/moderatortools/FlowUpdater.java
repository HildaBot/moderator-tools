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
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;

public class FlowUpdater {
    private final Hilda hilda;
    private final ModeratorToolsPlugin plugin;

    public FlowUpdater(Hilda hilda, ModeratorToolsPlugin plugin) {
        this.hilda = hilda;
        this.plugin = plugin;
    }

    public void save(Guild guild) {
        try {
            List<FlowMember> members = new ArrayList<FlowMember>();

            for (Member member : guild.getMembers()) {
                members.add(new FlowMember(member));
            }

            final FileOutputStream stream = new FileOutputStream("flow/" + guild.getId() + ".hildausers", false);
            final ObjectOutputStream obj = new ObjectOutputStream(stream);

            obj.writeObject(members);

            Hilda.getLogger().info("Saved " + members.size() + " members to disk for " + guild.getId());

            obj.close();
            stream.close();
        } catch (Exception e) {
            Hilda.getLogger().log(Level.WARNING, "Failed to save flow members to disk for " + guild.getName() + " (" + guild.getId() + ").", e);
        }
    }

    public void check(Guild guild) {
        if (!this.shouldCheck(guild)) {
            return;
        }

        File list = new File("flow/" + guild.getId() + ".hildausers");
        Configuration cfg = this.hilda.getConfigurationManager().getConfiguration(this.plugin, "flow-" + guild.getId());

        try {
            final FileInputStream stream = new FileInputStream(list);
            final ObjectInputStream obj = new ObjectInputStream(stream);

            @SuppressWarnings("unchecked")
            final ArrayList<FlowMember> members = (ArrayList<FlowMember>) obj.readObject();

            for (FlowMember putative : members) {
                Member member = null;

                try {
                    member = guild.getMemberById(putative.id);
                } catch (Exception e) {
                    // Ignore
                }

                if (member == null) {
                    FlowListener.sendMessage(guild, FlowListener.compute(cfg.getString("leave", FlowListener.DEFAULT_LEAVE), putative));
                }
            }

            for (Member member : guild.getMembers()) {
                boolean waspresent = members.stream().anyMatch(m -> m.id.equals(member.getUser().getId()));

                if (!waspresent) {
                    FlowListener.sendMessage(guild, FlowListener.compute(cfg.getString("join", FlowListener.DEFAULT_JOIN), member));
                }
            }

            obj.close();
            stream.close();

            list.delete();
        } catch (Exception e) {
            Hilda.getLogger().log(Level.WARNING, "Failed to check flow of " + guild.getName() + " (" + guild.getId() + ").", e);
        }
    }

    public boolean shouldCheck(final Guild guild) {
        return guild.getTextChannels().stream().anyMatch(c -> c.getTopic() != null && c.getTopic().toLowerCase().contains("[flow]"));
    }

}
