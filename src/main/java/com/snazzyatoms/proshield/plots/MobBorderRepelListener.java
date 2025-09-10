package com.snazzyatoms.proshield.tasks;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.plots.Claim;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.List;

public class MobBorderRepelListener {

    private final ProShield plugin;
    private final PlotManager plots;
    private BukkitTask task;

    // Config-driven values
    private double radius;
    private double pushHorizontal;
    private double pushVertical;
    private int intervalTicks;
    private boolean despawnInside;

    public MobBorderRepelListener(ProShield plugin, PlotManager plots) {
        this.plugin = plugin;
        this.plots = plots;
        loadConfig();
    }

    private void loadConfig() {
        this.radius = plugin.getConfig().getDouble("protection.mobs.border-repel.radius", 3.0);
        this.pushHorizontal = plugin.getConfig().getDouble("protection.mobs.border-repel.horizontal-push", 0.8);
        this.pushVertical = plugin.getConfig().getDouble("protection.mobs.border-repel.vertical-push", 0.2);
        this.intervalTicks = plugin.getConfig().getInt("protection.mobs.border-repel.interval-ticks", 20);
        this.despawnInside = plugin.getConfig().getBoolean("protection.mobs.despawn-inside-claims", true);
    }

    public void start() {
        stop(); // safety

        task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            Bukkit.getWorlds().forEach(world -> {
                List<LivingEntity> entities = world.getLivingEntities().stream()
                        .filter(e -> e instanceof Monster) // only hostile mobs
                        .toList();

                for (LivingEntity mob : entities) {
                    Location loc = mob.getLocation();
                    Claim claim = plots.getClaimAt(loc);

                    if (claim != null) {
                        // If mob is inside claim
                        if (despawnInside) {
                            mob.remove();
                            continue;
                        }

                        // Otherwise repel it strongly
                        Location border = claim.getCenter();
                        Vector push = loc.toVector().subtract(border.toVector()).normalize();
                        push.multiply(pushHorizontal);
                        push.setY(pushVertical);
                        mob.setVelocity(push);
                    }
                }
            });
        }, 40L, intervalTicks); // start after 2s, repeat every interval
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }
}
