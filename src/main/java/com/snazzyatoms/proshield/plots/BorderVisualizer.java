// src/main/java/com/snazzyatoms/proshield/plots/BorderVisualizer.java
package com.snazzyatoms.proshield.plots;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 * BorderVisualizer
 *
 * ✅ Fixes:
 * - Uses Particle.REDSTONE with DustOptions instead of old raw REDSTONE.
 * - Preserves prior logic of drawing red particle borders for claims.
 */
public class BorderVisualizer {

    private final Plot plot;

    public BorderVisualizer(Plot plot) {
        this.plot = plot;
    }

    public void show(Player player) {
        World world = player.getWorld();
        if (world == null || !world.getName().equals(plot.getWorldName())) return;

        int bx = plot.getX() << 4;
        int bz = plot.getZ() << 4;

        // ✅ Proper DustOptions (red color, size 1)
        Particle.DustOptions red = new Particle.DustOptions(Color.RED, 1.0F);

        // Draw north/south edges
        for (int x = bx; x < bx + 16; x++) {
            Location north = new Location(world, x + 0.5, player.getLocation().getY(), bz + 0.5);
            Location south = new Location(world, x + 0.5, player.getLocation().getY(), bz + 16 + 0.5);

            player.spawnParticle(Particle.REDSTONE, north, 1, 0, 0, 0, 0, red);
            player.spawnParticle(Particle.REDSTONE, south, 1, 0, 0, 0, 0, red);
        }

        // Draw west/east edges
        for (int z = bz; z < bz + 16; z++) {
            Location west = new Location(world, bx + 0.5, player.getLocation().getY(), z + 0.5);
            Location east = new Location(world, bx + 16 + 0.5, player.getLocation().getY(), z + 0.5);

            player.spawnParticle(Particle.REDSTONE, west, 1, 0, 0, 0, 0, red);
            player.spawnParticle(Particle.REDSTONE, east, 1, 0, 0, 0, 0, red);
        }
    }
}
