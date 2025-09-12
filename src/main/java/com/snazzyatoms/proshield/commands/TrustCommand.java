package com.snazzyatoms.proshield.commands;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.roles.ClaimRole;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * /trust <player> [role]
 *
 * - Allows claim owners/co-owners (or roles with ManageTrust) to trust players.
 * - Assigns the requested ClaimRole via ClaimRoleManager.assignRole().
 * - Saves plots after modification.
 */
public class TrustCommand implements CommandExecutor {

    private final ProShield plugin;
    private final PlotManager plots;
    private final ClaimRoleManager roles;
    private final MessagesUtil messages;

    public TrustCommand(ProShield plugin, PlotManager plots, ClaimRoleManager roles) {
        this.plugin = plugin;
        this.plots = plots;
        this.roles = roles;
        this.messages = plugin.getMessagesUtil();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            messages.send(sender, "error.players-only");
            return true;
        }

        Plot plot = plots.getPlot(player.getLocation());
        if (plot == null) {
            messages.send(player, "error.not-in-claim");
            return true;
        }

        // Must be owner or have ManageTrust permissions
        UUID playerId = player.getUniqueId();
        if (!plot.isOwner(playerId)) {
            if (!roles.canManageTrust(plot.getId(), playerId)) {
                messages.send(player, "error.cannot-modify-claim");
                return true;
            }
        }

        if (args.length < 1) {
            messages.send(player, "usage.trust", "/" + label + " <player> [role]");
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        if (target == null || target.getUniqueId() == null) {
            messages.send(player, "error.player-not-found", args[0]);
            return true;
        }

        UUID targetId = target.getUniqueId();

        if (targetId.equals(playerId)) {
            messages.send(player, "trust.cannot-self");
            return true;
        }

        // Parse role
        String roleName = (args.length >= 2) ? args[1].toLowerCase() : "trusted";
        ClaimRole role = ClaimRole.fromString(roleName);
        if (role == null) role = ClaimRole.TRUSTED;

        // Already trusted?
        if (plot.getTrusted().containsKey(targetId)) {
            messages.send(player, "trust.already-trusted", target.getName());
            return true;
        }

        // Assign via RoleManager
        roles.assignRole(plot.getId(), targetId, role.name().toLowerCase());
        plot.getTrusted().put(targetId, role);
        plots.saveAsync(plot);

        // Feedback
        messages.send(player, "trust.added", target.getName(), role.name());
        if (target.isOnline() && target.getPlayer() != null) {
            messages.send(target.getPlayer(), "trust.you-were-trusted",
                    plot.getDisplayNameSafe(), role.name());
        }

        return true;
    }
}
