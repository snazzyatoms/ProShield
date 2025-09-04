package com.proshield.listeners;

import com.proshield.ProShield;
import com.proshield.managers.GUIManager;
import com.proshield.managers.PlotManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerGUIListener implements Listener {

    private final ProShield plugin;

    public PlayerGUIListener(ProShield plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();

        if (event.getView().getTitle().equals(ChatColor.GREEN + "ProShield - Plot Menu")) {
            event.setCancelled(true); // prevent item stealing

            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

            Material type = clickedItem.getType();
            PlotManager plotManager = plugin.getPlotManager();
            GUIManager guiManager = plugin.getGuiManager();

            switch (type) {
                case GRASS_BLOCK:
                    player.sendMessage(ChatColor.GREEN + "Attempting to claim your first plot...");
                    plotManager.claimPlot(player);
                    break;

                case EMERALD:
                    player.sendMessage(ChatColor.YELLOW + "Opening expansion options...");
                    // TODO: open expansion wizard
                    break;

                case BOOK:
                    player.sendMessage(ChatColor.AQUA + "Launching Claim Wizard...");
                    // TODO: step-by-step claim tutorial
                    break;

                case WRITABLE_BOOK:
                    player.sendMessage(ChatColor.BLUE + "Opening Quick Guide...");
                    // TODO: open guide page
                    break;

                case MAP:
                    player.sendMessage(ChatColor.LIGHT_PURPLE + "Previewing your plot boundaries...");
                    plotManager.previewPlot(player);
                    break;

                default:
                    break;
            }
        }
    }
}
