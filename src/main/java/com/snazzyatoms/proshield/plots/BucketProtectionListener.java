// src/main/java/com/snazzyatoms/proshield/plots/BucketProtectionListener.java
package com.snazzyatoms.proshield.plots;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;

/**
 * BucketProtectionListener
 *
 * Preserves prior protection logic:
 *  ✅ Prevents untrusted players from placing water/lava buckets in claims
 *  ✅ Prevents untrusted players from scooping up water/lava from claims
 * 
 * Expanded:
 *  ✅ Works with both fill & empty events
 *  ✅ Uses PlotManager trust/ownership checks
 */
public class BucketProtectionListener implements Listener {

    private final PlotManager plots;

    public BucketProtectionListener(PlotManager plots) {
        this.plots = plots;
    }

    @EventHandler(ignoreCancelled = true)
    public void onBucketEmpty(PlayerBucketEmptyEvent e) {
        Player p = e.getPlayer();
        Location l = e.getBlockClicked().getLocation();

        Plot plot = plots.getPlot(l);
        if (plot == null) return; // wilderness, allow

        if (!plots.isTrustedOrOwner(p.getUniqueId(), l)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBucketFill(PlayerBucketFillEvent e) {
        Player p = e.getPlayer();
        Location l = e.getBlockClicked().getLocation();

        Plot plot = plots.getPlot(l);
        if (plot == null) return;

        if (!plots.isTrustedOrOwner(p.getUniqueId(), l)) {
            e.setCancelled(true);
        }
    }
}
