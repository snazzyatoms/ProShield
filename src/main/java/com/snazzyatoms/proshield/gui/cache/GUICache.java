package com.snazzyatoms.proshield.gui.cache;

import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * GUICache
 *
 * ✅ Tracks both Player and Admin menus per-player
 * ✅ Prevents duplicate inventories from being created repeatedly
 * ✅ Safe clearing when players quit
 */
public class GUICache {

    private final Map<UUID, Inventory> playerMenus = new HashMap<>();
    private final Map<UUID, Inventory> adminMenus = new HashMap<>();

    /* -------------------------------------------------------
     * PLAYER MENU
     * ------------------------------------------------------- */
    public void setPlayerMenu(UUID uuid, Inventory inv) {
        playerMenus.put(uuid, inv);
    }

    public Inventory getPlayerMenu(UUID uuid) {
        return playerMenus.get(uuid);
    }

    /* -------------------------------------------------------
     * ADMIN MENU
     * ------------------------------------------------------- */
    public void setAdminMenu(UUID uuid, Inventory inv) {
        adminMenus.put(uuid, inv);
    }

    public Inventory getAdminMenu(UUID uuid) {
        return adminMenus.get(uuid);
    }

    /* -------------------------------------------------------
     * CLEANUP
     * ------------------------------------------------------- */
    public void clear(UUID uuid) {
        playerMenus.remove(uuid);
        adminMenus.remove(uuid);
    }

    public void clearAll() {
        playerMenus.clear();
        adminMenus.clear();
    }
}
