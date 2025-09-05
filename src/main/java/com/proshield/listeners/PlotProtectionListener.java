package com.snazzyatoms.proshield.listeners;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class AdminJoinListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (player.isOp() || player.hasPermission("proshield.admin")) {
            boolean hasCompass = player.getInventory().contains(Material.COMPASS);

            if (!hasCompass) {
                ItemStack compass = new ItemStack(Material.COMPASS);
                ItemMeta meta = compass.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(ChatColor.GOLD + "🛡 ProShield Menu");
                    List<String> lore = new ArrayList<>();
                    lore.add(ChatColor.YELLOW + "Right-click to open ProShield GUI");
                    meta.setLore(lore);
                    compass.setItemMeta(meta);
                }

                player.getInventory().addItem(compass);
                player.sendMessage(ChatColor.GREEN + "[ProShield] Compass added for testing.");
            }
        }
    }
}
