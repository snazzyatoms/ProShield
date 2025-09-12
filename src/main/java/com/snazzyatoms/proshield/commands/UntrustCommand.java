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
 * /untrust <player>
 *
 * - Allows claim owners/co-owners (or roles with ManageTrust) to remove trusted players.
 * - Delegates cleanup to ClaimRoleManager.clearRole().
 * - Persists changes via PlotManager.
 */
public class UntrustCommand implements CommandExecutor {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final ClaimRoleManager roles;
    private final MessagesUtil messages;

    public UntrustCommand(ProShield plugin, PlotManager plotManager, ClaimRoleManager roles) {
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
            messages.send(player, "usage.untrust", "/" + label + " <player>");
            return true;
        }

        // Resolve target
        OfflinePlayer targetOP = Bukkit.getOfflinePlayer(args[0]);
        if (targetOP == null || targetOP.getUniqueId() == null) {
            messages.send(player, "error.player-not-found", args[0]);
            return true;
        }

        UUID targetId = targetOP.getUniqueId();

        // Must be inside a claim
        Plot plot = plotManager.getPlot(player.getLocation());
        if (plot == null) {
            messages.send(player, "error.not-in-claim");
            return true;
        }

        UUID playerId = player.getUniqueId();
        // Only owner or ManageTrust role may untrust
        if (!plot.isOwner(playerId)) {
            if (!roles.canManageTrust(plot.getId(), playerId)) {
                messages.send(player, "error.cannot-modify-claim");
                return true;
            }
        }

        // Ensure target is trusted
        if (!plot.getTrusted().containsKey(targetId)) {
            String targetName = targetOP.getName() != null ? targetOP.getName() : targetId.toString();
            messages.send(player, "untrust.not-trusted", targetName);
            return true;
        }

        // Remove trust via ClaimRoleManager
        roles.clearRole(plot.getId(), targetId);
        plot.getTrusted().remove(targetId);
        plotManager.saveAsync(plot);

        // Feedback
        String targetName = targetOP.getName() != null ? targetOP.getName() : targetId.toString();
        messages.send(player, "untrust.removed", targetName);

        // Notify target if online
        if (targetOP.isOnline() && targetOP.getPlayer() != null) {
            messages.send(targetOP.getPlayer(), "untrust.you-were-untrusted", plot.getDisplayNameSafe());
        }

        return true;
    }
}
