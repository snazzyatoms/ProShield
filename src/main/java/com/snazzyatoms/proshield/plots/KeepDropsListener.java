package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Chunk;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDeathEvent;

/**
 * Ensures dropped items from player deaths persist inside claims.
 * Supports global + per-claim overrides.
 */
public class KeepDropsListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plotManager;

    public KeepDropsListener(ProShield plugin, PlotManager plotManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Chunk chunk = player.getLocation().getChunk();
        Plot plot = plotManager.getPlot(chunk);

        boolean keep = resolveKeepItems(plot);
        if (keep) {
            event.setKeepInventory(true);
            event.setKeepLevel(true);

            // Drop nothing if keep-inventory is active
            event.getDrops().clear();

            player.sendMessage(plugin.getPrefix() + "§aYour items and levels have been preserved in this claim.");
        }
    }

    /**
     * Utility: resolve whether keep-items is active here.
     */
    private boolean resolveKeepItems(Plot plot) {
        if (plot != null && plot.getKeepItemsEnabled() != null) {
            return plot.getKeepItemsEnabled();
        }
        return plugin.getConfig().getBoolean("claims.keep-items.enabled", false);
    }
}
