package com.snazzyatoms.proshield.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class CompassListener implements Listener {

    @EventHandler
    public void onCompassUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (player.getInventory().getItemInMainHand() == null) return;

        ItemStack item = player.getInventory().getItemInMainHand();

        // Must be the Admin Compass
        if (item.getType() != Material.COMPASS) return;
        if (!item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !ChatColor.stripColor(meta.getDisplayName()).equalsIgnoreCase("Admin Compass")) return;

        // Cancel normal compass behavior
        event.setCancelled(true);

        // Open Claim Management GUI
        openClaimManagementGUI(player);
    }

    public static void openClaimManagementGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, ChatColor.DARK_GREEN + "Claim Management");

        // Claim Button
        ItemStack claim = new ItemStack(Material.GRASS_BLOCK);
        ItemMeta claimMeta = claim.getItemMeta();
        if (claimMeta != null) {
            claimMeta.setDisplayName(ChatColor.GREEN + "Create Claim");
            claim.setItemMeta(claimMeta);
        }

        // Info Button
        ItemStack info = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = info.getItemMeta();
        if (infoMeta != null) {
            infoMeta.setDisplayName(ChatColor.YELLOW + "Claim Info");
            info.setItemMeta(infoMeta);
        }

        // Unclaim Button
        ItemStack unclaim = new ItemStack(Material.BARRIER);
        ItemMeta unclaimMeta = unclaim.getItemMeta();
        if (unclaimMeta != null) {
            unclaimMeta.setDisplayName(ChatColor.RED + "Remove Claim");
            unclaim.setItemMeta(unclaimMeta);
        }

        gui.setItem(11, claim);
        gui.setItem(13, info);
        gui.setItem(15, unclaim);

        player.openInventory(gui);
    }
}
