// src/main/java/com/snazzyatoms/proshield/plots/PlotListener.java
package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleDestroyEvent;

import java.util.Map;
import java.util.UUID;

/**
 * PlotListener
 * - Unified claim protection handler
 * - Covers blocks, buckets, containers, item frames, vehicles
 * - Adds explosions, fire spread, piston checks
 * - Handles claim enter/leave messages
 *
 * Consolidated for v1.2.5+
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
        if (!canModify(e.getPlayer(), e.getBlock().getLocation())) {
            e.setCancelled(true);
            messages.send(e.getPlayer(), "error.no-permission");
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        if (!canModify(e.getPlayer(), e.getBlock().getLocation())) {
            e.setCancelled(true);
            messages.send(e.getPlayer(), "error.no-permission");
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
            if (!canInteract(player, block.getLocation(), block.getType())) {
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
     * Explosions
     * ====================================================== */
    @EventHandler
    public void onEntityExplode(EntityExplodeEvent e) {
        e.blockList().removeIf(block -> {
            Plot plot = plotManager.getPlot(block.getLocation());
            return plot != null; // block explosion inside claims
        });
    }

    @EventHandler
    public void onExplosionPrime(ExplosionPrimeEvent e) {
        Plot plot = plotManager.getPlot(e.getEntity().getLocation());
        if (plot != null) {
            e.setCancelled(true); // stop creepers, TNT minecarts, beds, etc.
        }
    }

    /* ======================================================
     * Fire Spread
     * ====================================================== */
    @EventHandler
    public void onBlockSpread(BlockSpreadEvent e) {
        if (e.getSource().getType() == Material.FIRE) {
            Plot plot = plotManager.getPlot(e.getBlock().getLocation());
            if (plot != null) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockIgnite(BlockIgniteEvent e) {
        Plot plot = plotManager.getPlot(e.getBlock().getLocation());
        if (plot != null) {
            e.setCancelled(true);
        }
    }

    /* ======================================================
     * Pistons (push/pull into claims)
     * ====================================================== */
    @EventHandler
    public void onPistonExtend(BlockPistonExtendEvent e) {
        for (Block block : e.getBlocks()) {
            Location loc = block.getRelative(e.getDirection()).getLocation();
            Plot from = plotManager.getPlot(block.getLocation());
            Plot to   = plotManager.getPlot(loc);
            if (from != null && to == null || from == null && to != null) {
                e.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onPistonRetract(BlockPistonRetractEvent e) {
        for (Block block : e.getBlocks()) {
            Plot from = plotManager.getPlot(block.getLocation());
            Plot to   = plotManager.getPlot(e.getBlock().getLocation());
            if (from != null && to == null || from == null && to != null) {
                e.setCancelled(true);
                return;
            }
        }
    }

    /* ======================================================
     * Claim enter/leave messages
     * ====================================================== */
    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (e.getFrom().getChunk().equals(e.getTo().getChunk())) return;

        Player player = e.getPlayer();
        String fromClaim = plotManager.getClaimName(e.getFrom());
        String toClaim   = plotManager.getClaimName(e.getTo());

        if (!fromClaim.equals(toClaim)) {
            if (!"Wilderness".equalsIgnoreCase(fromClaim)) {
                messages.send(player, "claim.leaving", Map.of("claim", fromClaim));
            }
            if ("Wilderness".equalsIgnoreCase(toClaim)) {
                if (plugin.getConfig().getBoolean("messages.show-wilderness", false)) {
                    messages.send(player, "claim.wilderness");
                }
            } else {
                messages.send(player, "claim.entering", Map.of("claim", toClaim));
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

    private boolean canInteract(Player player, Location loc, Material type) {
        UUID playerId = player.getUniqueId();
        Plot plot = plotManager.getPlot(loc);
        if (plot == null) return true;

        if (isContainer(type)) {
            return plot.isOwner(playerId) || roleManager.canContainers(playerId, plot.getId());
        }

        return plot.isOwner(playerId) || roleManager.canInteract(playerId, plot.getId());
    }

    private boolean isContainer(Material type) {
        return switch (type) {
            case CHEST, TRAPPED_CHEST, FURNACE, BLAST_FURNACE, SMOKER,
                 HOPPER, DROPPER, DISPENSER, BARREL, SHULKER_BOX -> true;
            default -> false;
        };
    }
}
