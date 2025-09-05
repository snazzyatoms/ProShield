package com.snazzyatoms.proshield.listeners;

import com.snazzyatoms.proshield.managers.GUIManager;
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

        String title = event.getView().getTitle();
        if (!GUIManager.TITLE.equals(title)) return;

        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        Material type = clicked.getType();
        switch (type) {
            case GRASS_BLOCK: { // Create
                boolean ok = plotManager.createClaim(player);
                if (ok) player.sendMessage(ChatColor.GREEN + "✅ Claim created successfully.");
                else    player.sendMessage(ChatColor.RED + "You already have a claim.");
                player.closeInventory();
                break;
            }
            case BOOK: { // Info
                String info = plotManager.getClaimInfo(player);
                if (info == null) player.sendMessage(ChatColor.RED + "You don’t have a claim yet.");
                else              player.sendMessage(ChatColor.AQUA + "📖 " + info);
                player.closeInventory();
                break;
            }
            case BARRIER: { // Remove
                boolean removed = plotManager.removeClaim(player);
                if (removed) player.sendMessage(ChatColor.RED + "❌ Claim removed.");
                else         player.sendMessage(ChatColor.RED + "No claim to remove.");
                player.closeInventory();
                break;
            }
            default:
                break;
        }
    }
}
