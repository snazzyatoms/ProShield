package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
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
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.List;

/**
 * MobProtectionListener
 * Unified protection for claims:
 * - Prevent hostile targeting/damage in safezones
 * - Repel hostiles near players inside claims
 * - Periodically despawn hostiles inside claims
 * - Block explosion grief inside claims (flags.explosions=false)
 * - Fire/ignite controls via granular flags
 * - Protect vehicles/frames in claims (trusted only)
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

    /* =========================
     * Spawning / Targeting
     * ========================= */

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
                event.setTarget(null);
            }
        }
    }

    /**
     * If a player moves into a claim (safezone), calm nearby angry mobs:
     * - clear targets
     * - reset anger (for zombified piglins, etc.)
     * - optionally play a subtle sound (config)
     */
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getTo() == null) return;
        Player player = event.getPlayer();

        Plot plot = plotManager.getPlot(event.getTo());
        if (plot == null) return;
        if (!plot.getFlag("safezone", plugin.getConfig())) return;

        // Only act when crossing chunk/claim boundaries or significant move
        if (event.getFrom().getChunk() == event.getTo().getChunk()) return;

        double radius = 16.0;
        List<Entity> nearby = player.getNearbyEntities(radius, radius, radius);
        for (Entity e : nearby) {
            if (!(e instanceof Monster monster)) continue;

            // Clear any target on entry
            if (monster.getTarget() != null && monster.getTarget().getUniqueId().equals(player.getUniqueId())) {
                monster.setTarget(null);
            }

            // Clear anger if applicable
            if (monster instanceof PigZombie pigZombie) {
                pigZombie.setAngry(false);
                pigZombie.setTarget(null);
            }
        }

        // Optional feedback
        if (plugin.getConfig().getBoolean("protection.mobs.border-repel.play-sound", false)) {
            try {
                String s = plugin.getConfig().getString("protection.mobs.border-repel.sound-type", "ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR");
                Sound sound = Sound.valueOf(s);
                player.playSound(player.getLocation(), sound, 0.25f, 1.0f);
            } catch (IllegalArgumentException ignored) { }
        }
    }

    /* =========================
     * Damage Protection
     * ========================= */

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        Entity victim = event.getEntity();
        Entity damager = event.getDamager();
        Plot plot = plotManager.getPlot(victim.getLocation());

        if (plot == null) return;
        boolean safezone = plot.getFlag("safezone", plugin.getConfig());

        // Player is safe from hostiles in safezone
        if (victim instanceof Player && safezone) {
            if (damager instanceof Monster) { event.setCancelled(true); return; }
            if (damager instanceof Projectile proj && proj.getShooter() instanceof Monster) { event.setCancelled(true); return; }
            if (damager instanceof AreaEffectCloud cloud && cloud.getSource() instanceof Monster) { event.setCancelled(true); return; }
        }

        // Pets & passive animals protection (config-driven)
        if (victim instanceof Tameable tame && tame.isTamed()
                && plugin.getConfig().getBoolean("protection.mobs.protect-pets", true)) {
            event.setCancelled(true);
        }
        if (victim instanceof Animals
                && plugin.getConfig().getBoolean("protection.mobs.protect-passive", true)) {
            event.setCancelled(true);
        }
    }

    /* =========================
     * Explosions / Fire / Grief
     * ========================= */

    @EventHandler
    public void onExplosion(EntityExplodeEvent event) {
        Plot plot = plotManager.getPlot(event.getLocation());
        if (plot == null) return;

        boolean allowExplosions = plot.getFlag("explosions", plugin.getConfig());
        if (!allowExplosions) {
            event.blockList().clear();
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onIgnite(BlockIgniteEvent event) {
        Plot plot = plotManager.getPlot(event.getBlock().getLocation());
        if (plot == null) return;

        switch (event.getCause()) {
            case FLINT_AND_STEEL -> {
                if (!plot.getFlag("ignite-flint", plugin.getConfig())) event.setCancelled(true);
            }
            case LAVA -> {
                if (!plot.getFlag("ignite-lava", plugin.getConfig())) event.setCancelled(true);
            }
            case LIGHTNING -> {
                if (!plot.getFlag("ignite-lightning", plugin.getConfig())) event.setCancelled(true);
            }
            default -> { /* leave others to vanilla or other flags */ }
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

        if (event.getBucket() == Material.LAVA_BUCKET && !plot.getFlag("bucket-lava", plugin.getConfig())) {
            event.setCancelled(true);
        }
        if (event.getBucket() == Material.WATER_BUCKET && !plot.getFlag("bucket-water", plugin.getConfig())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBucketFill(PlayerBucketFillEvent event) {
        Plot plot = plotManager.getPlot(event.getBlock().getLocation());
        if (plot == null) return;

        // Generic bucket-use toggle (use this if you expose such a flag in future)
        if (!plot.getFlag("bucket-use", plugin.getConfig())) {
            event.setCancelled(true);
        }
    }

    /* =========================
     * Entity Protections
     * ========================= */

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

    /* =========================
     * Repel + Despawn Tasks
     * ========================= */

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
