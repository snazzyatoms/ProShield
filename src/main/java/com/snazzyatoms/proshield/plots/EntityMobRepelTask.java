// src/main/java/com/snazzyatoms/proshield/plots/EntityMobRepelTask.java
package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class EntityMobRepelTask extends BukkitRunnable {

    private final ProShield plugin;
    private final PlotManager plotManager;

    private boolean repelEnabled;
    private double repelRadius;
    private double pushX;
    private double pushY;
    private boolean despawnInside;
    private boolean disableTargeting;

    public EntityMobRepelTask(ProShield plugin, PlotManager plotManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
        loadSettings();
    }

    private void loadSettings() {
        ConfigurationSection cfg = plugin.getConfig().getConfigurationSection("protection.mobs");
        if (cfg == null) return;

        repelEnabled = cfg.getConfigurationSection("border-repel").getBoolean("enabled", true);
        repelRadius = cfg.getConfigurationSection("border-repel").getDouble("radius", 3.0);
        pushX = cfg.getConfigurationSection("border-repel").getDouble("horizontal-push", 0.7);
        pushY = cfg.getConfigurationSection("border-repel").getDouble("vertical-push", 0.25);

        despawnInside = cfg.getBoolean("despawn-inside", true);
        disableTargeting = cfg.getBoolean("disable-targeting", true);
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Chunk chunk = player.getLocation().getChunk();
            Plot plot = plotManager.getPlot(chunk);

            if (plot == null) continue; // Only act if player is in a claim

            for (Entity entity : player.getNearbyEntities(repelRadius, repelRadius, repelRadius)) {
                if (!(entity instanceof Monster mob)) continue;

                // Despawn mobs inside claims
                if (despawnInside && plot.contains(entity.getLocation())) {
                    mob.remove();
                    continue;
                }

                // Repel mobs at the border
                if (repelEnabled && !plot.contains(entity.getLocation())) {
                    double dx = entity.getLocation().getX() - player.getLocation().getX();
                    double dz = entity.getLocation().getZ() - player.getLocation().getZ();
                    double distance = Math.sqrt(dx * dx + dz * dz);

                    if (distance < repelRadius) {
                        double vx = (dx / distance) * pushX;
                        double vz = (dz / distance) * pushX;
                        entity.setVelocity(new org.bukkit.util.Vector(vx, pushY, vz));
                    }
                }

                // Prevent targeting while player is inside claim
                if (disableTargeting) {
                    mob.setTarget(null);
                }
            }
        }
    }
}
