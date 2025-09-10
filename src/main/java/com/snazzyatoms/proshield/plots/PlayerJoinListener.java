package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.GUIManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

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

        // Check config if compass auto-give is enabled
        if (!plugin.getConfig().getBoolean("autogive.compass-on-join", true)) {
            return;
        }

        boolean isAdmin = player.isOp() || player.hasPermission("proshield.admin");
        ItemStack compass = gui.createCompass(isAdmin);

        // Remove duplicate compasses to prevent stacking
        player.getInventory().forEach(item -> {
            if (item != null && gui.isProShieldCompass(item)) {
                player.getInventory().remove(item);
            }
        });

        // Try to add compass
        if (player.getInventory().firstEmpty() != -1) {
            player.getInventory().addItem(compass);
        } else {
            boolean dropIfFull = plugin.getConfig().getBoolean("compass.drop-if-full", true);
            if (dropIfFull) {
                player.getWorld().dropItemNaturally(player.getLocation(), compass);
                player.sendMessage(ChatColor.YELLOW + "Your inventory was full, so your ProShield compass was dropped at your feet.");
            }
        }

        // Send a welcome message
        if (isAdmin) {
            player.sendMessage(ChatColor.AQUA + "[ProShield] You have received the Admin Compass.");
        } else {
            player.sendMessage(ChatColor.GREEN + "[ProShield] You have received the ProShield Compass.");
        }
    }
}
