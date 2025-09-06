// path: src/main/java/com/snazzyatoms/proshield/gui/GUIManager.java
package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class GUIManager {

    private final ProShield plugin;

    public GUIManager(ProShield plugin) {
        this.plugin = plugin;
    }

    /**
     * Open the main ProShield menu.
     */
    public void openMainMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 9, ChatColor.DARK_GREEN + "ProShield Menu");

        // Plot management button
        inv.setItem(2, createMenuItem(Material.GRASS_BLOCK, ChatColor.GREEN + "Manage Your Plots"));

        // Admin Compass button
        inv.setItem(4, createMenuItem(Material.COMPASS, ChatColor.YELLOW + "Admin Compass"));

        // Close button
        inv.setItem(6, createMenuItem(Material.BARRIER, ChatColor.RED + "Close Menu"));

        player.openInventory(inv);
    }

    /**
     * Open the player-specific plot GUI.
     */
    public void openPlayerGUI(Player player) {
        PlayerGUI gui = new PlayerGUI(plugin, player);
        gui.open();
    }

    /**
     * Utility method to create clickable menu items.
     */
    public static ItemStack createMenuItem(Material material, String displayName) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(displayName);
            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * Create the Admin Compass item.
     */
    public static ItemStack createAdminCompass() {
        return createMenuItem(Material.COMPASS, ChatColor.GOLD + "ProShield Admin Compass");
    }
}
