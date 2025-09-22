package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.roles.ClaimRole;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * PlotManager
 * - Handles persistent storage of plots (plots.yml)
 * - Applies default flags from config.yml when missing
 * - Ensures new plots are safezones with PvP disabled (v1.2.6 default)
 * - Migrates old plots to enforce safezone/pvp defaults
 * - Provides CRUD methods for plots and trusted roles
 */
public class PlotManager {

    private final ProShield plugin;
    private final Map<UUID, Plot> plots = new ConcurrentHashMap<>();
    private final File plotsFile;
    private FileConfiguration plotsConfig;

    public PlotManager(ProShield plugin) {
        this.plugin = plugin;
        this.plotsFile = new File(plugin.getDataFolder(), "plots.yml");
        load();
        migrateDefaults(); // ✅ enforce safezone/pvp for old plots
    }

    /* -------------------------
     * LOAD / SAVE
     * ------------------------- */
    public void load() {
        if (!plotsFile.exists()) {
            try {
                plotsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to create plots.yml: " + e.getMessage());
            }
        }
        this.plotsConfig = YamlConfiguration.loadConfiguration(plotsFile);
        plots.clear();

        if (!plotsConfig.contains("plots")) {
            plugin.getLogger().info("No plots found to load.");
            return;
        }

        for (String key : plotsConfig.getConfigurationSection("plots").getKeys(false)) {
            try {
                UUID id = UUID.fromString(key);
                String world = plotsConfig.getString("plots." + key + ".world");
                String ownerStr = plotsConfig.getString("plots." + key + ".owner");

                if (world == null || ownerStr == null) {
                    plugin.getLogger().warning("Skipping invalid plot entry: " + key);
                    continue;
                }

                int x = plotsConfig.getInt("plots." + key + ".x");
                int z = plotsConfig.getInt("plots." + key + ".z");
                UUID owner = UUID.fromString(ownerStr);
                int radius = plotsConfig.getInt("plots." + key + ".radius",
                        plugin.getConfig().getInt("claims.default-radius", 50));

                Plot plot = new Plot(owner, world, x, z, id, radius);

                // --- Flags ---
                Map<String, Object> flags = plotsConfig.getConfigurationSection("plots." + key + ".flags") != null
                        ? plotsConfig.getConfigurationSection("plots." + key + ".flags").getValues(false)
                        : Collections.emptyMap();

                for (String flag : plugin.getConfig().getConfigurationSection("flags.available").getKeys(false)) {
                    boolean def = plugin.getConfig().getBoolean("flags.available." + flag + ".default", false);
                    boolean value = flags.containsKey(flag)
                            ? Boolean.parseBoolean(String.valueOf(flags.get(flag)))
                            : def;
                    plot.setFlag(flag, value);
                }

                // --- Trusted players ---
                String base = "plots." + key + ".trusted";
                String defRole = plugin.getConfig().getString("roles.default", "member");

                if (plotsConfig.isList(base)) {
                    for (String s : plotsConfig.getStringList(base)) {
                        try {
                            plot.getTrusted().put(UUID.fromString(s), ClaimRole.fromName(defRole));
                        } catch (Exception ignored) {}
                    }
                } else if (plotsConfig.isConfigurationSection(base)) {
                    for (String u : plotsConfig.getConfigurationSection(base).getKeys(false)) {
                        String roleName = plotsConfig.getString(base + "." + u, defRole);
                        ClaimRole role = ClaimRole.fromName(roleName);
                        try {
                            plot.getTrusted().put(UUID.fromString(u), role);
                        } catch (Exception ignored) {}
                    }
                }

                plots.put(id, plot);

            } catch (Exception ex) {
                plugin.getLogger().warning("Failed to load plot " + key + ": " + ex.getMessage());
            }
        }
    }

    public void saveAll() {
        plotsConfig.set("plots", null);
        for (Map.Entry<UUID, Plot> entry : plots.entrySet()) {
            Plot plot = entry.getValue();
            String base = "plots." + entry.getKey();

            plotsConfig.set(base + ".world", plot.getWorld());
            plotsConfig.set(base + ".x", plot.getX());
            plotsConfig.set(base + ".z", plot.getZ());
            plotsConfig.set(base + ".owner", plot.getOwner().toString());
            plotsConfig.set(base + ".radius", plot.getRadius());
            plotsConfig.set(base + ".createdAt", plot.getCreatedAt().toString());

            for (Map.Entry<String, Boolean> flag : plot.getFlags().entrySet()) {
                plotsConfig.set(base + ".flags." + flag.getKey(), flag.getValue());
            }

            for (Map.Entry<UUID, ClaimRole> t : plot.getTrusted().entrySet()) {
                plotsConfig.set(base + ".trusted." + t.getKey(), t.getValue().name());
            }
        }

        try {
            plotsConfig.save(plotsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save plots.yml: " + e.getMessage());
        }
    }

    /* -------------------------
     * CRUD
     * ------------------------- */
    public Plot getPlot(UUID id) {
        return plots.get(id);
    }

    public Plot getPlotAt(Location loc) {
        if (loc == null) return null;
        for (Plot plot : plots.values()) {
            if (plot.isInPlot(loc)) return plot;
        }
        return null;
    }

    public Collection<Plot> getPlots() {
        return plots.values();
    }

    /** GUIManager expects this */
    public Collection<Plot> getAllPlots() {
        return plots.values();
    }

    public Plot createPlot(UUID owner, Location center) {
        UUID id = UUID.randomUUID();
        String world = center.getWorld().getName();
        int x = center.getBlockX();
        int z = center.getBlockZ();
        int radius = plugin.getConfig().getInt("claims.default-radius", 50);

        Plot plot = new Plot(owner, world, x, z, id, radius);

        // ✅ Apply defaults from config
        for (String flag : plugin.getConfig().getConfigurationSection("flags.available").getKeys(false)) {
            boolean def = plugin.getConfig().getBoolean("flags.available." + flag + ".default", false);
            plot.setFlag(flag, def);
        }

        // ✅ Override safezone/pvp regardless of config
        plot.setFlag("safezone", true);
        plot.setFlag("pvp", false);

        plots.put(id, plot);
        saveAll();

        if (plugin.isDebugEnabled()) {
            plugin.getLogger().info("[PlotManager] Created new plot for " + owner + " at "
                    + x + "," + z + " (radius=" + radius + ", id=" + id + ", safezone=true, pvp=false)");
        }

        return plot;
    }

    public void deletePlot(UUID id) {
        plots.remove(id);
        plotsConfig.set("plots." + id, null);
        saveAll();

        if (plugin.isDebugEnabled()) {
            plugin.getLogger().info("[PlotManager] Deleted plot " + id);
        }
    }

    public void expandPlot(UUID id, int extraRadius) {
        Plot plot = plots.get(id);
        if (plot != null) {
            plot.setRadius(plot.getRadius() + extraRadius);
            saveAll();

            if (plugin.isDebugEnabled()) {
                plugin.getLogger().info("[PlotManager] Expanded plot " + id
                        + " by +" + extraRadius + " (new radius=" + plot.getRadius() + ")");
            }
        }
    }

    public Plot getPlotByOwner(UUID owner) {
        for (Plot plot : plots.values()) {
            if (plot.getOwner().equals(owner)) {
                return plot;
            }
        }
        return null;
    }

    public boolean isInClaim(Location loc, UUID owner) {
        Plot plot = getPlotAt(loc);
        return plot != null && plot.getOwner().equals(owner);
    }

    public World getWorld(String name) {
        return Bukkit.getWorld(name);
    }

    /* -------------------------
     * COMPATIBILITY SHIMS (for GUIManager 1.2.6)
     * ------------------------- */

    public Plot claimChunk(UUID owner, Location where) {
        if (where == null) return null;
        return createPlot(owner, where);
    }

    public boolean unclaim(Plot plot) {
        if (plot == null) return false;
        deletePlot(plot.getId());
        return true;
    }

    public void save(Plot plot) {
        saveAll();
    }

    public Plot findNearestClaim(Location origin, int maxRadius) {
        if (origin == null) return null;
        Plot nearest = null;
        double nearestDist = Double.MAX_VALUE;

        for (Plot plot : plots.values()) {
            if (!plot.getWorld().equalsIgnoreCase(origin.getWorld().getName())) continue;
            Location center = plot.getCenter();
            if (center == null) continue;

            double dist = center.distance(origin);
            if (dist < nearestDist && dist <= maxRadius) {
                nearest = plot;
                nearestDist = dist;
            }
        }
        return nearest;
    }

    /* -------------------------
     * Migration
     * ------------------------- */
    private void migrateDefaults() {
        int changed = 0;
        for (Plot plot : plots.values()) {
            boolean updated = false;

            if (!plot.getFlags().containsKey("safezone")) {
                plot.setFlag("safezone", true);
                updated = true;
            }
            if (!plot.getFlags().containsKey("pvp")) {
                plot.setFlag("pvp", false);
                updated = true;
            }

            if (updated) {
                save(plot);
                changed++;
            }
        }

        if (changed > 0) {
            plugin.getLogger().info("[PlotManager] Migrated " + changed +
                    " plots → safezone=true, pvp=false.");
        }
    }
}
