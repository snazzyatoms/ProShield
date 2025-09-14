// src/main/java/com/snazzyatoms/proshield/gui/AdminGUIManager.java
package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Locale;

public class AdminGUIManager {

    private final ProShield plugin;

    public AdminGUIManager(ProShield plugin) {
        this.plugin = plugin;
    }

    /**
     * Opens an admin GUI menu by key (matches config under gui.menus.<key>)
     */
    public void openMenu(Player player, String key) {
        ConfigurationSection menuConfig = plugin.getConfig().getConfigurationSection("gui.menus." + key);
        if (menuConfig == null) {
            player.sendMessage(ChatColor.RED + "Menu not found: " + key);
            return;
        }

        String title = ChatColor.translateAlternateColorCodes('&', menuConfig.getString("title", "&cAdmin Menu"));
        int size = menuConfig.getInt("size", 27);
        Inventory inv = Bukkit.createInventory(null, size, title);

        ConfigurationSection items = menuConfig.getConfigurationSection("items");
        if (items != null) {
            for (String slotKey : items.getKeys(false)) {
                int slot;
                try {
                    slot = Integer.parseInt(slotKey);
                } catch (NumberFormatException e) {
                    continue; // ignore invalid slots
                }

                ConfigurationSection itemCfg = items.getConfigurationSection(slotKey);
                if (itemCfg == null) continue;

                String materialName = itemCfg.getString("material", "BARRIER");
                ItemStack item;
                try {
                    item = new ItemStack(org.bukkit.Material.valueOf(materialName.toUpperCase(Locale.ROOT)));
                } catch (IllegalArgumentException ex) {
                    item = new ItemStack(org.bukkit.Material.BARRIER);
                }

                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    if (itemCfg.contains("name")) {
                        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', itemCfg.getString("name")));
                    }
                    if (itemCfg.contains("lore")) {
                        List<String> lore = itemCfg.getStringList("lore");
                        lore.replaceAll(line -> ChatColor.translateAlternateColorCodes('&', line));
                        meta.setLore(lore);
                    }
                    item.setItemMeta(meta);
                }

                inv.setItem(slot, item);
            }
        }

        player.openInventory(inv);
    }
}
