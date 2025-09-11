package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.compass.CompassManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * PlayerJoinListener
 *
 * ✅ Automatically gives compass on join
 * ✅ Uses CompassManager.giveCompass for consistency
 * ✅ Prevents duplicates by removing old ProShield compasses first
 */
public class PlayerJoinListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plotManager;

    public PlayerJoinListener(ProShield plugin, com.snazzyatoms.proshield.gui.GUIManager guiManager, PlotManager plotManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Only auto-give if enabled in config
        if (!plugin.getConfig().getBoolean("compass.auto-give", true)) {
            return;
        }

        // Give or refresh the player/admin compass
        CompassManager.giveCompass(player, true);
    }
}
