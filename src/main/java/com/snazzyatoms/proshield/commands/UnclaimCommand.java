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
 * Handles the /unclaim command.
 * - Allows claim owners (or roles with permission) to remove claims.
 * - Uses messages.yml keys consistently.
 */
public class UnclaimCommand implements CommandExecutor {

    private final PlotManager plotManager;
    private final ClaimRoleManager roleManager;
    private final MessagesUtil messages;

    public UnclaimCommand(ProShield plugin, PlotManager plotManager, ClaimRoleManager roleManager, MessagesUtil messages) {
        this.plotManager = plotManager;
        this.roleManager = roleManager;
        this.messages = messages;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(MessagesUtil.PREFIX + "Only players can unclaim land.");
            return true;
        }

        UUID playerId = player.getUniqueId();
        Chunk chunk = player.getLocation().getChunk();
        Plot plot = plotManager.getPlot(chunk);

        if (plot == null) {
            messages.send(player, "error.no-claim");
            return true;
        }

        if (!plot.isOwner(playerId) && !roleManager.canUnclaim(playerId, plot.getId())) {
            messages.send(player, "claim.not-owner");
            return true;
        }

        plotManager.removePlot(plot);
        messages.send(player, "claim.unclaimed");
        return true;
    }
}
