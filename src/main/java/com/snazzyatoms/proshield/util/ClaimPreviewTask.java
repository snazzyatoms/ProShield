// src/main/java/com/snazzyatoms/proshield/util/ClaimPreviewTask.java
package com.snazzyatoms.proshield.util;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

/**
 * ClaimPreviewTask
 *
 * - Centralized manager for all active ClaimPreview particle tasks
 * - Supports per-player preview tracking & cancellation
 * - Cleans up safely on plugin disable (v1.2.5)
 */
public class ClaimPreviewTask {

    // Active previews: Player UUID → task
    private static final Map<UUID, BukkitTask> activePreviews = new HashMap<>();

    /**
     * Start tracking a preview task for a player.
     * If a preview is already running for this player, it will be cancelled first.
     */
    public static void track(Player player, BukkitTask task) {
        if (player == null || task == null) return;

        // Cancel any existing preview for this player
        cancel(player);

        activePreviews.put(player.getUniqueId(), task);
    }

    /**
     * Cancel a preview for a specific player.
     */
    public static void cancel(Player player) {
        if (player == null) return;

        BukkitTask existing = activePreviews.remove(player.getUniqueId());
        if (existing != null && !existing.isCancelled()) {
            existing.cancel();
        }
    }

    /**
     * Cancel all previews (e.g., on plugin disable).
     */
    public static void cancelAll() {
        for (BukkitTask task : activePreviews.values()) {
            if (task != null && !task.isCancelled()) {
                task.cancel();
            }
        }
        activePreviews.clear();
    }

    /**
     * Utility: Check if a player currently has a preview running.
     */
    public static boolean hasPreview(Player player) {
        return player != null && activePreviews.containsKey(player.getUniqueId());
    }
}
