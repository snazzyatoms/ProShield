package com.snazzyatoms.proshield.listeners;

import com.snazzyatoms.proshield.managers.PlotManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.entity.Player;

public class PlotProtectionListener implements Listener {

    private final PlotManager plotManager;

    public PlotProtectionListener(PlotManager plotManager) {
        this.plotManager = plotManager;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (plotManager.getPlot(player.getUniqueId()) == null) {
            player.sendMessage("§cYou must claim a plot before building!");
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (plotManager.getPlot(player.getUniqueId()) == null) {
            player.sendMessage("§cYou must claim a plot before breaking blocks!");
            event.setCancelled(true);
        }
    }
}
