// src/main/java/com/snazzyatoms/proshield/commands/UnclaimCommand.java
package com.snazzyatoms.proshield.commands;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Chunk;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * /unclaim command
 *
 * ✅ Allows a player to unclaim their current chunk.
 * ✅ Checks ownership or permission through ClaimRoleManager.
 * ✅ Integrates with PlotManager + MessagesUtil.
 */
public class UnclaimCommand implements CommandExecutor {

    private final ProShield plugin;
    private final PlotManager plots;
    private final ClaimRoleManager roles;
    private final MessagesUtil messages;

    public UnclaimCommand(ProShield plugin, PlotManager plots, ClaimRoleManager roles) {
        this.plugin = plugin;
        this.plots = plots;
        this.roles = roles;
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
        Plot plot = plots.getPlot(chunk);

        if (plot == null) {
            messages.send(player, "unclaim.not-claimed");
            return true;
        }

        // Check if owner OR role allows unclaim
        if (!plot.isOwner(player.getUniqueId())) {
            if (!roles.canUnclaim(plot.getId(), player.getUniqueId())) {
                messages.send(player, "unclaim.no-permission");
                return true;
            }
        }

        // Remove the claim
        plots.removePlot(plot);

        messages.send(player, "unclaim.success", chunk.getX(), chunk.getZ());
        return true;
    }
}
