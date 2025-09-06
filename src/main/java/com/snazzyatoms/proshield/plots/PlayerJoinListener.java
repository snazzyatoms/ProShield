// path: src/main/java/com/snazzyatoms/proshield/plots/PlayerJoinListener.java
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

    public PlayerJoinListener(ProShield plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        FileConfiguration ac = plugin.getAdminConfig();
        if (!ac.getBoolean("defaults.give-compass-on-join", true)) return;
        if (!p.hasPermission("proshield.compass")) return;

        // if player lacks a ProShield Compass, give one in configured slot if empty
        boolean has = p.getInventory().containsAtLeast(GUIManager.createAdminCompass(), 1);
        if (!has) {
            int slot = Math.max(0, Math.min(8, ac.getInt("defaults.compass-slot", 8)));
            ItemStack compass = GUIManager.createAdminCompass();
            if (p.getInventory().getItem(slot) == null) p.getInventory().setItem(slot, compass);
            else p.getInventory().addItem(compass);
        }
    }
}
