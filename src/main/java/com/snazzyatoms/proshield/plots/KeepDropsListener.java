package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Chunk;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.UUID;

/**
 * Handles keep-items logic inside claims.
 * - Respects global config
 * - Allows per-claim override via PlotSettings
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

        if (plot != null && plot.getSettings().isKeepItemsEnabled()) {
            event.setKeepInventory(true);
            event.getDrops().clear();
            player.sendMessage(plugin.getPrefix() + "§aYour items were kept because this claim has item-keep enabled.");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemDespawn(ItemDespawnEvent event) {
        Item item = event.getEntity();
        Chunk chunk = item.getLocation().getChunk();
        Plot plot = plotManager.getPlot(chunk);

        FileConfiguration config = plugin.getConfig();
        boolean globalEnabled = config.getBoolean("claims.keep-items.enabled", false);

        if (plot != null) {
            // Claim-specific override
            if (plot.getSettings().isKeepItemsEnabled()) {
                event.setCancelled(true);
                return;
            }
        }

        // Global fallback
        if (globalEnabled) {
            event.setCancelled(true);
        }
    }
}
