package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Chunk;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.player.PlayerDropItemEvent;

/**
 * Handles persistence of dropped items inside claims.
 * - Prevents items from despawning if keep-items is enabled
 * - Supports both global + per-claim toggles
 */
public class KeepDropsListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plotManager;

    public KeepDropsListener(ProShield plugin, PlotManager plotManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
    }

    /**
     * Player drops an item — respect keep-items rules
     */
    @EventHandler(ignoreCancelled = true)
    public void onPlayerDrop(PlayerDropItemEvent event) {
        Item item = event.getItemDrop();
        Chunk chunk = item.getLocation().getChunk();

        if (isKeepItemsEnabled(chunk)) {
            int despawnSeconds = getDespawnSeconds();
            item.setUnlimitedLifetime(true);
            item.setTicksLived(1); // reset age
            item.setPickupDelay(20); // 1s pickup delay
            plugin.debug("Item drop protected inside claim. Lifetime extended to " + despawnSeconds + "s.");
        }
    }

    /**
     * Prevent item despawn if inside claim with keep-items enabled
     */
    @EventHandler(ignoreCancelled = true)
    public void onItemDespawn(ItemDespawnEvent event) {
        Item item = event.getEntity();
        Chunk chunk = item.getLocation().getChunk();

        if (isKeepItemsEnabled(chunk)) {
            event.setCancelled(true);
            plugin.debug("Prevented item despawn inside claim.");
        }
    }

    /**
     * Checks whether keep-items is enabled globally or for this claim
     */
    private boolean isKeepItemsEnabled(Chunk chunk) {
        FileConfiguration config = plugin.getConfig();

        // Global toggle
        boolean globalKeep = config.getBoolean("claims.keep-items.enabled", false);

        Plot plot = plotManager.getPlot(chunk);
        if (plot == null) {
            return globalKeep; // wilderness uses global setting
        }

        // Per-claim override if set, otherwise fallback to global
        Boolean claimKeep = plot.getSettings().getKeepItemsEnabled();
        return (claimKeep != null) ? claimKeep : globalKeep;
    }

    /**
     * Returns configured despawn time from config
     */
    private int getDespawnSeconds() {
        FileConfiguration config = plugin.getConfig();
        return config.getInt("claims.keep-items.despawn-seconds", 900);
    }
}
