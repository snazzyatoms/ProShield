package com.proshield.managers;

import com.proshield.ProShield;
import com.proshield.gui.PlayerGUI;
import com.proshield.gui.AdminGUI;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;

public class GUIManager {

    private final ProShield plugin;

    public GUIManager(ProShield plugin) {
        this.plugin = plugin;
    }

    /**
     * Opens the Player GUI (Plot Compass menu)
     */
    public void openPlayerGUI(Player player) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            PlayerGUI playerGUI = new PlayerGUI(plugin, player);
            player.openInventory(playerGUI.getInventory());
        });
    }

    /**
     * Opens the Admin GUI (Admin Compass menu)
     */
    public void openAdminGUI(Player player) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (player.hasPermission("proshield.admin")) {
                AdminGUI adminGUI = new AdminGUI(plugin, player);
                player.openInventory(adminGUI.getInventory());
            } else {
                openPlayerGUI(player); // fallback: open normal GUI
            }
        });
    }
}
