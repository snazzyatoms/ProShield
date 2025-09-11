package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
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
 * ✅ Preserves claim fire/spread/ignite protections
 * ✅ Adds ignite sources: flint & steel, lava, lightning
 * ✅ Configurable globally + per-claim
 */
public class FireProtectionListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plots;
    private final MessagesUtil messages;

    public FireProtectionListener(ProShield plugin, PlotManager plots, MessagesUtil messages) {
        this.plugin = plugin;
        this.plots = plots;
        this.messages = messages;
    }

    /** Handle natural fire spread */
    @EventHandler(ignoreCancelled = true)
    public void onFireSpread(BlockSpreadEvent event) {
        Block block = event.getBlock();
        Chunk chunk = block.getChunk();
        Plot plot = plots.getPlot(chunk);

        // Wilderness → global toggle
        if (plot == null) {
            if (!plugin.getConfig().getBoolean("protection.fire.spread", false)) {
                event.setCancelled(true);
                messages.debug("&cFire spread prevented in wilderness");
            }
            return;
        }

        // Claim toggle
        if (!plot.getSettings().isFireSpreadAllowed()) {
            event.setCancelled(true);
            messages.debug("&cFire spread prevented in claim: " + plot.getDisplayNameSafe());
        }
    }

    /** Handle fire ignition (lava, lightning, flint & steel) */
    @EventHandler(ignoreCancelled = true)
    public void onFireIgnite(BlockIgniteEvent event) {
        Block block = event.getBlock();
        Chunk chunk = block.getChunk();
        Plot plot = plots.getPlot(chunk);

        // Wilderness → global config decides
        if (plot == null) {
            if (!isIgniteAllowed(event)) {
                event.setCancelled(true);
                messages.debug("&cFire ignition blocked in wilderness (" + event.getCause() + ")");
            }
            return;
        }

        // Inside claim → use claim flag + ignite rules
        PlotSettings s = plot.getSettings();
        if (!s.isFireAllowed() || !isIgniteAllowed(event)) {
            event.setCancelled(true);
            messages.debug("&cFire ignition blocked in claim: " + plot.getDisplayNameSafe() +
                    " (" + event.getCause() + ")");
        }
    }

    /**
     * Checks config for allowed ignite sources.
     */
    private boolean isIgniteAllowed(BlockIgniteEvent event) {
        switch (event.getCause()) {
            case FLINT_AND_STEEL ->
                    { return plugin.getConfig().getBoolean("protection.fire.ignite.flint_and_steel", false); }
            case LAVA ->
                    { return plugin.getConfig().getBoolean("protection.fire.ignite.lava", false); }
            case LIGHTNING ->
                    { return plugin.getConfig().getBoolean("protection.fire.ignite.lightning", false); }
            default ->
                    { return plugin.getConfig().getBoolean("protection.fire.burn", false); }
        }
    }
}
