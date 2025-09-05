package com.proshield.listeners;

import com.proshield.ProShield;
import com.proshield.managers.PlotManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class PlotProtectionListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plotManager;

    public PlotProtectionListener(ProShield plugin, PlotManager plotManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();

        // Check config option
        boolean protectOutside = plugin.getConfig().getBoolean("protection.protect-outside-plots", false);

        // If outside-plot protection is off, allow all building
        if (!protectOutside) {
            return;
        }

        // If protection is on, only allow building inside player’s plot
        if (!plotManager.isInsideOwnPlot(player, event.getBlock().getLocation())) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You can only build inside your claimed plot!");
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        // Check config option
        boolean protectOutside = plugin.getConfig().getBoolean("protection.protect-outside-plots", false);

        // If outside-plot protection is off, allow all breaking
        if (!protectOutside) {
            return;
        }

        // If protection is on, only allow breaking inside player’s plot
        if (!plotManager.isInsideOwnPlot(player, event.getBlock().getLocation())) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You can only break blocks inside your claimed plot!");
        }
    }
}
