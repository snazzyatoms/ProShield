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
 * ✅ Preserves prior logic
 * ✅ Uses per-claim PlotSettings for fire spread + ignite
 * ✅ Supports ignite cause: flint, lava, lightning
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

        if (plot == null) return; // wilderness → global config handles this

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

        // Spread (natural ignition)
        if (!s.isFireSpreadAllowed() &&
                (event.getCause() == BlockIgniteEvent.IgniteCause.SPREAD ||
                 event.getCause() == BlockIgniteEvent.IgniteCause.FIREBALL)) {
            event.setCancelled(true);
            messages.debug("&cFire ignition (spread) prevented in claim: " + plot.getDisplayNameSafe());
            return;
        }

        // Flint & Steel
        if (event.getCause() == BlockIgniteEvent.IgniteCause.FLINT_AND_STEEL &&
                !s.isFireFlintIgniteAllowed()) {
            event.setCancelled(true);
            messages.debug("&cFlint & Steel ignition prevented in claim: " + plot.getDisplayNameSafe());
            return;
        }

        // Lava ignition
        if (event.getCause() == BlockIgniteEvent.IgniteCause.LAVA &&
                !s.isFireLavaIgniteAllowed()) {
            event.setCancelled(true);
            messages.debug("&cLava ignition prevented in claim: " + plot.getDisplayNameSafe());
            return;
        }

        // Lightning ignition
        if (event.getCause() == BlockIgniteEvent.IgniteCause.LIGHTNING &&
                !s.isFireLightningIgniteAllowed()) {
            event.setCancelled(true);
            messages.debug("&cLightning ignition prevented in claim: " + plot.getDisplayNameSafe());
        }
    }
}
