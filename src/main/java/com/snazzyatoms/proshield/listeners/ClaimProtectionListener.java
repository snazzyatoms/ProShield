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

/**
 * ClaimProtectionListener
 * - Handles block interactions, buckets, and PvP inside claims
 * - Only owners & trusted players can interact (per flags)
 * - Provides clean denial messages and debug logs
 */
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

    /* -------------------------
     * BLOCK INTERACTIONS
     * ------------------------- */
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        handleBlockAction(event.getPlayer(), event.getBlock().getLocation(), "block-break",
                "&cYou cannot break blocks here.", event);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        handleBlockAction(event.getPlayer(), event.getBlock().getLocation(), "block-place",
                "&cYou cannot place blocks here.", event);
    }

    @EventHandler
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        handleBlockAction(event.getPlayer(), event.getBlock().getLocation(), "bucket-use",
                "&cYou cannot use buckets here.", event);
    }

    @EventHandler
    public void onBucketFill(PlayerBucketFillEvent event) {
        handleBlockAction(event.getPlayer(), event.getBlock().getLocation(), "bucket-use",
                "&cYou cannot fill buckets here.", event);
    }

    private void handleBlockAction(Player player, org.bukkit.Location loc, String flag, String denyMessage, org.bukkit.event.Cancellable event) {
        Plot plot = plotManager.getPlotAt(loc);
        if (plot == null) return;

        if (!plotListener.isProtected(player, plot)) {
            event.setCancelled(true);
            messages.send(player, denyMessage);
            messages.debug("Denied " + flag + " for non-trusted player " + player.getName() + " in plot " + plot.getId());
            return;
        }

        if (!plot.getFlag(flag)) {
            event.setCancelled(true);
            messages.send(player, denyMessage);
            messages.debug("Denied " + flag + " for player " + player.getName() + " in plot " + plot.getId());
        }
    }

    /* -------------------------
     * PVP HANDLING
     * ------------------------- */
    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!(event.getEntity() instanceof Player victim)) return;

        Plot plot = plotManager.getPlotAt(victim.getLocation());
        if (plot == null) return;

        if (!plot.getFlag("pvp")) {
            // Cancel PvP unless both attacker & victim are trusted/owner
            if (!plotListener.isProtected(attacker, plot) || !plotListener.isProtected(victim, plot)) {
                event.setCancelled(true);
                messages.send(attacker, "&cPvP is disabled in this claim.");
                messages.debug("Prevented PvP: " + attacker.getName() + " → " + victim.getName() + " in plot " + plot.getId());
            }
        }
    }
}
