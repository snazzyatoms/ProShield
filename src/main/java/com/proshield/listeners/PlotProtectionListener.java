package com.snazzyatoms.proshield.listeners;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.managers.PlotManager;
import org.bukkit.ChatColor;
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
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (!plugin.getPlotManager().canBuild(player, event.getBlock().getLocation())) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You cannot break blocks here!");
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (!plugin.getPlotManager().canBuild(player, event.getBlock().getLocation())) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You cannot place blocks here!");
        }
    }
}
