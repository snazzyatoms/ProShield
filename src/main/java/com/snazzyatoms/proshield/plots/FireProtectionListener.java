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
 * ✅ Handles spread + ignition (lava, flint & steel, lightning)
 * ✅ Global wilderness config
 * ✅ Per-claim flags (fireAllowed, fireSpreadAllowed)
 * ✅ Debug output for admins
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

    @EventHandler(ignoreCancelled = true)
    public void onFireSpread(BlockSpreadEvent event) {
        Block block = event.getBlock();
        Chunk chunk = block.getChunk();
        Plot plot = plots.getPlot(chunk);

        // Wilderness → global only
        if (plot == null) {
            if (!plugin.getConfig().getBoolean("protection.fire.spread", false)) {
                event.setCancelled(true);
                messages.debug("&cFire spread blocked in wilderness.");
            }
            return;
        }

        // Claim → per-claim toggle
        if (!plot.getSettings().isFireSpreadAllowed()) {
            event.setCancelled(true);
            messages.debug("&cFire spread blocked in claim: " + plot.getDisplayNameSafe());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onFireIgnite(BlockIgniteEvent event) {
        Block block = event.getBlock();
        Chunk chunk = block.getChunk();
        Plot plot = plots.getPlot(chunk);

        BlockIgniteEvent.IgniteCause cause = event.getCause();

        // Wilderness
        if (plot == null) {
            if (!isIgniteAllowedGlobal(cause)) {
                event.setCancelled(true);
                messages.debug("&cFire ignition (" + cause + ") blocked in wilderness.");
            }
            return;
        }

        // Claim
        if (!plot.getSettings().isFireAllowed()) {
            event.setCancelled(true);
            messages.debug("&cFire ignition (" + cause + ") blocked in claim: " + plot.getDisplayNameSafe());
        }
    }

    /**
     * Checks wilderness ignite rules from config.yml
     */
    private boolean isIgniteAllowedGlobal(BlockIgniteEvent.IgniteCause cause) {
        switch (cause) {
            case FLINT_AND_STEEL:
                return plugin.getConfig().getBoolean("protection.fire.ignite.flint_and_steel", false);
            case LAVA:
                return plugin.getConfig().getBoolean("protection.fire.ignite.lava", false);
            case LIGHTNING:
                return plugin.getConfig().getBoolean("protection.fire.ignite.lightning", false);
            default:
                return plugin.getConfig().getBoolean("protection.fire.burn", false);
        }
    }
}
