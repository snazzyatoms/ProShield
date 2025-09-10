package com.snazzyatoms.proshield.gui;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lightweight per-player GUI cache.
 * - Caches Inventories per player + menu id (GUIManager.MenuId)
 * - Fully invalidated on /proshield reload via invalidateAll()
 * - Also supports targeted invalidation per player
 */
public final class GUICache {

    public enum Scope {
        PLAYER, GLOBAL
    }

    private static final GUICache INSTANCE = new GUICache();

    // Key: player UUID + menu id
    private static final class Key {
        private final UUID uuid;
        private final String menuId;

        private Key(UUID uuid, String menuId) {
            this.uuid = uuid;
            this.menuId = menuId;
        }

        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Key)) return false;
            Key key = (Key) o;
            return Objects.equals(uuid, key.uuid) && Objects.equals(menuId, key.menuId);
        }

        @Override public int hashCode() { return Objects.hash(uuid, menuId); }
    }

    private final Map<Key, Inventory> cache = new ConcurrentHashMap<>();

    private GUICache() {}

    public static GUICache get() {
        return INSTANCE;
    }

    /**
     * Get cached inventory if present.
     */
    public Inventory get(Player player, String menuId) {
        return cache.get(new Key(player.getUniqueId(), menuId));
    }

    /**
     * Put inventory into cache.
     */
    public void put(Player player, String menuId, Inventory inv) {
        if (player == null || inv == null) return;
        cache.put(new Key(player.getUniqueId(), menuId), inv);
    }

    /**
     * Invalidate all menus for one player.
     */
    public void invalidate(Player player) {
        if (player == null) return;
        UUID id = player.getUniqueId();
        cache.keySet().removeIf(k -> k.uuid.equals(id));
    }

    /**
     * Invalidate everything (used on /proshield reload).
     */
    public void invalidateAll() {
        cache.clear();
    }

    public int size() {
        return cache.size();
    }
}
