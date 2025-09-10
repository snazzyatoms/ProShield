package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Monster;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.List;

public class MobBorderRepelListener {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private BukkitTask task;

    // Config values
    private final double radius;
    private final double pushHorizontal;
    private final double pushVertical;
    private final int interval;

    public MobBorderRepelListener(PlotManager plotManager) {
        this.plugin = ProShield.getInstance();
        this.plotManager = plotManager;

        // Load from config.yml
        this.radius = plugin.getConfig().getDouble("protection.mobs.border-repel.radius", 3.0);
        this.pushHorizontal = plugin.getConfig().getDouble("protection.mobs.border-repel.horizontal-push", 1.0);
        this.pushVertical = plugin.getConfig().getDouble("protection.mobs.border-repel.vertical-push", 0.25);
        this.interval = plugin.getConfig().getInt("protection.mobs.border-repel.interval-ticks", 20);
    }

    public void startTask() {
        if (task != null) {
            task.cancel();
        }
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::repelMobs, 0L, interval);
    }

    public void stopTask() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    private void repelMobs() {
        for (World world : Bukkit.getWorlds()) {
            List<LivingEntity> entities = world.getLivingEntities();
            for (LivingEntity entity : entities) {
                // Only apply to hostile mobs (zombies, skeletons, etc.)
                if (!(entity instanceof Monster)) continue;

                Location loc = entity.getLocation();

                // Check if mob is inside a claim
                if (plotManager.isClaimed(loc)) {
                    // Instantly despawn mobs that spawn inside claims
                    entity.remove();
                    continue;
                }

                // Check nearby players (to apply repel effect when mobs get close)
                for (Player player : world.getPlayers()) {
                    if (!plotManager.isClaimed(player.getLocation())) continue;

                    double distance = loc.distance(player.getLocation());
                    if (distance <= radius) {
                        // Calculate push direction
                        Vector push = entity.getLocation().toVector()
                                .subtract(player.getLocation().toVector())
                                .normalize()
                                .multiply(pushHorizontal);

                        push.setY(pushVertical);

                        entity.setVelocity(push);
                    }
                }
            }
        }
    }
}
