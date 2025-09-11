// src/main/java/com/snazzyatoms/proshield/commands/CompassCommand.java
package com.snazzyatoms.proshield.commands;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.compass.CompassManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * /compass command
 *
 * ✅ Gives players the ProShield compass.
 * ✅ Admins get admin-style compass (different look).
 * ✅ Uses CompassManager instance from ProShield.
 */
public class CompassCommand implements CommandExecutor {

    private final ProShield plugin;
    private final CompassManager compassManager;
    private final MessagesUtil messages;

    public CompassCommand(ProShield plugin, CompassManager compassManager) {
        this.plugin = plugin;
        this.compassManager = compassManager;
        this.messages = plugin.getMessagesUtil();
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

        boolean isAdmin = player.hasPermission("proshield.admin");
        compassManager.giveCompass(player, isAdmin);

        messages.send(player, "compass.given");
        return true;
    }
}
