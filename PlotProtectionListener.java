package com.snazzyatoms.proshield;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class PlotProtectionListener implements Listener {

    private final ProShield plugin;

    public PlotProtectionListener(ProShield plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Location location = event.getBlockPlaced().getLocation();

        if (!plugin.getPlotManager().canBuild(player, location)) {
            event.setCancelled(true);
            player.sendMessage("§cYou cannot build here! This area is protected.");
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Location location = event.getBlock().getLocation();

        if (!plugin.getPlotManager().canBuild(player, location)) {
            event.setCancelled(true);
            player.sendMessage("§cYou cannot break blocks here! This area is protected.");
        }
    }
}
