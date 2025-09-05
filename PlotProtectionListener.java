package com.snazzyatoms.proshield;

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
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (!plotManager.canBuild(player, event.getBlock().getLocation())) {
            event.setCancelled(true);
            player.sendMessage("§cYou cannot break blocks here!");
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (!plotManager.canBuild(player, event.getBlock().getLocation())) {
            event.setCancelled(true);
            player.sendMessage("§cYou cannot place blocks here!");
        }
    }
}
