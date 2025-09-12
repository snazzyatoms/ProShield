package com.snazzyatoms.proshield.commands;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FlagsCommand implements CommandExecutor {

    private final ProShield plugin;
    private final GUIManager guiManager;
    private final MessagesUtil messages;

    public FlagsCommand(ProShield plugin, GUIManager guiManager) {
        this.plugin = plugin;
        this.guiManager = guiManager;
        this.messages = plugin.getMessagesUtil();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            messages.send(sender, "error.players-only");
            return true;
        }

        if (!player.hasPermission("proshield.flags")) {
            messages.send(player, "error.no-permission");
            return true;
        }

        boolean fromAdmin = player.hasPermission("proshield.admin");
        guiManager.openFlagsMenu(player, fromAdmin);
        return true;
    }
}
