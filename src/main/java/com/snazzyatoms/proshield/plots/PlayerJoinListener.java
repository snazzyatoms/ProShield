package com.snazzyatoms.proshield.plots;

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
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Example usage of plugin instance
        plugin.getLogger().info(event.getPlayer().getName() + " joined the server!");

        // You can also hook into PlotManager here if needed
        // plugin.getPlotManager().loadClaims(event.getPlayer());
    }
}
