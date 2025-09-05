package com.snazzyatoms.proshield;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import java.util.UUID;

public class PlotProtectionListener implements Listener {

    private final PlotManager plotManager;

    public PlotProtectionListener(PlotManager plotManager) {
        this.plotManager = plotManager;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        UUID plotId = event.getBlock().getLocation().getWorld().getUID(); 
        if (plotManager.isProtected(plotId)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("This plot is protected!");
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        UUID plotId = event.getBlock().getLocation().getWorld().getUID(); 
        if (plotManager.isProtected(plotId)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("This plot is protected!");
        }
    }
}
