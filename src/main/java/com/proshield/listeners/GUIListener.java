package com.snazzyatoms.proshield.listeners;

import com.snazzyatoms.proshield.managers.PlotManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class GUIListener implements Listener {

    private final PlotManager plotManager;

    public GUIListener(PlotManager plotManager) {
        this.plotManager = plotManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();

        // Only handle clicks inside our GUI
        if (event.getView().getTitle().equals(ChatColor.DARK_GREEN + "Claim Management")) {
            event.setCancelled(true); // prevent item pickup

            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || !clicked.hasItemMeta()) return;

            Material type = clicked.getType();

            switch (type) {
                case GRASS_BLOCK: // Create claim
                    plotManager.createClaim(player);
                    player.closeInventory();
                    player.sendMessage(ChatColor.GREEN + "✅ Claim created successfully!");
                    break;

                case BOOK: // Claim info
                    plotManager.getClaimInfo(player);
                    player.closeInventory();
                    player.sendMessage(ChatColor.AQUA + "📖 Showing claim information...");
                    break;

                case BARRIER: // Remove claim
                    plotManager.removeClaim(player);
                    player.closeInventory();
                    player.sendMessage(ChatColor.RED + "❌ Claim removed.");
                    break;

                default:
                    break;
            }
        }
    }
}
