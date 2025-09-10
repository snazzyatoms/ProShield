package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemDespawnEvent;

public class KeepDropsListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plots;

    public KeepDropsListener(ProShield plugin, PlotManager plots) {
        this.plugin = plugin;
        this.plots = plots;
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemDespawn(ItemDespawnEvent event) {
        Item item = event.getEntity();

        // Get claim at item location
        Claim claim = plots.getClaimAt(item.getLocation());
        if (claim == null) return;

        // Global + per-claim config
        boolean keepItems = plugin.getConfig().getBoolean("claims.keep-items.enabled", false);
        if (claim.isKeepItemsEnabled() || keepItems) {
            event.setCancelled(true);

            // Reset the age so it doesn’t vanish later
            item.setTicksLived(0);
        }
    }
}
