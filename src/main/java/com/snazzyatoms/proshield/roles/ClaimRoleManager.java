// src/main/java/com/snazzyatoms/proshield/roles/ClaimRoleManager.java
package com.snazzyatoms.proshield.roles;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;

import java.util.UUID;

/**
 * ClaimRoleManager
 * - Centralized role handling (get/set/trust/untrust)
 * - Preserves prior logic
 * - Expanded with missing methods for listeners & commands
 */
public class ClaimRoleManager {

    private final ProShield plugin;
    private final PlotManager plots;

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

    /** Trust a player with a given role inside a plot. */
    public void trustPlayer(Plot plot, UUID playerId, ClaimRole role) {
        if (plot == null || playerId == null || role == null) return;
        plot.getTrusted().put(playerId, role);
        plots.saveAsync(plot);
    }

    /** Untrust a player from a plot. */
    public void untrustPlayer(Plot plot, UUID playerId) {
        if (plot == null || playerId == null) return;
        plot.getTrusted().remove(playerId);
        plots.saveAsync(plot);
    }

    /* ======================================================
     * PERMISSION HELPERS
     * ====================================================== */

    /** Can this role build inside a claim? */
    public boolean canBuild(ClaimRole role) {
        if (role == null) return false;
        return switch (role) {
            case BUILDER, COOWNER, OWNER -> true;
            default -> false;
        };
    }

    /** Can this role interact with blocks/entities? */
    public boolean canInteract(ClaimRole role) {
        if (role == null) return false;
        return switch (role) {
            case CONTAINER, BUILDER, COOWNER, OWNER -> true;
            default -> false;
        };
    }

    /** Can this role access containers (chests, barrels, furnaces)? */
    public boolean canAccessContainers(ClaimRole role) {
        if (role == null) return false;
        return switch (role) {
            case CONTAINER, BUILDER, COOWNER, OWNER -> true;
            default -> false;
        };
    }

    /** Can this role manage trust/roles? */
    public boolean canManageTrust(ClaimRole role) {
        if (role == null) return false;
        return switch (role) {
            case COOWNER, OWNER -> true;
            default -> false;
        };
    }

    /* ======================================================
     * RELOAD
     * ====================================================== */

    /** Hook for reload — currently no external config. */
    public void reloadFromConfig() {
        // Future role-based config options could be reloaded here.
    }
}
