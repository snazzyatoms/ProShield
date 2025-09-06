// path: src/main/java/com/snazzyatoms/proshield/listeners/BlockProtectionListener.java
package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.plots.PlotManager;
import org.bukkit.ChatColor;
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
        Location loc = event.getBlock().getLocation();

        if (plotManager.isClaimed(loc) && !plotManager.isOwner(player.getUniqueId(), loc)) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You cannot break blocks here!");
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Location loc = event.getBlock().getLocation();

        if (plotManager.isClaimed(loc) && !plotManager.isOwner(player.getUniqueId(), loc)) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You cannot place blocks here!");
        }
    }
}
