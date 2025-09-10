// ===========================================
// BlockProtectionListener.java
// ===========================================
package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockProtectionListener implements Listener {
    private final PlotManager plots;

    public BlockProtectionListener(PlotManager plots) {
        this.plots = plots;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        Player p = e.getPlayer();
        if (!plots.canBuild(p, e.getBlock().getLocation())) {
            e.setCancelled(true);
            p.sendMessage(ProShield.PREFIX + "You cannot break blocks here!");
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        Player p = e.getPlayer();
        if (!plots.canBuild(p, e.getBlock().getLocation())) {
            e.setCancelled(true);
            p.sendMessage(ProShield.PREFIX + "You cannot place blocks here!");
        }
    }
}
