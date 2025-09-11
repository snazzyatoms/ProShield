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
 * ✅ Protects claims from fire spread, lava ignition, flint & steel, and lightning
 * ✅ Controlled by both global config + per-claim flags
 * ✅ Uses MessagesUtil for debug + player feedback
 */
public class FireProtectionListener implements Listener {

    private final PlotManager plots;
    private final MessagesUtil messages;

    public FireProtectionListener(PlotManager plots, MessagesUtil messages) {
        this.plots = plots;
        this.messages = messages;
    }

    /* -------------------------------------------------------
     * Fire Spread (natural or block-to-block)
     * ------------------------------------------------------- */
    @EventHandler(ignoreCancelled = true)
    public void onFireSpread(BlockSpreadEvent event) {
        Block block = event.getBlock();
        Chunk chunk = block.getChunk();
        Plot plot = plots.getPlot(chunk);
        if (plot == null) return; // wilderness → controlled by global config

        if (!plot.getSettings().isFireSpreadAllowed()) {
            event.setCancelled(true);
            messages.debug("&cFire spread blocked in claim: " + plot.getDisplayNameSafe());
        }
    }

    /* -------------------------------------------------------
     * Fire Ignition (lava, lightning, flint & steel)
     * ------------------------------------------------------- */
    @EventHandler(ignoreCancelled = true)
    public void onFireIgnite(BlockIgniteEvent event) {
        Block block = event.getBlock();
        Chunk chunk = block.getChunk();
        Plot plot = plots.getPlot(chunk);
        if (plot == null) return;

        PlotSettings s = plot.getSettings();

        switch (event.getCause()) {
            case FLINT_AND_STEEL -> {
                if (!s.isFireAllowed()) {
                    event.setCancelled(true);
                    messages.debug("&cFlint & steel fire blocked in claim: " + plot.getDisplayNameSafe());
                }
            }
            case LAVA -> {
                if (!s.isFireAllowed()) {
                    event.setCancelled(true);
                    messages.debug("&cLava ignition blocked in claim: " + plot.getDisplayNameSafe());
                }
            }
            case LIGHTNING -> {
                if (!s.isFireAllowed()) {
                    event.setCancelled(true);
                    messages.debug("&cLightning fire blocked in claim: " + plot.getDisplayNameSafe());
                }
            }
            default -> {
                if (!s.isFireAllowed()) {
                    event.setCancelled(true);
                    messages.debug("&cFire ignition blocked in claim: " + plot.getDisplayNameSafe());
                }
            }
        }
    }
}
