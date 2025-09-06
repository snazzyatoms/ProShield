package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockProtectionListener implements Listener {

    private final PlotManager plotManager;

    public BlockProtectionListener(ProShield plugin) {
        this.plotManager = plugin.getPlotManager();
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Location loc = event.getBlock().getLocation();

        if (!plotManager.canBuild(player, loc)) {
            event.setCancelled(true);
            player.sendMessage("§cThis chunk is protected!");
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Location loc = event.getBlock().getLocation();

        if (!plotManager.canBuild(player, loc)) {
            event.setCancelled(true);
            player.sendMessage("§cThis chunk is protected!");
        }
    }
}
