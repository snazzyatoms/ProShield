package com.snazzyatoms.proshield.gui.cache;

import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * GUICache
 *
 * - Tracks open menus for each player (so listeners know context).
 * - Extendable for later caching (role selections, paging, etc.).
 */
public class GUICache {

    // Maps <Player UUID, menuKey>
    private final Map<UUID, String> openMenus = new ConcurrentHashMap<>();

    /**
     * Record which menu a player has open.
     * @param playerId player UUID
     * @param menuKey short key (ex: "main", "flags", "admin")
     */
    public void setOpenMenu(UUID playerId, String menuKey) {
        if (playerId != null && menuKey != null) {
            openMenus.put(playerId, menuKey);
        }
    }

    /**
     * Retrieve the last recorded menu for a player.
     * @param playerId player UUID
     * @return menu key (or null if none)
     */
    public String getOpenMenu(UUID playerId) {
        if (playerId == null) return null;
        return openMenus.get(playerId);
    }

    /**
     * Remove player from cache (ex: when they close inv or logout).
     * @param playerId player UUID
     */
    public void clearOpenMenu(UUID playerId) {
        if (playerId != null) {
            openMenus.remove(playerId);
        }
    }

    /**
     * Convenience: clear when Player object is available.
     */
    public void clearOpenMenu(Player player) {
        if (player != null) {
            clearOpenMenu(player.getUniqueId());
        }
    }
}
