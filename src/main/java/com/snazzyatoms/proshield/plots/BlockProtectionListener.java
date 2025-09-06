package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockProtectionListener implements Listener {

    private final ProShield plugin;

    public BlockProtectionListener(ProShield plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        // Example: Prevent breaking in protected areas
        if (plugin.getPlotManager().isInClaim(event.getBlock().getLocation())) {
            if (!plugin.getPlotManager().canBuild(event.getPlayer(), event.getBlock().getLocation())) {
                event.setCancelled(true);
                event.getPlayer().sendMessage("§cYou cannot break blocks in a protected claim!");
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        // Example: Prevent placing in protected areas
        if (plugin.getPlotManager().isInClaim(event.getBlockPlaced().getLocation())) {
            if (!plugin.getPlotManager().canBuild(event.getPlayer(), event.getBlockPlaced().getLocation())) {
                event.setCancelled(true);
                event.getPlayer().sendMessage("§cYou cannot place blocks in a protected claim!");
            }
        }
    }
}
