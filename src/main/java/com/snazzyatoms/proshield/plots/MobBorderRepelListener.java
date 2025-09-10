package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Collection;

public class MobBorderRepelListener {

    private final ProShield plugin;
    private final PlotManager plots;

    public MobBorderRepelListener(ProShield plugin, PlotManager plots) {
        this.plugin = plugin;
        this.plots = plots;
        startTask();
    }

    private void startTask() {
        int interval = plugin.getConfig().getInt("protection.mobs.border-repel.interval-ticks", 20);
        new BukkitRunnable() {
            @Override
            public void run() {
                repelMobs();
            }
        }.runTaskTimer(plugin, interval, interval);
    }

    private void repelMobs() {
        double repelRadius = plugin.getConfig().getDouble("protection.mobs.border-repel.radius", 3.5);
        double horizontalPush = plugin.getConfig().getDouble("protection.mobs.border-repel.horizontal-push", 1.2);
        double verticalPush = plugin.getConfig().getDouble("protection.mobs.border-repel.vertical-push", 0.3);
        boolean despawnInside = plugin.getConfig().getBoolean("protection.mobs.despawn-inside", true);

        Bukkit.getWorlds().forEach(world -> {
            Collection<LivingEntity> entities = world.getLivingEntities();
            for (LivingEntity entity : entities) {
                if (entity instanceof Player) continue;

                Location loc = entity.getLocation();

                if (plots.isInsideClaim(loc)) {
                    // If mobs are not allowed inside claims, remove them
                    if (despawnInside) {
                        entity.remove();
                        continue;
                    }
                } else {
                    // Repel mobs that approach a claim border
                    double distance = plots.distanceToClaimBorder(loc);
                    if (distance <= repelRadius) {
                        Location nearestBorder = plots.getNearestClaimBorder(loc);
                        if (nearestBorder != null) {
                            Vector push = loc.toVector()
                                    .subtract(nearestBorder.toVector())
                                    .normalize()
                                    .multiply(horizontalPush)
                                    .setY(verticalPush);
                            entity.setVelocity(push);
                        }
                    }
                }
            }
        });
    }
}
