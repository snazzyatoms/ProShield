package com.snazzyatoms.proshield.plots;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 * BorderVisualizer
 *
 * ✅ Uses Particle.DUST with DustOptions (modern Spigot/Paper API).
 * ✅ Keeps prior border visualization logic intact.
 */
public class BorderVisualizer {

    /**
     * Show claim border around a chunk to a player.
     */
    public static void showBorder(Player player, Plot plot) {
        if (plot == null) return;

        World world = player.getWorld();
        int x = plot.getX() << 4;
        int z = plot.getZ() << 4;

        // Use DustOptions for colored particle
        Particle.DustOptions redDust = new Particle.DustOptions(Color.RED, 1.0f);

        // Outline the 16x16 chunk edges
        for (int i = 0; i <= 16; i++) {
            Location north = new Location(world, x + i, player.getLocation().getY(), z);
            Location south = new Location(world, x + i, player.getLocation().getY(), z + 16);
            Location west  = new Location(world, x, player.getLocation().getY(), z + i);
            Location east  = new Location(world, x + 16, player.getLocation().getY(), z + i);

            spawnParticle(player, north, redDust);
            spawnParticle(player, south, redDust);
            spawnParticle(player, west, redDust);
            spawnParticle(player, east, redDust);
        }
    }

    private static void spawnParticle(Player player, Location loc, Particle.DustOptions dust) {
        // ✅ Use DUST instead of REDSTONE
        player.spawnParticle(Particle.DUST, loc, 1, 0, 0, 0, 0, dust);
    }
}
