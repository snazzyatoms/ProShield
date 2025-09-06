package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerJoinListener implements Listener {

    private final ProShield plugin;

    public PlayerJoinListener(ProShield plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (!e.getPlayer().hasPermission("proshield.admin")) return;
        boolean has = e.getPlayer().getInventory().contains(Material.COMPASS);
        if (!has) {
            ItemStack compass = GUIManager.createAdminCompass();
            e.getPlayer().getInventory().addItem(compass);
        }
    }
}
