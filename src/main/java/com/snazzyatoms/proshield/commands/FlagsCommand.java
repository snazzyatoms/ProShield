// src/main/java/com/snazzyatoms/proshield/commands/FlagsCommand.java
package com.snazzyatoms.proshield.commands;

import com.snazzyatoms.proshield.gui.GUIManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * FlagsCommand
 *
 * ✅ Opens the claim flags menu via GUIManager
 * ✅ Constructor only needs GUIManager (fixed to match ProShield.java)
 */
public class FlagsCommand implements CommandExecutor {

    private final GUIManager guiManager;

    public FlagsCommand(GUIManager guiManager) {
        this.guiManager = guiManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        guiManager.openFlagsMenu(player);
        return true;
    }
}
