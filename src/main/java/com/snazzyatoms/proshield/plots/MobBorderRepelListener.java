package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles mob pushback at claim borders & despawning inside claims.
 * - Mobs cannot enter claims (are pushed back)
 * - Mobs spawning inside claims despawn instantly
 * - Configurable radius & pushback force
 * - Uses global + per-claim overrides
 * - Integrated with MessagesUtil
 */
public class MobBorderRepelListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final MessagesUtil messages;
    private BukkitTask repelTask;

    public MobBorderRepelListener(ProShield plugin, PlotManager plotManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
        this.messages = plugin.getMessagesUtil();

        startRepelTask();
    }

    private void startRepelTask() {
        FileConfiguration config = plugin.getConfig();
        long interval = config.getLong("protection.mobs.border-repel.interval-ticks", 20L);

        if (repelTask != null) repelTask.cancel();

        repelTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (LivingEntity entity : Bukkit.getWorlds().stream()
                    .flatMap(world -> world.getLivingEntities().stream())
                    .collect(Collectors.toList())) {

                if (!(entity instanceof Monster)) continue; // only hostile mobs

                Chunk chunk = entity.getLocation().getChunk();
                Plot plot = plotManager.getPlot(chunk);

                // If inside claim and despawn enabled
                if (plot != null) {
                    if (plugin.getConfig().getBoolean("protection.mobs.border-repel.despawn-inside", true)) {
                        entity.remove();
                        messages.debug(plugin, "Mob despawned inside claim at " + chunk);
                    }
                    continue;
                }

                // Check nearby plots for pushback
                List<Plot> nearbyPlots = plotManager.getNearbyPlots(entity.getLocation(),
                        config.getDouble("protection.mobs.border-repel.radius", 3.0));

                if (!nearbyPlots.isEmpty()) {
                    pushBackFromClaims(entity, nearbyPlots,
                            config.getDouble("protection.mobs.border-repel.horizontal-push", 1.0),
                            config.getDouble("protection.mobs.border-repel.vertical-push", 0.3));
                }
            }
        }, interval, interval);
    }

    private void pushBackFromClaims(LivingEntity entity, List<Plot> plots, double hPush, double vPush) {
        Location entityLoc = entity.getLocation();

        for (Plot plot : plots) {
            Location plotCenter = plot.getCenter();
            double dx = entityLoc.getX() - plotCenter.getX();
            double dz = entityLoc.getZ() - plotCenter.getZ();
            double distance = Math.sqrt(dx * dx + dz * dz);

            if (distance < 0.1) distance = 0.1; // avoid div/0

            double nx = (dx / distance) * hPush;
            double nz = (dz / distance) * hPush;

            entity.setVelocity(entity.getVelocity().add(new org.bukkit.util.Vector(nx, vPush, nz)));
            messages.debug(plugin, "Mob pushed back from claim border near " + plot.getOwner());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        LivingEntity entity = event.getEntity();
        Chunk chunk = entity.getLocation().getChunk();

        Plot plot = plotManager.getPlot(chunk);
        if (plot == null) return;

        boolean blockSpawn = plugin.getConfig().getBoolean("protection.mobs.block-spawn", true);
        boolean allowSpawner = plugin.getConfig().getBoolean("protection.mobs.allow-spawner-spawn", true);

        if (blockSpawn && (!allowSpawner || event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.SPAWNER)) {
            event.setCancelled(true);
            messages.debug(plugin, "Mob spawn blocked inside claim at " + chunk);
        }
    }
}
