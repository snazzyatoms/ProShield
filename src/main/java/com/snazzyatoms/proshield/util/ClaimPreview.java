package com.snazzyatoms.proshield.util;

import com.snazzyatoms.proshield.plots.Plot;
import org.bukkit.*;
import org.bukkit.entity.Player;

/**
 * Simple visual boundary preview for a plot using particles.
 */
public class ClaimPreview {

    public static void show(Player player, Plot plot) {
        if (player == null || plot == null) return;
        World world = Bukkit.getWorld(plot.getWorld());
        if (world == null) return;

        int cx = plot.getX();
        int cz = plot.getZ();
        int minX = (cx << 4);
        int minZ = (cz << 4);
        int maxX = minX + 15;
        int maxZ = minZ + 15;
        int y = player.getLocation().getBlockY() + 1;

        for (int x = minX; x <= maxX; x += 1) {
            world.spawnParticle(Particle.VILLAGER_HAPPY, x + 0.5, y, minZ + 0.5, 1, 0, 0, 0, 0);
            world.spawnParticle(Particle.VILLAGER_HAPPY, x + 0.5, y, maxZ + 0.5, 1, 0, 0, 0, 0);
        }
        for (int z = minZ; z <= maxZ; z += 1) {
            world.spawnParticle(Particle.VILLAGER_HAPPY, minX + 0.5, y, z + 0.5, 1, 0, 0, 0, 0);
            world.spawnParticle(Particle.VILLAGER_HAPPY, maxX + 0.5, y, z + 0.5, 1, 0, 0, 0, 0);
        }
    }
}
