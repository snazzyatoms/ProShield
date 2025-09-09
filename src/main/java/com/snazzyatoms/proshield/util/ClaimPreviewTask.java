// path: src/main/java/com/snazzyatoms/proshield/util/ClaimPreviewTask.java
package com.snazzyatoms.proshield.util;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public final class ClaimPreviewTask {

    private static Plugin plugin;

    private ClaimPreviewTask() {}

    /** Call once during onEnable (you already do: ClaimPreviewTask.init(this)) */
    public static void init(Plugin pl) {
        plugin = pl;
    }

    /**
     * Optional helper: show a particle border around the player’s current chunk.
     * Safe no-op if plugin is not yet initialized.
     */
    public static void previewChunkBorder(Player player, int seconds) {
        if (plugin == null || player == null || !player.isOnline()) return;

        final World world = player.getWorld();
        final Chunk chunk = player.getLocation().getChunk();
        final int cx = chunk.getX() << 4;
        final int cz = chunk.getZ() << 4;

        // Vertical level to draw at (player’s feet clamped to 1..world max height)
        final int y = Math.max(1, Math.min(player.getLocation().getBlockY(), world.getMaxHeight() - 1));

        new BukkitRunnable() {
            int ticks = 0;
            final int maxTicks = Math.max(20, seconds * 20); // seconds -> ticks, minimum 1s

            @Override
            public void run() {
                if (!player.isOnline() || player.getWorld() != world) {
                    cancel();
                    return;
                }

                // Draw border: square of 16x16 around the chunk edges
                for (int i = 0; i < 16; i++) {
                    // North edge (z = cz)
                    spawn(world, cx + i + 0.5, y + 0.2, cz + 0.5);
                    // South edge (z = cz + 15)
                    spawn(world, cx + i + 0.5, y + 0.2, cz + 15.5);
                    // West edge (x = cx)
                    spawn(world, cx + 0.5, y + 0.2, cz + i + 0.5);
                    // East edge (x = cx + 15)
                    spawn(world, cx + 15.5, y + 0.2, cz + i + 0.5);
                }

                if ((ticks += 10) >= maxTicks) cancel(); // run ~2 times per second
            }
        }.runTaskTimer(plugin, 0L, 10L);
    }

    private static void spawn(World world, double x, double y, double z) {
        world.spawnParticle(Particle.END_ROD, x, y, z, 1, 0, 0, 0, 0);
    }
}
