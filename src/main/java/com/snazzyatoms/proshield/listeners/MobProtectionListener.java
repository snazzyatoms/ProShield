// src/main/java/com/snazzyatoms/proshield/plots/MobProtectionListener.java
package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
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

    /** ======================================================
     * Cancel hostile mob spawns & damage in safezones
     * ====================================================== */
    @EventHandler
    public void onMobSpawn(EntitySpawnEvent event) {
        if (!(event.getEntity() instanceof Monster)) return;
        Plot plot = plotManager.getPlot(event.getLocation());
        if (plot != null && plot.getFlag("safezone",
                plugin.getConfig().getBoolean("claims.default-flags.safezone", true))) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        Plot plot = plotManager.getPlot(player.getLocation());
        if (plot != null && plot.getFlag("safezone",
                plugin.getConfig().getBoolean("claims.default-flags.safezone", true))) {
            if (event.getDamager() instanceof Monster) {
                event.setCancelled(true);
            }
        }
    }

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

    /** ======================================================
     * Repel mobs at claim borders
     * ====================================================== */
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
                boolean debug = cfg.getBoolean("protection.mobs.border-repel.debug", false);
                String debugMsg = cfg.getString("protection.mobs.border-repel.debug-message",
                        "&8[Debug]&7 Repelled {mob} near {player} at claim {claim}");

                // Prefer per-section sound, fallback to global
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

                        // Push mobs away
                        Vector dir = e.getLocation().toVector()
                                .subtract(player.getLocation().toVector())
                                .normalize();
                        dir.setY(0.25); // upward component
                        e.setVelocity(dir.multiply(pushX).setY(pushY));

                        // Optional sound feedback
                        if (playSound) {
                            try {
                                player.playSound(player.getLocation(), repelSound, 0.5f, 1.0f);
                            } catch (Exception ignored) {
                                // silently ignore invalid sound keys
                            }
                        }

                        // Debug message
                        if (debug && debugMsg != null) {
                            String claimName = (plot.getOwner() != null)
                                    ? plot.getOwner()
                                    : plot.getId().toString();

                            String formatted = debugMsg
                                    .replace("{player}", player.getName())
                                    .replace("{mob}", e.getType().name())
                                    .replace("{claim}", claimName);
                            player.sendMessage(formatted.replace("&", "§"));
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L); // every second
    }
}
