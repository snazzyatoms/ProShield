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
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * PlotListener
 * - Unified handler for block, bucket, item, and movement protection.
 * - Condensed from multiple listeners (v1.2.0 → v1.2.5).
 * - Uses PlotManager + ClaimRoleManager for trust/role checks.
 */
public class PlotListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final ClaimRoleManager roleManager;
    private final MessagesUtil messages;

    // Cache player → last claim name (for enter/exit messages)
    private final Map<UUID, String> lastClaimMap = new HashMap<>();

    public PlotListener(ProShield plugin, PlotManager plotManager, ClaimRoleManager roleManager, MessagesUtil messages) {
        this.plugin = plugin;
        this.plotManager = plotManager;
        this.roleManager = roleManager;
        this.messages = messages;
    }

    /* -------------------------------------------------------
     * Block Breaking / Placing
     * ------------------------------------------------------- */
    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e) {
        Player player = e.getPlayer();
        Location loc = e.getBlock().getLocation();

        if (!plotManager.canInteract(player.getUniqueId(), loc)) {
            e.setCancelled(true);
            messages.send(player, "error.no-permission");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent e) {
        Player player = e.getPlayer();
        Location loc = e.getBlock().getLocation();

        if (!plotManager.canInteract(player.getUniqueId(), loc)) {
            e.setCancelled(true);
            messages.send(player, "error.no-permission");
        }
    }

    /* -------------------------------------------------------
     * Buckets (fill + empty)
     * ------------------------------------------------------- */
    @EventHandler(ignoreCancelled = true)
    public void onBucketEmpty(PlayerBucketEmptyEvent e) {
        Player player = e.getPlayer();
        Location loc = e.getBlock().getLocation();

        if (!plotManager.canInteract(player.getUniqueId(), loc)) {
            e.setCancelled(true);
            messages.send(player, "error.no-permission");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBucketFill(PlayerBucketFillEvent e) {
        Player player = e.getPlayer();
        Location loc = e.getBlock().getLocation();

        if (!plotManager.canInteract(player.getUniqueId(), loc)) {
            e.setCancelled(true);
            messages.send(player, "error.no-permission");
        }
    }

    /* -------------------------------------------------------
     * Interactions (doors, containers, armor stands, etc.)
     * ------------------------------------------------------- */
    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent e) {
        if (e.getHand() != EquipmentSlot.HAND) return; // only main hand
        Player player = e.getPlayer();

        Block block = e.getClickedBlock();
        if (block == null) return;

        Material type = block.getType();
        Location loc = block.getLocation();

        if (!plotManager.canInteract(player.getUniqueId(), loc)) {
            // Containers & interactive blocks blocked
            if (type.toString().contains("CHEST") ||
                type.toString().contains("FURNACE") ||
                type.toString().contains("HOPPER") ||
                type == Material.ARMOR_STAND ||
                type == Material.ITEM_FRAME) {
                e.setCancelled(true);
                messages.send(player, "error.no-permission");
            }
        }
    }

    /* -------------------------------------------------------
     * Claim Enter / Exit Messages
     * ------------------------------------------------------- */
    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        Location from = e.getFrom();
        Location to = e.getTo();
        if (to == null) return;

        String fromClaim = plotManager.getClaimName(from);
        String toClaim = plotManager.getClaimName(to);

        if (!fromClaim.equalsIgnoreCase(toClaim)) {
            UUID id = player.getUniqueId();
            String last = lastClaimMap.getOrDefault(id, "");

            if (!toClaim.equalsIgnoreCase(last)) {
                // Leaving claim
                if (!"Wilderness".equalsIgnoreCase(fromClaim)) {
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("claim", fromClaim);
                    messages.send(player, "claim.leaving", placeholders);
                }

                // Entering claim / wilderness
                if ("Wilderness".equalsIgnoreCase(toClaim)) {
                    if (plugin.getConfig().getBoolean("messages.show-wilderness", false)) {
                        messages.send(player, "claim.wilderness");
                    }
                } else {
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("claim", toClaim);
                    messages.send(player, "claim.entering", placeholders);
                }

                lastClaimMap.put(id, toClaim);
            }
        }
    }
}
