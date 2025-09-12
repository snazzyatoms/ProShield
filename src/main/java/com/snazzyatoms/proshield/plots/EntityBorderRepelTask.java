// src/main/java/com/snazzyatoms/proshield/plots/EntityBorderRepelTask.java
package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

/**
 * EntityBorderRepelTask
 * - Pushes mobs back when they approach claim borders
 *
 * Fixed for v1.2.5:
 *   • Uses Plot#getWorldName(), getX(), getZ()
 *   • Null-safe world lookups
 */
public class EntityBorderRepelTask extends BukkitRunnable {

    private final ProShield plugin;
    private final PlotManager plotManager;

    private final double radius;
    private final double pushX;
    private final double pushY;

    public EntityBorderRepelTask(ProShield plugin, PlotManager plotManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;

        this.radius = plugin.getConfig().getDouble("protection.mobs.border-repel.radius", 3.0);
        this.pushX = plugin.getConfig().getDouble("protection.mobs.border-repel.horizontal-push", 0.7);
        this.pushY = plugin.getConfig().getDouble("protection.mobs.border-repel.vertical-push", 0.25);
    }

    @Override
    public void run() {
        for (World world : Bukkit.getWorlds()) {
            for (LivingEntity entity : world.getLivingEntities()) {
                if (!(entity instanceof Monster mob)) continue;

                Location loc = mob.getLocation();
                Plot insidePlot = plotManager.getPlot(loc);

                if (insidePlot == null) {
                    // Check if mob is near a claim border
                    Plot nearbyPlot = getNearbyPlot(loc);
                    if (nearbyPlot != null) {
                        Location center = getPlotCenter(nearbyPlot);
                        if (center != null) {
                            Vector push = loc.toVector().subtract(center.toVector());
                            push.normalize().multiply(pushX).setY(pushY);
                            mob.setVelocity(push);
                        }
                    }
                }
            }
        }
    }

    private Plot getNearbyPlot(Location loc) {
        for (Plot plot : plotManager.getAllPlots()) {
            Location center = getPlotCenter(plot);
            if (center == null) continue;
            if (!center.getWorld().equals(loc.getWorld())) continue;

            if (center.distance(loc) <= radius) {
                return plot;
            }
        }
        return null;
    }

    private Location getPlotCenter(Plot plot) {
        World world = Bukkit.getWorld(plot.getWorldName());
        if (world == null) return null;

        int blockX = plot.getX() * 16 + 8;
        int blockZ = plot.getZ() * 16 + 8;
        return new Location(world, blockX, world.getHighestBlockYAt(blockX, blockZ), blockZ);
    }
}
