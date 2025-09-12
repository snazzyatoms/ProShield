// src/main/java/com/snazzyatoms/proshield/util/ClaimPreview.java
package com.snazzyatoms.proshield.util;

import com.snazzyatoms.proshield.plots.Plot;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * ClaimPreview
 *
 * ✅ Fixed for v1.2.5:
 * - Uses Particle.VILLAGER_HAPPY (available in 1.20.1 API).
 * - Integrated with ClaimPreviewTask to track and cancel tasks cleanly.
 * - Preserves particle border logic from earlier versions.
 */
public class ClaimPreview {

    public static void show(Player player, Plot plot) {
        World world = Bukkit.getWorld(plot.getWorldName());
        if (world == null) return;

        int bx = plot.getX() << 4; // chunk to block coords
        int bz = plot.getZ() << 4;

        // Schedule repeating particle preview
        BukkitRunnable runnable = new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    return;
                }

                Location base = player.getLocation();
                double y = base.getY() + 1.0;

                // Emit particles along claim border
                for (int i = 0; i < 16; i++) {
                    spawnParticle(player, world, bx + i, y, bz);
                    spawnParticle(player, world, bx + i, y, bz + 16);
                    spawnParticle(player, world, bx, y, bz + i);
                    spawnParticle(player, world, bx + 16, y, bz + i);
                }

                ticks++;
                if (ticks > 60) { // ~3 seconds
                    cancel();
                }
            }
        };

        int taskId = runnable.runTaskTimer(
                Bukkit.getPluginManager().getPlugin("ProShield"),
                0L, 10L
        ).getTaskId();

        // Track task so it can be stopped globally
        ClaimPreviewTask.track(taskId);
    }

    private static void spawnParticle(Player player, World world, double x, double y, double z) {
        Location loc = new Location(world, x + 0.5, y, z + 0.5);
        player.spawnParticle(
                Particle.VILLAGER_HAPPY, // ✅ rolled back for 1.20.1 support
                loc,
                3, // count
                0.2, 0.2, 0.2, // offsets
                0.01 // speed
        );
    }
}
