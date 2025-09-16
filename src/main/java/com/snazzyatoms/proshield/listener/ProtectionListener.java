package com.snazzyatoms.proshield.listener;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;

public class ProtectionListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plotManager;

    public ProtectionListener(ProShield plugin, PlotManager plotManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Plot plot = plotManager.getPlotAt(event.getBlock().getLocation());
        if (plot != null && !plot.isTrusted(player.getUniqueId())) {
            if (!plot.getFlag("block-break")) {
                event.setCancelled(true);
                player.sendMessage(plugin.getMessagesUtil().get("prefix") + "§cYou cannot break blocks here.");
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Plot plot = plotManager.getPlotAt(event.getBlock().getLocation());
        if (plot != null && !plot.isTrusted(player.getUniqueId())) {
            if (!plot.getFlag("block-place")) {
                event.setCancelled(true);
                player.sendMessage(plugin.getMessagesUtil().get("prefix") + "§cYou cannot place blocks here.");
            }
        }
    }

    @EventHandler
    public void onBucketUse(PlayerBucketEmptyEvent event) {
        Player player = event.getPlayer();
        Plot plot = plotManager.getPlotAt(event.getBlock().getLocation());
        if (plot != null && !plot.isTrusted(player.getUniqueId())) {
            if (!plot.getFlag("bucket-use")) {
                event.setCancelled(true);
                player.sendMessage(plugin.getMessagesUtil().get("prefix") + "§cYou cannot use buckets here.");
            }
        }
    }

    @EventHandler
    public void onBucketFill(PlayerBucketFillEvent event) {
        Player player = event.getPlayer();
        Plot plot = plotManager.getPlotAt(event.getBlock().getLocation());
        if (plot != null && !plot.isTrusted(player.getUniqueId())) {
            if (!plot.getFlag("bucket-use")) {
                event.setCancelled(true);
                player.sendMessage(plugin.getMessagesUtil().get("prefix") + "§cYou cannot fill buckets here.");
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof Player victim)) return;

        Plot plot = plotManager.getPlotAt(victim.getLocation());
        if (plot != null && !plot.getFlag("pvp")) {
            event.setCancelled(true);
            player.sendMessage(plugin.getMessagesUtil().get("prefix") + "§cPvP is disabled in this claim.");
        }
    }
}
