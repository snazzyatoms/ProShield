package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;

/**
 * Handles bucket protections:
 * - Lava, water, and other bucket placements
 * - Per-claim overrides in PlotSettings
 * - Global wilderness checks from config
 */
public class BucketProtectionListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final MessagesUtil messages;

    public BucketProtectionListener(ProShield plugin, PlotManager plotManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
        this.messages = plugin.getMessagesUtil();
    }

    @EventHandler(ignoreCancelled = true)
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        Player player = event.getPlayer();
        Chunk chunk = event.getBlockClicked().getChunk();
        Plot plot = plotManager.getPlot(chunk);

        if (plot == null) {
            if (!plugin.getConfig().getBoolean("protection.buckets.block-empty", true)) {
                event.setCancelled(true);
                messages.send(player, "bucket-empty-deny");
                messages.debug(plugin, "&cBucket empty prevented in wilderness by " + player.getName());
            }
            return;
        }

        if (!plot.getSettings().isBucketsAllowed()) {
            event.setCancelled(true);
            messages.send(player, "bucket-empty-deny");
            messages.debug(plugin, "&cBucket empty prevented in claim: " + plot.getName() + " by " + player.getName());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBucketFill(PlayerBucketFillEvent event) {
        Player player = event.getPlayer();
        Chunk chunk = event.getBlockClicked().getChunk();
        Plot plot = plotManager.getPlot(chunk);

        if (plot == null) {
            if (!plugin.getConfig().getBoolean("protection.buckets.block-fill", true)) {
                event.setCancelled(true);
                messages.send(player, "bucket-fill-deny");
                messages.debug(plugin, "&cBucket fill prevented in wilderness by " + player.getName());
            }
            return;
        }

        if (!plot.getSettings().isBucketsAllowed()) {
            event.setCancelled(true);
            messages.send(player, "bucket-fill-deny");
            messages.debug(plugin, "&cBucket fill prevented in claim: " + plot.getName() + " by " + player.getName());
        }
    }
}
