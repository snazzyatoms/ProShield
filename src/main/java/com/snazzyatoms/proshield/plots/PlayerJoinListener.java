// src/main/java/com/snazzyatoms/proshield/plots/PlayerJoinListener.java
package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.compass.CompassManager;
import com.snazzyatoms.proshield.gui.GUIManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * PlayerJoinListener
 *
 * ✅ Ensures players automatically receive the ProShield compass on join.
 * ✅ Uses CompassManager (no static calls).
 * ✅ Prevents duplicate compasses.
 */
public class PlayerJoinListener implements Listener {

    private final ProShield plugin;
    private final GUIManager guiManager;
    private final PlotManager plotManager;
    private final CompassManager compassManager;

    public PlayerJoinListener(ProShield plugin,
                              GUIManager guiManager,
                              PlotManager plotManager,
                              CompassManager compassManager) {
        this.plugin = plugin;
        this.guiManager = guiManager;
        this.plotManager = plotManager;
        this.compassManager = compassManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // ✅ Give compass safely (avoid duplicates)
        compassManager.giveCompass(player, player.isOp());
    }
}
