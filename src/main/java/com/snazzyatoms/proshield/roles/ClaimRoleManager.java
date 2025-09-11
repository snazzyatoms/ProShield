// src/main/java/com/snazzyatoms/proshield/roles/ClaimRoleManager.java
package com.snazzyatoms.proshield.roles;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;

import java.util.UUID;

/**
 * ClaimRoleManager
 *
 * ✅ Preserves prior logic
 * ✅ Fixed to use plotManager.saveAsync() correctly
 * ✅ Handles trust/untrust with roles
 */
public class ClaimRoleManager {

    private final ProShield plugin;
    private final PlotManager plotManager;

    public ClaimRoleManager(ProShield plugin) {
        this.plugin = plugin;
        this.plotManager = plugin.getPlotManager();
    }

    /* -------------------------------------------------------
     * Trust Management
     * ------------------------------------------------------- */

    public void trustPlayer(Plot plot, UUID playerId, ClaimRole role) {
        plot.getRoles().put(playerId, role);
        plotManager.saveAsync(plot); // save just this plot
    }

    public void untrustPlayer(Plot plot, UUID playerId) {
        plot.getRoles().remove(playerId);
        plotManager.saveAsync(plot); // save just this plot
    }

    public ClaimRole getRole(Plot plot, UUID playerId) {
        return plot.getRoles().get(playerId);
    }

    /* -------------------------------------------------------
     * Reload Support
     * ------------------------------------------------------- */

    /** Reloads role settings across all plots (called on plugin reload). */
    public void reloadFromConfig() {
        // Currently roles are per-plot and already deserialized in Plot
        // Future: global role defaults could be reloaded here.
        plotManager.saveAsync(); // ✅ save all plots safely
    }
}
