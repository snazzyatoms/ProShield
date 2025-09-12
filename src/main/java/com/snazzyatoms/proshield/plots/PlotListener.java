// src/main/java/com/snazzyatoms/proshield/plots/PlotListener.java
package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.roles.ClaimRole;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.UUID;

/**
 * PlotListener
 * - Unified from v1.2.0 → v1.2.5
 * - Handles block breaks, places, fire, explosions, buckets,
 *   item frames, armor stands, containers, vehicles, pets.
 * - Uses PlotManager + ClaimRoleManager + config flags.
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

    /* -------------------------------------------------------
     * Block protection (break + place)
     * ------------------------------------------------------- */
    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if (!canModify(e.getPlayer(), e.getBlock())) {
            e.setCancelled(true);
            messages.send(e.getPlayer(), "error.no-permission");
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        if (!canModify(e.getPlayer(), e.getBlockPlaced())) {
            e.setCancelled(true);
            messages.send(e.getPlayer(), "error.no-permission");
        }
    }

    /* -------------------------------------------------------
     * Fire + Explosions
     * ------------------------------------------------------- */
    @EventHandler
    public void onBlockIgnite(BlockIgniteEvent e) {
        if (e.getPlayer() == null) return;
        if (!canModify(e.getPlayer(), e.getBlock())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBurn(BlockBurnEvent e) {
        Plot plot = plotManager.getPlot(e.getBlock().getLocation());
        if (plot != null && !plot.getFlag("fire")) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent e) {
        e.blockList().removeIf(block -> {
            Plot plot = plotManager.getPlot(block.getLocation());
            return plot != null && !plot.getFlag("explosions");
        });
    }

    /* -------------------------------------------------------
     * Buckets
     * ------------------------------------------------------- */
    @EventHandler
    public void onBucketEmpty(PlayerBucketEmptyEvent e) {
        if (!canModify(e.getPlayer(), e.getBlockClicked())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBucketFill(PlayerBucketFillEvent e) {
        if (!canModify(e.getPlayer(), e.getBlockClicked())) {
            e.setCancelled(true);
        }
    }

    /* -------------------------------------------------------
     * Interactions (containers, armor stands, item frames, pets)
     * ------------------------------------------------------- */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (e.getHand() == EquipmentSlot.OFF_HAND) return; // ignore off-hand
        if (e.getClickedBlock() == null) return;

        Block block = e.getClickedBlock();
        Player player = e.getPlayer();
        Plot plot = plotManager.getPlot(block.getLocation());
        if (plot == null) return;

        Material type = block.getType();

        if (isContainer(type) && !plot.getFlag("containers") && !plot.isOwner(player.getUniqueId())) {
            if (!plotManager.isTrustedOrOwner(player.getUniqueId(), block.getLocation())) {
                e.setCancelled(true);
                messages.send(player, "error.no-permission");
            }
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent e) {
        Player player = e.getPlayer();
        Entity entity = e.getRightClicked();
        Plot plot = plotManager.getPlot(entity.getLocation());
        if (plot == null) return;

        if (entity.getType().toString().contains("ARMOR_STAND") && !plot.getFlag("armor-stands")) {
            e.setCancelled(true);
        }
        if (entity.getType().toString().contains("ITEM_FRAME") && !plot.getFlag("item-frames")) {
            e.setCancelled(true);
        }
        if (entity instanceof org.bukkit.entity.Animals && !plot.getFlag("animals")) {
            e.setCancelled(true);
        }
        if (entity instanceof org.bukkit.entity.Tameable && !plot.getFlag("pets")) {
            e.setCancelled(true);
        }
    }

    /* -------------------------------------------------------
     * Vehicles
     * ------------------------------------------------------- */
    @EventHandler
    public void onVehicleDestroy(VehicleDestroyEvent e) {
        if (!(e.getAttacker() instanceof Player player)) return;

        Vehicle vehicle = e.getVehicle();
        Plot plot = plotManager.getPlot(vehicle.getLocation());
        if (plot == null) return;

        if (!plot.getFlag("vehicles") && !plot.isOwner(player.getUniqueId())) {
            e.setCancelled(true);
            messages.send(player, "error.no-permission");
        }
    }

    /* -------------------------------------------------------
     * Utility
     * ------------------------------------------------------- */
    private boolean canModify(Player player, Block block) {
        if (player.hasPermission("proshield.bypass")) return true;
        Plot plot = plotManager.getPlot(block.getLocation());
        if (plot == null) return true;

        UUID playerId = player.getUniqueId();
        if (plot.isOwner(playerId)) return true;

        ClaimRole role = plot.getRole(playerId);
        return role != null && role.canBuild();
    }

    private boolean isContainer(Material mat) {
        return mat == Material.CHEST ||
               mat == Material.TRAPPED_CHEST ||
               mat == Material.BARREL ||
               mat == Material.HOPPER ||
               mat == Material.FURNACE ||
               mat == Material.BLAST_FURNACE ||
               mat == Material.SMOKER ||
               mat == Material.DISPENSER ||
               mat == Material.DROPPER ||
               mat == Material.SHULKER_BOX;
    }
}
