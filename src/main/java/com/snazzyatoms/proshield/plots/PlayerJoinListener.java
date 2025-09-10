package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.GUIManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class PlayerJoinListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plots;
    private final GUIManager gui;

    public PlayerJoinListener(ProShield plugin, PlotManager plots, GUIManager gui) {
        this.plugin = plugin;
        this.plots = plots;
        this.gui = gui;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Config check: autogive compass
        if (!plugin.getConfig().getBoolean("autogive.compass-on-join", true)) {
            return;
        }

        // Ops/admins always get the Admin Compass
        if (player.isOp() || player.hasPermission("proshield.admin")) {
            giveCompassIfMissing(player, gui.createAdminCompass());
        } else if (player.hasPermission("proshield.use")) {
            // Regular players get the Player Compass
            giveCompassIfMissing(player, gui.createPlayerCompass());
        }
    }

    /**
     * Gives the player a compass if they don't already have one.
     */
    private void giveCompassIfMissing(Player player, ItemStack compass) {
        if (player == null || compass == null) return;

        // Check if player already has a ProShield compass
        Inventory inv = player.getInventory();
        for (ItemStack item : inv.getContents()) {
            if (item != null && gui.isProShieldCompass(item)) {
                return; // Already has a compass
            }
        }

        // Add compass (or drop if full)
        if (inv.firstEmpty() != -1) {
            inv.addItem(compass);
        } else {
            player.getWorld().dropItemNaturally(player.getLocation(), compass);
            player.sendMessage("§e[ProShield] Your inventory was full, compass dropped at your feet.");
        }
    }
}
