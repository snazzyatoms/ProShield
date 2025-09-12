// src/main/java/com/snazzyatoms/proshield/plots/BucketProtectionListener.java
package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;

import java.util.UUID;

public class BucketProtectionListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plots;
    private final ClaimRoleManager roles;
    private final MessagesUtil messages;

    public BucketProtectionListener(ProShield plugin, PlotManager plots, ClaimRoleManager roles) {
        this.plugin = plugin;
        this.plots = plots;
        this.roles = roles;
        this.messages = plugin.getMessagesUtil();
    }

    @EventHandler(ignoreCancelled = true)
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        handleBucket(event.getPlayer(), event.getBlockClicked().getLocation(), "bucket-empty-deny", event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBucketFill(PlayerBucketFillEvent event) {
        handleBucket(event.getPlayer(), event.getBlockClicked().getLocation(), "bucket-fill-deny", event);
    }

    private void handleBucket(Player player, Location loc, String msgKey, org.bukkit.event.Cancellable event) {
        Chunk chunk = loc.getChunk();
        Plot plot = plots.getPlot(chunk);
        UUID uid = player.getUniqueId();

        if (plot == null) {
            if (!plugin.getConfig().getBoolean("protection.buckets.wilderness", true)) {
                event.setCancelled(true);
                messages.send(player, msgKey);
            }
            return;
        }

        if (!roles.canBuild(uid, plot)) {
            event.setCancelled(true);
            messages.send(player, msgKey);
        }
    }
}
