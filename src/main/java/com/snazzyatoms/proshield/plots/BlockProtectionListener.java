package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockProtectionListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plots;

    public BlockProtectionListener(ProShield plugin, PlotManager plots) {
        this.plugin = plugin;
        this.plots = plots;
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        Claim claim = plots.getClaimAt(event.getBlock().getLocation());
        if (claim == null) return;

        // Allow bypass
        if (player.hasPermission("proshield.bypass")) return;

        // Check if trusted (Builder or above)
        if (!claim.hasBuildAccess(player.getUniqueId())) {
            player.sendMessage(plugin.getPrefix() + "§cYou cannot break blocks in this claim!");
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();

        Claim claim = plots.getClaimAt(event.getBlockPlaced().getLocation());
        if (claim == null) return;

        // Allow bypass
        if (player.hasPermission("proshield.bypass")) return;

        // Check if trusted (Builder or above)
        if (!claim.hasBuildAccess(player.getUniqueId())) {
            player.sendMessage(plugin.getPrefix() + "§cYou cannot place blocks in this claim!");
            event.setCancelled(true);
        }
    }
}
