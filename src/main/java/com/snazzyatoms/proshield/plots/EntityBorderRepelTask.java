// src/main/java/com/snazzyatoms/proshield/plots/EntityBorderRepelTask.java
package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;

public class EntityBorderRepelTask extends BukkitRunnable {

    private final ProShield plugin;
    private final PlotManager plotManager;

    private final double radius;
    private final double pushH;
    private final double pushV;

    public EntityBorderRepelTask(ProShield plugin, PlotManager plotManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;

        radius = plugin.getConfig().getDouble("protection.mobs.border-repel.radius", 3.0);
        pushH  = plugin.getConfig().getDouble("protection.mobs.border-repel.horizontal-push", 0.7);
        pushV  = plugin.getConfig().getDouble("protection.mobs.border-repel.vertical-push", 0.25);
    }

    @Override
    public void run() {
        Bukkit.getWorlds().forEach(world -> {
            for (LivingEntity entity : world.getLivingEntities()) {
                if (entity.getCustomName() != null || entity.getType().isAlive() == false) continue;

                Location loc = entity.getLocation();
                Chunk chunk = loc.getChunk();

                Plot plot = plotManager.getPlot(chunk);
                if (plot == null) continue; // wilderness

                // Repel entity near border
                Location center = chunk.getBlock(8, loc.getBlockY(), 8).getLocation();
                double dx = loc.getX() - center.getX();
                double dz = loc.getZ() - center.getZ();

                double distSq = dx * dx + dz * dz;
                if (distSq <= radius * radius) {
                    double mag = Math.sqrt(dx * dx + dz * dz);
                    if (mag > 0) {
                        entity.setVelocity(entity.getVelocity().add(
                            new org.bukkit.util.Vector(dx / mag * pushH, pushV, dz / mag * pushH)
                        ));
                    }
                }
            }
        });
    }
}
