package com.snazzyatoms.proshield.gui.cache;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * GUICache
 *
 * Tracks which players have which GUIs open.
 * - Supports Player menus
 * - Supports Admin menus
 * Prevents conflicts and makes listeners simpler.
 */
public class GUICache {

    private final Map<UUID, Inventory> playerMenus = new HashMap<>();
    private final Map<UUID, Inventory> adminMenus = new HashMap<>();

    /* -------------------------
     * Player Menus
     * ------------------------- */
    public void setPlayerMenu(Player player, Inventory inv) {
        playerMenus.put(player.getUniqueId(), inv);
    }

    public boolean isPlayerMenu(UUID uuid, Inventory inv) {
        return playerMenus.containsKey(uuid) && playerMenus.get(uuid).equals(inv);
    }

    public void clearPlayerMenu(UUID uuid) {
        playerMenus.remove(uuid);
    }

    /* -------------------------
     * Admin Menus
     * ------------------------- */
    public void setAdminMenu(Player player, Inventory inv) {
        adminMenus.put(player.getUniqueId(), inv);
    }

    public boolean isAdminMenu(UUID uuid, Inventory inv) {
        return adminMenus.containsKey(uuid) && adminMenus.get(uuid).equals(inv);
    }

    public void clearAdminMenu(UUID uuid) {
        adminMenus.remove(uuid);
    }

    /* -------------------------
     * Global
     * ------------------------- */
    public void clearAll(UUID uuid) {
        playerMenus.remove(uuid);
        adminMenus.remove(uuid);
    }

    public void clearCache() {
        playerMenus.clear();
        adminMenus.clear();
    }
}
