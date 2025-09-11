package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockSpreadEvent;

/**
 * FireProtectionListener
 *
 * ✅ Constructor cleaned (PlotManager, MessagesUtil)
 * ✅ Uses per-claim PlotSettings (fireAllowed + fireSpreadAllowed)
 * ✅ Preserves wilderness/global behavior
 * ✅ Debug messages for admins
 */
public class FireProtectionListener implements Listener {

    private final PlotManager plots;
    private final MessagesUtil messages;

    public FireProtectionListener(PlotManager plots, MessagesUtil messages) {
        this.plots = plots;
        this.messages = messages;
    }

    @EventHandler(ignoreCancelled = true)
    public void onFireSpread(BlockSpreadEvent event) {
        Block block = event.getBlock();
        Chunk chunk = block.getChunk();
        Plot plot = plots.getPlot(chunk);

        if (plot == null) return; // wilderness – follow global config only

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
        Plot plot = plots.getPlot(chunk);

        if (plot == null) return; // wilderness – follow global config

        PlotSettings s = plot.getSettings();

        if (!s.isFireAllowed()) {
            event.setCancelled(true);
            messages.debug("&cFire ignition prevented in claim: " + plot.getDisplayNameSafe());
        }
    }
}
