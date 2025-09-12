// src/main/java/com/snazzyatoms/proshield/plots/EntityMobRepelTask.java
package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * EntityMobRepelTask
 * - Removes mobs inside claims (if enabled)
 * - Resets targeting so mobs ignore players inside claims
 *
 * Fixed for v1.2.5:
 *   • Uses Plot#getWorldName(), getX(), getZ()
 *   • Null-safe checks
 */
public class EntityMobRepelTask extends BukkitRunnable {

    private final ProShield plugin;
    private final PlotManager plotManager;

    private final boolean despawnInside;

    public EntityMobRepelTask(ProShield plugin, PlotManager plotManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
        this.despawnInside = plugin.getConfig().getBoolean("protection.mobs.despawn-inside", true);
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
                        // Instantly despawn mobs that enter claims
                        mob.remove();
                    } else {
                        // Reset target → mobs act like player is "invisible" inside claims
                        mob.setTarget(null);
                    }
                }
            }
        }
    }
}
