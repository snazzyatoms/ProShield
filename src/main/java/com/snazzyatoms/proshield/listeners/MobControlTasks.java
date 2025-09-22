package com.snazzyatoms.proshield.listeners;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.List;

/**
 * MobControlTasks
 * - Repels monsters from safezone claims
 * - Despawns hostile mobs inside claims
 * - Prevents pathing/locking onto players in claims
 * - Driven by config under protection.mobs.*
 */
public class MobControlTasks {

    private final ProShield plugin;
    private final PlotManager plots;

    public MobControlTasks(ProShield plugin) {
        this.plugin = plugin;
        this.plots = plugin.getPlotManager();
        startRepelTask();
        startDespawnTask();
        startPathBlockerTask();
    }

    private void startRepelTask() {
        FileConfiguration cfg = plugin.getConfig();
        if (!cfg.getBoolean("protection.mobs.border-repel.enabled", true)) return;

        double radius = cfg.getDouble("protection.mobs.border-repel.radius", 15.0);
        double pushX = cfg.getDouble("protection.mobs.border-repel.horizontal-push", 0.8);
        double pushY = cfg.getDouble("protection.mobs.border-repel.vertical-push", 0.25);

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    Plot plot = plots.getPlotAt(player.getLocation());
                    if (plot == null || !plot.getFlag("safezone")) continue; // ✅ check per-plot flag

                    List<Entity> nearby = player.getNearbyEntities(radius, radius, radius);
                    for (Entity e : nearby) {
                        if (!(e instanceof Monster)) continue;

                        // Push mob away from player
                        Vector dir = e.getLocation().toVector()
                                .subtract(player.getLocation().toVector())
                                .normalize();
                        dir.setY(pushY);
                        e.setVelocity(dir.multiply(pushX));

                        debug("Repelled " + e.getType() + " near " + player.getName()
                                + " in safezone plot=" + plot.getId());
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L); // Run every second
    }

    private void startDespawnTask() {
        FileConfiguration cfg = plugin.getConfig();
        if (!cfg.getBoolean("protection.mobs.despawn-inside", true)) return;

        long interval = cfg.getLong("protection.mobs.despawn-interval-seconds", 30L) * 20L;

        new BukkitRunnable() {
            @Override
            public void run() {
                for (World world : Bukkit.getWorlds()) {
                    for (Entity entity : world.getEntities()) {
                        if (!(entity instanceof Monster)) continue;

                        Plot plot = plots.getPlotAt(entity.getLocation());
                        if (plot != null && plot.getFlag("safezone")) { // ✅ per-plot safezone
                            entity.remove();
                            debug("Despawned " + entity.getType()
                                    + " inside safezone plot=" + plot.getId());
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, interval, interval);
    }

    private void startPathBlockerTask() {
        FileConfiguration cfg = plugin.getConfig();
        if (!cfg.getBoolean("protection.mobs.pathing.enabled", true)) return;

        long interval = cfg.getLong("protection.mobs.pathing.interval-ticks", 40L);

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    Plot plot = plots.getPlotAt(player.getLocation());
                    if (plot == null || !plot.getFlag("safezone")) continue; // ✅ per-plot safezone

                    List<Entity> nearby = player.getNearbyEntities(20, 10, 20);
                    for (Entity e : nearby) {
                        if (e instanceof Monster mob) {
                            if (mob.getTarget() instanceof Player target) {
                                Plot targetPlot = plots.getPlotAt(target.getLocation());
                                if (targetPlot != null && targetPlot.getFlag("safezone")) {
                                    mob.setTarget(null);
                                    debug("Cleared target of " + mob.getType()
                                            + " on player=" + target.getName()
                                            + " inside safezone plot=" + targetPlot.getId());
                                }
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, interval, interval);
    }

    /**
     * Debug logger for MobControlTasks.
     */
    private void debug(String msg) {
        if (plugin.isDebugEnabled()) {
            plugin.getLogger().info("[MobControlTasks] " + msg);
        }
    }
}
