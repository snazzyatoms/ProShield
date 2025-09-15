package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.List;

public class MobProtectionListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plotManager;

    public MobProtectionListener(ProShield plugin, PlotManager plotManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
        startRepelTask();
        startDespawnTask();
    }

    /* =========================================
     * MOB SPAWN / TARGET / DAMAGE
     * ========================================= */
    @EventHandler
    public void onMobSpawn(CreatureSpawnEvent event) {
        if (!(event.getEntity() instanceof Monster)) return;
        Plot plot = plotManager.getPlot(event.getLocation());
        if (plot != null && !plot.getFlag("mob-spawn",
                plugin.getConfig().getBoolean("protection.world-controls.defaults.mob-spawn", true))) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityTarget(EntityTargetLivingEntityEvent event) {
        if (!(event.getTarget() instanceof Player player)) return;
        Plot plot = plotManager.getPlot(player.getLocation());
        if (plot != null && !plot.getFlag("mob-damage",
                plugin.getConfig().getBoolean("protection.world-controls.defaults.mob-damage", true))) {
            if (event.getEntity() instanceof Monster) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        Entity victim = event.getEntity();

        // Protect players in claims
        if (victim instanceof Player player) {
            Plot plot = plotManager.getPlot(player.getLocation());
            if (plot != null && !plot.getFlag("pvp",
                    plugin.getConfig().getBoolean("protection.world-controls.defaults.pvp", true))) {
                // cancel PvP
                if (event.getDamager() instanceof Player || (event.getDamager() instanceof Projectile p && p.getShooter() instanceof Player)) {
                    event.setCancelled(true);
                    return;
                }
            }

            // cancel mob damage in claims
            if (plot != null && !plot.getFlag("mob-damage",
                    plugin.getConfig().getBoolean("protection.world-controls.defaults.mob-damage", true))) {
                if (event.getDamager() instanceof Monster) {
                    event.setCancelled(true);
                    return;
                }
            }
        }

        // Protect pets / passive mobs
        if (victim instanceof Tameable tameable) {
            if (tameable.isTamed()) {
                event.setCancelled(true);
                return;
            }
        }
        if (victim instanceof Animals || victim instanceof Villager) {
            event.setCancelled(true);
        }
    }

    /* =========================================
     * FIRE & EXPLOSIONS
     * ========================================= */
    @EventHandler
    public void onBlockIgnite(BlockIgniteEvent event) {
        Plot plot = plotManager.getPlot(event.getBlock().getLocation());
        FileConfiguration cfg = plugin.getConfig();

        if (plot != null) {
            if (!plot.getFlag("fire-burn", cfg.getBoolean("protection.world-controls.defaults.fire-burn", true))) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onExplosion(EntityExplodeEvent event) {
        Plot plot = plotManager.getPlot(event.getLocation());
        FileConfiguration cfg = plugin.getConfig();

        if (plot != null && !plot.getFlag("explosions", cfg.getBoolean("protection.world-controls.defaults.explosions", true))) {
            event.blockList().clear(); // no block damage
            event.setCancelled(true);
        }
    }

    /* =========================================
     * BUCKET USE
     * ========================================= */
    @EventHandler
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        Plot plot = plotManager.getPlot(event.getBlockClicked().getLocation());
        FileConfiguration cfg = plugin.getConfig();

        if (plot != null && !plot.getFlag("bucket-use", cfg.getBoolean("protection.world-controls.defaults.bucket-use", true))) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBucketFill(PlayerBucketFillEvent event) {
        Plot plot = plotManager.getPlot(event.getBlockClicked().getLocation());
        FileConfiguration cfg = plugin.getConfig();

        if (plot != null && !plot.getFlag("bucket-use", cfg.getBoolean("protection.world-controls.defaults.bucket-use", true))) {
            event.setCancelled(true);
        }
    }

    /* =========================================
     * WILDERNESS ENTITIES
     * ========================================= */
    @EventHandler
    public void onHangingBreak(HangingBreakByEntityEvent event) {
        Plot plot = plotManager.getPlot(event.getEntity().getLocation());
        FileConfiguration cfg = plugin.getConfig();

        if (plot == null) { // wilderness
            if (!cfg.getBoolean("protection.entities.wilderness.item-frames", false)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onVehicleDestroy(VehicleDestroyEvent event) {
        Plot plot = plotManager.getPlot(event.getVehicle().getLocation());
        FileConfiguration cfg = plugin.getConfig();

        if (plot == null) { // wilderness
            if (!cfg.getBoolean("protection.entities.wilderness.vehicles", false)) {
                event.setCancelled(true);
            }
        }
    }

    /* =========================================
     * BORDER REPEL & DESPAWN
     * ========================================= */
    private void startRepelTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                FileConfiguration cfg = plugin.getConfig();
                if (!cfg.getBoolean("protection.mobs.border-repel.enabled", true)) return;

                double radius = cfg.getDouble("protection.mobs.border-repel.radius", 3.0);
                double pushX = cfg.getDouble("protection.mobs.border-repel.horizontal-push", 0.7);
                double pushY = cfg.getDouble("protection.mobs.border-repel.vertical-push", 0.25);

                for (Player player : Bukkit.getOnlinePlayers()) {
                    Plot plot = plotManager.getPlot(player.getLocation());
                    if (plot == null) continue;

                    List<Entity> nearby = player.getNearbyEntities(radius, radius, radius);
                    for (Entity e : nearby) {
                        if (!(e instanceof Monster)) continue;

                        Vector dir = e.getLocation().toVector()
                                .subtract(player.getLocation().toVector())
                                .normalize();
                        e.setVelocity(dir.multiply(pushX).setY(pushY));
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    private void startDespawnTask() {
        FileConfiguration cfg = plugin.getConfig();
        if (!cfg.getBoolean("protection.mobs.despawn-inside", true)) return;

        long interval = cfg.getLong("protection.mobs.despawn-interval-seconds", 30) * 20L;
        new BukkitRunnable() {
            @Override
            public void run() {
                for (World world : Bukkit.getWorlds()) {
                    for (Monster mob : world.getEntitiesByClass(Monster.class)) {
                        Plot plot = plotManager.getPlot(mob.getLocation());
                        if (plot != null) {
                            mob.remove();
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, interval, interval);
    }
}
