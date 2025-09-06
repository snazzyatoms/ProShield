// path: src/main/java/com/snazzyatoms/proshield/gui/PlayerGUI.java
package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class PlayerGUI {

    private final ProShield plugin;
    private final Player player;

    public PlayerGUI(ProShield plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    /**
     * Opens the player's plot management GUI.
     */
    public void open() {
        Inventory inv = Bukkit.createInventory(null, 9, ChatColor.GREEN + "Your Plots");

        // Claim plot button
        inv.setItem(2, GUIManager.createMenuItem(Material.EMERALD_BLOCK, ChatColor.GREEN + "Claim Current Chunk"));

        // Unclaim plot button
        inv.setItem(4, GUIManager.createMenuItem(Material.REDSTONE_BLOCK, ChatColor.RED + "Unclaim Current Chunk"));

        // Back button to main menu
        inv.setItem(6, GUIManager.createMenuItem(Material.ARROW, ChatColor.YELLOW + "Back to Main Menu"));

        player.openInventory(inv);
    }

    /**
     * Handle clicks from this GUI (called by GUIListener).
     */
    public void handleClick(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return;

        String name = ChatColor.stripColor(item.getItemMeta().getDisplayName());
        switch (name.toLowerCase()) {
            case "claim current chunk":
                plugin.getPlotManager().claimChunk(player);
                player.sendMessage(ChatColor.GREEN + "You claimed this chunk!");
                break;

            case "unclaim current chunk":
                plugin.getPlotManager().unclaimChunk(player);
                player.sendMessage(ChatColor.RED + "You unclaimed this chunk!");
                break;

            case "back to main menu":
                plugin.getGuiManager().openMainMenu(player);
                break;

            default:
                player.sendMessage(ChatColor.GRAY + "Option not implemented yet.");
                break;
        }
    }
}
