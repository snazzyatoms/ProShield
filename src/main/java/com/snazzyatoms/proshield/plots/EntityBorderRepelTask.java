// src/main/java/com/snazzyatoms/proshield/plots/EntityMobRepelTask.java
package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.scheduler.BukkitTask;

/**
 * EntityMobRepelTask
 *
 * ✅ Preserves prior repel logic
 * ✅ Fixed duplication (now only in this file, not EntityBorderRepelTask.java)
 * ✅ Provides .start() for scheduling
 */
public class EntityMobRepelTask implements Runnable {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private BukkitTask task;

    public EntityMobRepelTask(ProShield plugin, PlotManager plotManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
    }

    /**
     * Starts the repeating repel task (every 5 seconds).
     */
    public void start() {
        if (task != null) {
            task.cancel();
        }
        task = Bukkit.getScheduler().runTaskTimer(plugin, this, 20L, 100L); // 5s interval
    }

    @Override
    public void run() {
        for (Plot plot : plotManager.getAllPlots()) {
            if (!plot.getSettings().isMobRepelEnabled()) continue;

            Chunk chunk = Bukkit.getWorld(plot.getWorldName()).getChunkAt(plot.getX(), plot.getZ());
            Location center = chunk.getBlock(8, 64, 8).getLocation(); // chunk center
            double radius = 16.0; // repel distance

            for (LivingEntity entity : chunk.getEntitiesByClass(LivingEntity.class)) {
                if (entity instanceof Monster) {
                    Location mobLoc = entity.getLocation();
                    if (mobLoc.distanceSquared(center) <= radius * radius) {
                        // Push mob away from claim center
                        entity.setVelocity(mobLoc.toVector().subtract(center.toVector()).normalize().multiply(1.2));
                    }
                }
            }
        }
    }
}
