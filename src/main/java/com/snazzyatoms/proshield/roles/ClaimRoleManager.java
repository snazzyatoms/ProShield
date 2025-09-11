package com.snazzyatoms.proshield.roles;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * ClaimRoleManager
 * - Centralized role handling (get/set/trust/untrust)
 * - Extended with missing methods for GUI listeners & commands
 */
public class ClaimRoleManager {

    private final ProShield plugin;
    private final PlotManager plots;

    // Temporary cache for GUI → target selection
    private final Map<UUID, UUID> pendingTargets = new HashMap<>();

    public ClaimRoleManager(ProShield plugin) {
        this.plugin = plugin;
        this.plots = plugin.getPlotManager();
    }

    /* ======================================================
     * CORE GETTERS
     * ====================================================== */

    /** Get the role of a player inside a plot (defaults to VISITOR). */
    public ClaimRole getRole(Plot plot, UUID playerId) {
        if (plot == null || playerId == null) return ClaimRole.VISITOR;
        if (plot.getOwner() != null && plot.getOwner().equals(playerId)) return ClaimRole.OWNER;
        return plot.getTrusted().getOrDefault(playerId, ClaimRole.VISITOR);
    }

    /** Assign a role directly (wrapper for trust/untrust). */
    public void setRole(Plot plot, UUID playerId, ClaimRole role) {
        if (plot == null || playerId == null || role == null) return;

        if (role == ClaimRole.VISITOR) {
            // Visitor = untrusted
            untrustPlayer(plot, playerId);
        } else {
            plot.getTrusted().put(playerId, role);
            plots.saveAsync(plot);
        }
    }

    /** Remove any role (back to VISITOR). */
    public void removeRole(Plot plot, UUID playerId) {
        if (plot == null || playerId == null) return;
        plot.getTrusted().remove(playerId);
        plots.saveAsync(plot);
    }

    /** Trust a player with a given role inside a plot. */
    public void trustPlayer(Plot plot, UUID playerId, ClaimRole role) {
        setRole(plot, playerId, role);
    }

    /** Untrust a player from a plot. */
    public void untrustPlayer(Plot plot, UUID playerId) {
        removeRole(plot, playerId);
    }

    /* ======================================================
     * GUI TARGET HELPERS
     * ====================================================== */

    /** Store which target player this executor is modifying. */
    public void setPendingTarget(Player executor, UUID target) {
        pendingTargets.put(executor.getUniqueId(), target);
    }

    /** Retrieve the pending target for this executor. */
    public UUID getPendingTarget(Player executor) {
        return pendingTargets.get(executor.getUniqueId());
    }

    /** Clear a pending target after use. */
    public void clearPendingTarget(Player executor) {
        pendingTargets.remove(executor.getUniqueId());
    }

    /* ======================================================
     * PERMISSION HELPERS
     * ====================================================== */

    /** Can this role build inside a claim? */
    public boolean canBuild(ClaimRole role) {
        if (role == null) return false;
        return switch (role) {
            case BUILDER, OWNER -> true;
            default -> false;
        };
    }

    /** Can this role interact with blocks/entities? */
    public boolean canInteract(ClaimRole role) {
        if (role == null) return false;
        return switch (role) {
            case CONTAINER, BUILDER, OWNER -> true;
            default -> false;
        };
    }

    /** Can this role access containers (chests, barrels, furnaces)? */
    public boolean canAccessContainers(ClaimRole role) {
        if (role == null) return false;
        return switch (role) {
            case CONTAINER, BUILDER, OWNER -> true;
            default -> false;
        };
    }

    /** Can this role manage trust/roles? */
    public boolean canManageTrust(ClaimRole role) {
        if (role == null) return false;
        return role == ClaimRole.OWNER; // only Owner for v1.2.5
    }

    /** Is this role an Owner (used in RolesCommand). */
    public boolean isOwnerOrCoOwner(ClaimRole role) {
        return role == ClaimRole.OWNER; // no Co-Owner in v1.2.5
    }

    /* ======================================================
     * RELOAD
     * ====================================================== */

    /** Hook for reload — currently no external config. */
    public void reloadFromConfig() {
        // Future role-based config options could be reloaded here.
    }
}
