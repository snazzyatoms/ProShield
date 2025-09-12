package com.snazzyatoms.proshield.commands;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.compass.CompassManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Handles core ProShield admin/meta commands.
 * Includes: reload, debug, compass, help.
 */
public class ProShieldCommand implements CommandExecutor {

    private final ProShield plugin;
    private final CompassManager compassManager;
    private final MessagesUtil messages;

    public ProShieldCommand(ProShield plugin, CompassManager compassManager) {
        this.plugin = plugin;
        this.compassManager = compassManager;
        this.messages = plugin.getMessagesUtil();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "reload" -> {
                if (!sender.hasPermission("proshield.admin.reload")) {
                    messages.send(sender, "error.no-permission");
                    return true;
                }
                plugin.reloadConfig();
                messages.reload();
                messages.send(sender, "messages.reloaded");
            }

            case "debug" -> {
                if (!sender.hasPermission("proshield.admin")) {
                    messages.send(sender, "error.no-permission");
                    return true;
                }
                plugin.toggleDebug();
                messages.send(sender, "admin.debug-" +
                        (plugin.isDebugEnabled() ? "on" : "off"));
            }

            case "compass" -> {
                if (!(sender instanceof Player player)) {
                    messages.send(sender, "error.player-only");
                    return true;
                }
                if (!player.hasPermission("proshield.compass")) {
                    messages.send(player, "error.no-permission");
                    return true;
                }
                compassManager.giveCompass(player, player.isOp());
                messages.send(player, "compass.given");
            }

            default -> sendHelp(sender);
        }
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(MessagesUtil.PREFIX + "§bProShield Commands:");
        sender.sendMessage(" §7/proshield reload §f- Reload plugin configs");
        sender.sendMessage(" §7/proshield debug §f- Toggle debug mode");
        sender.sendMessage(" §7/proshield compass §f- Get your ProShield compass");
        sender.sendMessage(" §7/claim, /unclaim, /trust... §f- Player land commands");
    }
}
