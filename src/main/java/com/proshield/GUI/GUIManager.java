package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.PlotManager; // ✅ updated import
import com.snazzyatoms.proshield.plots.Claim;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class GUIManager {
    private final ProShield plugin;
    private final PlotManager plotManager;

    public GUIManager(ProShield plugin, PlotManager plotManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
    }

    public void openClaimGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 9, ChatColor.GREEN + "Claim Management");

        // Create Claim
        ItemStack create = new ItemStack(Material.GRASS_BLOCK);
        ItemMeta createMeta = create.getItemMeta();
        createMeta.setDisplayName(ChatColor.GREEN + "Create Claim");
        create.setItemMeta(createMeta);

        // Claim Info
        ItemStack info = new ItemStack(Material.PAPER);
        ItemMeta infoMeta = info.getItemMeta();
        infoMeta.setDisplayName(ChatColor.YELLOW + "Claim Info");
        info.setItemMeta(infoMeta);

        // Remove Claim
        ItemStack remove = new ItemStack(Material.BARRIER);
        ItemMeta removeMeta = remove.getItemMeta();
        removeMeta.setDisplayName(ChatColor.RED + "Remove Claim");
        remove.setItemMeta(removeMeta);

        gui.setItem(2, create);
        gui.setItem(4, info);
        gui.setItem(6, remove);

        player.openInventory(gui);
    }

    // Action methods
    public void handleCreate(Player player) {
        // Example corners — replace with actual selection system later
        Claim claim = plotManager.createClaim(
            player,
            player.getLocation(),
            player.getLocation().add(10, 0, 10)
        );
        player.sendMessage(ChatColor.GREEN + "Created a new claim at your location!");
    }

    public void handleInfo(Player player) {
        Claim claim = plotManager.getClaimAt(player.getLocation());
        if (claim != null) {
            player.sendMessage(ChatColor.YELLOW + "This claim belongs to: " + claim.getOwner());
        } else {
            player.sendMessage(ChatColor.RED + "No claim found at your location.");
        }
    }

    public void handleRemove(Player player) {
        Claim claim = plotManager.getClaimAt(player.getLocation());
        if (claim != null && plotManager.removeClaim(player, claim)) {
            player.sendMessage(ChatColor.RED + "Claim removed successfully.");
        } else {
            player.sendMessage(ChatColor.RED + "No claim found or you don't own it.");
        }
    }
}
