// src/main/java/com/snazzyatoms/proshield/commands/FlagsCommand.java
package com.snazzyatoms.proshield.commands;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.GUIManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FlagsCommand implements CommandExecutor {

    private final ProShield plugin;
    private final GUIManager gui;

    public FlagsCommand(ProShield plugin, GUIManager gui) {
        this.plugin = plugin;
        this.gui = gui;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be run by a player.");
            return true;
        }

        if (!player.hasPermission("proshield.flags")) {
            player.sendMessage("§cYou don't have permission to use this.");
            return true;
        }

        gui.openFlagsMenu(player);
        return true;
    }
}
