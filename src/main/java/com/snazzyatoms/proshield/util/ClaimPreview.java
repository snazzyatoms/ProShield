// src/main/java/com/snazzyatoms/proshield/util/ClaimPreview.java
package com.snazzyatoms.proshield.util;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * ClaimPreview
 * - Shows a temporary particle outline of a claimed chunk
 * - Useful when claiming or inspecting land
 *
 * Fixed for v1.2.5:
 *   • Uses Plot#getWorldName(), getX(), getZ()
 *   • Replaced legacy VILLAGER_HAPPY → HAPPY_VILLAGER
 *   • Null-safe + runs tied to ProShield plugin instance
 */
public class ClaimPreview {

    public static void show(Player player, Plot plot) {
        if (player == null || plot == null) return;

        World world = Bukkit.getWorld(plot.getWorldName());
        if (world == null) return;

        int bx = plot.getX() << 4; // chunkX * 16
        int bz = plot.getZ() << 4; // chunkZ * 16

        new BukkitRunnable() {
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
                    spawnParticle(player, world, bx + i, y, bz + 15);
                    spawnParticle(player, world, bx, y, bz + i);
                    spawnParticle(player, world, bx + 15, y, bz + i);
                }

                ticks++;
                if (ticks > 60) { // ~3 seconds (60 * 10 ticks)
                    cancel();
                }
            }
        }.runTaskTimer(ProShield.getInstance(), 0L, 10L);
    }

    private static void spawnParticle(Player player, World world, double x, double y, double z) {
        Location loc = new Location(world, x + 0.5, y, z + 0.5);
        player.spawnParticle(
                Particle.HAPPY_VILLAGER, // ✅ modern API particle
                loc,
                3, // count
                0.2, 0.2, 0.2, // offsets
                0.01 // speed
        );
    }
}
