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

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * GUIManager (config-driven)
 *
 * - Builds menus dynamically from config.yml
 * - Provides access to "actions" for GUIListener
 */
public class GUIManager {

    private final ProShield plugin;

    public GUIManager(ProShield plugin) {
        this.plugin = plugin;
    }

    /**
     * Opens a menu by name (from config).
     */
    public void openMenu(Player player, String menuName) {
        ConfigurationSection menu = plugin.getConfig().getConfigurationSection("gui.menus." + menuName);
        if (menu == null) {
            plugin.getLogger().warning("Menu not found: " + menuName);
            return;
        }

        String title = ChatColor.translateAlternateColorCodes('&', menu.getString("title", "&cMenu"));
        int size = menu.getInt("size", 27);
        Inventory inv = Bukkit.createInventory(null, size, title);

        ConfigurationSection items = menu.getConfigurationSection("items");
        if (items != null) {
            for (String key : items.getKeys(false)) {
                try {
                    int slot = Integer.parseInt(key);
                    ConfigurationSection sec = items.getConfigurationSection(key);

                    String matName = sec.getString("material", "BARRIER");
                    Material mat = Material.matchMaterial(matName);
                    if (mat == null) mat = Material.BARRIER;

                    String name = ChatColor.translateAlternateColorCodes('&', sec.getString("name", ""));
                    List<String> lore = sec.getStringList("lore");
                    lore.replaceAll(line -> ChatColor.translateAlternateColorCodes('&', line));

                    ItemStack item = new ItemStack(mat);
                    ItemMeta meta = item.getItemMeta();
                    if (meta != null) {
                        meta.setDisplayName(name);
                        meta.setLore(lore);
                        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                        item.setItemMeta(meta);
                    }

                    inv.setItem(slot, item);
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to load GUI item in menu " + menuName + " at slot " + key);
                }
            }
        }

        player.openInventory(inv);
    }

    /**
     * Checks if the title belongs to a ProShield menu.
     */
    public boolean isProShieldMenu(String title) {
        ConfigurationSection menus = plugin.getConfig().getConfigurationSection("gui.menus");
        if (menus == null) return false;
        for (String key : menus.getKeys(false)) {
            String t = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfig().getString("gui.menus." + key + ".title", "")));
            if (t.equalsIgnoreCase(ChatColor.stripColor(title))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get the actions of an item in a given menu.
     */
    public List<String> getItemActions(String menuTitle, int slot) {
        ConfigurationSection menus = plugin.getConfig().getConfigurationSection("gui.menus");
        if (menus == null) return Collections.emptyList();

        for (String key : menus.getKeys(false)) {
            String t = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfig().getString("gui.menus." + key + ".title", "")));
            if (t.equalsIgnoreCase(ChatColor.stripColor(menuTitle))) {
                String path = "gui.menus." + key + ".items." + slot + ".actions";
                return plugin.getConfig().getStringList(path);
            }
        }

        return Collections.emptyList();
    }
}
