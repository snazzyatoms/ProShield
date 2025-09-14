package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
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
    }

    // Cancel hostile mob spawns in safezones
    @EventHandler
    public void onMobSpawn(CreatureSpawnEvent event) {
        if (!(event.getEntity() instanceof Monster)) return;
        Plot plot = plotManager.getPlot(event.getLocation());
        if (plot != null && plot.getFlag("safezone",
                plugin.getConfig().getBoolean("claims.default-flags.safezone", true))) {
            event.setCancelled(true);
        }
    }

    // Cancel damage to players in safezones (mobs & their projectiles)
    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        Plot plot = plotManager.getPlot(player.getLocation());
        if (plot == null || !plot.getFlag("safezone",
                plugin.getConfig().getBoolean("claims.default-flags.safezone", true))) {
            return;
        }

        Entity damager = event.getDamager();

        // Direct monster hit
        if (damager instanceof Monster) {
            event.setCancelled(true);
            return;
        }

        // Projectile from monster (skeleton arrow, blaze fireball, etc.)
        if (damager instanceof Projectile projectile) {
            ProjectileSource src = projectile.getShooter();
            if (src instanceof Monster) {
                event.setCancelled(true);
                return;
            }
        }

        // AreaEffectCloud from a monster (e.g., witch lingering)
        if (damager instanceof AreaEffectCloud cloud && cloud.getSource() instanceof LivingEntity le && le instanceof Monster) {
            event.setCancelled(true);
        }
    }

    // Cancel hostile targeting of players in safezones
    @EventHandler
    public void onEntityTarget(EntityTargetLivingEntityEvent event) {
        if (!(event.getTarget() instanceof Player player)) return;
        Plot plot = plotManager.getPlot(player.getLocation());
        if (plot != null && plot.getFlag("safezone",
                plugin.getConfig().getBoolean("claims.default-flags.safezone", true))) {
            if (event.getEntity() instanceof Monster) {
                event.setCancelled(true);
            }
        }
    }

    // Repel mobs at claim borders
    private void startRepelTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                FileConfiguration cfg = plugin.getConfig();
                if (!cfg.getBoolean("protection.mobs.border-repel.enabled", true)) return;

                double radius = cfg.getDouble("protection.mobs.border-repel.radius", 3.0);
                double pushX = cfg.getDouble("protection.mobs.border-repel.horizontal-push", 0.7);
                double pushY = cfg.getDouble("protection.mobs.border-repel.vertical-push", 0.25);

                boolean playSound = cfg.getBoolean("protection.mobs.border-repel.play-sound", true);
                String repelSound = cfg.getString("protection.mobs.border-repel.sound-type", null);
                if (repelSound == null || repelSound.isBlank()) {
                    repelSound = cfg.getString("sounds.repel-sound", "ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR");
                }

                for (Player player : Bukkit.getOnlinePlayers()) {
                    Plot plot = plotManager.getPlot(player.getLocation());
                    if (plot == null) continue;

                    List<Entity> nearby = player.getNearbyEntities(radius, radius, radius);
                    for (Entity e : nearby) {
                        if (!(e instanceof Monster)) continue;

                        Vector dir = e.getLocation().toVector()
                            .subtract(player.getLocation().toVector())
                            .normalize();
                        dir.setY(0.25);
                        e.setVelocity(dir.multiply(pushX).setY(pushY));

                        if (playSound) {
                            try { player.playSound(player.getLocation(), repelSound, 0.5f, 1.0f); } catch (Exception ignored) {}
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }
}
