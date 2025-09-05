package com.snazzyatoms.proshield.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class PlotProtectionListener implements Listener {

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        // Protection logic here
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        // Protection logic here
    }
}


        // If not inside any plot, allow building
        if (!plotManager.isInsideAnyPlot(event.getBlockPlaced().getLocation())) {
            return;
        }

        // If inside their own plot, allow building
        if (plotManager.isInsideOwnPlot(player, event.getBlockPlaced().getLocation())) {
            return;
        }

        // Otherwise, cancel
        event.setCancelled(true);
        player.sendMessage(ChatColor.RED + "You cannot build in another player's plot!");
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        // If not inside any plot, allow breaking
        if (!plotManager.isInsideAnyPlot(event.getBlock().getLocation())) {
            return;
        }

        // If inside their own plot, allow breaking
        if (plotManager.isInsideOwnPlot(player, event.getBlock().getLocation())) {
            return;
        }

        // Otherwise, cancel
        event.setCancelled(true);
        player.sendMessage(ChatColor.RED + "You cannot break blocks in another player's plot!");
    }
}
