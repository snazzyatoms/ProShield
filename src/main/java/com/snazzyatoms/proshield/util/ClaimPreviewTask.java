// src/main/java/com/snazzyatoms/proshield/util/ClaimPreviewTask.java
package com.snazzyatoms.proshield.util;

import org.bukkit.Bukkit;

import java.util.HashSet;
import java.util.Set;

/**
 * ClaimPreviewTask
 *
 * ✅ Handles tracking and stopping of all active ClaimPreview particle tasks.
 */
public class ClaimPreviewTask {

    private static final Set<Integer> activeTasks = new HashSet<>();

    /** Track a new scheduled task ID */
    public static void track(int taskId) {
        activeTasks.add(taskId);
    }

    /** Cancel all tracked tasks (e.g., on plugin disable) */
    public static void cancelAll() {
        for (int id : activeTasks) {
            Bukkit.getScheduler().cancelTask(id);
        }
        activeTasks.clear();
    }
}
