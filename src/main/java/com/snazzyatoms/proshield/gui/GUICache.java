package com.snazzyatoms.proshield.gui;

import org.bukkit.inventory.Inventory;
import java.util.HashMap;
import java.util.UUID;

/**
 * Lightweight cache for ProShield GUIs to avoid regenerating
 * menus on every click. Keeps inventories per player in memory.
 */
public class GUICache {

    private final HashMap<UUID, Inventory> mainMenus = new HashMap<>();
    private final HashMap<UUID, Inventory> adminMenus = new HashMap<>();
    private final HashMap<UUID, Inventory> trustMenus = new HashMap<>();
    private final HashMap<UUID, Inventory> roleMenus = new HashMap<>();
    private final HashMap<UUID, Inventory> flagMenus = new HashMap<>();
    private final HashMap<UUID, Inventory> transferMenus = new HashMap<>();

    public void cacheMain(UUID uuid, Inventory inv) {
        mainMenus.put(uuid, inv);
    }

    public Inventory getMain(UUID uuid) {
        return mainMenus.get(uuid);
    }

    public void cacheAdmin(UUID uuid, Inventory inv) {
        adminMenus.put(uuid, inv);
    }

    public Inventory getAdmin(UUID uuid) {
        return adminMenus.get(uuid);
    }

    public void cacheTrust(UUID uuid, Inventory inv) {
        trustMenus.put(uuid, inv);
    }

    public Inventory getTrust(UUID uuid) {
        return trustMenus.get(uuid);
    }

    public void cacheRole(UUID uuid, Inventory inv) {
        roleMenus.put(uuid, inv);
    }

    public Inventory getRole(UUID uuid) {
        return roleMenus.get(uuid);
    }

    public void cacheFlag(UUID uuid, Inventory inv) {
        flagMenus.put(uuid, inv);
    }

    public Inventory getFlag(UUID uuid) {
        return flagMenus.get(uuid);
    }

    public void cacheTransfer(UUID uuid, Inventory inv) {
        transferMenus.put(uuid, inv);
    }

    public Inventory getTransfer(UUID uuid) {
        return transferMenus.get(uuid);
    }

    public void clear(UUID uuid) {
        mainMenus.remove(uuid);
        adminMenus.remove(uuid);
        trustMenus.remove(uuid);
        roleMenus.remove(uuid);
        flagMenus.remove(uuid);
        transferMenus.remove(uuid);
    }

    public void clearAll() {
        mainMenus.clear();
        adminMenus.clear();
        trustMenus.clear();
        roleMenus.clear();
        flagMenus.clear();
        transferMenus.clear();
    }
}
