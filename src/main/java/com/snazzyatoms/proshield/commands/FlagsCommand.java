package com.snazzyatoms.proshield.commands;

import com.snazzyatoms.proshield.gui.GUIManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * /flags command
 *
 * ✅ Opens the claim flags GUI
 * ✅ Players need "proshield.flags"
 * ✅ Admins with "proshield.admin.flags" can use anywhere
 */
public class FlagsCommand implements CommandExecutor {

    private final GUIManager gui;

    public FlagsCommand(GUIManager gui) {
        this.gui = gui;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }

        if (!player.hasPermission("proshield.flags")) {
            player.sendMessage("§cYou don’t have permission to use this command.");
            return true;
        }

        gui.openFlagsMenu(player);
        return true;
    }
}
