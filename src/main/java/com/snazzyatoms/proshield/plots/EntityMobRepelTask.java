package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Collection;

/**
 * EntityMobRepelTask
 *
 * ✅ Preserves all prior logic
 * ✅ Uses PlotSettings.isMobRepelEnabled()
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
        World world = Bukkit.getWorld(plot.getWorldName());
        if (world == null) return;

        Chunk chunk = world.getChunkAt(plot.getX(), plot.getZ());
        if (chunk == null || !chunk.isLoaded()) return;

        for (Entity e : chunk.getEntities()) {
            if (e instanceof Monster monster) {
                // Push mobs away from the center of the chunk
                double centerX = (chunk.getX() << 4) + 8;
                double centerZ = (chunk.getZ() << 4) + 8;

                double dx = monster.getLocation().getX() - centerX;
                double dz = monster.getLocation().getZ() - centerZ;
                double distance = Math.sqrt(dx * dx + dz * dz);

                if (distance < 16) { // inside claim radius
                    double strength = 0.4;
                    monster.setVelocity(
                        monster.getLocation().toVector()
                                .subtract(chunk.getBlock(8, monster.getLocation().getBlockY(), 8).getLocation().toVector())
                                .normalize()
                                .multiply(strength)
                    );
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
