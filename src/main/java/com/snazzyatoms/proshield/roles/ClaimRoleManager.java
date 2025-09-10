package com.snazzyatoms.proshield.roles;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;

import java.util.Map;
import java.util.UUID;

/**
 * ClaimRoleManager - Handles trusted players and their roles per-plot.
 *
 * Preserves all prior trust/untrust logic and extends
 * with helper methods (canBuild, canInteract, isOwnerOrCoOwner).
 */
public class ClaimRoleManager {

    private final ProShield plugin;
    private final PlotManager plots;

    public ClaimRoleManager(ProShield plugin) {
        this.plugin = plugin;
        this.plots = plugin.getPlotManager();
    }

    /* -------------------------------------------------------
     * Trust / Untrust
     * ------------------------------------------------------- */

    public boolean trustPlayer(Plot plot, UUID playerId, ClaimRole role) {
        if (plot == null || playerId == null || role == null) return false;

        // Prevent overriding owner role
        if (role == ClaimRole.OWNER) return false;

        Map<UUID, ClaimRole> trusted = plot.getTrusted();
        trusted.put(playerId, role);
        plots.saveAsync();
        return true;
    }

    public boolean untrustPlayer(Plot plot, UUID playerId) {
        if (plot == null || playerId == null) return false;
        if (!plot.getTrusted().containsKey(playerId)) return false;

        plot.getTrusted().remove(playerId);
        plots.saveAsync();
        return true;
    }

    /* -------------------------------------------------------
     * Role Lookups
     * ------------------------------------------------------- */

    public ClaimRole getRole(Plot plot, UUID playerId) {
        if (plot == null || playerId == null) return null;

        if (plot.getOwner() != null && plot.getOwner().equals(playerId)) {
            return ClaimRole.OWNER;
        }
        return plot.getTrusted().get(playerId);
    }

    public boolean isTrustedOrOwner(UUID playerId, Plot plot) {
        if (plot == null || playerId == null) return false;
        if (plot.getOwner() != null && plot.getOwner().equals(playerId)) return true;
        return plot.getTrusted().containsKey(playerId);
    }

    /* -------------------------------------------------------
     * Role Permission Helpers
     * ------------------------------------------------------- */

    /** Can this role build (break/place blocks)? */
    public boolean canBuild(ClaimRole role) {
        if (role == null) return false;
        return role.atLeast(ClaimRole.BUILDER);
    }

    /** Can this role interact with entities/blocks? */
    public boolean canInteract(ClaimRole role) {
        if (role == null) return false;
        return role.atLeast(ClaimRole.CONTAINER);
    }

    /** Is this role the owner or co-owner of the claim? */
    public boolean isOwnerOrCoOwner(ClaimRole role) {
        if (role == null) return false;
        return role == ClaimRole.OWNER || role == ClaimRole.COOWNER || role == ClaimRole.CO_OWNER;
    }

    /* -------------------------------------------------------
     * Reload
     * ------------------------------------------------------- */

    public void reloadFromConfig() {
        // Future: load role defaults/permissions from config
    }
}
