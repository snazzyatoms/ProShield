package com.snazzytom.proshield.listeners;

import com.snazzytom.proshield.plots.PlotManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockProtectionListener implements Listener {

    private final PlotManager plotManager;

    public BlockProtectionListener(PlotManager plotManager) {
        this.plotManager = plotManager;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Location location = event.getBlock().getLocation();

        if (!plotManager.isOwner(player, location)) {
            event.setCancelled(true);
            player.sendMessage("You cannot break blocks here!");
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Location location = event.getBlock().getLocation();

        if (!plotManager.isOwner(player, location)) {
            event.setCancelled(true);
            player.sendMessage("You cannot place blocks here!");
        }
    }
}
