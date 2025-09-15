package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.List;

/**
 * Unified Protection Listener
 * - Handles mobs, explosions, fire, lava/water grief, pets, passive animals
 * - Provides safezones inside claims
 * - Repels/Despawns hostile mobs inside claims
 * - Prevents environmental grief
 */
public class MobProtectionListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plotManager;

    public MobProtectionListener(ProShield plugin, PlotManager plotManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
        startRepelTask();
        startDespawnTask();
    }

    /* ============================================================= */
    /* Spawning / Targeting                                           */
    /* ============================================================= */

    @EventHandler
    public void onMobSpawn(CreatureSpawnEvent event) {
        if (!(event.getEntity() instanceof Monster)) return;
        Plot plot = plotManager.getPlot(event.getLocation());
        if (plot != null && plot.getFlag("safezone", plugin.getConfig())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityTarget(EntityTargetLivingEntityEvent event) {
        if (!(event.getTarget() instanceof Player player)) return;
        Plot plot = plotManager.getPlot(player.getLocation());
        if (plot != null && plot.getFlag("safezone", plugin.getConfig())) {
            if (event.getEntity() instanceof Monster) {
                event.setCancelled(true);
            }
        }
    }

    /* ============================================================= */
    /* Damage Protection                                              */
    /* ============================================================= */

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        Entity victim = event.getEntity();
        Entity damager = event.getDamager();
        Plot plot = plotManager.getPlot(victim.getLocation());

        if (plot == null) return;
        FileConfiguration cfg = plugin.getConfig();
        boolean safezone = plot.getFlag("safezone", cfg);

        // Player victim inside safezone
        if (victim instanceof Player && safezone) {
            if (damager instanceof Monster) { event.setCancelled(true); return; }
            if (damager instanceof Projectile proj && proj.getShooter() instanceof Monster) { event.setCancelled(true); return; }
            if (damager instanceof AreaEffectCloud cloud && cloud.getSource() instanceof Monster) { event.setCancelled(true); return; }
        }

        // Pets & passive animals
        if (victim instanceof Tameable tame && tame.isTamed() && cfg.getBoolean("protection.mobs.protect-pets", true)) {
            event.setCancelled(true);
        }
        if (victim instanceof Animals && cfg.getBoolean("protection.mobs.protect-passive", true)) {
            event.setCancelled(true);
        }
    }

    /* ============================================================= */
    /* Explosion / Fire / Grief                                       */
    /* ============================================================= */

    @EventHandler
    public void onExplosion(EntityExplodeEvent event) {
        Plot plot = plotManager.getPlot(event.getLocation());
        if (plot == null) return;

        FileConfiguration cfg = plugin.getConfig();
        if (!plot.getFlag("explosions", cfg)) {
            event.blockList().clear();
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onIgnite(BlockIgniteEvent event) {
        Plot plot = plotManager.getPlot(event.getBlock().getLocation());
        if (plot == null) return;

        FileConfiguration cfg = plugin.getConfig();
        switch (event.getCause()) {
            case FLINT_AND_STEEL -> {
                if (!plot.getFlag("ignite-flint", cfg)) event.setCancelled(true);
            }
            case LAVA -> {
                if (!plot.getFlag("ignite-lava", cfg)) event.setCancelled(true);
            }
            case LIGHTNING -> {
                if (!plot.getFlag("ignite-lightning", cfg)) event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onFireSpread(BlockSpreadEvent event) {
        if (event.getSource() != null && event.getSource().getType() == Material.FIRE) {
            Plot plot = plotManager.getPlot(event.getBlock().getLocation());
            if (plot != null && !plot.getFlag("fire-spread", plugin.getConfig())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        Plot plot = plotManager.getPlot(event.getBlock().getLocation());
        if (plot == null) return;
        FileConfiguration cfg = plugin.getConfig();

        if (event.getBucket() == Material.LAVA_BUCKET && !plot.getFlag("bucket-lava", cfg)) {
            event.setCancelled(true);
        }
        if (event.getBucket() == Material.WATER_BUCKET && !plot.getFlag("bucket-water", cfg)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBucketFill(PlayerBucketFillEvent event) {
        Plot plot = plotManager.getPlot(event.getBlock().getLocation());
        if (plot == null) return;
        if (!plot.getFlag("bucket-use", plugin.getConfig())) {
            event.setCancelled(true);
        }
    }

    /* ============================================================= */
    /* Entity Protections (Frames, Vehicles, etc.)                    */
    /* ============================================================= */

    @EventHandler
    public void onVehicleDestroy(VehicleDestroyEvent event) {
        Plot plot = plotManager.getPlot(event.getVehicle().getLocation());
        if (plot == null) return;

        if (event.getAttacker() instanceof Player player) {
            if (!plot.isTrusted(player.getUniqueId())) {
                event.setCancelled(true);
            }
        } else {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onHangingBreak(HangingBreakByEntityEvent event) {
        Plot plot = plotManager.getPlot(event.getEntity().getLocation());
        if (plot == null) return;

        if (event.getRemover() instanceof Player player) {
            if (!plot.isTrusted(player.getUniqueId())) {
                event.setCancelled(true);
            }
        } else {
            event.setCancelled(true);
        }
    }

    /* ============================================================= */
    /* Repel + Despawn Tasks                                          */
    /* ============================================================= */

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
                    if (!plot.getFlag("safezone", cfg)) continue;

                    List<Entity> nearby = player.getNearbyEntities(radius, radius, radius);
                    for (Entity e : nearby) {
                        if (!(e instanceof Monster)) continue;

                        Vector dir = e.getLocation().toVector()
                                .subtract(player.getLocation().toVector())
                                .normalize();
                        dir.setY(pushY);
                        e.setVelocity(dir.multiply(pushX));
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    private void startDespawnTask() {
        FileConfiguration cfg = plugin.getConfig();
        if (!cfg.getBoolean("protection.mobs.despawn-inside", true)) return;
        long ticks = cfg.getLong("protection.mobs.despawn-interval-seconds", 30) * 20L;

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    Plot plot = plotManager.getPlot(player.getLocation());
                    if (plot == null) continue;
                    if (!plot.getFlag("safezone", plugin.getConfig())) continue;

                    List<Entity> ents = player.getNearbyEntities(30, 30, 30);
                    for (Entity e : ents) {
                        if (e instanceof Monster) {
                            e.remove();
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, ticks, ticks);
    }
}
