// src/main/java/com/snazzyatoms/proshield/plots/PlotManager.java
package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.roles.ClaimRole;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * PlotManager
 * - Manages all plots in the world.
 * - Consolidates PlotSettings + PlotService into one unified manager.
 * - Provides creation, removal, role checks, persistence, and default flag handling.
 */
public class PlotManager {

    private final ProShield plugin;
    private final ClaimRoleManager roleManager;

    // Map of plotId -> Plot
    private final Map<UUID, Plot> plots = new ConcurrentHashMap<>();

    // Default flags loaded from config.yml
    private final Map<String, Boolean> defaultFlags = new HashMap<>();

    public PlotManager(ProShield plugin, ClaimRoleManager roleManager) {
        this.plugin = plugin;
        this.roleManager = roleManager;
        loadDefaultFlags();
    }

    /**
     * Load default claim flags from config.yml
     */
    private void loadDefaultFlags() {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("claims.default-flags");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                defaultFlags.put(key.toLowerCase(), section.getBoolean(key, false));
            }
        }
        plugin.getLogger().info("Loaded " + defaultFlags.size() + " default claim flags.");
    }

    public ProShield getPlugin() {
        return plugin;
    }

    public Collection<Plot> getAllPlots() {
        return Collections.unmodifiableCollection(plots.values());
    }

    public Plot getPlotById(UUID id) {
        return plots.get(id);
    }

    public Plot getPlot(Location location) {
        if (location == null) return null;
        Chunk chunk = location.getChunk();
        return getPlot(chunk);
    }

    public Plot getPlot(Chunk chunk) {
        for (Plot plot : plots.values()) {
            if (plot.getChunk().equals(chunk)) {
                return plot;
            }
        }
        return null;
    }

    /**
     * Alias for backwards compatibility.
     * Older code calls getPlotAt(...).
     */
    public Plot getPlotAt(Chunk chunk) {
        return getPlot(chunk);
    }

    /**
     * Get a safe display name for the claim at this location.
     * Returns "Wilderness" if unclaimed.
     */
    public String getClaimName(Location location) {
        Plot plot = getPlot(location);
        return (plot != null) ? plot.getDisplayNameSafe() : "Wilderness";
    }

    /**
     * Create a new plot with default flags.
     */
    public Plot createPlot(UUID owner, Chunk chunk) {
        Plot plot = new Plot(chunk, owner);

        // Apply default flags
        for (Map.Entry<String, Boolean> entry : defaultFlags.entrySet()) {
            plot.setFlag(entry.getKey(), entry.getValue());
        }

        plots.put(plot.getId(), plot);
        saveAsync(plot);
        return plot;
    }

    public void removePlot(Plot plot) {
        if (plot == null) return;
        plots.remove(plot.getId());
        // TODO: persist removal to storage
    }

    public void saveAsync(Plot plot) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            // TODO: save plot to disk/database
        });
    }

    public void saveAll() {
        for (Plot plot : plots.values()) {
            saveAsync(plot);
        }
    }

    /**
     * Checks if a player is trusted or the owner of a claim.
     */
    public boolean isTrustedOrOwner(UUID playerId, Location loc) {
        Plot plot = getPlot(loc);
        if (plot == null) return true; // not claimed
        if (plot.isOwner(playerId)) return true;

        ClaimRole role = plot.getRole(playerId);
        return role != null && role != ClaimRole.NONE && role != ClaimRole.VISITOR;
    }

    /**
     * Checks if a player can interact within a plot.
     */
    public boolean canInteract(UUID playerId, Location loc) {
        Plot plot = getPlot(loc);
        if (plot == null) return true;

        ClaimRole role = plot.getRole(playerId);
        return role != null && role.canInteract();
    }

    /**
     * Checks if a player can manage a plot.
     */
    public boolean canManage(UUID playerId, Location loc) {
        Plot plot = getPlot(loc);
        if (plot == null) return false;

        ClaimRole role = plot.getRole(playerId);
        return role != null && role.canManage();
    }

    /**
     * Get default flag value by key.
     */
    public boolean getDefaultFlag(String key) {
        return defaultFlags.getOrDefault(key.toLowerCase(), false);
    }
}
