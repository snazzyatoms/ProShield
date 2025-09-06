// path: src/main/java/com/snazzyatoms/proshield/plots/PlayerJoinListener.java
package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.gui.GUIManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        // Auto-give compass to ops (or anyone with permission)
        if (e.getPlayer().isOp() || e.getPlayer().hasPermission("proshield.compass")) {
            if (!e.getPlayer().getInventory().containsAtLeast(GUIManager.createAdminCompass(), 1)) {
                e.getPlayer().getInventory().addItem(GUIManager.createAdminCompass());
            }
        }
    }
}
