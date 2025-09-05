package com.snazzyatoms.proshield.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

/**
 * Basic plot protection listener.
 * Prevents block breaking/placing without proper permissions.
 */
public class PlotProtectionListener implements Listener {

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!event.getPlayer().hasPermission("proshield.build")) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("§cYou cannot break blocks here!");
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!event.getPlayer().hasPermission("proshield.build")) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("§cYou cannot place blocks here!");
        }
    }
}
