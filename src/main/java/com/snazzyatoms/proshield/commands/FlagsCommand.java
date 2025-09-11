// src/main/java/com/snazzyatoms/proshield/commands/FlagsCommand.java
package com.snazzyatoms.proshield.commands;

import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FlagsCommand implements CommandExecutor {

    private final GUIManager guiManager;
    private final MessagesUtil messages;

    public FlagsCommand(GUIManager guiManager) {
        this.guiManager = guiManager;
        this.messages = guiManager != null ? guiManager.getPlugin().getMessagesUtil() : null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            if (messages != null) messages.send(sender, "error.players-only");
            return true;
        }

        if (!player.hasPermission("proshield.flags")) {
            if (messages != null) messages.send(player, "error.no-permission");
            return true;
        }

        // ✅ Open the Claim Flags menu
        guiManager.openFlagsMenu(player);
        return true;
    }
}
