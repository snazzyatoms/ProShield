package com.snazzyatoms.proshield.listeners;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class GUIListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        // Only handle clicks in our Claim Management GUI
        if (event.getView().getTitle() == null || 
            !ChatColor.stripColor(event.getView().getTitle()).equalsIgnoreCase("Claim Management")) {
            return;
        }

        event.setCancelled(true); // Prevent item pickup

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        ItemMeta meta = clicked.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;

        String displayName = ChatColor.stripColor(meta.getDisplayName());

        switch (displayName.toLowerCase()) {
            case "create claim":
                player.closeInventory();
                player.sendMessage(ChatColor.GREEN + "[ProShield] Starting claim creation...");
                // TODO: Hook into PlotManager to handle claim logic
                break;

            case "claim info":
                player.closeInventory();
                player.sendMessage(ChatColor.YELLOW + "[ProShield] Showing claim info...");
                // TODO: Show actual claim data from PlotManager
                break;

            case "remove claim":
                player.closeInventory();
                player.sendMessage(ChatColor.RED + "[ProShield] Removing claim...");
                // TODO: Hook into PlotManager to remove claim
                break;

            default:
                break;
        }
    }
}
