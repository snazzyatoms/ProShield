package com.snazzyatoms.proshield.managers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class GUIManager {

    private final PlotManager plotManager;

    public GUIManager(PlotManager plotManager) {
        this.plotManager = plotManager;
    }

    /**
     * Opens the Claim Management GUI for a player
     */
    public void openClaimGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 9, ChatColor.DARK_GREEN + "Claim Management");

        // Create Claim button
        ItemStack create = new ItemStack(Material.GRASS_BLOCK);
        ItemMeta createMeta = create.getItemMeta();
        if (createMeta != null) {
            createMeta.setDisplayName(ChatColor.GREEN + "Create Claim");
            create.setItemMeta(createMeta);
        }

        // Claim Info button
        ItemStack info = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = info.getItemMeta();
        if (infoMeta != null) {
            infoMeta.setDisplayName(ChatColor.AQUA + "Claim Info");
            info.setItemMeta(infoMeta);
        }

        // Remove Claim button
        ItemStack remove = new ItemStack(Material.BARRIER);
        ItemMeta removeMeta = remove.getItemMeta();
        if (removeMeta != null) {
            removeMeta.setDisplayName(ChatColor.RED + "Remove Claim");
            remove.setItemMeta(removeMeta);
        }

        // Place items in GUI
        gui.setItem(2, create);
        gui.setItem(4, info);
        gui.setItem(6, remove);

        // Open GUI
        player.openInventory(gui);
    }

    /**
     * Handle clicks inside the Claim Management GUI
     */
    public void handleGUIClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getView().getTitle() == null || !ChatColor.stripColor(event.getView().getTitle()).equalsIgnoreCase("Claim Management")) {
            return;
        }

        event.setCancelled(true); // Prevent taking items

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta() || !clicked.getItemMeta().hasDisplayName()) return;

        String name = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());

        switch (name.toLowerCase()) {
            case "create claim" -> {
                plotManager.createClaim(player);
                player.closeInventory();
                player.sendMessage(ChatColor.GREEN + "✅ Claim created successfully!");
            }
            case "claim info" -> {
                String info = plotManager.getClaimInfo(player);
                player.closeInventory();
                player.sendMessage(ChatColor.AQUA + "📖 Claim Info: " + info);
            }
            case "remove claim" -> {
                plotManager.removeClaim(player);
                player.closeInventory();
                player.sendMessage(ChatColor.RED + "❌ Claim removed successfully!");
            }
            default -> player.sendMessage(ChatColor.GRAY + "Unknown action.");
        }
    }
}
