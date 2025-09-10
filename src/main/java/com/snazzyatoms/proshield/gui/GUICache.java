package com.snazzyatoms.proshield.gui;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * GUICache is responsible for storing and reusing inventories
 * so menus don't have to be rebuilt every time a player opens them.
 *
 * Supports per-player caching for all GUI types.
 */
public class GUICache {

    public enum MenuType {
        MAIN,
        TRUST,
        UNTRUST,
        ROLES,
        FLAGS,
        TRANSFER,
        ADMIN
    }

    // Map<PlayerUUID, Map<MenuType, Inventory>>
    private final Map<UUID, Map<MenuType, Inventory>> cache = new HashMap<>();

    /**
     * Store an inventory in cache.
     */
    public void put(Player player, MenuType type, Inventory inv) {
        cache.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>()).put(type, inv);
    }

    /**
     * Retrieve a cached inventory if it exists.
     */
    public Inventory get(Player player, MenuType type) {
        Map<MenuType, Inventory> menus = cache.get(player.getUniqueId());
        if (menus == null) return null;
        return menus.get(type);
    }

    /**
     * Clear cache for a specific player (e.g., on quit).
     */
    public void clear(Player player) {
        cache.remove(player.getUniqueId());
    }

    /**
     * Clear everything (e.g., on config reload).
     */
    public void clearAll() {
        cache.clear();
    }
}
