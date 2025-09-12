// src/main/java/com/snazzyatoms/proshield/plots/EntityBorderRepelTask.java
package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class EntityBorderRepelTask extends BukkitRunnable {

    private final ProShield plugin;
    private final PlotManager plotManager;

    private boolean repelEnabled;
    private double repelRadius;
    private double pushX;
    private double pushY;

    public EntityBorderRepelTask(ProShield plugin, PlotManager plotManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
        loadSettings();
    }

    private void loadSettings() {
        ConfigurationSection cfg = plugin.getConfig().getConfigurationSection("protection.mobs.border-repel");
        if (cfg == null) return;

        repelEnabled = cfg.getBoolean("enabled", true);
        repelRadius = cfg.getDouble("radius", 3.0);
        pushX = cfg.getDouble("horizontal-push", 0.7);
        pushY = cfg.getDouble("vertical-push", 0.25);
    }

    @Override
    public void run() {
        if (!repelEnabled) return;

        for (Player player : Bukkit.getOnlinePlayers()) {
            Plot plot = plotManager.getPlot(player.getLocation());
            if (plot == null) continue;

            for (Entity entity : player.getNearbyEntities(repelRadius, repelRadius, repelRadius)) {
                if (entity instanceof Monster mob) {
                    if (!plot.contains(entity.getLocation())) {
                        Vector push = entity.getLocation().toVector()
                                .subtract(player.getLocation().toVector())
                                .normalize()
                                .multiply(pushX)
                                .setY(pushY);
                        mob.setVelocity(push);
                    }
                }
            }
        }
    }
}
