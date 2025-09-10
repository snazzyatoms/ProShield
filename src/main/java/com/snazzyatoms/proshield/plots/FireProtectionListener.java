package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockSpreadEvent;

/**
 * Handles fire spread, ignition, and burning inside claims and globally.
 * - Controlled via config + per-claim settings
 * - Uses MessagesUtil for player feedback
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
    public void onBlockIgnite(BlockIgniteEvent event) {
        Block block = event.getBlock();
        Chunk chunk = block.getChunk();
        Plot plot = plotManager.getPlot(chunk);

        FileConfiguration config = plugin.getConfig();
        if (!config.getBoolean("protection.fire.enabled", true)) return;

        boolean cancel = switch (event.getCause()) {
            case FLINT_AND_STEEL -> !config.getBoolean("protection.fire.ignite.flint_and_steel", true);
            case LAVA -> !config.getBoolean("protection.fire.ignite.lava", true);
            case LIGHTNING -> !config.getBoolean("protection.fire.ignite.lightning", true);
            case EXPLOSION -> !config.getBoolean("protection.fire.ignite.explosion", true);
            case SPREAD -> !config.getBoolean("protection.fire.ignite.spread", true);
            default -> false;
        };

        if (cancel) {
            event.setCancelled(true);
            messages.broadcastToNearby(block.getLocation(),
                    "protection.fire-ignite-blocked",
                    "%cause%", event.getCause().name());
            return;
        }

        if (plot != null && !plot.getSettings().isFireAllowed()) {
            event.setCancelled(true);
            messages.broadcastToNearby(block.getLocation(),
                    "protection.fire-ignite-blocked",
                    "%cause%", event.getCause().name());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event) {
        Block block = event.getBlock();
        Plot plot = plotManager.getPlot(block.getChunk());

        FileConfiguration config = plugin.getConfig();
        if (!config.getBoolean("protection.fire.burn", true)) return;

        if (plot != null && !plot.getSettings().isFireAllowed()) {
            event.setCancelled(true);
            messages.broadcastToNearby(block.getLocation(),
                    "protection.fire-burn-blocked",
                    "%block%", block.getType().name());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockSpread(BlockSpreadEvent event) {
        if (event.getSource().getType() != Material.FIRE) return;

        Block block = event.getBlock();
        Plot plot = plotManager.getPlot(block.getChunk());

        FileConfiguration config = plugin.getConfig();
        if (!config.getBoolean("protection.fire.spread", true)) return;

        if (plot != null && !plot.getSettings().isFireAllowed()) {
            event.setCancelled(true);
            messages.broadcastToNearby(block.getLocation(),
                    "protection.fire-spread-blocked",
                    "%block%", block.getType().name());
        }
    }
}
