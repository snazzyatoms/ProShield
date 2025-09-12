// src/main/java/com/snazzyatoms/proshield/plots/PlotService.java
package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.scheduler.BukkitTask;

import java.util.Iterator;
import java.util.UUID;

/**
 * PlotService
 * - Runs background tasks for claims
 * - Replaces repel tasks, cleanup tasks, and expiry checks
 */
public class PlotService {

    private final ProShield plugin;
    private final PlotManager plotManager;

    private BukkitTask repelTask;
    private BukkitTask expiryTask;

    public PlotService(ProShield plugin, PlotManager plotManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
    }

    /**
     * Start all background tasks
     */
    public void start() {
        startMobRepelTask();
        startExpiryTask();
        plugin.getLogger().info("PlotService tasks started.");
    }

    /**
     * Stop all tasks
     */
    public void stop() {
        if (repelTask != null) repelTask.cancel();
        if (expiryTask != null) expiryTask.cancel();
        plugin.getLogger().info("PlotService tasks stopped.");
    }

    /* ======================================================
     * MOB REPEL
     * ====================================================== */
    private void startMobRepelTask() {
        double radius = plugin.getConfig().getDouble("protection.mobs.border-repel.radius", 3.0);
        double pushX = plugin.getConfig().getDouble("protection.mobs.border-repel.horizontal-push", 0.7);
        double pushY = plugin.getConfig().getDouble("protection.mobs.border-repel.vertical-push", 0.25);

        repelTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Plot plot : plotManager.getAllPlots()) {
                Location loc = plot.getCenter();
                for (Entity entity : loc.getWorld().getNearbyEntities(loc, radius, radius, radius)) {
                    if (entity instanceof Monster mob) {
                        // Push mobs out of claim edge
                        mob.setVelocity(mob.getVelocity().setX(pushX).setY(pushY).setZ(pushX));
                    }
                }
            }
        }, 20L, 20L); // runs every second
    }

    /* ======================================================
     * CLAIM EXPIRY
     * ====================================================== */
    private void startExpiryTask() {
        long expireTicks = 20L * 60L * 30L; // every 30 min

        expiryTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            Iterator<Plot> it = plotManager.getAllPlots().iterator();
            while (it.hasNext()) {
                Plot plot = it.next();
                if (plot.isExpired()) {
                    it.remove();
                    Bukkit.broadcastMessage("§cClaim expired: " + plot.getDisplayNameSafe());
                }
            }
        }, expireTicks, expireTicks);
    }
}
