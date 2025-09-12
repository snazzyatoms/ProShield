package com.snazzyatoms.proshield.commands;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * /transfer <player>
 *
 * - Allows claim owners (or admins) to transfer ownership of a claim.
 * - Cleans up old roles for both old/new owners.
 * - Persists change via PlotManager and ClaimRoleManager.
 */
public class TransferCommand implements CommandExecutor {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final ClaimRoleManager roles;
    private final MessagesUtil messages;

    public TransferCommand(ProShield plugin, PlotManager plotManager, ClaimRoleManager roles) {
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

        if (!player.hasPermission("proshield.use")) {
            messages.send(player, "error.no-permission");
            return true;
        }

        if (args.length < 1) {
            messages.send(player, "usage.transfer", "/" + label + " <player>");
            return true;
        }

        // Resolve target
        OfflinePlayer targetOP = Bukkit.getOfflinePlayer(args[0]);
        if (targetOP == null || targetOP.getUniqueId() == null) {
            messages.send(player, "error.player-not-found", args[0]);
            return true;
        }

        UUID targetId = targetOP.getUniqueId();

        // Must be in a claim
        Plot plot = plotManager.getPlot(player.getLocation());
        if (plot == null) {
            messages.send(player, "error.not-in-claim");
            return true;
        }

        UUID playerId = player.getUniqueId();
        // Only owner or admin can transfer
        if (!plot.isOwner(playerId) && !player.hasPermission("proshield.admin")) {
            messages.send(player, "error.cannot-transfer");
            return true;
        }

        // Prevent transferring to self
        if (plot.isOwner(targetId)) {
            messages.send(player, "transfer.already-owner");
            return true;
        }

        // Clear roles for new owner (in case they were trusted before)
        roles.clearRole(plot.getId(), targetId);

        // Clear roles for old owner (ensure no dangling permissions)
        roles.clearRole(plot.getId(), plot.getOwner());

        // Transfer ownership
        plot.setOwner(targetId);
        plotManager.saveAsync(plot);

        // Feedback
        String targetName = targetOP.getName() != null ? targetOP.getName() : targetId.toString();
        messages.send(player, "transfer.success", targetName);

        // Notify target if online
        if (targetOP.isOnline() && targetOP.getPlayer() != null) {
            messages.send(targetOP.getPlayer(), "transfer.you-are-owner", plot.getDisplayNameSafe());
        }

        plugin.getLogger().info("[ProShield] Claim at " + plot.getWorldName() + " (" + plot.getX() + "," + plot.getZ() +
                ") transferred from " + player.getName() + " to " + targetName);

        return true;
    }
}
