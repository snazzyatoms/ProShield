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

public class EntityBorderRepelTask extends BukkitRunnable {

    private final ProShield plugin;
    private final PlotManager plotManager;

    private final boolean repelEnabled;
    private final double radius;
    private final double pushX;
    private final double pushY;

    public EntityBorderRepelTask(ProShield plugin, PlotManager plotManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;

        this.repelEnabled = plugin.getConfig().getBoolean("protection.mobs.border-repel.enabled", true);
        this.radius = plugin.getConfig().getDouble("protection.mobs.border-repel.radius", 3.0);
        this.pushX = plugin.getConfig().getDouble("protection.mobs.border-repel.horizontal-push", 0.7);
        this.pushY = plugin.getConfig().getDouble("protection.mobs.border-repel.vertical-push", 0.25);
    }

    @Override
    public void run() {
        if (!repelEnabled) return;

        for (World world : Bukkit.getWorlds()) {
            for (LivingEntity entity : world.getLivingEntities()) {
                if (!(entity instanceof Monster mob)) continue;

                Location loc = mob.getLocation();

                // If mob is not inside a claim, check if near border
                if (plotManager.getPlot(loc) == null) {
                    Plot nearest = getNearbyPlot(loc);
                    if (nearest != null) {
                        Location nearestEdge = getNearestEdge(loc, nearest);
                        if (nearestEdge != null && loc.distance(nearestEdge) <= radius) {
                            Vector push = loc.toVector().subtract(nearestEdge.toVector()).normalize();
                            push.setX(push.getX() * pushX);
                            push.setZ(push.getZ() * pushX);
                            push.setY(pushY);
                            mob.setVelocity(push);
                        }
                    }
                }
            }
        }
    }

    /**
     * Finds the nearest edge of a plot relative to a location.
     */
    private Location getNearestEdge(Location loc, Plot plot) {
        int x = plot.getX() << 4; // chunkX * 16
        int z = plot.getZ() << 4; // chunkZ * 16
        int maxX = x + 15;
        int maxZ = z + 15;

        double clampedX = Math.max(x, Math.min(loc.getX(), maxX));
        double clampedZ = Math.max(z, Math.min(loc.getZ(), maxZ));

        return new Location(loc.getWorld(), clampedX, loc.getY(), clampedZ);
    }

    /**
     * Returns a nearby plot if within radius.
     */
    private Plot getNearbyPlot(Location loc) {
        Plot plot = plotManager.getPlot(loc);
        if (plot != null) return plot;

        // Check surrounding chunks (8 directions)
        int cx = loc.getChunk().getX();
        int cz = loc.getChunk().getZ();
        String world = loc.getWorld().getName();

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                Plot p = plotManager.getPlot(world, cx + dx, cz + dz);
                if (p != null) return p;
            }
        }
        return null;
    }
}
