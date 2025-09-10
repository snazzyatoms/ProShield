package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Chunk;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

/**
 * Handles creation, lookup, saving, and loading of plots.
 */
public class PlotManager {

    private final ProShield plugin;
    private final Map<String, Plot> plots = new HashMap<>();

    public PlotManager(ProShield plugin) {
        this.plugin = plugin;
    }

    // === Utility ===
    private String getKey(String world, int x, int z) {
        return world + ":" + x + "," + z;
    }

    private String getKey(Chunk chunk) {
        return getKey(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
    }

    // === CRUD ===
    public Plot getPlot(Chunk chunk) {
        return plots.get(getKey(chunk));
    }

    public Plot getPlot(String world, int x, int z) {
        return plots.get(getKey(world, x, z));
    }

    public Plot createPlot(UUID owner, Chunk chunk) {
        String key = getKey(chunk);
        Plot plot = new Plot(owner, chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
        plots.put(key, plot);
        return plot;
    }

    public void removePlot(Chunk chunk) {
        plots.remove(getKey(chunk));
    }

    public Collection<Plot> getAllPlots() {
        return plots.values();
    }

    public boolean isClaimed(Chunk chunk) {
        return plots.containsKey(getKey(chunk));
    }

    // === Persistence ===
    public void savePlots() {
        FileConfiguration config = plugin.getConfig();
        config.set("claims", null); // clear old section

        for (Plot plot : plots.values()) {
            String key = plot.getKey();
            String base = "claims." + key;

            config.set(base + ".owner", plot.getOwner().toString());

            // Trusted players
            List<String> trustedList = new ArrayList<>();
            for (UUID uuid : plot.getTrusted()) {
                trustedList.add(uuid.toString());
            }
            config.set(base + ".trusted", trustedList);

            // === Plot Settings ===
            PlotSettings s = plot.getSettings();
            config.set(base + ".settings.pvp", s.isPvpEnabled());
            config.set(base + ".settings.keep-drops", s.isKeepDropsEnabled());

            Map<String, Boolean> itemRules = s.getItemRules();
            for (Map.Entry<String, Boolean> entry : itemRules.entrySet()) {
                config.set(base + ".settings.item-rules." + entry.getKey(), entry.getValue());
            }
        }

        plugin.saveConfig();
    }

    public void loadPlots() {
        plots.clear();
        FileConfiguration config = plugin.getConfig();

        ConfigurationSection claims = config.getConfigurationSection("claims");
        if (claims == null) return;

        for (String key : claims.getKeys(false)) {
            ConfigurationSection section = claims.getConfigurationSection(key);
            if (section == null) continue;

            UUID owner = UUID.fromString(section.getString("owner", ""));
            String[] split = key.split(":");
            if (split.length != 2) continue;

            String world = split[0];
            String[] coords = split[1].split(",");
            if (coords.length != 2) continue;

            int x, z;
            try {
                x = Integer.parseInt(coords[0]);
                z = Integer.parseInt(coords[1]);
            } catch (NumberFormatException e) {
                continue;
            }

            Plot plot = new Plot(owner, world, x, z);

            // Trusted
            List<String> trustedList = section.getStringList("trusted");
            for (String uuidStr : trustedList) {
                try {
                    plot.addTrusted(UUID.fromString(uuidStr));
                } catch (IllegalArgumentException ignored) {}
            }

            // === Plot Settings ===
            ConfigurationSection settings = section.getConfigurationSection("settings");
            if (settings != null) {
                PlotSettings s = plot.getSettings();
                s.setPvpEnabled(settings.getBoolean("pvp", false));
                s.setKeepDropsEnabled(settings.getBoolean("keep-drops", false));

                ConfigurationSection itemRules = settings.getConfigurationSection("item-rules");
                if (itemRules != null) {
                    for (String ruleKey : itemRules.getKeys(false)) {
                        s.setItemRule(ruleKey, itemRules.getBoolean(ruleKey, false));
                    }
                }
            }

            plots.put(key, plot);
        }
    }

    // === Access ===
    public ProShield getPlugin() {
        return plugin;
    }
}
