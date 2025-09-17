package com.snazzyatoms.proshield.roles;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Locale;
import java.util.UUID;

/**
 * ClaimRoleManager
 * - Central authority for handling player roles in claims
 * - Synchronized with ClaimRole enum (v1.2.5)
 */
public class ClaimRoleManager {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final MessagesUtil messages;

    public ClaimRoleManager(ProShield plugin) {
        this.plugin = plugin;
        this.plotManager = plugin.getPlotManager();
        this.messages = plugin.getMessagesUtil();
    }

    public void loadAll() {
        // TODO: load roles from disk if you persist them separately
    }

    public void saveAll() {
        // TODO: persist roles if applicable
    }

    // ========================
    // Core Role Utilities
    // ========================

    /**
     * Get a player's role inside a plot.
     * Defaults to VISITOR if not trusted/owner.
     */
    public ClaimRole getRole(UUID playerId, Plot plot) {
        if (plot == null || playerId == null) return ClaimRole.NONE;
        if (plot.getOwner().equals(playerId)) return ClaimRole.OWNER;

        String raw = plot.getTrusted().get(playerId);
        return raw != null ? ClaimRole.fromName(raw) : ClaimRole.VISITOR;
    }

    /**
     * Assign a role to a target inside a claim.
     */
    public void setRole(Plot plot, UUID targetId, ClaimRole role) {
        if (plot == null || targetId == null || role == null) return;

        if (role == ClaimRole.NONE || role == ClaimRole.VISITOR) {
            plot.getTrusted().remove(targetId);
        } else {
            plot.getTrusted().put(targetId, role.name());
        }
        plotManager.saveAll();
    }

    /**
     * Convenience: check if a player can build in a plot.
     */
    public boolean canBuild(Player player, Plot plot) {
        ClaimRole role = getRole(player.getUniqueId(), plot);
        return role.canBuild();
    }

    public boolean canInteract(Player player, Plot plot) {
        ClaimRole role = getRole(player.getUniqueId(), plot);
        return role.canInteract();
    }

    public boolean canManage(Player player, Plot plot) {
        ClaimRole role = getRole(player.getUniqueId(), plot);
        return role.canManageRoles() || role.canModifyFlags() || role.canTransferClaim();
    }

    // ========================
    // Chat-based Role Assignment (legacy support)
    // ========================

    /**
     * Assign a role via chat input (fallback if GUI not used).
     */
    public void assignRoleViaChat(Player actor, UUID targetUuid, String rawRole) {
        if (actor == null || targetUuid == null || rawRole == null) return;

        Plot plot = plotManager.getPlotAt(actor.getLocation());
        if (plot == null) {
            messages.send(actor, "&cYou must stand inside your claim to manage roles.");
            return;
        }

        // Only owner or admin can assign roles
        if (!plot.getOwner().equals(actor.getUniqueId()) && !actor.hasPermission("proshield.admin")) {
            messages.send(actor, "&cOnly the owner (or admin) can assign roles in this claim.");
            return;
        }

        ClaimRole role = ClaimRole.fromName(rawRole);
        if (role == ClaimRole.NONE) role = ClaimRole.TRUSTED; // fallback

        setRole(plot, targetUuid, role);

        OfflinePlayer target = plugin.getServer().getOfflinePlayer(targetUuid);
        String targetName = (target != null && target.getName() != null)
                ? target.getName()
                : targetUuid.toString().substring(0, 8);

        messages.send(actor, "&aAssigned &f" + targetName + " &ato role &f" + role.getDisplayName() + " &ain this claim.");
    }
}
