package com.snazzyatoms.proshield.gui;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GUICache {

    private final Map<UUID, Inventory> playerMenus = new ConcurrentHashMap<>();

    /**
     * Cache a player's inventory GUI.
     */
    public void set(Player player, Inventory inv) {
        playerMenus.put(player.getUniqueId(), inv);
    }

    /**
     * Get a cached inventory for the player.
     */
    public Inventory get(Player player) {
        return playerMenus.get(player.getUniqueId());
    }

    /**
     * Remove cached GUI for a player.
     */
    public void invalidate(Player player) {
        playerMenus.remove(player.getUniqueId());
    }

    /**
     * Clear all caches.
     */
    public void clearAll() {
        playerMenus.clear();
    }
}
