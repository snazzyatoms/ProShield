// src/main/java/com/snazzyatoms/proshield/roles/ClaimRoleManager.java
package com.snazzyatoms.proshield.roles;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * ClaimRoleManager
 * - Central authority for handling claim roles and permissions
 * - Fully synchronized with ClaimRole (v1.2.5)
 * - Provides fine-grained helpers for GUI, listeners, and protection checks
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
        // TODO: load roles from disk if persisted separately
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
     * Assign or remove a role in a plot.
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

    // ========================
    // Permission Helpers
    // ========================
    public boolean canInteract(Player player, Plot plot) {
        return getRole(player.getUniqueId(), plot).canInteract();
    }

    public boolean canBuild(Player player, Plot plot) {
        return getRole(player.getUniqueId(), plot).canBuild();
    }

    public boolean canOpenContainers(Player player, Plot plot) {
        return getRole(player.getUniqueId(), plot).canOpenContainers();
    }

    public boolean canModifyFlags(Player player, Plot plot) {
        return getRole(player.getUniqueId(), plot).canModifyFlags();
    }

    public boolean canManageRoles(Player player, Plot plot) {
        return getRole(player.getUniqueId(), plot).canManageRoles();
    }

    public boolean canTransferClaim(Player player, Plot plot) {
        return getRole(player.getUniqueId(), plot).canTransferClaim();
    }

    // ========================
    // Utility
    // ========================

    /**
     * Compare two players' hierarchy inside a plot.
     */
    public boolean hasHigherOrEqualRole(Player actor, UUID target, Plot plot) {
        ClaimRole actorRole = getRole(actor.getUniqueId(), plot);
        ClaimRole targetRole = getRole(target, plot);
        return actorRole.isAtLeast(targetRole);
    }

    /**
     * Assign a role via chat input (legacy support).
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
