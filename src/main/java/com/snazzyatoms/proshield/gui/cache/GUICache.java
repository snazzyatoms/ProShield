// src/main/java/com/snazzyatoms/proshield/gui/cache/GUICache.java
package com.snazzyatoms.proshield.gui.cache;

import com.snazzyatoms.proshield.gui.GUIManager;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * GUICache - lightweight cache for storing temporary player GUI states.
 *
 * ✅ Keeps prior logic
 * ✅ Fixed so it no longer declares ProShield as a public class
 */
public class GUICache {

    private final GUIManager guiManager;
    private final Map<UUID, Object> playerCache = new ConcurrentHashMap<>();

    public GUICache(GUIManager guiManager) {
        this.guiManager = guiManager;
    }

    /** Store arbitrary GUI-related state for a player. */
    public void put(Player player, Object state) {
        if (player != null) {
            playerCache.put(player.getUniqueId(), state);
        }
    }

    /** Retrieve cached state for a player. */
    public Object get(Player player) {
        return (player != null) ? playerCache.get(player.getUniqueId()) : null;
    }

    /** Remove cached state for a player. */
    public void remove(Player player) {
        if (player != null) {
            playerCache.remove(player.getUniqueId());
        }
    }

    /** Clear all cached GUI states. */
    public void clearCache() {
        playerCache.clear();
    }

    /** Check if player has cached GUI state. */
    public boolean has(Player player) {
        return player != null && playerCache.containsKey(player.getUniqueId());
    }

    public GUIManager getGuiManager() {
        return guiManager;
    }
}
