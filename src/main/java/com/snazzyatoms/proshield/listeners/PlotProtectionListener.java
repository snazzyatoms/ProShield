package com.snazzyatoms.proshield.listeners;

import com.snazzyatoms.proshield.plots.PlotManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class PlotProtectionListener implements Listener {

    private final PlotManager plots;

    public PlotProtectionListener(PlotManager plots) {
        this.plots = plots;
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        Player p = event.getPlayer();
        if (!plots.isClaimed(event.getBlockPlaced().getLocation())) return;

        if (!plots.isOwner(p, event.getBlockPlaced().getLocation()) && !p.hasPermission("proshield.admin")) {
            event.setCancelled(true);
            p.sendMessage(ChatColor.RED + "This area is claimed.");
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        Player p = event.getPlayer();
        if (!plots.isClaimed(event.getBlock().getLocation())) return;

        if (!plots.isOwner(p, event.getBlock().getLocation()) && !p.hasPermission("proshield.admin")) {
            event.setCancelled(true);
            p.sendMessage(ChatColor.RED + "This area is claimed.");
        }
    }
}
