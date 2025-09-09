// path: src/main/java/com/snazzyatoms/proshield/util/ClaimPreview.java
package com.snazzyatoms.proshield.util;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Simple glowing particle border preview for the current chunk.
 * Call ClaimPreview.start(player, durationTicks) to show; it auto-stops after duration.
 */
public class ClaimPreview {

    private static final Map<UUID, BukkitTask> RUNNING = new HashMap<>();

    /** Start a temporary preview for the player's current chunk. */
    public static void start(Player player, long durationTicks) {
        stop(player); // ensure no duplicate
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(ProShield.getInstance(), new BorderRunnable(player), 0L, 10L);
        RUNNING.put(player.getUniqueId(), task);
        // auto-stop
        Bukkit.getScheduler().runTaskLater(ProShield.getInstance(), () -> stop(player), Math.max(40L, durationTicks));
    }

    /** Stop any running preview for this player. */
    public static void stop(Player player) {
        BukkitTask t = RUNNING.remove(player.getUniqueId());
        if (t != null) t.cancel();
    }

    /** The draw loop. */
    private static class BorderRunnable implements Runnable {
        private final UUID uuid;

        BorderRunnable(Player p) { this.uuid = p.getUniqueId(); }

        @Override
        public void run() {
            Player p = Bukkit.getPlayer(uuid);
            if (p == null || !p.isOnline()) {
                BukkitTask t = RUNNING.remove(uuid);
                if (t != null) t.cancel();
                return;
            }
            World w = p.getWorld();
            Chunk chunk = p.getLocation().getChunk();
            drawChunkOutline(w, chunk, p.getLocation().getY() + 0.2, p);
        }

        private void drawChunkOutline(World w, Chunk c, double y, Player viewer) {
            int bx = c.getX() << 4;
            int bz = c.getZ() << 4;
            // draw perimeter with particles
            for (int i = 0; i <= 16; i += 2) {
                spawn(w, bx + i, y, bz, viewer);
                spawn(w, bx + i, y, bz + 16, viewer);
                spawn(w, bx, y, bz + i, viewer);
                spawn(w, bx + 16, y, bz + i, viewer);
            }
        }

        private void spawn(World w, double x, double y, double z, Player viewer) {
            viewer.spawnParticle(Particle.VILLAGER_HAPPY, x + 0.5, y, z + 0.5, 1, 0, 0, 0, 0);
        }
    }
}
