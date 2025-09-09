package com.snazzyatoms.proshield.plots;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;

public class ItemProtectionListener implements Listener {
    private final PlotManager plots;
    public ItemProtectionListener(PlotManager plots) { this.plots = plots; }

    @EventHandler(ignoreCancelled = true)
    public void onBucketEmpty(PlayerBucketEmptyEvent e) {
        if (plots.getClaim(e.getBlockClicked().getLocation()).isEmpty()) return;
        if (plots.getPlugin().getConfig().getBoolean("protection.buckets.block-empty", true)) e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBucketFill(PlayerBucketFillEvent e) {
        if (plots.getClaim(e.getBlockClicked().getLocation()).isEmpty()) return;
        if (plots.getPlugin().getConfig().getBoolean("protection.buckets.block-fill", true)) e.setCancelled(true);
    }
}
