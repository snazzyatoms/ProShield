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
     * Opens a GUI menu defined in config.yml under gui.menus.<menuKey>
     */
    public void openMenu(Player player, String menuKey) {
        ConfigurationSection menus = plugin.getConfig().getConfigurationSection("gui.menus");
        if (menus == null) {
            plugin.getLogger().warning("No menus defined in config.yml (gui.menus missing).");
            return;
        }

        ConfigurationSection menu = menus.getConfigurationSection(menuKey);
        if (menu == null) {
            plugin.getLogger().warning("Menu not found in config.yml: " + menuKey);
            return;
        }

        String title = ChatColor.translateAlternateColorCodes('&', menu.getString("title", "&7Menu"));

        // Auto-size inventory if not provided (round up to nearest multiple of 9)
        int size = menu.getInt("size", -1);
        if (size <= 0) {
            ConfigurationSection items = menu.getConfigurationSection("items");
            int maxSlot = 0;
            if (items != null) {
                for (String key : items.getKeys(false)) {
                    try {
                        int slot = Integer.parseInt(key);
                        if (slot > maxSlot) maxSlot = slot;
                    } catch (NumberFormatException ignored) {}
                }
            }
            size = Math.min(54, ((maxSlot / 9) + 1) * 9); // nearest multiple of 9, capped at 54
            if (size <= 0) size = 27; // fallback default
        }

        Inventory inv = Bukkit.createInventory(null, size, title);

        ConfigurationSection items = menu.getConfigurationSection("items");
        if (items != null) {
            for (String slotKey : items.getKeys(false)) {
                try {
                    int slot = Integer.parseInt(slotKey);
                    if (slot < 0 || slot >= size) {
                        plugin.getLogger().warning("Invalid slot " + slot + " in menu " + menuKey);
                        continue;
                    }

                    ConfigurationSection itemSec = items.getConfigurationSection(slotKey);
                    if (itemSec == null) continue;

                    // --- Permission Check ---
                    String permission = itemSec.getString("permission");
                    if (permission != null && !permission.isBlank() && !player.hasPermission(permission)) {
                        continue; // skip this item if player lacks permission
                    }

                    Material mat = Material.matchMaterial(itemSec.getString("material", "BARRIER"));
                    if (mat == null) mat = Material.BARRIER;

                    String name = ChatColor.translateAlternateColorCodes('&', itemSec.getString("name", ""));
                    List<String> lore = formatLore(itemSec.getStringList("lore"));

                    ItemStack item = new ItemStack(mat);
                    ItemMeta meta = item.getItemMeta();
                    if (meta != null) {
                        meta.setDisplayName(name);
                        meta.setLore(lore);
                        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                        item.setItemMeta(meta);
                    }

                    inv.setItem(slot, item);
                } catch (NumberFormatException e) {
                    plugin.getLogger().warning("Invalid slot in menu " + menuKey + ": " + slotKey);
                }
            }
        }

        player.openInventory(inv);
    }

    /**
     * Format lore lines with color codes.
     */
    private List<String> formatLore(List<String> input) {
        List<String> out = new ArrayList<>();
        for (String line : input) {
            out.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        return out;
    }
}
