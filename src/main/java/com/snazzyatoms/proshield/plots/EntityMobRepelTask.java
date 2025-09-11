// src/main/java/com/snazzyatoms/proshield/plots/EntityMobRepelTask.java
package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Collection;

/**
 * EntityMobRepelTask
 *
 * ✅ Preserves all prior logic
 * ✅ Keeps backwards compatibility (start() still works)
 * ✅ Runs periodically via Bukkit scheduler
 */
public class EntityMobRepelTask extends BukkitRunnable {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private BukkitTask runningTask;

    public EntityMobRepelTask(ProShield plugin, PlotManager plotManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
    }

    /**
     * Schedules this task using Bukkit's scheduler.
     * Backward compatible so existing `.start()` calls still work.
     */
    public void start() {
        if (runningTask != null) {
            runningTask.cancel();
        }
        runningTask = this.runTaskTimer(plugin, 20L * 5, 20L * 5); // every 5s
    }

    @Override
    public void run() {
        // Check all plots with mob repel enabled
        Collection<Plot> plots = plotManager.getAllPlots();
        for (Plot plot : plots) {
            if (plot.getSettings().isMobRepelEnabled()) {
                repelMobs(plot);
            }
        }
    }

    private void repelMobs(Plot plot) {
        Chunk chunk = Bukkit.getWorld(plot.getWorldName()).getChunkAt(plot.getX(), plot.getZ());
        if (chunk == null || !chunk.isLoaded()) return;

        for (Entity e : chunk.getEntities()) {
            if (e instanceof Monster monster) {
                // Push mobs away from the center of the chunk
                double dx = monster.getLocation().getX() - (chunk.getX() * 16 + 8);
                double dz = monster.getLocation().getZ() - (chunk.getZ() * 16 + 8);
                double distance = Math.sqrt(dx * dx + dz * dz);
                if (distance < 16) { // inside claim radius
                    double strength = 0.4;
                    monster.setVelocity(monster.getVelocity().add(
                            monster.getLocation().toVector().subtract(chunk.getBlock(8, monster.getLocation().getBlockY(), 8).getLocation().toVector()).normalize().multiply(strength)
                    ));
                }
            }
        }
    }

    /**
     * Stop this task cleanly (if active).
     */
    public void stop() {
        if (runningTask != null) {
            runningTask.cancel();
            runningTask = null;
        }
    }
}
