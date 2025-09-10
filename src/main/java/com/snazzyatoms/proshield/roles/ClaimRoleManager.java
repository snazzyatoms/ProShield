package com.snazzyatoms.proshield.roles;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;

import java.util.Map;
import java.util.UUID;

/**
 * ClaimRoleManager - Trust map ops and role checks.
 * Preserves earlier logic and adds helper methods referenced by listeners.
 */
public class ClaimRoleManager {

    private final ProShield plugin;
    private final PlotManager plots;

    public ClaimRoleManager(ProShield plugin) {
        this.plugin = plugin;
        this.plots = plugin.getPlotManager();
    }

    /* -------------------------
     * Trust map operations
     * ------------------------- */
    public boolean trustPlayer(Plot plot, UUID playerId, ClaimRole role) {
        if (plot == null || playerId == null || role == null) return false;
        if (role == ClaimRole.OWNER) return false; // don't grant OWNER via trust

        // mutate internal trusted
        // Obtain modifiable map via internal bridge (or use plot.putTrusted)
        plot.putTrusted(playerId, role);
        plots.saveAsync(plot);
        return true;
    }

    public boolean untrustPlayer(Plot plot, UUID playerId) {
        if (plot == null || playerId == null) return false;
        if (!plot.hasTrusted(playerId)) return false;

        plot.removeTrusted(playerId);
        plots.saveAsync(plot);
        return true;
    }

    public ClaimRole getRole(Plot plot, UUID playerId) {
        if (plot == null || playerId == null) return null;
        if (plot.getOwner() != null && plot.getOwner().equals(playerId)) {
            return ClaimRole.OWNER;
        }
        Map<UUID, ClaimRole> map = plot.getTrusted();
        return map.get(playerId);
    }

    public boolean isTrustedOrOwner(UUID playerId, Plot plot) {
        if (plot == null || playerId == null) return false;
        if (plot.getOwner() != null && plot.getOwner().equals(playerId)) return true;
        return plot.getTrusted().containsKey(playerId);
    }

    /* -------------------------
     * Capability helpers (used by listeners)
     * ------------------------- */
    public boolean canBuild(ClaimRole role) {
        return role != null && role.atLeast(ClaimRole.BUILDER);
    }

    public boolean canInteract(ClaimRole role) {
        return role != null && role.atLeast(ClaimRole.CONTAINER);
    }

    public boolean isOwnerOrCoOwner(ClaimRole role) {
        return role == ClaimRole.OWNER || role == ClaimRole.COOWNER || role == ClaimRole.CO_OWNER;
    }

    /* -------------------------
     * Reload hook
     * ------------------------- */
    public void reloadFromConfig() {
        // no-op for now; placeholder kept for API symmetry
    }
}
