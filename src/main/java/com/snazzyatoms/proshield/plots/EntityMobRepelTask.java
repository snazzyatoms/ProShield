package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Collection;

/**
 * EntityMobRepelTask
 *
 * ✅ Repels mobs OUT of claims
 * ✅ Adds buffer zone (configurable) → mobs can’t loiter near claims
 * ✅ Uses PlotSettings to check per-claim toggle
 * ✅ Runs every few seconds via Bukkit scheduler
 */
public class EntityMobRepelTask extends BukkitRunnable {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private BukkitTask runningTask;

    // Pull from config
    private final double bufferRadius;
    private final double pushStrengthX;
    private final double pushStrengthY;

    public EntityMobRepelTask(ProShield plugin, PlotManager plotManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;

        // Config values
        this.bufferRadius   = plugin.getConfig().getDouble("protection.mobs.border-repel.radius", 2.5);
        this.pushStrengthX  = plugin.getConfig().getDouble("protection.mobs.border-repel.horizontal-push", 0.6);
        this.pushStrengthY  = plugin.getConfig().getDouble("protection.mobs.border-repel.vertical-push", 0.2);
    }

    /** Start task (runs every 5s). */
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
                repelMobs(plot);
            }
        }
    }

    private void repelMobs(Plot plot) {
        Chunk chunk = Bukkit.getWorld(plot.getWorldName()).getChunkAt(plot.getX(), plot.getZ());
        if (chunk == null || !chunk.isLoaded()) return;

        for (Entity e : chunk.getEntities()) {
            if (e instanceof Monster monster) {
                Location mobLoc = monster.getLocation();
                Location center = new Location(chunk.getWorld(),
                        chunk.getX() * 16 + 8,
                        mobLoc.getY(),
                        chunk.getZ() * 16 + 8);

                // Distance from claim center
                double dx = mobLoc.getX() - center.getX();
                double dz = mobLoc.getZ() - center.getZ();
                double distance = Math.sqrt(dx * dx + dz * dz);

                // If inside claim OR within buffer zone → repel outward
                double claimRadius = 8.0; // half chunk size
                if (distance < claimRadius + bufferRadius) {
                    double strength = pushStrengthX;
                    monster.setVelocity(monster.getVelocity().add(
                            mobLoc.toVector().subtract(center.toVector())
                                    .normalize()
                                    .multiply(strength)
                                    .setY(pushStrengthY) // add slight upward push
                    ));
                }
            }
        }
    }

    /** Stop task cleanly. */
    public void stop() {
        if (runningTask != null) {
            runningTask.cancel();
            runningTask = null;
        }
    }
}
