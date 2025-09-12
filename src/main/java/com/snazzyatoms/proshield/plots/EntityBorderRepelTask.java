// src/main/java/com/snazzyatoms/proshield/plots/EntityBorderRepelTask.java
package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class EntityBorderRepelTask extends BukkitRunnable {

    private final ProShield plugin;
    private final PlotManager plots;

    public EntityBorderRepelTask(ProShield plugin, PlotManager plots) {
        this.plugin = plugin;
        this.plots = plots;
    }

    @Override
    public void run() {
        double radius = plugin.getConfig().getDouble("protection.mobs.border-repel.radius", 3.0);
        double hPush  = plugin.getConfig().getDouble("protection.mobs.border-repel.horizontal-push", 0.7);
        double vPush  = plugin.getConfig().getDouble("protection.mobs.border-repel.vertical-push", 0.25);

        for (Plot plot : plots.getAllPlots()) {
            Chunk c = plot.getChunk();
            World w = c.getWorld();
            int cx = c.getX() << 4;
            int cz = c.getZ() << 4;
            int minX = cx, maxX = cx + 15, minZ = cz, maxZ = cz + 15;

            // Scan entities *in* the claimed chunk and push them out when close to edges.
            for (LivingEntity e : w.getLivingEntities()) {
                Location l = e.getLocation();
                if (l.getChunk().equals(c)) {
                    double dx = Math.min(Math.abs(l.getX() - minX), Math.abs(l.getX() - maxX));
                    double dz = Math.min(Math.abs(l.getZ() - minZ), Math.abs(l.getZ() - maxZ));
                    double edge = Math.min(dx, dz);

                    if (edge <= radius) {
                        // Push outward from the nearest edge
                        Vector v = new Vector(0, vPush, 0);
                        if (dx < dz) v.setX(l.getX() < (minX + maxX) / 2.0 ? -hPush : hPush);
                        else         v.setZ(l.getZ() < (minZ + maxZ) / 2.0 ? -hPush : hPush);
                        e.setVelocity(v);
                    }
                }
            }
        }
    }
}
