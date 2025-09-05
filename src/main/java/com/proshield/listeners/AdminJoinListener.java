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
    public void onAdminJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Only give compass if player is OP
        if (player.isOp()) {
            ItemStack compass = new ItemStack(Material.COMPASS, 1);
            ItemMeta meta = compass.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("§bProShield Menu");
                compass.setItemMeta(meta);
            }

            // Add compass to inventory if not already present
            if (!player.getInventory().contains(Material.COMPASS)) {
                player.getInventory().addItem(compass);
            }

            // Debug log
            Bukkit.getLogger().info("[ProShield] Gave admin compass to " + player.getName());
        }
    }
}
