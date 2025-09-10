package com.snazzyatoms.proshield.gui;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * GUICache holds lightweight mappings between GUI buttons (ItemStacks)
 * and their corresponding actions. This prevents heavy config lookups
 * or meta parsing on every click.
 *
 * All menus (Player & Admin) register their buttons here.
 */
public class GUICache {

    // Cache of Item → action string
    private static final Map<String, String> itemActionCache = new HashMap<>();

    // =========================================================
    // REGISTRATION
    // =========================================================

    /**
     * Registers a mapping between an ItemStack and an action string.
     */
    public static void register(ItemStack item, String action) {
        if (item == null || action == null) return;

        String key = makeKey(item);
        itemActionCache.put(key, action.toLowerCase());
    }

    /**
     * Bulk registration for convenience.
     */
    public static void registerAll(Map<ItemStack, String> items) {
        for (Map.Entry<ItemStack, String> entry : items.entrySet()) {
            register(entry.getKey(), entry.getValue());
        }
    }

    // =========================================================
    // LOOKUP
    // =========================================================

    /**
     * Gets the action string for a clicked item.
     *
     * @param clicked ItemStack clicked
     * @return action string, or null if not recognized
     */
    public static String getAction(ItemStack clicked) {
        if (clicked == null) return null;

        String key = makeKey(clicked);
        return itemActionCache.getOrDefault(key, null);
    }

    /**
     * Debugging utility to see all registered mappings.
     */
    public static Map<String, String> dumpCache() {
        return Collections.unmodifiableMap(itemActionCache);
    }

    // =========================================================
    // UTIL
    // =========================================================

    /**
     * Generates a stable key for caching.
     * Uses Material + DisplayName if present.
     */
    private static String makeKey(ItemStack item) {
        Material type = item.getType();
        ItemMeta meta = item.getItemMeta();

        if (meta != null && meta.hasDisplayName()) {
            return type.name() + "|" + meta.getDisplayName().toLowerCase();
        }
        return type.name();
    }

    /**
     * Clears all cache entries (e.g., on reload).
     */
    public static void clear() {
        itemActionCache.clear();
    }
}
