// src/main/java/com/snazzyatoms/proshield/plots/EntityMobRepelTask.java
package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.scheduler.BukkitRunnable;

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

                if (plot != null && despawnInside) {
                    // Instantly despawn mobs that enter claims
                    mob.remove();
                } else if (plot != null) {
                    // "Invisible effect" → reset target if mob is inside a claim
                    mob.setTarget(null);
                }
            }
        }
    }
}
