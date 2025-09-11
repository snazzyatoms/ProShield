package com.snazzyatoms.proshield.roles;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;

import java.util.UUID;

/**
 * ClaimRoleManager
 *
 * ✅ Preserves prior logic
 * ✅ Expanded to use PlotManager.saveAsync() correctly
 * ✅ Supports saving per-plot or all plots
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

    public void addTrusted(Plot plot, UUID player, String role) {
        if (plot == null || player == null) return;

        plot.getTrusted().put(player, role);
        plotManager.saveAsync(plot); // preserve prior per-plot logic
    }

    public void removeTrusted(Plot plot, UUID player) {
        if (plot == null || player == null) return;

        plot.getTrusted().remove(player);
        plotManager.saveAsync(plot); // preserve prior per-plot logic
    }

    /* -------------------------------------------------------
     * Reload Support
     * ------------------------------------------------------- */

    /**
     * Reloads role-related settings from config and ensures plots are persisted.
     * This now calls PlotManager.saveAsync() (all plots) instead of failing.
     */
    public void reloadFromConfig() {
        // TODO: extend with role limits / default role loading if needed
        plotManager.saveAsync(); // save ALL plots async, new safe method
    }
}
