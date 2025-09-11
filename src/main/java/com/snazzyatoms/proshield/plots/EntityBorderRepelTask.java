// src/main/java/com/snazzyatoms/proshield/plots/EntityBorderRepelTask.java
package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Collection;

/**
 * EntityBorderRepelTask
 *
 * ✅ Preserves all prior logic
 * ✅ Fixed class/file mismatch (no more duplicate EntityMobRepelTask)
 * ✅ Added start()/stop() for backward compatibility
 */
public class EntityBorderRepelTask extends BukkitRunnable {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private BukkitTask runningTask;

    public EntityBorderRepelTask(ProShield plugin, PlotManager plotManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
    }

    /**
     * Start this task (legacy compatibility).
     */
    public void start() {
        if (runningTask != null) {
            runningTask.cancel();
        }
        runningTask = this.runTaskTimer(plugin, 20L * 5, 20L * 5); // every 5s
    }

    @Override
    public void run() {
        Collection<Plot> plots = plotManager.getAllPlots();
        for (Plot plot : plots) {
            if (plot.getSettings().isMobRepelEnabled()) {
                repelAtBorder(plot);
            }
        }
    }

    private void repelAtBorder(Plot plot) {
        Chunk chunk = Bukkit.getWorld(plot.getWorldName()).getChunkAt(plot.getX(), plot.getZ());
        if (chunk == null || !chunk.isLoaded()) return;

        for (Entity entity : chunk.getEntities()) {
            if (entity instanceof LivingEntity living) {
                Location loc = living.getLocation();
                double edgeDistance = getDistanceToChunkEdge(loc, chunk);

                if (edgeDistance < 3.0) { // near the border
                    double pushStrength = 0.5;
                    Location center = new Location(chunk.getWorld(),
                            chunk.getX() * 16 + 8,
                            loc.getY(),
                            chunk.getZ() * 16 + 8);

                    living.setVelocity(living.getVelocity().add(
                            loc.toVector().subtract(center.toVector()).normalize().multiply(pushStrength)
                    ));
                }
            }
        }
    }

    private double getDistanceToChunkEdge(Location loc, Chunk chunk) {
        int minX = chunk.getX() * 16;
        int maxX = minX + 15;
        int minZ = chunk.getZ() * 16;
        int maxZ = minZ + 15;

        double dx = Math.min(Math.abs(loc.getX() - minX), Math.abs(loc.getX() - maxX));
        double dz = Math.min(Math.abs(loc.getZ() - minZ), Math.abs(loc.getZ() - maxZ));

        return Math.min(dx, dz);
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
