package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.List;

public class MobBorderRepelListener {

    private final ProShield plugin;
    private final PlotManager plots;

    public MobBorderRepelListener(ProShield plugin, PlotManager plots) {
        this.plugin = plugin;
        this.plots = plots;

        startTask();
    }

    private void startTask() {
        FileConfiguration config = plugin.getConfig();
        int interval = config.getInt("protection.mobs.border-repel.interval-ticks", 20);

        new BukkitRunnable() {
            @Override
            public void run() {
                repelMobs();
            }
        }.runTaskTimer(plugin, interval, interval);
    }

    private void repelMobs() {
        FileConfiguration config = plugin.getConfig();

        if (!config.getBoolean("protection.mobs.border-repel.enabled", true)) return;

        double radius = config.getDouble("protection.mobs.border-repel.radius", 2.5);
        double pushH = config.getDouble("protection.mobs.border-repel.horizontal-push", 0.8);
        double pushV = config.getDouble("protection.mobs.border-repel.vertical-push", 0.2);
        boolean despawnInside = config.getBoolean("protection.mobs.despawn-in-claims", true);

        for (Player player : Bukkit.getOnlinePlayers()) {
            // Get the claim at the player location
            Plot claim = plots.getClaimAt(player.getLocation());
            if (claim == null) continue;

            // Get all nearby entities
            List<Entity> nearby = player.getNearbyEntities(radius, radius, radius);
            for (Entity e : nearby) {
                if (!(e instanceof LivingEntity)) continue;
                if (e instanceof Player) continue;

                Location loc = e.getLocation();
                boolean insideClaim = plots.getClaimAt(loc) != null;

                if (insideClaim && despawnInside) {
                    // Instantly remove mobs that spawn inside claims
                    e.remove();
                    continue;
                }

                // Repel mobs trying to enter the claim
                if (!insideClaim) {
                    Vector dir = loc.toVector().subtract(player.getLocation().toVector()).normalize();
                    dir.multiply(pushH);
                    dir.setY(pushV);

                    e.setVelocity(dir);
                }
            }
        }
    }
}
