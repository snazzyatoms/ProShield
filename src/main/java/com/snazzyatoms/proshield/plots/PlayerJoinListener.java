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
 * ✅ Gives ProShield compass on join (if enabled in config).
 * ✅ Prevents duplicate compasses.
 * ✅ Admins get the Admin compass; players get the regular one.
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

        // ✅ Check config first
        if (!plugin.getConfig().getBoolean("settings.give-compass-on-join", true)) {
            return; // Disabled in config
        }

        // ✅ Prevent duplicates by checking inventory first
        for (ItemStack item : player.getInventory().getContents()) {
            if (compassManager.isProShieldCompass(item)) {
                return; // Already has a compass → do nothing
            }
        }

        // ✅ Give compass safely
        compassManager.giveCompass(player, player.isOp());
    }
}
