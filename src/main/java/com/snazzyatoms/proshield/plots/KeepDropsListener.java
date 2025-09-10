package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Chunk;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.player.PlayerDropItemEvent;

/**
 * Handles persistence of dropped items inside claims.
 * - Global config: claims.keep-items.enabled
 * - Per-claim override: plot.getSettings().isKeepDrops()
 */
public class KeepDropsListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plotManager;

    public KeepDropsListener(ProShield plugin, PlotManager plotManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        Chunk chunk = player.getLocation().getChunk();
        Plot plot = plotManager.getPlot(chunk);
        Item item = event.getItemDrop();

        FileConfiguration config = plugin.getConfig();
        boolean globalKeep = config.getBoolean("claims.keep-items.enabled", false);
        boolean allowOverride = config.getBoolean("claims.keep-items.per-claim-override", true);

        boolean keepDrops = globalKeep;
        if (plot != null && allowOverride) {
            keepDrops = plot.getSettings().isKeepDropsEnabled();
        }

        if (keepDrops) {
            int lifespan = config.getInt("claims.keep-items.despawn-seconds", 900) * 20;
            item.setTicksLived(1); // reset
            item.setUnlimitedLifetime(true);
            item.setTicksLived(-lifespan);
            player.sendMessage(plugin.getPrefix() + "§aThis item will not despawn inside your claim.");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemDespawn(ItemDespawnEvent event) {
        Chunk chunk = event.getLocation().getChunk();
        Plot plot = plotManager.getPlot(chunk);
        FileConfiguration config = plugin.getConfig();

        boolean globalKeep = config.getBoolean("claims.keep-items.enabled", false);
        boolean allowOverride = config.getBoolean("claims.keep-items.per-claim-override", true);

        boolean keepDrops = globalKeep;
        if (plot != null && allowOverride) {
            keepDrops = plot.getSettings().isKeepDropsEnabled();
        }

        if (keepDrops) {
            event.setCancelled(true);
        }
    }
}
