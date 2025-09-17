// src/main/java/com/snazzyatoms/proshield/util/ClaimPreview.java
package com.snazzyatoms.proshield.util;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

/**
 * ClaimPreview
 *
 * - Visual particle boundary preview for a claim
 * - Repeats for a short configurable duration
 * - Integrated with ClaimPreviewTask (v1.2.5)
 */
public class ClaimPreview {

    /**
     * Show a preview of a claim boundary to a player.
     *
     * @param plugin ProShield instance
     * @param player Player to preview for
     * @param plot   The claim/plot to preview
     */
    public static void show(ProShield plugin, Player player, Plot plot) {
        if (plugin == null || player == null || plot == null) return;
        World world = Bukkit.getWorld(plot.getWorld());
        if (world == null) return;

        // Configurable settings
        int durationTicks = plugin.getConfig().getInt("claims.preview.duration-ticks", 100); // ~5s
        int intervalTicks = plugin.getConfig().getInt("claims.preview.interval-ticks", 20); // 1s
        String particleName = plugin.getConfig().getString("claims.preview.particle", "VILLAGER_HAPPY");

        Particle particle;
        try {
            particle = Particle.valueOf(particleName.toUpperCase());
        } catch (IllegalArgumentException e) {
            particle = Particle.VILLAGER_HAPPY; // fallback
        }

        int cx = plot.getX();
        int cz = plot.getZ();
        int minX = (cx << 4);
        int minZ = (cz << 4);
        int maxX = minX + 15;
        int maxZ = minZ + 15;

        // Cancel any existing preview for this player
        ClaimPreviewTask.cancel(player);

        // Start a repeating task for the preview
        BukkitTask task = new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    return;
                }

                int y = player.getLocation().getBlockY() + 1;

                for (int x = minX; x <= maxX; x++) {
                    world.spawnParticle(particle, x + 0.5, y, minZ + 0.5, 1, 0, 0, 0, 0);
                    world.spawnParticle(particle, x + 0.5, y, maxZ + 0.5, 1, 0, 0, 0, 0);
                }
                for (int z = minZ; z <= maxZ; z++) {
                    world.spawnParticle(particle, minX + 0.5, y, z + 0.5, 1, 0, 0, 0, 0);
                    world.spawnParticle(particle, maxX + 0.5, y, z + 0.5, 1, 0, 0, 0, 0);
                }

                ticks += intervalTicks;
                if (ticks >= durationTicks) {
                    cancel();
                    ClaimPreviewTask.cancel(player);
                }
            }
        }.runTaskTimer(plugin, 0L, intervalTicks);

        // Track this preview
        ClaimPreviewTask.track(player, task);
    }
}
