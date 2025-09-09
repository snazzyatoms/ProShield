// path: src/main/java/com/snazzyatoms/proshield/plots/KeepDropsListener.java
package com.snazzyatoms.proshield.plots;

import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemDespawnEvent;

public class KeepDropsListener implements Listener {
    private final PlotManager plots;

    public KeepDropsListener(PlotManager plots) {
        this.plots = plots;
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemDespawn(ItemDespawnEvent e) {
        // Only act if feature is enabled
        if (!plots.getPlugin().getConfig().getBoolean("claims.keep-items.enabled", false)) return;

        // Only inside claims
        Item item = e.getEntity();
        if (item == null) return;
        if (plots.getClaim(item.getLocation()).isEmpty()) return;

        // Cancel despawn while feature is ON
        e.setCancelled(true);

        // Note: We keep items indefinitely while the toggle is ON.
        // The configured "despawn-seconds" remains for display/UX; if you ever want timed re-despawn,
        // you can schedule a future remove when the toggle flips OFF.
    }
}
