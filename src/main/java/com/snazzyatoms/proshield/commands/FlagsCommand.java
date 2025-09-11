// src/main/java/com/snazzyatoms/proshield/commands/FlagsCommand.java
package com.snazzyatoms.proshield.commands;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.GUIManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * /flags command
 *
 * ✅ Opens the flags GUI for players
 * ✅ Requires "proshield.flags" permission
 * ✅ Admins bypass ownership restrictions
 */
public class FlagsCommand implements CommandExecutor {

    private final ProShield plugin;
    private final GUIManager guiManager;

    public FlagsCommand(ProShield plugin, GUIManager guiManager) {
        this.plugin = plugin;
        this.guiManager = guiManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        if (!player.hasPermission("proshield.flags")) {
            player.sendMessage(ChatColor.RED + "You don’t have permission to toggle claim flags.");
            return true;
        }

        guiManager.openFlagsMenu(player);
        return true;
    }
}
