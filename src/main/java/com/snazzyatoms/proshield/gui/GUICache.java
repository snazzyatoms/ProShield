package com.snazzyatoms.proshield.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;

public class GUICache {

    private final Map<String, Inventory> cache = new HashMap<>();

    public enum GUIType {
        MAIN,
        ADMIN,
        PLAYER
    }

    public Inventory get(GUIType type) {
        return cache.get(type.name());
    }

    public void put(GUIType type, Inventory inv) {
        cache.put(type.name(), inv);
    }

    public void open(Player player, GUIType type) {
        Inventory inv = cache.get(type.name());
        if (inv != null) {
            player.openInventory(inv);
        } else {
            player.sendMessage("§cProShield GUI not available. Please try again later.");
        }
    }

    public void clear() {
        cache.clear();
    }

    public void preload() {
        // optional: preload empty GUIs for safety
        cache.put(GUIType.MAIN.name(), Bukkit.createInventory(null, 54, "ProShield Menu"));
        cache.put(GUIType.ADMIN.name(), Bukkit.createInventory(null, 54, "ProShield Admin"));
        cache.put(GUIType.PLAYER.name(), Bukkit.createInventory(null, 54, "ProShield Player"));
    }
}
