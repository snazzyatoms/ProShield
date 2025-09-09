package com.snazzyatoms.proshield.plots;

import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemDespawnEvent;

public class KeepDropsListener implements Listener {
    private final PlotManager plots;
    public KeepDropsListener(PlotManager plots) { this.plots = plots; }

    @EventHandler(ignoreCancelled = true)
    public void onItemDespawn(ItemDespawnEvent e) {
        if (!(e.getEntity() instanceof Item it)) return;
        var loc = it.getLocation();
        if (plots.getClaim(loc).isEmpty()) return;
        if (!plots.getPlugin().getConfig().getBoolean("claims.keep-items.enabled", false)) return;

        int sec = plots.getPlugin().getConfig().getInt("claims.keep-items.despawn-seconds", 900);
        e.setCancelled(true);
        // re-schedule natural despawn if needed; here we simply cancel while feature is ON
        // (lightweight approach, consistent with "keep items" semantics)
    }
}
