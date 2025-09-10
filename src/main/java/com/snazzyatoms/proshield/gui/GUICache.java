package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * GUICache handles caching of all ProShield GUIs
 * for better performance and smoother player experience.
 *
 * v1.2.5:
 *  - Player GUI updated with Trust/Untrust, Roles, Flags, Transfer Ownership
 *  - Admin GUI includes Reload, Debug, Compass toggle, Spawn Guard, TP tools
 *  - Caching ensures GUIs don’t rebuild on every open/click
 *  - Reload support clears and refreshes menus
 */
public class GUICache {

    private final ProShield plugin;
    private final Map<String, Inventory> cachedInventories = new HashMap<>();
    private final Set<String> validTitles = new HashSet<>();

    public GUICache(ProShield plugin) {
        this.plugin = plugin;
        preloadTitles();
    }

    /**
     * Preload all GUI titles so we can quickly check if an inventory belongs to ProShield
     */
    private void preloadTitles() {
        validTitles.clear();

        validTitles.add("§bProShield Menu");
        validTitles.add("§cProShield Admin");
        validTitles.add("§aTrust Players");
        validTitles.add("§6Roles Manager");
        validTitles.add("§dClaim Flags");
        validTitles.add("§eTransfer Ownership");
    }

    /**
     * Check if the given title belongs to a ProShield GUI
     */
    public boolean isProShieldGUI(String title) {
        return validTitles.contains(title);
    }

    /**
     * Store a cached inventory by title
     */
    public void put(String title, Inventory inv) {
        cachedInventories.put(title, inv);
    }

    /**
     * Get a cached inventory by title
     */
    public Inventory get(String title) {
        return cachedInventories.get(title);
    }

    /**
     * Check if an inventory is cached
     */
    public boolean has(String title) {
        return cachedInventories.containsKey(title);
    }

    /**
     * Clear all caches (e.g., on reload)
     */
    public void clear() {
        cachedInventories.clear();
        plugin.getLogger().info("[ProShield] GUI cache cleared.");
    }

    /**
     * Cleanup for a specific player (optional for per-player caches later)
     */
    public void cleanup(Player player) {
        // Currently not caching per-player GUIs, but this is future-proof
    }

    /**
     * Force refresh of a menu (rebuilds it and updates the cache)
     */
    public void refreshMenu(String title, Inventory inv) {
        cachedInventories.put(title, inv);
    }

    /**
     * Utility: build or fetch a cached menu.
     * If it exists, returns cache. Otherwise, creates a fresh one.
     */
    public Inventory getOrBuild(String title, Runnable builder) {
        if (has(title)) {
            return get(title);
        } else {
            builder.run(); // builder should put() into cache
            return get(title);
        }
    }
}
