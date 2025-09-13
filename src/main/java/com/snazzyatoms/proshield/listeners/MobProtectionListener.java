// src/main/java/com/snazzyatoms/proshield/listeners/MobProtectionListener.java
package com.snazzyatoms.proshield.listeners;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.util.Vector;

import java.util.List;

public class MobProtectionListener implements Listener {

    private final ProShield plugin;

    public MobProtectionListener(ProShield plugin) {
        this.plugin = plugin;
        // start repel task
        Bukkit.getScheduler().runTaskTimer(plugin, this::repelMobsTask, 20L, 20L); // every 1s
    }

    private boolean debug() {
        FileConfiguration cfg = plugin.getConfig();
        return cfg.getBoolean("messages.admin-flag-chat", true); // reuse toggle
    }

    @EventHandler
    public void onMobTarget(EntityTargetEvent event) {
        if (!(event.getTarget() instanceof Player player)) return;

        Plot plot = plugin.getPlotManager().getPlot(player.getLocation());
        if (plot == null) return;

        if (plot.getFlag("safezone", plugin.getConfig().getBoolean("claims.default-flags.safezone", true))) {
            event.setCancelled(true);
            if (debug()) {
                plugin.getLogger().info("[Debug] " + event.getEntityType() + " prevented from targeting "
                        + player.getName() + " in safe claim " + plot.getId());
            }
        }
    }

    @EventHandler
    public void onMobDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        Plot plot = plugin.getPlotManager().getPlot(player.getLocation());
        if (plot == null) return;

        if (plot.getFlag("safezone", plugin.getConfig().getBoolean("claims.default-flags.safezone", true))) {
            if (event.getDamager() instanceof Monster) {
                event.setCancelled(true);
                if (debug()) {
                    plugin.getLogger().info("[Debug] Prevented mob attack on " + player.getName() + " inside safe claim.");
                }
            }
        }
    }

    @EventHandler
    public void onMobSpawn(CreatureSpawnEvent event) {
        if (!(event.getEntity() instanceof LivingEntity mob)) return;

        Plot plot = plugin.getPlotManager().getPlot(event.getLocation());
        if (plot == null) return;

        if (plot.getFlag("mob-despawn", plugin.getConfig().getBoolean("claims.default-flags.mob-despawn", true))) {
            event.setCancelled(true);
            if (debug()) {
                plugin.getLogger().info("[Debug] Prevented " + mob.getType() + " from spawning inside safe claim.");
            }
        }
    }

    /** ======================================================
     * Repel hostile mobs away from claim borders
     * ====================================================== */
    private void repelMobsTask() {
        FileConfiguration cfg = plugin.getConfig();
        boolean repelEnabled = cfg.getBoolean("protection.mobs.border-repel.enabled", true);
        double radius = cfg.getDouble("protection.mobs.border-repel.radius", 3.0);
        double pushH = cfg.getDouble("protection.mobs.border-repel.horizontal-push", 0.7);
        double pushV = cfg.getDouble("protection.mobs.border-repel.vertical-push", 0.25);

        if (!repelEnabled) return;

        for (Player player : Bukkit.getOnlinePlayers()) {
            Plot plot = plugin.getPlotManager().getPlot(player.getLocation());
            if (plot == null) continue;

            // Check safezone flag
            if (!plot.getFlag("safezone", cfg.getBoolean("claims.default-flags.safezone", true))) continue;

            List<LivingEntity> nearby = player.getLocation().getNearbyLivingEntities(radius, radius, radius)
                    .stream()
                    .filter(e -> e instanceof Monster)
                    .map(e -> (LivingEntity) e)
                    .toList();

            for (LivingEntity mob : nearby) {
                // Compute push vector
                Location pLoc = player.getLocation();
                Vector dir = mob.getLocation().toVector().subtract(pLoc.toVector()).normalize();
                dir.multiply(pushH).setY(pushV);

                mob.setVelocity(dir);

                if (debug()) {
                    plugin.getLogger().info("[Debug] Repelled " + mob.getType() + " near claim border of " + plot.getId());
                }
            }
        }
    }
}
