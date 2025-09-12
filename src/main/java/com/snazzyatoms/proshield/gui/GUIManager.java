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

/**
 * GUIManager
 * - Dynamically builds menus from config.yml (gui.menus)
 * - Paired with GUIListener to handle click actions
 * - Future-proof: size, titles, items all come from config
 */
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
            plugin.getLogger().warning("[ProShield] No menus defined in config.yml (gui.menus missing).");
            return;
        }

        ConfigurationSection menu = menus.getConfigurationSection(menuKey);
        if (menu == null) {
            plugin.getLogger().warning("[ProShield] Menu not found in config.yml: " + menuKey);
            return;
        }

        String title = ChatColor.translateAlternateColorCodes('&',
                menu.getString("title", "&7Menu"));
        int size = menu.getInt("size", 27); // default 27 slots

        Inventory inv = Bukkit.createInventory(null, size, title);

        ConfigurationSection items = menu.getConfigurationSection("items");
        if (items != null) {
            for (String slotKey : items.getKeys(false)) {
                int slot;
                try {
                    slot = Integer.parseInt(slotKey);
                } catch (NumberFormatException e) {
                    plugin.getLogger().warning("[ProShield] Invalid slot in menu " + menuKey + ": " + slotKey);
                    continue;
                }

                ConfigurationSection itemSec = items.getConfigurationSection(slotKey);
                if (itemSec == null) continue;

                // Material
                Material mat = Material.matchMaterial(itemSec.getString("material", "BARRIER"));
                if (mat == null) mat = Material.BARRIER;

                // Name & Lore
                String name = ChatColor.translateAlternateColorCodes('&',
                        itemSec.getString("name", ""));
                List<String> lore = formatLore(itemSec.getStringList("lore"));

                // Build item
                ItemStack item = new ItemStack(mat);
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    if (!name.isEmpty()) meta.setDisplayName(name);
                    if (!lore.isEmpty()) meta.setLore(lore);
                    meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                    item.setItemMeta(meta);
                }

                inv.setItem(slot, item);
            }
        }

        player.openInventory(inv);
    }

    /**
     * Utility: format lore lines with color codes.
     */
    private List<String> formatLore(List<String> input) {
        List<String> out = new ArrayList<>();
        if (input != null) {
            for (String line : input) {
                out.add(ChatColor.translateAlternateColorCodes('&', line));
            }
        }
        return out;
    }
}
