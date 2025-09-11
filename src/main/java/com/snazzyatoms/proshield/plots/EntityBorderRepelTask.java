// src/main/java/com/snazzyatoms/proshield/plots/EntityBorderRepelTask.java
package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;

/**
 * EntityBorderRepelTask
 *
 * ✅ Preserves prior border repel logic
 * ✅ Fixed duplication issue (EntityMobRepelTask is now separate)
 * ✅ Can still be used for future extensions (border-specific repel logic)
 */
public class EntityBorderRepelTask implements Runnable {

    private final ProShield plugin;
    private final PlotManager plotManager;

    public EntityBorderRepelTask(ProShield plugin, PlotManager plotManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
    }

    @Override
    public void run() {
        // Example: additional border repel logic (expandable later)
        for (Plot plot : plotManager.getAllPlots()) {
            if (!plot.getSettings().isMobRepelEnabled()) continue;

            Chunk chunk = Bukkit.getWorld(plot.getWorldName()).getChunkAt(plot.getX(), plot.getZ());
            Location center = chunk.getBlock(8, 64, 8).getLocation();
            double borderRadius = 16.0;

            for (LivingEntity entity : chunk.getEntitiesByClass(LivingEntity.class)) {
                if (entity instanceof Monster) {
                    double distSq = entity.getLocation().distanceSquared(center);
                    if (distSq <= borderRadius * borderRadius) {
                        // Mild pushback to discourage mobs lingering at borders
                        entity.setVelocity(
                                entity.getLocation().toVector().subtract(center.toVector()).normalize().multiply(0.5)
                        );
                    }
                }
            }
        }
    }
}
