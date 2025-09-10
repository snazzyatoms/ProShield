package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

/**
 * Manages all plots (claims) on the server.
 * Handles loading/saving from config.yml and runtime lookups.
 */
public class PlotManager {

    private final ProShield plugin;
    private final Map<String, Plot> plots = new HashMap<>(); // key = world:x:z

    public PlotManager(ProShield plugin) {
        this.plugin = plugin;
    }

    // === Core ===

    private String getKey(Chunk chunk) {
        return chunk.getWorld().getName() + ":" + chunk.getX() + ":" + chunk.getZ();
    }

    public Plot getPlot(Chunk chunk) {
        return plots.get(getKey(chunk));
    }

    public boolean isClaimed(Chunk chunk) {
        return plots.containsKey(getKey(chunk));
    }

    public void addPlot(Chunk chunk, UUID owner) {
        String key = getKey(chunk);
        Plot plot = new Plot(chunk, owner);
        plots.put(key, plot);
        savePlot(key, plot);
    }

    public void removePlot(Chunk chunk) {
        String key = getKey(chunk);
        plots.remove(key);

        FileConfiguration config = plugin.getConfig();
        ConfigurationSection claimsSection = config.getConfigurationSection("claims");
        if (claimsSection != null) {
            claimsSection.set(key, null);
        }
        plugin.saveConfig();
    }

    public Collection<Plot> getAllPlots() {
        return Collections.unmodifiableCollection(plots.values());
    }

    // === Save/Load ===

    public void loadAll() {
        plots.clear();
        FileConfiguration config = plugin.getConfig();
        ConfigurationSection claimsSection = config.getConfigurationSection("claims");
        if (claimsSection == null) return;

        for (String key : claimsSection.getKeys(false)) {
            try {
                ConfigurationSection section = claimsSection.getConfigurationSection(key);
                if (section == null) continue;

                UUID owner = UUID.fromString(section.getString("owner"));
                String[] parts = key.split(":");
                if (parts.length != 3) continue;

                String world = parts[0];
                int x = Integer.parseInt(parts[1]);
                int z = Integer.parseInt(parts[2]);

                Chunk chunk = Bukkit.getWorld(world).getChunkAt(x, z);
                Plot plot = new Plot(chunk, owner);

                // === Existing settings ===
                if (section.isSet("settings.pvp")) {
                    plot.setPvpEnabled(section.getBoolean("settings.pvp"));
                }
                if (section.isSet("settings.explosions")) {
                    plot.setExplosionsEnabled(section.getBoolean("settings.explosions"));
                }
                if (section.isSet("settings.fire")) {
                    plot.setFireEnabled(section.getBoolean("settings.fire"));
                }

                // === NEW: Keep-items per-claim ===
                if (section.isSet("settings.keep-items")) {
                    plot.setKeepItemsEnabled(section.getBoolean("settings.keep-items"));
                } else {
                    plot.setKeepItemsEnabled(null); // fallback to global
                }

                plots.put(key, plot);

            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load claim at " + key + ": " + e.getMessage());
            }
        }
    }

    public void saveAll() {
        FileConfiguration config = plugin.getConfig();
        config.set("claims", null); // wipe and rebuild

        for (Map.Entry<String, Plot> entry : plots.entrySet()) {
            savePlot(entry.getKey(), entry.getValue());
        }
        plugin.saveConfig();
    }

    private void savePlot(String key, Plot plot) {
        FileConfiguration config = plugin.getConfig();
        ConfigurationSection claimsSection = config.getConfigurationSection("claims");
        if (claimsSection == null) {
            claimsSection = config.createSection("claims");
        }

        ConfigurationSection section = claimsSection.createSection(key);
        section.set("owner", plot.getOwner().toString());

        // === Existing settings ===
        section.set("settings.pvp", plot.isPvpEnabled());
        section.set("settings.explosions", plot.isExplosionsEnabled());
        section.set("settings.fire", plot.isFireEnabled());

        // === NEW: Keep-items per-claim ===
        if (plot.getKeepItemsEnabled() != null) {
            section.set("settings.keep-items", plot.getKeepItemsEnabled());
        }
    }
}
