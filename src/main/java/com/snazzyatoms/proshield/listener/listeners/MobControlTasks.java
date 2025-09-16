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
 * - Keeps hostile mobs out of claims (repel near players in claims)
 * - Despawns any hostiles that are inside claims on an interval
 * Driven entirely by config under protection.mobs.*
 */
public class MobControlTasks {

    private final ProShield plugin;
    private final PlotManager plots;

    public MobControlTasks(ProShield plugin) {
        this.plugin = plugin;
        this.plots = plugin.getPlotManager();
        startRepelTask();
        startDespawnTask();
    }

    private void startRepelTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                FileConfiguration cfg = plugin.getConfig();
                if (!cfg.getBoolean("protection.mobs.border-repel.enabled", true)) return;

                double radius = cfg.getDouble("protection.mobs.border-repel.radius", 15.0);
                double pushX = cfg.getDouble("protection.mobs.border-repel.horizontal-push", 0.7);
                double pushY = cfg.getDouble("protection.mobs.border-repel.vertical-push", 0.25);

                for (Player player : Bukkit.getOnlinePlayers()) {
                    Plot plot = plots.getPlot(player.getLocation());
                    if (plot == null) continue;
                    if (!plot.getFlag("safezone", cfg)) continue;

                    List<Entity> nearby = player.getNearbyEntities(radius, radius, radius);
                    for (Entity e : nearby) {
                        if (!(e instanceof Monster)) continue;

                        // push away from player (simple repel)
                        Vector dir = e.getLocation().toVector()
                                .subtract(player.getLocation().toVector())
                                .normalize();
                        dir.setY(pushY);
                        e.setVelocity(dir.multiply(pushX));
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L); // every second
    }

    private void startDespawnTask() {
        FileConfiguration cfg = plugin.getConfig();
        if (!cfg.getBoolean("protection.mobs.despawn-inside", true)) return;
        long period = cfg.getLong("protection.mobs.despawn-interval-seconds", 30) * 20L;

        new BukkitRunnable() {
            @Override
            public void run() {
                for (World world : Bukkit.getWorlds()) {
                    for (Entity e : world.getEntities()) {
                        if (!(e instanceof Monster)) continue;
                        Plot plot = plots.getPlot(e.getLocation());
                        if (plot == null) continue;
                        if (plot.getFlag("safezone", cfg)) {
                            e.remove(); // hostile inside a safe claim → despawn
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, period, period);
    }
}
