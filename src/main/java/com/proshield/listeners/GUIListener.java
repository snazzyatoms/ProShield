package com.snazzyatoms.proshield.listeners;

import com.snazzyatoms.proshield.gui.GUIManager;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class GUIListener implements Listener {
    private final GUIManager guiManager;

    public GUIListener(GUIManager guiManager) {
        this.guiManager = guiManager;
    }

    // Open GUI when compass is right-clicked
    @EventHandler
    public void onCompassUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item != null && item.getType() == Material.COMPASS &&
            item.hasItemMeta() &&
            ChatColor.stripColor(item.getItemMeta().getDisplayName()).equalsIgnoreCase("ProShield Admin Compass")) {
            event.setCancelled(true);
            guiManager.openClaimGUI(player);
        }
    }

    // Handle GUI button clicks
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        if (event.getView().getTitle().equals(ChatColor.GREEN + "Claim Management")) {
            event.setCancelled(true);

            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || !clicked.hasItemMeta()) return;

            String name = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
            switch (name) {
                case "Create Claim":
                    guiManager.handleCreate(player);
                    break;
                case "Claim Info":
                    guiManager.handleInfo(player);
                    break;
                case "Remove Claim":
                    guiManager.handleRemove(player);
                    break;
            }
            player.closeInventory();
        }
    }
}
