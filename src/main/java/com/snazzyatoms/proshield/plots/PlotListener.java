package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

/**
 * Basic build protection based on per-plot flags and trust.
 */
public class PlotListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final MessagesUtil messages;

    public PlotListener(ProShield plugin, PlotManager plotManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
        this.messages = plugin.getMessagesUtil();
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Plot p = plotManager.getPlot(event.getBlock().getLocation());
        if (p == null) return;

        Player player = event.getPlayer();
        boolean allowed = p.getFlag("block-break", plugin.getConfig()); // ✅ pass cfg
        if (!allowed && !p.isTrusted(player.getUniqueId()) && !player.hasPermission("proshield.admin")) {
            event.setCancelled(true);
            messages.send(player, "&cYou cannot break blocks here.");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Plot p = plotManager.getPlot(event.getBlock().getLocation());
        if (p == null) return;

        Player player = event.getPlayer();
        boolean allowed = p.getFlag("block-place", plugin.getConfig()); // ✅ pass cfg
        if (!allowed && !p.isTrusted(player.getUniqueId()) && !player.hasPermission("proshield.admin")) {
            event.setCancelled(true);
            messages.send(player, "&cYou cannot place blocks here.");
        }
    }
}
