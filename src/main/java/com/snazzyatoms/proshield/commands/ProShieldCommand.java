// src/main/java/com/snazzyatoms/proshield/commands/ProShieldCommand.java
package com.snazzyatoms.proshield.commands;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.compass.CompassManager;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ProShieldCommand implements CommandExecutor {

    private final ProShield plugin;
    private final MessagesUtil messages;
    private final GUIManager guiManager;
    private final CompassManager compassManager;

    public ProShieldCommand(ProShield plugin, GUIManager guiManager, CompassManager compassManager) {
        this.plugin = plugin;
        this.messages = plugin.getMessagesUtil();
        this.guiManager = guiManager;
        this.compassManager = compassManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "reload":
                if (!sender.hasPermission("proshield.admin.reload")) {
                    messages.send(sender, "error.no-permission");
                    return true;
                }
                plugin.reloadConfig();
                plugin.getMessagesUtil().reload();
                messages.send(sender, "messages.reloaded", "&aProShield configuration reloaded.");
                return true;

            case "debug":
                if (!sender.hasPermission("proshield.admin")) {
                    messages.send(sender, "error.no-permission");
                    return true;
                }
                plugin.toggleDebug();
                messages.send(sender, "admin.debug", "&eDebug mode toggled: " + plugin.isDebugEnabled());
                return true;

            case "compass":
                if (!(sender instanceof Player player)) {
                    messages.send(sender, "error.player-only");
                    return true;
                }
                if (!player.hasPermission("proshield.compass")) {
                    messages.send(player, "error.no-permission");
                    return true;
                }
                compassManager.giveCompass(player, player.isOp());
                messages.send(player, "compass.given", "&aProShield compass has been added to your inventory.");
                return true;

            case "flags":
                if (!(sender instanceof Player playerFlags)) {
                    messages.send(sender, "error.player-only");
                    return true;
                }
                if (!playerFlags.hasPermission("proshield.flags")) {
                    messages.send(playerFlags, "error.no-permission");
                    return true;
                }
                guiManager.openMenu(playerFlags, "flags");
                return true;

            default:
                sendHelp(sender);
                return true;
        }
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§bProShield Commands:");
        sender.sendMessage("§7/ps reload §f- Reload configuration");
        sender.sendMessage("§7/ps debug §f- Toggle debug mode");
        sender.sendMessage("§7/ps compass §f- Get your ProShield compass");
        sender.sendMessage("§7/ps flags §f- Open the flags GUI");
        sender.sendMessage("§7/claim, /unclaim, /trust, /untrust, /roles, /transfer §f- Player commands");
    }
}
