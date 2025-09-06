// path: src/main/java/com/snazzyatoms/proshield/gui/GUIListener.java
package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class GUIListener implements Listener {

    private final ProShield plugin;

    public GUIListener(ProShield plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();

        // Make sure inventory belongs to our GUI
        if (event.getView().getTitle() == null) return;
        String title = ChatColor.stripColor(event.getView().getTitle());

        if (title.equalsIgnoreCase("ProShield Menu")) {
            event.setCancelled(true); // Prevent taking items from GUI

            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) {
                return;
            }

            // Handle clicks
            switch (clicked.getType()) {
                case GRASS_BLOCK:
                    player.sendMessage(ChatColor.GREEN + "Opening plot management...");
                    // open plot GUI
                    plugin.getGuiManager().openPlayerGUI(player);
                    break;

                case COMPASS:
                    player.sendMessage(ChatColor.YELLOW + "You clicked the Admin Compass!");
                    // Add admin compass logic if needed
                    break;

                case BARRIER:
                    player.closeInventory();
                    player.sendMessage(ChatColor.RED + "Closed ProShield menu.");
                    break;

                default:
                    player.sendMessage(ChatColor.GRAY + "This option is not implemented yet.");
                    break;
            }
        }
    }
}
