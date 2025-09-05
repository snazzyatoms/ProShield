package com.snazzyatoms.proshield.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class AdminJoinListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Only give the item if they are OP
        if (player.isOp()) {
            // Create a compass with custom name
            ItemStack compass = new ItemStack(Material.COMPASS, 1);
            ItemMeta meta = compass.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("§aProShield Menu");
                compass.setItemMeta(meta);
            }

            // If they don’t already have it, give them one
            if (!player.getInventory().contains(compass)) {
                player.getInventory().addItem(compass);
            }

            // Debug logging
            Bukkit.getLogger().info("[ProShield] OP " + player.getName() + " received the ProShield menu compass.");
        }
    }
}
