package com.proshield.listeners;

import com.proshield.ProShield;
import com.proshield.managers.PlotManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class PlotProtectionListener implements Listener {

    private final PlotManager plotManager;

    public PlotProtectionListener(ProShield plugin) {
        this.plotManager = plugin.getPlotManager();
    }

    private boolean isInsideOwnPlot(Player player, Location loc) {
        return plotManager.isInsidePlot(player.getUniqueId(), loc);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Location loc = event.getBlock().getLocation();

        if (!isInsideOwnPlot(player, loc)) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You cannot break blocks here. This land is protected!");
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Location loc = event.getBlock().getLocation();

        if (!isInsideOwnPlot(player, loc)) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You cannot place blocks here. This land is protected!");
        }
    }
}
