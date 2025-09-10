package com.snazzyatoms.proshield.gui;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple in-memory GUI cache for ProShield menus.
 * Prevents lag from regenerating inventories every time a player clicks.
 */
public class GUICache {

    private final Map<String, Inventory> playerMainCache = new HashMap<>();
    private final Map<String, Inventory> playerFlagsCache = new HashMap<>();
    private final Map<String, Inventory> playerRolesCache = new HashMap<>();
    private final Map<String, Inventory> playerTrustCache = new HashMap<>();
    private final Map<String, Inventory> adminMainCache = new HashMap<>();

    /** Get cached Player Main GUI */
    public Inventory getPlayerMain(Player player) {
        return playerMainCache.get(player.getName());
    }

    /** Save Player Main GUI */
    public void setPlayerMain(Player player, Inventory inv) {
        playerMainCache.put(player.getName(), inv);
    }

    /** Get cached Player Flags GUI */
    public Inventory getPlayerFlags(Player player) {
        return playerFlagsCache.get(player.getName());
    }

    public void setPlayerFlags(Player player, Inventory inv) {
        playerFlagsCache.put(player.getName(), inv);
    }

    /** Get cached Player Roles GUI */
    public Inventory getPlayerRoles(Player player) {
        return playerRolesCache.get(player.getName());
    }

    public void setPlayerRoles(Player player, Inventory inv) {
        playerRolesCache.put(player.getName(), inv);
    }

    /** Get cached Player Trust GUI */
    public Inventory getPlayerTrust(Player player) {
        return playerTrustCache.get(player.getName());
    }

    public void setPlayerTrust(Player player, Inventory inv) {
        playerTrustCache.put(player.getName(), inv);
    }

    /** Get cached Admin Main GUI */
    public Inventory getAdminMain(Player player) {
        return adminMainCache.get(player.getName());
    }

    public void setAdminMain(Player player, Inventory inv) {
        adminMainCache.put(player.getName(), inv);
    }

    /** Clear all caches (called on /proshield reload) */
    public void clear() {
        playerMainCache.clear();
        playerFlagsCache.clear();
        playerRolesCache.clear();
        playerTrustCache.clear();
        adminMainCache.clear();
    }
}
