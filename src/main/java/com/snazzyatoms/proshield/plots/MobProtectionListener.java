package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerJoinEvent;
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

    private boolean isSafeZone(Location loc) {
        Plot plot = plotManager.getPlot(loc);
        if (plot == null) return false;
        return plot.getFlag("safezone",
                plugin.getConfig().getBoolean("claims.safezone-enabled", true));
    }

    private boolean isOpBypass(Player player) {
        if (player.isOp() && plugin.getConfig().getBoolean("settings.ops-have-full-access", true)) {
            return !plugin.getBypassing().contains(player.getUniqueId());
        }
        return false;
    }

    /* =====================================
     * Mob Spawn Protection
     * ===================================== */
    @EventHandler
    public void onMobSpawn(CreatureSpawnEvent event) {
        if (!(event.getEntity() instanceof Monster)) return;
        if (isSafeZone(event.getLocation())) {
            if (!event.getEntity().hasMetadata("ProShieldIgnore")) {
                event.setCancelled(true);
            }
        }
    }

    /* =====================================
     * Damage Protection (Players + Pets + Passives)
     * ===================================== */
    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        Entity victim = event.getEntity();

        // Protect players in safezones
        if (victim instanceof Player player) {
            if (isSafeZone(player.getLocation()) && !isOpBypass(player)) {
                event.setCancelled(true);
                return;
            }
        }

        // Protect tamed pets
        if (victim instanceof Tameable pet && pet.isTamed()) {
            if (isSafeZone(pet.getLocation())) {
                event.setCancelled(true);
                return;
            }
        }

        // Protect passive mobs
        if (victim instanceof Animals || victim instanceof Villager) {
            if (isSafeZone(victim.getLocation())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        Entity victim = event.getEntity();

        // Prevent environment grief (fire, lava, explosions) inside claims
        if (isSafeZone(victim.getLocation())) {
            switch (event.getCause()) {
                case FIRE, FIRE_TICK, LAVA, LIGHTNING, ENTITY_EXPLOSION, BLOCK_EXPLOSION -> {
                    event.setCancelled(true);
                }
                default -> {}
            }
        }
    }

    /* =====================================
     * Cancel Hostile Targeting
     * ===================================== */
    @EventHandler
    public void onEntityTarget(EntityTargetLivingEntityEvent event) {
        if (!(event.getTarget() instanceof Player player)) return;
        if (isSafeZone(player.getLocation()) && event.getEntity() instanceof Monster) {
            event.setCancelled(true);
        }
    }

    /* =====================================
     * Repel Hostiles from Claims
     * ===================================== */
    private void startRepelTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                FileConfiguration cfg = plugin.getConfig();
                if (!cfg.getBoolean("protection.mobs.border-repel.enabled", true)) return;

                double radius = cfg.getDouble("protection.mobs.border-repel.radius", 15.0);
                double pushX = cfg.getDouble("protection.mobs.border-repel.horizontal-push", 0.7);
                double pushY = cfg.getDouble("protection.mobs.border-repel.vertical-push", 0.25);

                boolean playSound = cfg.getBoolean("protection.mobs.border-repel.play-sound", false);
                String repelSound = cfg.getString("protection.mobs.border-repel.sound-type",
                        "ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR");

                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!isSafeZone(player.getLocation())) continue;

                    List<Entity> nearby = player.getNearbyEntities(radius, radius, radius);
                    for (Entity e : nearby) {
                        if (!(e instanceof Monster)) continue;

                        Vector dir = e.getLocation().toVector()
                                .subtract(player.getLocation().toVector())
                                .normalize();
                        dir.setY(0.25);
                        e.setVelocity(dir.multiply(pushX).setY(pushY));

                        if (playSound) {
                            try {
                                player.playSound(player.getLocation(), repelSound, 0.5f, 1.0f);
                            } catch (Exception ignored) {}
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    /* =====================================
     * Despawn Hostiles in Claims
     * ===================================== */
    private void startDespawnTask() {
        FileConfiguration cfg = plugin.getConfig();
        if (!cfg.getBoolean("protection.mobs.despawn-inside", true)) return;

        int interval = cfg.getInt("protection.mobs.despawn-interval-seconds", 30) * 20;
        new BukkitRunnable() {
            @Override
            public void run() {
                for (World world : Bukkit.getWorlds()) {
                    for (Monster mob : world.getEntitiesByClass(Monster.class)) {
                        if (isSafeZone(mob.getLocation())) {
                            mob.remove();
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, interval, interval);
    }

    /* =====================================
     * Optional: Compass re-grant to OPs with auto-replace
     * ===================================== */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player.isOp() && plugin.getConfig().getBoolean("settings.compass-auto-replace", false)) {
            plugin.getGuiManager().giveCompass(player);
        }
    }
}
