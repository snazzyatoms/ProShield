package com.snazzyatoms.proshield.util;

import org.bukkit.Bukkit;

import java.util.HashSet;
import java.util.Set;

public class ClaimPreviewTask {
    private static final Set<Integer> TASKS = new HashSet<>();
    public static void stopAll() {
        TASKS.forEach(Bukkit.getScheduler()::cancelTask);
        TASKS.clear();
    }
    public static void track(int id) { TASKS.add(id); }
}
