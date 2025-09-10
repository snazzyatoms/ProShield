package com.snazzyatoms.proshield.commands;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * /roles
 * Opens the roles management GUI.
 */
public class RolesCommand implements CommandExecutor {

    private final GUIManager guiManager;
    private final MessagesUtil messages;

    public RolesCommand(ProShield plugin, GUIManager guiManager) {
        this.guiManager = guiManager;
        this.messages = plugin.getMessagesUtil();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            messages.send(sender, "player-only");
            return true;
        }

        guiManager.openRolesMenu(player);
        messages.send(player, "commands.roles.opened");
        return true;
    }
}
