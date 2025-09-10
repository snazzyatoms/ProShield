package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Chunk;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.entity.ItemDespawnEvent;

/**
 * Handles item drop & pickup rules inside claims.
 * Supports global and per-claim overrides for keep-items.
 */
@SuppressWarnings("deprecation") // for PlayerPickupItemEvent (legacy support 1.18+)
public class ItemProtectionListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plotManager;

    public ItemProtectionListener(ProShield plugin, PlotManager plotManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
    }

    /**
     * Prevent despawning if global or per-claim keep-items is enabled.
     */
    @EventHandler(ignoreCancelled = true)
    public void onItemDespawn(ItemDespawnEvent event) {
        Item item = event.getEntity();
        Chunk chunk = item.getLocation().getChunk();
        Plot plot = plotManager.getPlot(chunk);

        boolean keep = resolveKeepItems(plot);
        if (keep) {
            event.setCancelled(true);
        }
    }

    /**
     * Prevent unauthorized players from picking up items if keep-items protection is enabled.
     */
    @EventHandler(ignoreCancelled = true)
    public void onItemPickup(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        Chunk chunk = event.getItem().getLocation().getChunk();
        Plot plot = plotManager.getPlot(chunk);

        boolean keep = resolveKeepItems(plot);
        if (keep && plot != null && !plot.isTrusted(player.getUniqueId())) {
            event.setCancelled(true);
            player.sendMessage(plugin.getPrefix() + "§cYou cannot pick up items in this claim.");
        }
    }

    /**
     * Utility: resolve whether keep-items is active here.
     */
    private boolean resolveKeepItems(Plot plot) {
        if (plot != null && plot.getKeepItemsEnabled() != null) {
            return plot.getKeepItemsEnabled();
        }
        return plugin.getConfig().getBoolean("claims.keep-items.enabled", false);
    }
}
