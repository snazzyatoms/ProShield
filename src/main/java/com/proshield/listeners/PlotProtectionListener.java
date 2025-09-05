package com.proshield.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import com.proshield.managers.PlotManager;

public class PlotProtectionListener implements Listener {

    private final PlotManager plotManager;

    public PlotProtectionListener(PlotManager plotManager) {
        this.plotManager = plotManager;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (plotManager.isPlotProtected(event.getBlock().getLocation())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("This plot is protected!");
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (plotManager.isPlotProtected(event.getBlock().getLocation())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("This plot is protected!");
        }
    }
}
