// src/main/java/com/snazzyatoms/proshield/plots/EntityMobRepelTask.java
package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Bukkit;
import org.bukkit.entity.Monster;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Periodically despawns mobs inside claims
 * Also prevents mobs from targeting players inside claims.
 */
public class EntityMobRepelTask extends BukkitRunnable {

    private final ProShield plugin;
    private final PlotManager plotManager;

    public EntityMobRepelTask(ProShield plugin, PlotManager plotManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
    }

    @Override
    public void run() {
        Bukkit.getWorlds().forEach(world -> {
            world.getEntitiesByClass(Monster.class).forEach(mob -> {
                Plot plot = plotManager.getPlot(mob.getLocation());
                if (plot != null) {
                    // Despawn mob inside claim
                    mob.remove();
                } else {
                    // Wilderness: clear targeting if player is inside a claim
                    if (mob.getTarget() != null && plotManager.getPlot(mob.getTarget().getLocation()) != null) {
                        mob.setTarget(null);
                    }
                }
            });
        });
    }
}
