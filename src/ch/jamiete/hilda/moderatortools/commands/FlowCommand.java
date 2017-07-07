package ch.jamiete.hilda.moderatortools.commands;

import ch.jamiete.hilda.Hilda;
import ch.jamiete.hilda.Util;
import ch.jamiete.hilda.commands.ChannelCommand;
import ch.jamiete.hilda.commands.CommandManager;
import ch.jamiete.hilda.commands.CommandTranscendLevel;
import ch.jamiete.hilda.configuration.Configuration;
import ch.jamiete.hilda.moderatortools.ModeratorToolsPlugin;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.MessageBuilder.Formatting;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Message;

public class FlowCommand extends ChannelCommand {
    private ModeratorToolsPlugin plugin;

    public FlowCommand(Hilda hilda, ModeratorToolsPlugin plugin) {
        super(hilda);

        this.plugin = plugin;

        this.setName("flow");
        this.setDescription("Manages the content of flow messages.");
        this.setMinimumPermission(Permission.MANAGE_SERVER);
        this.setTranscend(CommandTranscendLevel.PERMISSION);
    }

    @Override
    public void execute(Message message, String[] arguments, String label) {
        Configuration cfg = this.hilda.getConfigurationManager().getConfiguration(this.plugin, "flow-" + message.getGuild().getId());

        if (arguments.length == 0) {
            MessageBuilder mb = new MessageBuilder();

            mb.append("Flow messages", Formatting.UNDERLINE).append("\n");
            mb.append("Current flow message configuration", Formatting.ITALICS).append("\n\n");

            mb.append("Join:", Formatting.BOLD).append(" ").append(cfg.getString("join", "Default.")).append("\n");
            mb.append("Leave:", Formatting.BOLD).append(" ").append(cfg.getString("leave", "Default.")).append("\n\n");

            mb.append("Change these with ").append(CommandManager.PREFIX + label + " <join/leave> <message>", Formatting.BOLD);
            mb.append(". You can use the following substitutions: $mention $username $effective $discriminator $id. ");
            mb.append("The default use `$mention ($username#$discriminator)`.");

            this.reply(message, mb.build());
            return;
        }

        String direction = null;
        String to = null;

        if (arguments[0].equalsIgnoreCase("join") || arguments[0].equalsIgnoreCase("leave")) {
            direction = arguments[0];

            if (arguments.length > 1) {
                to = Util.combineSplit(1, arguments, " ");
            }
        }

        if (direction == null) {
            this.usage(message, "<join/leave> <message> OR with no arguments to view current", label);
            return;
        }

        cfg.get().addProperty(direction, to);
        cfg.save();

        this.reply(message, "OK, I've set the " + direction + " message to " + (to == null ? "the default" : to));
    }

}
