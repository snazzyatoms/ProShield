// src/main/java/com/snazzyatoms/proshield/plots/EntityMobRepelTask.java
package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Chunk;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.scheduler.BukkitRunnable;

public class EntityMobRepelTask extends BukkitRunnable {

    private final ProShield plugin;
    private final PlotManager plots;

    public EntityMobRepelTask(ProShield plugin, PlotManager plots) {
        this.plugin = plugin;
        this.plots = plots;
    }

    @Override
    public void run() {
        boolean despawn = plugin.getConfig().getBoolean("protection.mobs.despawn-inside", true);
        if (!despawn) return;

        for (Plot plot : plots.getAllPlots()) {
            Chunk c = plot.getChunk();
            for (org.bukkit.entity.Entity e : c.getEntities()) {
                if (e instanceof Monster m) {
                    // Remove hostile mobs that are inside a claimed chunk
                    m.remove();
                } else if (e instanceof LivingEntity le) {
                    // Optionally, push passives upward slightly so they wander off
                    le.setVelocity(le.getVelocity().setY(0.1));
                }
            }
        }
    }
}
