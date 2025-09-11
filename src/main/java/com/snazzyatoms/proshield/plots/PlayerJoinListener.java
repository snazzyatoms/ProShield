// src/main/java/com/snazzyatoms/proshield/plots/PlayerJoinListener.java
package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.compass.CompassManager;
import com.snazzyatoms.proshield.gui.GUIManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

/**
 * PlayerJoinListener
 *
 * ✅ Ensures players automatically receive the ProShield compass on join (first time).
 * ✅ Uses CompassManager (no static calls).
 * ✅ Prevents duplicate compasses by checking inventory first.
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

        // ✅ Only give compass if the player doesn’t already have one
        for (ItemStack item : player.getInventory().getContents()) {
            if (compassManager.isProShieldCompass(item)) {
                return; // Already has a compass → do nothing
            }
        }

        compassManager.giveCompass(player, player.isOp());
    }
}
