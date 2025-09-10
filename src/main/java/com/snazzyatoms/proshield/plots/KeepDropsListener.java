package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Chunk;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Handles the "keep items in claims" feature (configurable).
 * Items dropped inside claims will NOT despawn if enabled in config.
 */
public class KeepDropsListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plotManager;

    public KeepDropsListener(ProShield plugin, PlotManager plotManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDrop(PlayerDropItemEvent event) {
        if (!plugin.getConfig().getBoolean("claims.keep-items.enabled")) return;

        Item item = event.getItemDrop();
        Chunk chunk = item.getLocation().getChunk();

        Plot plot = plotManager.getPlot(chunk);
        if (plot == null) return; // wilderness = ignore

        protectItem(item);
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemSpawn(ItemSpawnEvent event) {
        if (!plugin.getConfig().getBoolean("claims.keep-items.enabled")) return;

        Item item = event.getEntity();
        Chunk chunk = item.getLocation().getChunk();

        Plot plot = plotManager.getPlot(chunk);
        if (plot == null) return;

        protectItem(item);
    }

    private void protectItem(Item item) {
        int seconds = plugin.getConfig().getInt("claims.keep-items.despawn-seconds", 900);

        // Cancel natural despawn
        item.setUnlimitedLifetime(true);
        item.setTicksLived(1);

        // Optional manual cleanup after expiry
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!item.isValid() || item.isDead()) return;
                item.remove();
            }
        }.runTaskLater(plugin, seconds * 20L);
    }
}
