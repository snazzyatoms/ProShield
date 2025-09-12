// src/main/java/com/snazzyatoms/proshield/commands/ClaimCommand.java
package com.snazzyatoms.proshield.commands;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Chunk;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * /claim command
 *
 * ✅ Allows players to claim their current chunk.
 * ✅ Checks permissions and ownership.
 * ✅ Integrates with PlotManager + MessagesUtil.
 */
public class ClaimCommand implements CommandExecutor {

    private final ProShield plugin;
    private final PlotManager plots;
    private final MessagesUtil messages;

    public ClaimCommand(ProShield plugin, PlotManager plots) {
        this.plugin = plugin;
        this.plots = plots;
        this.messages = plugin.getMessagesUtil();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            messages.send(sender, "error.players-only");
            return true;
        }

        if (!player.hasPermission("proshield.use")) {
            messages.send(player, "error.no-permission");
            return true;
        }

        Chunk chunk = player.getLocation().getChunk();

        // Already claimed?
        Plot existing = plots.getPlot(chunk);
        if (existing != null) {
            if (existing.isOwner(player.getUniqueId())) {
                messages.send(player, "claim.already-owner");
            } else {
                messages.send(player, "claim.already-claimed", existing.getOwnerNameSafe());
            }
            return true;
        }

        // Create new claim
        boolean success = plots.createClaim(player.getUniqueId(), chunk);
        if (!success) {
            messages.send(player, "claim.failed");
            return true;
        }

        messages.send(player, "claim.success", chunk.getX(), chunk.getZ());
        return true;
    }
}
