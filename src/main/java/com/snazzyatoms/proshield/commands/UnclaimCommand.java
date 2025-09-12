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

import java.util.UUID;

/**
 * /unclaim command
 *
 * ✅ Removes a player’s claim if they are the owner or have unclaim permission.
 */
public class UnclaimCommand implements CommandExecutor {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final ClaimRoleManager roles;
    private final MessagesUtil messages;

    public UnclaimCommand(ProShield plugin, PlotManager plotManager, ClaimRoleManager roles) {
        this.plugin = plugin;
        this.plotManager = plotManager;
        this.roles = roles;
        this.messages = plugin.getMessagesUtil();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            messages.send(sender, "error.players-only");
            return true;
        }

        Chunk chunk = player.getLocation().getChunk();
        Plot plot = plotManager.getPlot(chunk);

        if (plot == null) {
            messages.send(player, "error.not-in-claim");
            return true;
        }

        UUID uid = player.getUniqueId();

        // Owner bypass
        if (!plot.isOwner(uid)) {
            if (!roles.canUnclaim(uid, plot)) {
                messages.send(player, "error.cannot-unclaim");
                return true;
            }
        }

        // Unclaim
        plotManager.removePlot(plot);
        messages.send(player, "unclaim.success", plot.getDisplayNameSafe());

        messages.debug("&eClaim unclaimed: " + plot.getDisplayNameSafe() + " by " + player.getName());
        return true;
    }
}
