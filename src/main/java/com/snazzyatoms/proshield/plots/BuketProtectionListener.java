package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.roles.ClaimRole;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;

/**
 * Handles bucket interactions inside claims and globally.
 * - Obeys global config + per-claim flags
 * - Role-based access (Builder or higher can use buckets in claims)
 * - Uses MessagesUtil for consistent feedback
 */
public class BucketProtectionListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final ClaimRoleManager roleManager;
    private final MessagesUtil messages;

    public BucketProtectionListener(ProShield plugin, PlotManager plotManager, ClaimRoleManager roleManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
        this.roleManager = roleManager;
        this.messages = plugin.getMessagesUtil();
    }

    @EventHandler(ignoreCancelled = true)
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlockClicked();
        Chunk chunk = block.getChunk();

        FileConfiguration config = plugin.getConfig();
        Plot plot = plotManager.getPlot(chunk);

        // === Wilderness ===
        if (plot == null) {
            if (!config.getBoolean("protection.buckets.block-empty", true)) return;
            event.setCancelled(true);
            messages.send(player, "protection.bucket-empty-blocked",
                    "%player%", player.getName(),
                    "%bucket%", event.getBucket().name());
            return;
        }

        // === Inside claim ===
        ClaimRole role = roleManager.getRole(plot, player);
        if (!roleManager.canBuild(role) || !plot.getSettings().isBucketsAllowed()) {
            event.setCancelled(true);
            messages.send(player, "protection.bucket-empty-blocked",
                    "%player%", player.getName(),
                    "%bucket%", event.getBucket().name());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBucketFill(PlayerBucketFillEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlockClicked();
        Chunk chunk = block.getChunk();

        FileConfiguration config = plugin.getConfig();
        Plot plot = plotManager.getPlot(chunk);

        // === Wilderness ===
        if (plot == null) {
            if (!config.getBoolean("protection.buckets.block-fill", true)) return;
            event.setCancelled(true);
            messages.send(player, "protection.bucket-fill-blocked",
                    "%player%", player.getName(),
                    "%bucket%", event.getBucket().name());
            return;
        }

        // === Inside claim ===
        ClaimRole role = roleManager.getRole(plot, player);
        if (!roleManager.canBuild(role) || !plot.getSettings().isBucketsAllowed()) {
            event.setCancelled(true);
            messages.send(player, "protection.bucket-fill-blocked",
                    "%player%", player.getName(),
                    "%bucket%", event.getBucket().name());
        }
    }
}
