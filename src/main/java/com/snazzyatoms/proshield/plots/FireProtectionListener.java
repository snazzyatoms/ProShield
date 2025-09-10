package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockSpreadEvent;

/**
 * Handles fire protection inside claims and wilderness.
 * - Spread, burn, and ignite sources
 * - Global config respected
 * - Per-claim overrides via PlotSettings
 */
public class FireProtectionListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final MessagesUtil messages;

    public FireProtectionListener(ProShield plugin, PlotManager plotManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
        this.messages = plugin.getMessagesUtil();
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event) {
        Block block = event.getBlock();
        Chunk chunk = block.getChunk();
        Plot plot = plotManager.getPlot(chunk);

        if (plot == null) {
            if (!plugin.getConfig().getBoolean("protection.fire.burn", true)) {
                event.setCancelled(true);
                messages.debug(plugin, "&cFire burn prevented in wilderness at " + block.getLocation());
            }
            return;
        }

        if (!plot.getSettings().isFireAllowed()) {
            event.setCancelled(true);
            messages.debug(plugin, "&cFire burn prevented in claim: " + plot.getName());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockSpread(BlockSpreadEvent event) {
        Block block = event.getBlock();
        Chunk chunk = block.getChunk();
        Plot plot = plotManager.getPlot(chunk);

        if (plot == null) {
            if (!plugin.getConfig().getBoolean("protection.fire.spread", true)) {
                event.setCancelled(true);
                messages.debug(plugin, "&cFire spread prevented in wilderness at " + block.getLocation());
            }
            return;
        }

        if (!plot.getSettings().isFireAllowed()) {
            event.setCancelled(true);
            messages.debug(plugin, "&cFire spread prevented in claim: " + plot.getName());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockIgnite(BlockIgniteEvent event) {
        Block block = event.getBlock();
        Chunk chunk = block.getChunk();
        Plot plot = plotManager.getPlot(chunk);

        if (plot == null) {
            if (!plugin.getConfig().getBoolean("protection.fire.ignite.flint_and_steel", true) && event.getCause() == BlockIgniteEvent.IgniteCause.FLINT_AND_STEEL) {
                event.setCancelled(true);
                messages.debug(plugin, "&cIgnite by Flint & Steel prevented in wilderness.");
            }
            if (!plugin.getConfig().getBoolean("protection.fire.ignite.lava", true) && event.getCause() == BlockIgniteEvent.IgniteCause.LAVA) {
                event.setCancelled(true);
                messages.debug(plugin, "&cIgnite by Lava prevented in wilderness.");
            }
            if (!plugin.getConfig().getBoolean("protection.fire.ignite.lightning", true) && event.getCause() == BlockIgniteEvent.IgniteCause.LIGHTNING) {
                event.setCancelled(true);
                messages.debug(plugin, "&cIgnite by Lightning prevented in wilderness.");
            }
            return;
        }

        if (!plot.getSettings().isFireAllowed()) {
            event.setCancelled(true);
            messages.debug(plugin, "&cIgnite prevented in claim: " + plot.getName() + " (Cause: " + event.getCause() + ")");
        }
    }
}
