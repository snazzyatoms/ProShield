package com.proshield.listeners;

import com.proshield.managers.PlotManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class PlotProtectionListener implements Listener {

    private final PlotManager plotManager;

    public PlotProtectionListener(PlotManager plotManager) {
        this.plotManager = plotManager;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();

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
