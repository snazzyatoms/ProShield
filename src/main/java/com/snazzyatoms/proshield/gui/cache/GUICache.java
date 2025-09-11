// src/main/java/com/snazzyatoms/proshield/gui/cache/GUICache.java
package com.snazzyatoms.proshield.gui.cache;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GUICache {

    public enum Origin { PLAYER, ADMIN }

    private final Map<UUID, Inventory> playerMenus = new HashMap<>();
    private final Map<UUID, Inventory> adminMenus = new HashMap<>();
    private final Map<UUID, Origin> origins = new HashMap<>();

    public void setPlayerMenu(Player player, Inventory inv) {
        playerMenus.put(player.getUniqueId(), inv);
        origins.put(player.getUniqueId(), Origin.PLAYER);
    }

    public void setAdminMenu(Player player, Inventory inv) {
        adminMenus.put(player.getUniqueId(), inv);
        origins.put(player.getUniqueId(), Origin.ADMIN);
    }

    public boolean isPlayerMenu(UUID uuid, Inventory inv) {
        return inv.equals(playerMenus.get(uuid));
    }

    public boolean isAdminMenu(UUID uuid, Inventory inv) {
        return inv.equals(adminMenus.get(uuid));
    }

    public Origin getOrigin(UUID uuid) {
        return origins.getOrDefault(uuid, Origin.PLAYER);
    }

    public void clearCache() {
        playerMenus.clear();
        adminMenus.clear();
        origins.clear();
    }
}
