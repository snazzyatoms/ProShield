package com.snazzyatoms.proshield.commands;

import com.snazzyatoms.proshield.compass.CompassManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * /compass command
 *
 * ✅ Gives the correct ProShield compass (player vs admin style)
 * ✅ Replaces any existing compasses
 * ✅ Requires proshield.compass permission
 */
public class CompassCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }

        if (!player.hasPermission("proshield.compass")) {
            player.sendMessage("§cYou don’t have permission to use this command.");
            return true;
        }

        // Give/refresh compass
        CompassManager.giveCompass(player, true);
        player.sendMessage("§aYour ProShield compass has been refreshed!");

        return true;
    }
}
