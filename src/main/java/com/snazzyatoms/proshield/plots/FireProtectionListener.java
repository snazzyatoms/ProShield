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
 * ✅ Controls fire spread and ignite rules.
 * ✅ Configurable per-claim (fireAllowed / fireSpreadAllowed).
 * ✅ Global config supports flint, lava, lightning restrictions.
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

    /* -------------------------------------------------------
     * Fire Spread (wild spread between blocks)
     * ------------------------------------------------------- */
    @EventHandler(ignoreCancelled = true)
    public void onFireSpread(BlockSpreadEvent event) {
        Block block = event.getBlock();
        Chunk chunk = block.getChunk();
        Plot plot = plots.getPlot(chunk);

        // Wilderness → global toggle
        if (plot == null) {
            if (!plugin.getConfig().getBoolean("protection.fire.spread", false)) {
                event.setCancelled(true);
                messages.debug("&cFire spread prevented in wilderness.");
            }
            return;
        }

        // Claims → per-plot toggle
        if (!plot.getSettings().isFireSpreadAllowed()) {
            event.setCancelled(true);
            messages.debug("&cFire spread prevented in claim: " + plot.getDisplayNameSafe());
        }
    }

    /* -------------------------------------------------------
     * Fire Ignition (causes: flint, lava, lightning)
     * ------------------------------------------------------- */
    @EventHandler(ignoreCancelled = true)
    public void onFireIgnite(BlockIgniteEvent event) {
        Block block = event.getBlock();
        Chunk chunk = block.getChunk();
        Plot plot = plots.getPlot(chunk);

        String cause = event.getCause().name();

        // Wilderness → check global config
        if (plot == null) {
            if (!isIgniteAllowed(event)) {
                event.setCancelled(true);
                messages.debug("&cIgnite blocked in wilderness (" + cause + ")");
            }
            return;
        }

        // Claims → per-plot flag
        if (!plot.getSettings().isFireAllowed()) {
            event.setCancelled(true);
            messages.debug("&cIgnite blocked in claim: " + plot.getDisplayNameSafe() + " (" + cause + ")");
        }
    }

    /* -------------------------------------------------------
     * Helpers
     * ------------------------------------------------------- */
    private boolean isIgniteAllowed(BlockIgniteEvent event) {
        switch (event.getCause()) {
            case FLINT_AND_STEEL -> {
                return plugin.getConfig().getBoolean("protection.fire.ignite.flint_and_steel", false);
            }
            case LAVA -> {
                return plugin.getConfig().getBoolean("protection.fire.ignite.lava", false);
            }
            case LIGHTNING -> {
                return plugin.getConfig().getBoolean("protection.fire.ignite.lightning", false);
            }
            default -> {
                return true; // allow other causes
            }
        }
    }
}
