package com.snazzyatoms.proshield.listeners;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class AdminJoinListener implements Listener {

    private final ProShield plugin;

    public AdminJoinListener(ProShield plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onAdminJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (player.isOp()) {
            ItemStack compass = new ItemStack(Material.COMPASS, 1);
            ItemMeta meta = compass.getItemMeta();

            if (meta != null) {
                meta.setDisplayName("§bProShield Menu");
                compass.setItemMeta(meta);
            }

            // Give compass if they don't already have one
            if (!player.getInventory().contains(compass)) {
                player.getInventory().addItem(compass);
            }

            Bukkit.getLogger().info("Gave ProShield menu compass to admin: " + player.getName());
        }
    }
}
