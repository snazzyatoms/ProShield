package com.snazzyatoms.proshield.listeners;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class AdminJoinListener implements Listener {

    @EventHandler
    public void onAdminJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Check if player is OP (server operator)
        if (player.isOp()) {
            // Check if player already has the compass
            boolean hasCompass = player.getInventory().contains(Material.COMPASS);

            if (!hasCompass) {
                ItemStack compass = new ItemStack(Material.COMPASS, 1);
                ItemMeta meta = compass.getItemMeta();

                if (meta != null) {
                    meta.setDisplayName("§bProShield Menu");
                    compass.setItemMeta(meta);
                }

                // Give compass to admin
                player.getInventory().addItem(compass);
                player.sendMessage("§a[ProShield] You have been given a ProShield compass.");
            }
        }
    }
}
