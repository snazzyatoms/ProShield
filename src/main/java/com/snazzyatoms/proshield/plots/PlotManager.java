package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * PlotManager (v1.2.5)
 * - Handles claim persistence (save/load to disk).
 * - Claiming/unclaiming chunks, trust, and flags.
 * - Migration for granular fire flags (fire-spread, ignite-*).
 */
public class PlotManager {

    private final ProShield plugin;
    private final MessagesUtil messages;
    private final Map<String, Plot> plots = new ConcurrentHashMap<>();

    private final File dataFile;

    public PlotManager(ProShield plugin) {
        this.plugin = plugin;
        this.messages = plugin.getMessagesUtil();
        this.dataFile = new File(plugin.getDataFolder(), "claims.yml");
        loadAll();
    }

    /* =========================================================
     * Claim Management
     * ========================================================= */

    public boolean claimPlot(Player player) {
        Chunk chunk = player.getLocation().getChunk();
        String key = key(chunk);

        if (plots.containsKey(key)) {
            messages.send(player, "&cThis chunk is already claimed.");
            return false;
        }

        int radius = plugin.getConfig().getInt("claims.default-radius", 50);
        Plot plot = new Plot(chunk.getWorld().getName(), chunk.getX(), chunk.getZ(),
                player.getUniqueId(), radius);

        plots.put(key, plot);
        saveAll();
        messages.send(player, "&aYou successfully claimed this chunk.");
        return true;
    }

    public boolean unclaimPlot(Player player) {
        Chunk chunk = player.getLocation().getChunk();
        String key = key(chunk);

        Plot plot = plots.get(key);
        if (plot == null) {
            messages.send(player, "&cNo claim here.");
            return false;
        }

        if (!plot.getOwner().equals(player.getUniqueId())
                && !player.hasPermission("proshield.admin")) {
            messages.send(player, "&cYou do not own this claim.");
            return false;
        }

        plots.remove(key);
        saveAll();
        messages.send(player, "&cYou unclaimed this chunk.");
        return true;
    }

    public Plot getPlot(Location loc) {
        if (loc == null) return null;
        String key = key(loc.getChunk());
        return plots.get(key);
    }

    public Collection<Plot> getAllPlots() {
        return plots.values();
    }

    /* =========================================================
     * Persistence
     * ========================================================= */

    public void saveAll() {
        FileConfiguration cfg = plugin.getYaml("claims", dataFile);

        cfg.set("claims", null);
        for (Plot plot : plots.values()) {
            String path = "claims." + plot.getWorld() + "," + plot.getX() + "," + plot.getZ();
            cfg.set(path + ".owner", plot.getOwner().toString());
            cfg.set(path + ".radius", plot.getRadius());
            cfg.set(path + ".trusted", plot.getTrusted());
            cfg.set(path + ".flags", plot.getFlags());
        }

        try {
            cfg.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save claims.yml: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void loadAll() {
        FileConfiguration cfg = plugin.getYaml("claims", dataFile);
        if (!cfg.isConfigurationSection("claims")) return;

        for (String key : cfg.getConfigurationSection("claims").getKeys(false)) {
            String[] parts = key.split(",");
            if (parts.length != 3) continue;

            String world = parts[0];
            int x = Integer.parseInt(parts[1]);
            int z = Integer.parseInt(parts[2]);

            UUID owner = UUID.fromString(cfg.getString("claims." + key + ".owner"));
            int radius = cfg.getInt("claims." + key + ".radius", plugin.getConfig().getInt("claims.default-radius", 50));

            Plot plot = new Plot(world, x, z, owner, radius);

            // Load trusted
            if (cfg.isConfigurationSection("claims." + key + ".trusted")) {
                plot.getTrusted().putAll((Map<String, String>)
                        cfg.getConfigurationSection("claims." + key + ".trusted").getValues(false));
            }

            // Load flags
            if (cfg.isConfigurationSection("claims." + key + ".flags")) {
                Map<String, Object> rawFlags = cfg.getConfigurationSection("claims." + key + ".flags").getValues(false);
                for (Map.Entry<String, Object> e : rawFlags.entrySet()) {
                    if (e.getValue() instanceof Boolean b) {
                        plot.getFlags().put(e.getKey(), b);
                    }
                }

                // 🔄 Migration: old "fire" flag -> new granular ones
                if (plot.getFlags().containsKey("fire")) {
                    boolean fireVal = plot.getFlags().get("fire");
                    plot.getFlags().putIfAbsent("fire-spread", fireVal);
                    plot.getFlags().putIfAbsent("ignite-flint", fireVal);
                    plot.getFlags().putIfAbsent("ignite-lava", fireVal);
                    plot.getFlags().putIfAbsent("ignite-lightning", fireVal);
                    // Optionally remove old fire key
                    plot.getFlags().remove("fire");
                }
            }

            plots.put(key, plot);
        }
        plugin.getLogger().info("Loaded " + plots.size() + " claims.");
    }

    private String key(Chunk chunk) {
        return chunk.getWorld().getName() + "," + chunk.getX() + "," + chunk.getZ();
    }
}
