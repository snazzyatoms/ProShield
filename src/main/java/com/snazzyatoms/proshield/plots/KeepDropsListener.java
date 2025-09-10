// ===========================================
// KeepDropsListener.java
// ===========================================
package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemDespawnEvent;

public class KeepDropsListener implements Listener {
    private final PlotManager plots;

    public KeepDropsListener(PlotManager plots) {
        this.plots = plots;
    }

    @EventHandler
    public void onItemDespawn(ItemDespawnEvent e) {
        Item item = e.getEntity();
        if (plots.isInsideClaim(item.getLocation()) && plots.getConfig().getBoolean("claims.keep-items.enabled", false)) {
            e.setCancelled(true);
        }
    }
}
