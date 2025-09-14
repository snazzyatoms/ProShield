// src/main/java/com/snazzyatoms/proshield/util/ClaimPreviewTask.java
package com.snazzyatoms.proshield.util;

import org.bukkit.Bukkit;

import java.util.HashSet;
import java.util.Set;

public final class ClaimPreviewTask {
    private static final Set<Integer> TASKS = new HashSet<>();

    private ClaimPreviewTask() {}

    public static void track(int taskId) {
        TASKS.add(taskId);
    }

    /** Optional global cancel if you ever need it */
    public static void cancelAll() {
        for (int id : TASKS) {
            Bukkit.getScheduler().cancelTask(id);
        }
        TASKS.clear();
    }
}
