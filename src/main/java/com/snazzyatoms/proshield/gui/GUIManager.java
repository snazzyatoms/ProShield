// src/main/java/com/snazzyatoms/proshield/gui/GUIManager.java
package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class GUIManager {

    private final ProShield plugin;

    public GUIManager(ProShield plugin) {
        this.plugin = plugin;
    }

    /**
     * Open a menu by config key.
     */
    public void openMenu(Player player, String menuKey) {
        Inventory inv = buildMenu(menuKey);
        if (inv != null) {
            player.openInventory(inv);
        } else {
            plugin.getLogger().warning("Tried to open missing menu: " + menuKey);
        }
    }

    /**
     * Builds an inventory menu dynamically from config.yml
     */
    public Inventory buildMenu(String menuKey) {
        ConfigurationSection menus = plugin.getConfig().getConfigurationSection("ui.menus");
        if (menus == null || !menus.isConfigurationSection(menuKey)) return null;

        ConfigurationSection menu = menus.getConfigurationSection(menuKey);
        String title = ChatColor.translateAlternateColorCodes('&', menu.getString("title", "Menu"));
        int size = menu.getInt("size", 27);

        Inventory inv = Bukkit.createInventory(null, size, title);
        ConfigurationSection items = menu.getConfigurationSection("items");
        if (items == null) return inv;

        for (String key : items.getKeys(false)) {
            ConfigurationSection item = items.getConfigurationSection(key);
            if (item == null) continue;

            int slot = item.getInt("slot", -1);
            Material mat = Material.matchMaterial(item.getString("material", "BARRIER"));
            if (mat == null || slot < 0 || slot >= size) continue;

            String name = ChatColor.translateAlternateColorCodes('&', item.getString("name", key));
            List<String> lore = new ArrayList<>();
            for (String l : item.getStringList("lore")) {
                lore.add(ChatColor.translateAlternateColorCodes('&', l));
            }

            ItemStack stack = new ItemStack(mat);
            ItemMeta meta = stack.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(name);
                if (!lore.isEmpty()) meta.setLore(lore);
                if (item.getBoolean("hide-attributes", true)) {
                    meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                }
                stack.setItemMeta(meta);
            }

            inv.setItem(slot, stack);
        }

        return inv;
    }
}
