// ===========================================
// PlayerJoinListener.java
// ===========================================
package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.gui.GUIManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {
    private final GUIManager gui;

    public PlayerJoinListener(GUIManager gui) {
        this.gui = gui;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        gui.giveCompass(e.getPlayer(), e.getPlayer().isOp());
    }
}
