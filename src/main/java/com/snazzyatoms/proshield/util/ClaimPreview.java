package com.snazzyatoms.proshield.util;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * ClaimPreview
 * - Visual boundary preview for claims (radius-based)
 * - Configurable particle type, duration, and spacing
 * - Supports both chunk-based and radius-based claims
 */
public class ClaimPreview {

    public static void show(ProShield plugin, Player player, Plot plot) {
        if (plugin == null || player == null || plot == null) return;
        World world = Bukkit.getWorld(plot.getWorld());
        if (world == null) return;

        // Configurable particle
        String particleName = plugin.getConfig().getString("settings.claim-preview.particle", "VILLAGER_HAPPY");
        Particle particle;
        try {
            particle = Particle.valueOf(particleName.toUpperCase());
        } catch (IllegalArgumentException ex) {
            particle = Particle.VILLAGER_HAPPY;
        }

        int spacing = plugin.getConfig().getInt("settings.claim-preview.spacing", 2); // every N blocks
        int duration = plugin.getConfig().getInt("settings.claim-preview.duration-seconds", 5); // seconds

        int cx = plot.getX() << 4;
        int cz = plot.getZ() << 4;
        int radius = plot.getRadius();

        // Calculate square bounds around the plot center
        int minX = cx - radius;
        int maxX = cx + radius;
        int minZ = cz - radius;
        int maxZ = cz + radius;

        int y = player.getLocation().getBlockY() + 1;

        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks++ > duration * 20 || !player.isOnline()) {
                    cancel();
                    return;
                }

                // Draw borders
                for (int x = minX; x <= maxX; x += spacing) {
                    world.spawnParticle(particle, x + 0.5, y, minZ + 0.5, 1, 0, 0, 0, 0);
                    world.spawnParticle(particle, x + 0.5, y, maxZ + 0.5, 1, 0, 0, 0, 0);
                }
                for (int z = minZ; z <= maxZ; z += spacing) {
                    world.spawnParticle(particle, minX + 0.5, y, z + 0.5, 1, 0, 0, 0, 0);
                    world.spawnParticle(particle, maxX + 0.5, y, z + 0.5, 1, 0, 0, 0, 0);
                }
            }
        }.runTaskTimer(plugin, 0L, 10L); // update every 0.5s
    }
}
