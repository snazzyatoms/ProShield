package com.snazzyatoms.proshield.cache;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;

/**
 * Lightweight cache for GUIs and items to reduce rebuild overhead.
 * Ensures menus don’t hammer config or rebuild inventories every click.
 */
public class GUICache {

    private final Map<String, Inventory> guiCache = new HashMap<>();
    private final Map<String, ItemStack> itemCache = new HashMap<>();

    // ==========================
    // INVENTORY CACHING
    // ==========================

    public void storeGUI(String key, Inventory inv) {
        guiCache.put(key, inv);
    }

    public Inventory getGUI(String key) {
        return guiCache.get(key);
    }

    public void clearGUIs() {
        guiCache.clear();
    }

    // ==========================
    // ITEM CACHING
    // ==========================

    public void storeItem(String key, ItemStack item) {
        itemCache.put(key, item);
    }

    public ItemStack getItem(String key) {
        return itemCache.get(key);
    }

    public void clearItems() {
        itemCache.clear();
    }

    // ==========================
    // FACTORY HELPERS
    // ==========================

    public static ItemStack createItem(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
            if (lore.length > 0) {
                java.util.List<String> loreList = new java.util.ArrayList<>();
                for (String line : lore) {
                    loreList.add(ChatColor.translateAlternateColorCodes('&', line));
                }
                meta.setLore(loreList);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    public static Inventory createInventory(String title, int size) {
        return Bukkit.createInventory(null, size, ChatColor.translateAlternateColorCodes('&', title));
    }
}
