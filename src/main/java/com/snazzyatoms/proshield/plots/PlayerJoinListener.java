package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.ProShield;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    private final ProShield plugin;

    public PlayerJoinListener(ProShield plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        var p = e.getPlayer();
        if (!p.hasPermission("proshield.admin")) return;

        // Give the ProShield compass ONLY if they don't have OUR compass already
        if (!GUIManager.hasProShieldCompass(p)) {
            p.getInventory().addItem(GUIManager.createAdminCompass());
        }
    }
}
