package com.snazzyatoms.proshield.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class PlayerGUI {

    private PlayerGUI() {}

    public static Inventory buildMain() {
        Inventory gui = Bukkit.createInventory(null, 9, ChatColor.DARK_GREEN + "ProShield Menu");

        gui.setItem(2, make(Material.GRASS_BLOCK, ChatColor.GREEN + "Create Claim"));
        gui.setItem(4, make(Material.PAPER, ChatColor.YELLOW + "Claim Info"));
        gui.setItem(6, make(Material.BARRIER, ChatColor.RED + "Remove Claim"));

        return gui;
    }

    public static void open(Player player) {
        player.openInventory(buildMain());
    }

    private static ItemStack make(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            item.setItemMeta(meta);
        }
        return item;
    }
}
