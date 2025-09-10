package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Tameable;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Periodically repels hostile mobs attempting to step inside claimed chunks.
 * - Uses a border radius near chunk edges to detect intrusion
 * - Applies a configurable push vector
 * - Optional: can integrate despawn logic per PlotSettings (future-proofed)
 */
public class EntityMobRepelTask extends BukkitRunnable {

    private final ProShield plugin;
    private final PlotManager plots;

    // Cooldown so we don’t spam velocity every tick for the same entity
    private final Map<UUID, Long> lastPush = new HashMap<>();

    // Config snapshot (reloaded with onConfigReload)
    private boolean enabled;
    private double borderRadius;
    private double pushH; // horizontal
    private double pushV; // vertical

    public EntityMobRepelTask(ProShield plugin, PlotManager plots) {
        this.plugin = plugin;
        this.plots = plots;
        reloadSettings();
    }

    /** Reloads all repel settings from config.yml */
    public void reloadSettings() {
        FileConfiguration cfg = plugin.getConfig();
        this.enabled = cfg.getBoolean("protection.mobs.border-repel.enabled", true);
        this.borderRadius = cfg.getDouble("protection.mobs.border-repel.radius", 1.5D);
        this.pushH = cfg.getDouble("protection.mobs.border-repel.horizontal-push", 0.6D);
        this.pushV = cfg.getDouble("protection.mobs.border-repel.vertical-push", 0.15D);
    }

    @Override
    public void run() {
        if (!enabled) return;

        for (World world : Bukkit.getWorlds()) {
            for (Entity e : world.getEntities()) {
                // Only repel hostile mobs; skip players, animals, items, etc.
                if (!(e instanceof Monster)) continue;

                // If tamed hostile variants ever exist (unlikely), skip them
                if (e instanceof Tameable t && t.isTamed()) continue;

                Location loc = e.getLocation();
                Plot plot = plots.getPlot(loc.getChunk());
                if (plot == null) continue; // only act inside claims

                // If the claim disables mob repel, respect settings
                if (!plot.getSettings().isMobRepelEnabled()) continue;

                // Only act near the *chunk border* to keep cost/feel reasonable.
                if (!isNearChunkBorder(loc, borderRadius)) continue;

                // Light cooldown (every 10 ticks) per entity
                long now = System.currentTimeMillis();
                Long last = lastPush.get(e.getUniqueId());
                if (last != null && (now - last) < 500) continue; // ~10 ticks @ 20 TPS
                lastPush.put(e.getUniqueId(), now);

                // Push the mob outward—away from the nearest border toward the outside of the chunk.
                Vector push = outwardNormalFromNearestEdge(loc).multiply(pushH);
                push.setY(pushV);
                e.setVelocity(e.getVelocity().add(push));
            }
        }
    }

    /** True if the location is within radius of any edge of its chunk (0..15 local coords). */
    private boolean isNearChunkBorder(Location loc, double radius) {
        int bx = loc.getBlockX();
        int bz = loc.getBlockZ();
        int lx = Math.floorMod(bx, 16); // local x in chunk [0..15]
        int lz = Math.floorMod(bz, 16); // local z in chunk [0..15]

        // Distance to edges
        double west = lx;
        double east = 15 - lx;
        double north = lz;
        double south = 15 - lz;

        double min = Math.min(Math.min(west, east), Math.min(north, south));
        return min <= radius;
    }

    /**
     * Returns a unit vector pointing OUTWARD from the nearest edge of the chunk.
     * West -> (-X), East -> (+X), North -> (-Z), South -> (+Z)
     */
    private Vector outwardNormalFromNearestEdge(Location loc) {
        int bx = loc.getBlockX();
        int bz = loc.getBlockZ();
        int lx = Math.floorMod(bx, 16);
        int lz = Math.floorMod(bz, 16);

        double west = lx;
        double east = 15 - lx;
        double north = lz;
        double south = 15 - lz;

        double min = west;
        Vector n = new Vector(-1, 0, 0); // west

        if (east < min) { min = east; n = new Vector(1, 0, 0); }
        if (north < min) { min = north; n = new Vector(0, 0, -1); }
        if (south < min) { n = new Vector(0, 0, 1); }

        return n; // already unit length in axis directions
    }
}
