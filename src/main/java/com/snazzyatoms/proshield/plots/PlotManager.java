package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * PlotManager (v1.2.5)
 * - Handles claim persistence (save/load to disk).
 * - Handles mob despawn + repel in claims.
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
        startMobTasks();
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
            plot.getTrusted().putAll((Map<String, String>) cfg.getConfigurationSection("claims." + key + ".trusted").getValues(false));
            plot.getFlags().putAll((Map<String, Boolean>) cfg.getConfigurationSection("claims." + key + ".flags").getValues(false));

            plots.put(key, plot);
        }
        plugin.getLogger().info("Loaded " + plots.size() + " claims.");
    }

    private String key(Chunk chunk) {
        return chunk.getWorld().getName() + "," + chunk.getX() + "," + chunk.getZ();
    }

    /* =========================================================
     * Mob Handling
     * ========================================================= */

    private void startMobTasks() {
        Bukkit.getScheduler().runTaskTimer(plugin, this::handleMobDespawn, 20L, 20L * 10); // every 10s
        Bukkit.getScheduler().runTaskTimer(plugin, this::handleMobRepel, 20L, 20L * 5);   // every 5s
    }

    private void handleMobDespawn() {
        if (!plugin.getConfig().getBoolean("protection.mobs.despawn-inside", true)) return;

        for (Plot plot : plots.values()) {
            World world = Bukkit.getWorld(plot.getWorld());
            if (world == null) continue;

            int cx = plot.getX();
            int cz = plot.getZ();
            Chunk chunk = world.getChunkAt(cx, cz);

            for (Entity e : chunk.getEntities()) {
                if (e instanceof Monster monster) {
                    monster.remove();
                }
            }
        }
    }

    private void handleMobRepel() {
        if (!plugin.getConfig().getBoolean("protection.mobs.border-repel.enabled", true)) return;

        double radius = plugin.getConfig().getDouble("protection.mobs.border-repel.radius", 15.0);
        double pushH = plugin.getConfig().getDouble("protection.mobs.border-repel.horizontal-push", 0.7);
        double pushV = plugin.getConfig().getDouble("protection.mobs.border-repel.vertical-push", 0.25);

        for (Player player : Bukkit.getOnlinePlayers()) {
            Plot plot = getPlot(player.getLocation());
            if (plot == null) continue;

            List<Entity> nearby = player.getNearbyEntities(radius, radius, radius);
            for (Entity e : nearby) {
                if (e instanceof Monster monster) {
                    Location pLoc = player.getLocation();
                    Location mLoc = monster.getLocation();

                    double dx = mLoc.getX() - pLoc.getX();
                    double dz = mLoc.getZ() - pLoc.getZ();
                    double dist = Math.sqrt(dx * dx + dz * dz);

                    if (dist < 0.1) dist = 0.1;

                    double vx = (dx / dist) * pushH;
                    double vz = (dz / dist) * pushH;

                    monster.setVelocity(monster.getVelocity().add(new org.bukkit.util.Vector(vx, pushV, vz)));
                }
            }
        }
    }
}
