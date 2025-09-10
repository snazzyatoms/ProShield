package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.List;

public class MobBorderRepelListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plots;

    private boolean enabled;
    private double radius;
    private double horizontalPush;
    private double verticalPush;
    private boolean despawnInside;
    private int intervalTicks;

    public MobBorderRepelListener(ProShield plugin, PlotManager plots) {
        this.plugin = plugin;
        this.plots = plots;
        reloadFromConfig();
        startRepelTask();
    }

    public void reloadFromConfig() {
        FileConfiguration config = plugin.getConfig();
        enabled = config.getBoolean("protection.mobs.border-repel.enabled", true);
        radius = config.getDouble("protection.mobs.border-repel.radius", 4.0);
        horizontalPush = config.getDouble("protection.mobs.border-repel.horizontal-push", 1.2);
        verticalPush = config.getDouble("protection.mobs.border-repel.vertical-push", 0.4);
        intervalTicks = config.getInt("protection.mobs.border-repel.interval-ticks", 20);
        despawnInside = config.getBoolean("protection.mobs.despawn-inside-claims", true);
    }

    private void startRepelTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!enabled) return;

                for (Player player : Bukkit.getOnlinePlayers()) {
                    Chunk chunk = player.getLocation().getChunk();
                    if (!plots.isClaimed(chunk)) continue;

                    List<LivingEntity> entities = player.getWorld().getLivingEntities();
                    for (LivingEntity entity : entities) {
                        if (entity instanceof Player) continue;
                        if (entity instanceof Tameable tame && tame.isTamed()) continue;

                        double distance = entity.getLocation().distance(player.getLocation());

                        // Inside claim → despawn (if enabled)
                        if (despawnInside && plots.isClaimed(entity.getLocation().getChunk())) {
                            entity.remove();
                            continue;
                        }

                        // Near border → push back
                        if (distance <= radius) {
                            Vector push = entity.getLocation().toVector()
                                    .subtract(player.getLocation().toVector())
                                    .normalize()
                                    .multiply(horizontalPush);
                            push.setY(verticalPush);
                            entity.setVelocity(push);
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, intervalTicks);
    }

    @EventHandler
    public void onMobSpawn(CreatureSpawnEvent event) {
        if (!despawnInside) return;

        LivingEntity entity = event.getEntity();
        if (entity instanceof Tameable tame && tame.isTamed()) return;

        if (plots.isClaimed(entity.getLocation().getChunk())) {
            event.setCancelled(true);
            entity.remove();
        }
    }
}
