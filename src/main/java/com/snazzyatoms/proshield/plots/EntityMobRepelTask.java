// src/main/java/com/snazzyatoms/proshield/plots/EntityMobRepelTask.java
package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class EntityMobRepelTask extends BukkitRunnable {

    private final ProShield plugin;
    private final PlotManager plotManager;

    private final boolean despawnInside;
    private final boolean repelEnabled;
    private final double repelRadius;
    private final double pushHorizontal;
    private final double pushVertical;

    public EntityMobRepelTask(ProShield plugin, PlotManager plotManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;

        // Configurable values
        this.despawnInside = plugin.getConfig().getBoolean("protection.mobs.despawn-inside", true);
        this.repelEnabled = plugin.getConfig().getBoolean("protection.mobs.border-repel.enabled", true);
        this.repelRadius = plugin.getConfig().getDouble("protection.mobs.border-repel.radius", 3.0);
        this.pushHorizontal = plugin.getConfig().getDouble("protection.mobs.border-repel.horizontal-push", 0.7);
        this.pushVertical = plugin.getConfig().getDouble("protection.mobs.border-repel.vertical-push", 0.25);
    }

    @Override
    public void run() {
        for (World world : Bukkit.getWorlds()) {
            for (LivingEntity entity : world.getLivingEntities()) {
                if (!(entity instanceof Monster mob)) continue;

                Location loc = mob.getLocation();
                Plot plot = plotManager.getPlot(loc);

                if (plot != null) {
                    if (despawnInside) {
                        mob.remove(); // Instantly despawn mobs inside claims
                    } else {
                        mob.setTarget(null); // Reset target → players inside claims are "invisible"

                        // Repel mobs from claim borders
                        if (repelEnabled) {
                            Location nearestEdge = getNearestEdge(loc, plot);
                            if (nearestEdge != null && loc.distance(nearestEdge) <= repelRadius) {
                                Vector push = loc.toVector().subtract(nearestEdge.toVector()).normalize();
                                push.setX(push.getX() * pushHorizontal);
                                push.setZ(push.getZ() * pushHorizontal);
                                push.setY(pushVertical);
                                mob.setVelocity(push);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Calculate nearest edge of claim to push mobs away.
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
}
