// src/main/java/com/snazzyatoms/proshield/plots/PlotListener.java
package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
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
 * - Unified protection & claim boundary messages
 * - Covers block, bucket, interaction, entry/exit, and wilderness checks
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

    /* ------------------------------------------------------
     * BLOCK BREAK / PLACE
     * ------------------------------------------------------ */
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

    /* ------------------------------------------------------
     * BUCKETS
     * ------------------------------------------------------ */
    @EventHandler(ignoreCancelled = true)
    public void onBucketEmpty(PlayerBucketEmptyEvent e) {
        handleBucketEvent(e.getPlayer(), e.getBlockClicked().getLocation(), e);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBucketFill(PlayerBucketFillEvent e) {
        handleBucketEvent(e.getPlayer(), e.getBlockClicked().getLocation(), e);
    }

    private void handleBucketEvent(Player player, Location loc, org.bukkit.event.Cancellable e) {
        if (!plotManager.canInteract(player.getUniqueId(), loc)) {
            e.setCancelled(true);
            messages.send(player, "error.no-permission");
        }
    }

    /* ------------------------------------------------------
     * INTERACTIONS (chests, doors, item frames, etc.)
     * ------------------------------------------------------ */
    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent e) {
        if (e.getHand() != EquipmentSlot.HAND) return; // ignore offhand
        if (e.getClickedBlock() == null) return;

        Player player = e.getPlayer();
        Location loc = e.getClickedBlock().getLocation();

        if (!plotManager.canInteract(player.getUniqueId(), loc)) {
            e.setCancelled(true);
            messages.send(player, "error.no-permission");
        }
    }

    /* ------------------------------------------------------
     * CLAIM ENTRY / EXIT MESSAGES
     * ------------------------------------------------------ */
    private final Map<UUID, String> lastClaim = new HashMap<>();

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (e.getFrom().getChunk().equals(e.getTo().getChunk())) return;

        Player player = e.getPlayer();
        String fromClaim = plotManager.getClaimName(e.getFrom());
        String toClaim = plotManager.getClaimName(e.getTo());

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
            lastClaim.put(player.getUniqueId(), toClaim);
        }
    }
}
