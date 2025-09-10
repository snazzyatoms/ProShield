package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Chunk;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerDropItemEvent;

/**
 * Handles item protection inside claims.
 * - Prevents unauthorized players from picking up or dropping items if disabled
 * - Reads from global + per-claim settings
 */
public class ItemProtectionListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plotManager;

    public ItemProtectionListener(ProShield plugin, PlotManager plotManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        Chunk chunk = player.getLocation().getChunk();

        if (!plotManager.isItemProtectionEnabled(chunk)) {
            return; // globally/per-claim allowed
        }

        Plot plot = plotManager.getPlot(chunk);
        if (plot == null) return; // wilderness, allow

        if (!plot.getSettings().isTrusted(player.getUniqueId())) {
            event.setCancelled(true);
            player.sendMessage(plugin.getPrefix() + "§cYou cannot drop items here.");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemSpawn(ItemSpawnEvent event) {
        Item item = event.getEntity();
        Chunk chunk = item.getLocation().getChunk();

        if (!plotManager.isItemProtectionEnabled(chunk)) {
            return; // globally/per-claim allowed
        }

        Plot plot = plotManager.getPlot(chunk);
        if (plot == null) return; // wilderness, allow

        // Respect keep-items toggle (global + per-claim)
        if (plotManager.isKeepItemsEnabled(chunk)) {
            item.setTicksLived(1); // prevent despawn
            item.setUnlimitedLifetime(true);
        }
    }
}
