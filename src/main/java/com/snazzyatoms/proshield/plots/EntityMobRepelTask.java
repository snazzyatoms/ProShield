package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.util.Vector;

public class EntityMobRepelTask implements Runnable {

    private final ProShield plugin;
    private final PlotManager plots;
    private int taskId = -1;

    private EntityMobRepelTask(ProShield plugin, PlotManager plots) {
        this.plugin = plugin;
        this.plots = plots;
    }

    @Override
    public void run() {
        var cfg = plugin.getConfig();
        if (!cfg.getBoolean("protection.mobs.border-repel.enabled", true)) return;

        double radius = cfg.getDouble("protection.mobs.border-repel.radius", 1.5);
        double h = cfg.getDouble("protection.mobs.border-repel.horizontal-push", 0.6);
        double v = cfg.getDouble("protection.mobs.border-repel.vertical-push", 0.15);

        Bukkit.getWorlds().forEach(world ->
            world.getEntitiesByClass(Monster.class).forEach(mob -> {
                Location l = mob.getLocation();
                var claim = plots.getClaim(l);
                if (claim.isEmpty()) return;

                // simple push outwards: away from center of chunk (or nearest border)
                Location center = plots.keyToCenter(claim.get().key());
                if (center == null) return;
                Vector dir = mob.getLocation().toVector().subtract(center.toVector());
                if (dir.length() < radius) {
                    dir.normalize().multiply(h).setY(v);
                    mob.setVelocity(dir);
                }
            })
        );
    }

    public static void startIfEnabled(ProShield plugin, PlotManager plots) {
        int interval = plugin.getConfig().getInt("protection.mobs.border-repel.interval-ticks", 20);
        if (interval <= 0) return;
        EntityMobRepelTask task = new EntityMobRepelTask(plugin, plots);
        int id = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, task, 40L, interval);
        task.taskId = id;
        plugin.getLogger().info("Mob repel task started (interval " + interval + " ticks).");
    }

    public static void restartIfNeeded(ProShield plugin, PlotManager plots) {
        // simplest approach: cancel all tasks by name not reliable—let server restart tasks on reload
        startIfEnabled(plugin, plots);
    }
}
