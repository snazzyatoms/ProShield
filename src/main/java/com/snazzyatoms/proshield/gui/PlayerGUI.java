package com.snazzyatoms.proshield.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Simple UI builder for ProShield menus.
 * No PlotManager calls or claim logic here — that is handled in GUIListener.
 */
public final class PlayerGUI {

    private PlayerGUI() {}

    /**
     * Build the main ProShield menu (9 slots).
     * Slots:
     *  2 = Create Claim   (GRASS_BLOCK)
     *  4 = Claim Info     (PAPER)
     *  6 = Remove Claim   (BARRIER)
     */
    public static Inventory buildMain() {
        Inventory gui = Bukkit.createInventory(null, 9, ChatColor.DARK_GREEN + "ProShield Menu");

        gui.setItem(2, make(Material.GRASS_BLOCK, ChatColor.GREEN + "Create Claim"));
        gui.setItem(4, make(Material.PAPER, ChatColor.YELLOW + "Claim Info"));
        gui.setItem(6, make(Material.BARRIER, ChatColor.RED + "Remove Claim"));

        return gui;
    }

    /** Convenience: open the main GUI for a player. */
    public static void open(Player player) {
        player.openInventory(buildMain());
    }

    /** Utility to make a named item. */
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
