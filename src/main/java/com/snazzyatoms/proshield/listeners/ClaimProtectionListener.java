package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;

public class ClaimProtectionListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final PlotListener plotListener;
    private final MessagesUtil messages;

    public ClaimProtectionListener(ProShield plugin, PlotManager plotManager, PlotListener plotListener) {
        this.plugin = plugin;
        this.plotManager = plotManager;
        this.plotListener = plotListener;
        this.messages = plugin.getMessagesUtil();
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Plot plot = plotManager.getPlotAt(event.getBlock().getLocation());

        if (plot != null) {
            if (!plotListener.isProtected(player, plot)) {
                event.setCancelled(true);
                messages.send(player, "&cYou cannot break blocks here.");
                return;
            }

            if (!plot.getFlag("block-break")) {
                event.setCancelled(true);
                messages.send(player, "&cYou cannot break blocks here.");
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Plot plot = plotManager.getPlotAt(event.getBlock().getLocation());

        if (plot != null) {
            if (!plotListener.isProtected(player, plot)) {
                event.setCancelled(true);
                messages.send(player, "&cYou cannot place blocks here.");
                return;
            }

            if (!plot.getFlag("block-place")) {
                event.setCancelled(true);
                messages.send(player, "&cYou cannot place blocks here.");
            }
        }
    }

    @EventHandler
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        Player player = event.getPlayer();
        Plot plot = plotManager.getPlotAt(event.getBlock().getLocation());

        if (plot != null) {
            if (!plotListener.isProtected(player, plot)) {
                event.setCancelled(true);
                messages.send(player, "&cYou cannot use buckets here.");
                return;
            }

            if (!plot.getFlag("bucket-use")) {
                event.setCancelled(true);
                messages.send(player, "&cYou cannot use buckets here.");
            }
        }
    }

    @EventHandler
    public void onBucketFill(PlayerBucketFillEvent event) {
        Player player = event.getPlayer();
        Plot plot = plotManager.getPlotAt(event.getBlock().getLocation());

        if (plot != null) {
            if (!plotListener.isProtected(player, plot)) {
                event.setCancelled(true);
                messages.send(player, "&cYou cannot fill buckets here.");
                return;
            }

            if (!plot.getFlag("bucket-use")) {
                event.setCancelled(true);
                messages.send(player, "&cYou cannot fill buckets here.");
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!(event.getEntity() instanceof Player victim)) return;

        Plot plot = plotManager.getPlotAt(victim.getLocation());
        if (plot != null) {
            if (!plot.getFlag("pvp")) {
                // Cancel PvP unless both players are trusted/owners
                if (!plotListener.isProtected(attacker, plot) || !plotListener.isProtected(victim, plot)) {
                    event.setCancelled(true);
                    messages.send(attacker, "&cPvP is disabled in this claim.");
                }
            }
        }
    }
}
