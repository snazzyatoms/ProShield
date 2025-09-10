package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.GUIManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Handles player join events:
 * - Auto-gives compass if enabled in config.
 * - Differentiates between Player Compass and Admin Compass.
 */
public class PlayerJoinListener implements Listener {

    private final ProShield plugin;
    private final GUIManager gui;

    public PlayerJoinListener(ProShield plugin, GUIManager gui) {
        this.plugin = plugin;
        this.gui = gui;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (!plugin.getConfig().getBoolean("autogive.compass-on-join", true)) {
            return; // Disabled in config
        }

        boolean isAdmin = player.isOp() || player.hasPermission("proshield.admin");

        // Get correct compass
        ItemStack compass = gui.createCompass(isAdmin);

        // Check if player already has one
        boolean hasCompass = player.getInventory().containsAtLeast(compass, 1);
        if (hasCompass) {
            return; // Already has compass
        }

        // Give compass (drop if inventory full, based on config)
        if (player.getInventory().firstEmpty() == -1) {
            if (plugin.getConfig().getBoolean("compass.drop-if-full", true)) {
                player.getWorld().dropItemNaturally(player.getLocation(), compass);
                player.sendMessage(ChatColor.YELLOW + "Your ProShield compass was dropped because your inventory was full.");
            }
        } else {
            player.getInventory().addItem(compass);
        }

        Bukkit.getScheduler().runTaskLater(plugin, player::updateInventory, 2L);
    }
}
