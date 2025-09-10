package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.GUIManager;
import org.bukkit.configuration.file.FileConfiguration;
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
        FileConfiguration config = plugin.getConfig();

        boolean giveOnJoin = config.getBoolean("autogive.compass-on-join", true);
        if (!giveOnJoin) return;

        boolean isAdmin = player.isOp() || player.hasPermission("proshield.admin");

        // Prevent duplication: check if player already has the compass
        if (alreadyHasCompass(player, isAdmin)) return;

        ItemStack compass = gui.createCompass(isAdmin);
        if (player.getInventory().firstEmpty() != -1) {
            player.getInventory().addItem(compass);
        } else {
            boolean dropIfFull = config.getBoolean("compass.drop-if-full", true);
            if (dropIfFull) {
                player.getWorld().dropItemNaturally(player.getLocation(), compass);
            }
        }
    }

    private boolean alreadyHasCompass(Player player, boolean isAdmin) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null) continue;
            if (gui.isProShieldCompass(item)) {
                // If player has the wrong type (admin vs player), replace it
                boolean itemIsAdmin = gui.isAdminCompass(item);
                if (itemIsAdmin != isAdmin) {
                    player.getInventory().remove(item);
                    return false; // force re-give
                }
                return true; // already correct compass
            }
        }
        return false;
    }
}
