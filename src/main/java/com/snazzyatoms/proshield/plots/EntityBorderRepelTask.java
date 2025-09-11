// src/main/java/com/snazzyatoms/proshield/plots/EntityMobRepelTask.java
package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.List;

/**
 * Periodic task that applies "mob border repel" or "despawn inside" logic
 * for entities in claimed chunks.
 *
 * Preserves prior behavior (high-level):
 *  - If a claim has mob-despan enabled -> remove non-player living entities inside.
 *  - Else if mob-repel enabled -> gently push non-player living entities back
 *    toward the chunk edge (away from the claim's center) when near the border.
 *
 * Implementation notes:
 *  - Scans around online players (cheap) instead of whole worlds.
 *  - No usage of removed/unstable helpers like getNearestBorder(...).
 *  - Excludes players. Keeps animals/monsters as "LivingEntity" targets.
 */
public class EntityMobRepelTask implements Runnable {

    private final ProShield plugin;
    private final PlotManager plots;

    // tuneable scan params (kept simple to be safe)
    private int periodTicks = 40;     // ~2s
    private double scanRadius = 16.0; // one chunk radius around players
    private double borderThreshold = 2.0; // how near to chunk border to push

    private BukkitTask task;

    public EntityMobRepelTask(ProShield plugin, PlotManager plots) {
        this.plugin = plugin;
        this.plots = plots;
    }

    /** Start the repeating task (safe to call once). */
    public void start() {
        if (task != null) return;
        task = Bukkit.getScheduler().runTaskTimer(plugin, this, periodTicks, periodTicks);
    }

    /** Stop the task (if running). */
    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    /** Optional: adjust period before start(). */
    public void setPeriodTicks(int ticks) {
        this.periodTicks = Math.max(1, ticks);
        // If already running, restart with new period
        if (task != null) {
            stop();
            start();
        }
    }

    /** Optional: adjust scan radius (blocks). */
    public void setScanRadius(double radius) {
        this.scanRadius = Math.max(4.0, radius);
    }

    /** Optional: distance from a chunk border (in blocks) to begin repelling. */
    public void setBorderThreshold(double threshold) {
        this.borderThreshold = Math.max(0.5, threshold);
    }

    @Override
    public void run() {
        // Iterate players so we don't scan entire worlds every tick
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (!viewer.isOnline() || viewer.isDead()) continue;

            List<LivingEntity> nearby = viewer.getNearbyEntities(scanRadius, scanRadius / 2, scanRadius).stream()
                    .filter(e -> e instanceof LivingEntity && !(e instanceof Player))
                    .map(e -> (LivingEntity) e)
                    .toList();

            for (LivingEntity mob : nearby) {
                Location l = mob.getLocation();
                Chunk chunk = l.getChunk();
                Plot plot = plots.getPlot(chunk);
                if (plot == null) continue;

                PlotSettings s = plot.getSettings();
                // Despawn inside takes priority if enabled
                if (s.isMobDespawnInsideEnabled()) {
                    // Only remove naturally-spawned style mobs; you can add more checks if needed
                    mob.remove();
                    continue;
                }

                if (!s.isMobRepelEnabled()) continue;

                // If repel is enabled, nudge mobs away from the claim center when they are near the border.
                // We'll compute distance to the closest border of the chunk; if within threshold, push outward.
                double dx = l.getX() - (chunk.getX() * 16 + 8.0); // relative to chunk center X
                double dz = l.getZ() - (chunk.getZ() * 16 + 8.0); // relative to chunk center Z

                // Distance to the nearest block edge of the chunk (0..15 local coords)
                int localX = l.getBlockX() & 15; // 0..15
                int localZ = l.getBlockZ() & 15; // 0..15
                double distToEdgeX = Math.min(localX, 15 - localX);
                double distToEdgeZ = Math.min(localZ, 15 - localZ);
                double minDistToEdge = Math.min(distToEdgeX, distToEdgeZ);

                if (minDistToEdge <= borderThreshold) {
                    // Push outward from the center so they don't cross deeply into the claim
                    Vector away = new Vector(dx, 0, dz).normalize().multiply(0.35);
                    // Add a small upward to avoid ground stickiness
                    away.setY(0.15);
                    mob.setVelocity(mob.getVelocity().add(away));
                }
            }
        }
    }
}
