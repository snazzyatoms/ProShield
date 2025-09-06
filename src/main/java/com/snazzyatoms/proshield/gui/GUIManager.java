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

    public void openMainGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 9, ChatColor.DARK_GREEN + "ProShield Menu");

        gui.setItem(2, makeItem(Material.GRASS_BLOCK, ChatColor.GREEN + "Create Claim"));
        gui.setItem(4, makeItem(Material.BOOK, ChatColor.YELLOW + "Claim Info"));
        gui.setItem(6, makeItem(Material.BARRIER, ChatColor.RED + "Remove Claim"));

        player.openInventory(gui);
    }

    public static ItemStack createAdminCompass() {
        ItemStack compass = new ItemStack(Material.COMPASS);
        ItemMeta meta = compass.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.AQUA + "ProShield Compass");
            compass.setItemMeta(meta);
        }
        return compass;
    }

    private ItemStack makeItem(Material mat, String name) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            item.setItemMeta(meta);
        }
        return item;
    }
}
