// src/main/java/com/snazzyatoms/proshield/plots/PlotListener.java
package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.UUID;

/**
 * PlotListener
 * - Unified claim protection handler
 * - Covers block breaking/placing, buckets, item frames, vehicles
 * - Handles claim enter/leave messages
 *
 * Consolidated for v1.2.5
 */
public class PlotListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final ClaimRoleManager roleManager;
    private final MessagesUtil messages;

    public PlotListener(ProShield plugin, PlotManager plotManager, ClaimRoleManager roleManager, MessagesUtil messages) {
        this.plugin = plugin;
        this.plotManager = plotManager;
        this.roleManager = roleManager;
        this.messages = messages;
    }

    /* ======================================================
     * Block protections
     * ====================================================== */
    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        Player player = e.getPlayer();
        if (!canModify(player, e.getBlock().getLocation())) {
            e.setCancelled(true);
            messages.send(player, "error.no-permission");
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        Player player = e.getPlayer();
        if (!canModify(player, e.getBlock().getLocation())) {
            e.setCancelled(true);
            messages.send(player, "error.no-permission");
        }
    }

    /* ======================================================
     * Interactions (doors, containers, etc.)
     * ====================================================== */
    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock() != null) {
            Player player = e.getPlayer();
            Block block = e.getClickedBlock();
            if (!canInteract(player, block.getLocation())) {
                e.setCancelled(true);
                messages.send(player, "error.no-permission");
            }
        }
    }

    /* ======================================================
     * Buckets
     * ====================================================== */
    @EventHandler
    public void onBucketEmpty(PlayerBucketEmptyEvent e) {
        if (!canModify(e.getPlayer(), e.getBlock().getLocation())) {
            e.setCancelled(true);
            messages.send(e.getPlayer(), "error.no-permission");
        }
    }

    @EventHandler
    public void onBucketFill(PlayerBucketFillEvent e) {
        if (!canModify(e.getPlayer(), e.getBlock().getLocation())) {
            e.setCancelled(true);
            messages.send(e.getPlayer(), "error.no-permission");
        }
    }

    /* ======================================================
     * Item Frames & Armor Stands
     * ====================================================== */
    @EventHandler
    public void onHangingPlace(HangingPlaceEvent e) {
        Player player = e.getPlayer();
        if (!canModify(player, e.getEntity().getLocation())) {
            e.setCancelled(true);
            messages.send(player, "error.no-permission");
        }
    }

    @EventHandler
    public void onHangingBreak(HangingBreakByEntityEvent e) {
        if (e.getRemover() instanceof Player player) {
            if (!canModify(player, e.getEntity().getLocation())) {
                e.setCancelled(true);
                messages.send(player, "error.no-permission");
            }
        }
    }

    /* ======================================================
     * Vehicles
     * ====================================================== */
    @EventHandler
    public void onVehicleDestroy(VehicleDestroyEvent e) {
        if (e.getAttacker() instanceof Player player) {
            if (!canModify(player, e.getVehicle().getLocation())) {
                e.setCancelled(true);
                messages.send(player, "error.no-permission");
            }
        }
    }

    /* ======================================================
     * Claim enter/leave messages
     * ====================================================== */
    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        String fromClaim = plotManager.getClaimName(e.getFrom());
        String toClaim   = plotManager.getClaimName(e.getTo());

        if (!fromClaim.equals(toClaim)) {
            if (!"Wilderness".equalsIgnoreCase(fromClaim)) {
                messages.send(player, "claim.leaving", java.util.Map.of("claim", fromClaim));
            }
            if ("Wilderness".equalsIgnoreCase(toClaim)) {
                if (plugin.getConfig().getBoolean("messages.show-wilderness", false)) {
                    messages.send(player, "claim.wilderness");
                }
            } else {
                messages.send(player, "claim.entering", java.util.Map.of("claim", toClaim));
            }
        }
    }

    /* ======================================================
     * Helpers
     * ====================================================== */
    private boolean canModify(Player player, Location loc) {
        UUID playerId = player.getUniqueId();
        Plot plot = plotManager.getPlot(loc);
        if (plot == null) return true;
        return plot.isOwner(playerId) || roleManager.canManage(playerId, plot.getId());
    }

    private boolean canInteract(Player player, Location loc) {
        UUID playerId = player.getUniqueId();
        Plot plot = plotManager.getPlot(loc);
        if (plot == null) return true;
        return plot.isOwner(playerId) || roleManager.canManage(playerId, plot.getId());
    }
}
