package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.List;

public class MobBorderRepelListener implements Listener {

    private final PlotManager plots;
    private final ProShield plugin;

    private boolean enabled;
    private double radius;
    private double pushH;
    private double pushV;
    private int interval;

    public MobBorderRepelListener(PlotManager plots) {
        this.plots = plots;
        this.plugin = plots.getPlugin();
        readConfig();
        // schedule tick if enabled
        if (enabled) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    try {
                        tickRepel();
                    } catch (Throwable ignored) {}
                }
            }.runTaskTimer(plugin, interval, interval);
        }
    }

    private void readConfig() {
        enabled  = plugin.getConfig().getBoolean("protection.mobs.border-repel.enabled", true);
        radius   = plugin.getConfig().getDouble("protection.mobs.border-repel.radius", 1.5D);
        pushH    = plugin.getConfig().getDouble("protection.mobs.border-repel.horizontal-push", 0.6D);
        pushV    = plugin.getConfig().getDouble("protection.mobs.border-repel.vertical-push", 0.15D);
        interval = plugin.getConfig().getInt("protection.mobs.border-repel.interval-ticks", 20);
        if (interval < 1) interval = 20;
        if (radius < 0D) radius = 0D;
    }

    private void tickRepel() {
        if (!enabled) return;

        // Iterate players (cheap) and consider nearby mobs (also cheap) rather than scanning all entities.
        for (Player p : plugin.getServer().getOnlinePlayers()) {
            World w = p.getWorld();
            // Quick AABB around player; 12 block cube is usually enough to feel responsive
            double rScan = Math.max(8.0, radius + 4.0);
            List<LivingEntity> nearby = p.getLocation().getNearbyLivingEntities(rScan);
            for (LivingEntity le : nearby) {
                if (!(le instanceof Monster)) continue;
                Location el = le.getLocation();

                // Only care when the mob is *inside* a claimed chunk
                if (!plots.isClaimed(el)) continue;

                // Compute distance to nearest chunk edge (0..16 local coords)
                Chunk ch = el.getChunk();
                int baseX = ch.getX() << 4;
                int baseZ = ch.getZ() << 4;

                double localX = el.getX() - baseX;
                double localZ = el.getZ() - baseZ;

                double distLeft   = localX;
                double distRight  = 16.0 - localX;
                double distTop    = localZ;
                double distBottom = 16.0 - localZ;

                double minEdge = Math.min(Math.min(distLeft, distRight), Math.min(distTop, distBottom));

                // Only repel if mob is within "radius" of the border (trying to cross)
                if (minEdge <= radius) {
                    Vector push = new Vector(0, pushV, 0);

                    // Push outward toward the nearest edge direction
                    if (minEdge == distLeft) {
                        push.setX(-pushH);
                    } else if (minEdge == distRight) {
                        push.setX(pushH);
                    } else if (minEdge == distTop) {
                        push.setZ(-pushH);
                    } else { // bottom
                        push.setZ(pushH);
                    }

                    // Apply velocity gently (don’t teleport)
                    Vector v = le.getVelocity();
                    le.setVelocity(new Vector(
                            clamp(v.getX() + push.getX(), -1.2, 1.2),
                            clamp(v.getY() + push.getY(), -0.2, 0.6),
                            clamp(v.getZ() + push.getZ(), -1.2, 1.2)
                    ));
                }
            }
        }
    }

    private double clamp(double v, double lo, double hi) {
        return Math.max(lo, Math.min(hi, v));
    }
}
