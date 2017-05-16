package ch.jamiete.hilda.moderatortools.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;
import ch.jamiete.hilda.Hilda;
import ch.jamiete.hilda.commands.ChannelCommand;
import ch.jamiete.hilda.configuration.Configuration;
import ch.jamiete.hilda.moderatortools.ModeratorToolsPlugin;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;

public class IgnoreCommand extends ChannelCommand {
    private ModeratorToolsPlugin plugin;

    public IgnoreCommand(Hilda hilda, ModeratorToolsPlugin plugin) {
        super(hilda);

        this.plugin = plugin;

        this.setName("ignore");
        this.setAliases(Arrays.asList(new String[] { "unignore" }));
        this.setDescription("Manages ignored channels.");
        this.setMinimumPermission(Permission.ADMINISTRATOR);
    }

    @Override
    public void execute(Message message, String[] arguments, String label) {
        if (arguments.length == 1 && arguments[0].equalsIgnoreCase("list")) {
            MessageBuilder mb = new MessageBuilder();
            List<String> strings = this.hilda.getCommandManager().getIgnoredChannels();
            List<TextChannel> ignored = new ArrayList<TextChannel>();

            for (String s : strings) {
                TextChannel c = message.getGuild().getTextChannelById(s);

                if (c != null) {
                    ignored.add(c);
                }
            }

            if (ignored.isEmpty()) {
                this.reply(message, "I'm not ignoring any channels!");
            } else {
                mb.append("I'm currently ignoring ");

                for (TextChannel c : ignored) {
                    mb.append(c.getAsMention());
                    mb.append(", ");
                }

                mb.replaceLast(", ", "");
                mb.append(".");
                this.reply(message, mb.build());
            }

            return;
        }

        IgnoreDirection direction = IgnoreDirection.valueOf(label.toUpperCase());
        List<TextChannel> channels = new ArrayList<TextChannel>();

        if (arguments.length == 0) {
            channels.add(message.getTextChannel());
        }

        if (!message.getMentionedChannels().isEmpty()) {
            channels.addAll(message.getMentionedChannels());
        }

        Configuration cfg = this.hilda.getConfigurationManager().getConfiguration(this.plugin, "ignore-" + message.getGuild().getId());
        JsonArray array = cfg.get().getAsJsonArray("channels");

        if (array == null) {
            array = new JsonArray();
        }

        for (TextChannel channel : channels) {
            if (direction == IgnoreDirection.IGNORE) {
                this.hilda.getCommandManager().addIgnoredChannel(channel.getId());

                if (!array.contains(new JsonPrimitive(channel.getId()))) {
                    array.add(channel.getId());
                }
            }

            if (direction == IgnoreDirection.UNIGNORE) {
                this.hilda.getCommandManager().removeIgnoredChannel(channel.getId());
                array.remove(new JsonPrimitive(channel.getId()));
            }
        }

        cfg.get().add("channels", array);
        cfg.save();

        MessageBuilder mb = new MessageBuilder();

        mb.append("OK! I'm ").append(direction == IgnoreDirection.IGNORE ? "now" : "no longer").append(" ignoring ");

        if (channels.size() == 1) {
            mb.append(channels.get(0).getAsMention());
        } else {
            channels.forEach(c -> mb.append(c.getAsMention() + ", "));
            mb.replaceLast(", ", "");
        }

        mb.append(".");

        mb.buildAll().forEach(m -> message.getChannel().sendMessage(m).queue());
    }

    private enum IgnoreDirection {
        IGNORE, UNIGNORE;
    }

}
