package com.snazzyatoms.proshield.roles;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;

import java.util.Map;
import java.util.UUID;

/**
 * ClaimRoleManager - Handles trusted players and their roles per-plot.
 *
 * Preserves all previous trust/untrust logic.
 * Fixed enum usage (ClaimRole.OWNER, etc.) and typing issues (UUID -> ClaimRole).
 */
public class ClaimRoleManager {

    private final ProShield plugin;
    private final PlotManager plots;

    public ClaimRoleManager(ProShield plugin) {
        this.plugin = plugin;
        this.plots = plugin.getPlotManager();
    }

    /**
     * Trust a player in a plot with the given role.
     */
    public boolean trustPlayer(Plot plot, UUID playerId, ClaimRole role) {
        if (plot == null || playerId == null || role == null) return false;

        // Prevent overriding owner role
        if (role == ClaimRole.OWNER) return false;

        Map<UUID, ClaimRole> trusted = plot.getTrusted();
        trusted.put(playerId, role);
        plot.setDirty(true);
        plots.saveAsync(plot);
        return true;
    }

    /**
     * Untrust a player in a plot.
     */
    public boolean untrustPlayer(Plot plot, UUID playerId) {
        if (plot == null || playerId == null) return false;
        if (!plot.getTrusted().containsKey(playerId)) return false;

        plot.getTrusted().remove(playerId);
        plot.setDirty(true);
        plots.saveAsync(plot);
        return true;
    }

    /**
     * Get the role of a player in a plot (or null if not trusted).
     */
    public ClaimRole getRole(Plot plot, UUID playerId) {
        if (plot == null || playerId == null) return null;

        if (plot.getOwner() != null && plot.getOwner().equals(playerId)) {
            return ClaimRole.OWNER;
        }
        return plot.getTrusted().get(playerId);
    }

    /**
     * Check whether a player is owner or trusted in a plot.
     */
    public boolean isTrustedOrOwner(UUID playerId, Plot plot) {
        if (plot == null || playerId == null) return false;
        if (plot.getOwner() != null && plot.getOwner().equals(playerId)) return true;
        return plot.getTrusted().containsKey(playerId);
    }

    /**
     * Reload manager settings (currently placeholder, but consistent with plugin reload).
     */
    public void reloadFromConfig() {
        // Future: hook into config for role defaults if needed
    }
}
