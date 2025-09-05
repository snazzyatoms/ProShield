package com.snazzyatoms.proshield.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class GUIManager {

    private final String GUI_TITLE = ChatColor.BLUE + "ProShield Claim Manager";

    public GUIManager(Object plugin) {
        // Plugin reference not required for now, placeholder for future expansions
    }

    public void openClaimGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 9, GUI_TITLE);

        gui.setItem(2, createMenuItem(Material.GRASS_BLOCK, ChatColor.GREEN + "Create Claim", "Claim this area"));
        gui.setItem(4, createMenuItem(Material.PAPER, ChatColor.YELLOW + "Claim Info", "View info about your claim"));
        gui.setItem(6, createMenuItem(Material.BARRIER, ChatColor.RED + "Remove Claim", "Remove your claim"));

        player.openInventory(gui);
    }

    private ItemStack createMenuItem(Material material, String name, String lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(java.util.Collections.singletonList(ChatColor.GRAY + lore));
            item.setItemMeta(meta);
        }
        return item;
    }
}
