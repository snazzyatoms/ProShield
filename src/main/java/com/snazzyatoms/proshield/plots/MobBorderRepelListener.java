package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class MobBorderRepelListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plots;

    private boolean enabled;
    private boolean despawnInside;
    private double repelRadius;
    private double pushHorizontal;
    private double pushVertical;
    private int intervalTicks;

    public MobBorderRepelListener(ProShield plugin, PlotManager plots) {
        this.plugin = plugin;
        this.plots = plots;
        loadConfig();

        if (enabled) {
            startTask();
        }
    }

    /**
     * Reload settings from config.yml
     */
    public void loadConfig() {
        enabled = plugin.getConfig().getBoolean("protection.mobs.border-repel.enabled", true);
        despawnInside = plugin.getConfig().getBoolean("protection.mobs.despawn-inside", true);
        repelRadius = plugin.getConfig().getDouble("protection.mobs.border-repel.radius", 3.5);
        pushHorizontal = plugin.getConfig().getDouble("protection.mobs.border-repel.horizontal-push", 1.2);
        pushVertical = plugin.getConfig().getDouble("protection.mobs.border-repel.vertical-push", 0.3);
        intervalTicks = plugin.getConfig().getInt("protection.mobs.border-repel.interval-ticks", 10);
    }

    private void startTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!enabled) return;

                Bukkit.getWorlds().forEach(world -> {
                    List<LivingEntity> mobs = world.getLivingEntities();

                    for (LivingEntity mob : mobs) {
                        // Skip players
                        if (mob.getType().isPlayer()) continue;

                        Location loc = mob.getLocation();

                        if (plots.isInsideClaim(loc)) {
                            if (despawnInside) {
                                mob.remove(); // instantly despawn
                            }
                            continue;
                        }

                        double dist = plots.distanceToClaimBorder(loc, repelRadius);
                        if (dist < repelRadius) {
                            // Push mob away from claim
                            Location nearestBorder = plots.closestClaimBorder(loc);
                            if (nearestBorder != null) {
                                double dx = loc.getX() - nearestBorder.getX();
                                double dz = loc.getZ() - nearestBorder.getZ();
                                double len = Math.sqrt(dx * dx + dz * dz);

                                if (len > 0.001) {
                                    double nx = dx / len;
                                    double nz = dz / len;

                                    mob.setVelocity(mob.getVelocity().add(
                                            new org.bukkit.util.Vector(nx * pushHorizontal, pushVertical, nz * pushHorizontal)
                                    ));
                                }
                            }
                        }
                    }
                });
            }
        }.runTaskTimer(plugin, intervalTicks, intervalTicks);
    }
}
