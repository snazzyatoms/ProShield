package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockSpreadEvent;

/**
 * Handles fire protections inside claims and wilderness.
 *
 * ✅ Uses PlotSettings.fireSpreadAllowed for per-claim fire toggle
 */
public class FireProtectionListener implements Listener {

    private final PlotManager plotManager;
    private final MessagesUtil messages;

    public FireProtectionListener(PlotManager plotManager, MessagesUtil messages) {
        this.plotManager = plotManager;
        this.messages = messages;
    }

    @EventHandler(ignoreCancelled = true)
    public void onFireSpread(BlockSpreadEvent event) {
        Block block = event.getBlock();
        Chunk chunk = block.getChunk();
        Plot plot = plotManager.getPlot(chunk);

        if (plot == null) return; // wilderness → handled globally

        PlotSettings s = plot.getSettings();
        if (!s.isFireSpreadAllowed()) {
            event.setCancelled(true);
            messages.debug("&cFire spread prevented in claim: " + plot.getDisplayNameSafe());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onFireIgnite(BlockIgniteEvent event) {
        Block block = event.getBlock();
        Chunk chunk = block.getChunk();
        Plot plot = plotManager.getPlot(chunk);

        if (plot == null) return;

        PlotSettings s = plot.getSettings();
        if (!s.isFireSpreadAllowed()) {
            event.setCancelled(true);
            messages.debug("&cFire ignition prevented in claim: " + plot.getDisplayNameSafe());
        }
    }
}
