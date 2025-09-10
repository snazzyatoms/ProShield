package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

/**
 * Handles mob pushback & despawn inside claims.
 * - Prevents mobs from walking into claims (repel radius)
 * - Removes mobs that spawn inside claims if enabled
 * - Supports per-claim overrides and global settings
 */
public class MobBorderRepelListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final MessagesUtil messages;

    public MobBorderRepelListener(ProShield plugin, PlotManager plotManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
        this.messages = plugin.getMessagesUtil();

        startRepelTask();
    }

    private void startRepelTask() {
        FileConfiguration config = plugin.getConfig();

        boolean enabled = config.getBoolean("protection.mobs.border-repel.enabled", true);
        int intervalTicks = config.getInt("protection.mobs.border-repel.interval-ticks", 20);

        if (!enabled) {
            messages.debug(plugin, "&eMob repel task disabled via config.");
            return;
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                repelAndDespawnMobs();
            }
        }.runTaskTimer(plugin, 0L, intervalTicks);
    }

    /**
     * Applies pushback or despawn to mobs based on claim/global rules.
     */
    private void repelAndDespawnMobs() {
        FileConfiguration config = plugin.getConfig();

        double globalRadius = config.getDouble("protection.mobs.border-repel.radius", 3.0);
        double hPush = config.getDouble("protection.mobs.border-repel.horizontal-push", 1.0);
        double vPush = config.getDouble("protection.mobs.border-repel.vertical-push", 0.3);
        boolean globalDespawnInside = config.getBoolean("protection.mobs.border-repel.despawn-inside", true);

        for (org.bukkit.World world : Bukkit.getWorlds()) {
            List<Entity> entities = world.getEntities();

            for (Entity entity : entities) {
                if (!(entity instanceof LivingEntity living)) continue;
                if (entity.getCustomName() != null && entity.getCustomName().equalsIgnoreCase("§c[ProShield-Bypass]")) continue;

                Chunk chunk = entity.getLocation().getChunk();
                Plot plot = plotManager.getPlot(chunk);

                if (plot == null) continue; // no claim here

                PlotSettings settings = plot.getSettings();
                boolean repelEnabled = settings.isMobRepelEnabled();
                boolean despawnInside = settings.isMobDespawnInsideEnabled();

                // If mobs are inside claim and despawn is active
                if (despawnInside || globalDespawnInside) {
                    messages.debug(plugin, "&cDespawning mob inside claim: " + entity.getType());
                    entity.remove();
                    continue;
                }

                // If repel is enabled
                if (repelEnabled) {
                    Location edge = plot.getNearestBorder(entity.getLocation());
                    if (edge != null && entity.getLocation().distance(edge) < globalRadius) {
                        double dx = entity.getLocation().getX() - edge.getX();
                        double dz = entity.getLocation().getZ() - edge.getZ();

                        entity.setVelocity(entity.getVelocity().add(
                                new org.bukkit.util.Vector(dx * hPush, vPush, dz * hPush)
                        ));

                        messages.debug(plugin, "&ePushed mob back from claim border: " + entity.getType());
                    }
                }
            }
        }
    }
}
