// src/main/java/com/snazzyatoms/proshield/commands/CompassCommand.java
package com.snazzyatoms.proshield.commands;

import com.snazzyatoms.proshield.compass.CompassManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * /compass command
 *
 * ✅ Gives the player their ProShield compass (only one).
 * ✅ Uses CompassManager (injected via constructor).
 * ✅ Handles OP/Admin vs regular players (admin compass vs normal).
 * ✅ Warns if player already has one instead of duplicating.
 */
public class CompassCommand implements CommandExecutor {

    private final CompassManager compassManager;
    private final MessagesUtil messages;

    public CompassCommand(CompassManager compassManager, MessagesUtil messages) {
        this.compassManager = compassManager;
        this.messages = messages;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            messages.send(sender, "error.players-only");
            return true;
        }

        if (!player.hasPermission("proshield.compass")) {
            messages.send(player, "error.no-permission");
            return true;
        }

        // ✅ Check if player already has a ProShield compass
        for (ItemStack item : player.getInventory().getContents()) {
            if (compassManager.isProShieldCompass(item)) {
                messages.send(player, "prefix", "&eYou already have a ProShield compass!");
                return true;
            }
        }

        // ✅ Give the compass through CompassManager
        compassManager.giveCompass(player, player.isOp());
        messages.send(player, "prefix", "&aYou have been given the ProShield compass!");

        return true;
    }
}
