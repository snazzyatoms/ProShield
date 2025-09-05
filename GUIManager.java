package com.snazzyatoms.proshield.managers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class GUIManager {

    /**
     * Opens the Claim Management GUI for a player
     */
    public void openClaimGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, ChatColor.DARK_GREEN + "Claim Management");

        // Create Claim button
        ItemStack createClaim = new ItemStack(Material.GRASS_BLOCK);
        ItemMeta createMeta = createClaim.getItemMeta();
        createMeta.setDisplayName(ChatColor.GREEN + "Create Claim");
        createMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Protect this area from griefers",
                ChatColor.YELLOW + "Click to create a new claim."
        ));
        createClaim.setItemMeta(createMeta);

        // Claim Info button
        ItemStack claimInfo = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = claimInfo.getItemMeta();
        infoMeta.setDisplayName(ChatColor.AQUA + "Claim Info");
        infoMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "View details about your claim",
                ChatColor.YELLOW + "Click to see claim owner, size, etc."
        ));
        claimInfo.setItemMeta(infoMeta);

        // Remove Claim button
        ItemStack removeClaim = new ItemStack(Material.BARRIER);
        ItemMeta removeMeta = removeClaim.getItemMeta();
        removeMeta.setDisplayName(ChatColor.RED + "Remove Claim");
        removeMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Delete this claim permanently",
                ChatColor.YELLOW + "Click to remove your claim."
        ));
        removeClaim.setItemMeta(removeMeta);

        // Fill empty slots with gray glass for polish
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(" ");
        filler.setItemMeta(fillerMeta);

        for (int i = 0; i < gui.getSize(); i++) {
            gui.setItem(i, filler);
        }

        // Place items in nice positions
        gui.setItem(11, createClaim); // left
        gui.setItem(13, claimInfo);   // center
        gui.setItem(15, removeClaim); // right

        // Open GUI for player
        player.openInventory(gui);
    }
}
