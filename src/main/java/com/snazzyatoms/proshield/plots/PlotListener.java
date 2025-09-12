// src/main/java/com/snazzyatoms/proshield/plots/PlotListener.java
package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * PlotListener
 * - Unified protection & claim boundary messages
 * - Block/place, buckets, interactions
 * - Claim entry/exit messages
 * - Mob repel & border repel tasks (replaces EntityMobRepelTask + EntityBorderRepelTask)
 */
public class PlotListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final ClaimRoleManager roleManager;
    private final MessagesUtil messages;

    private final Map<UUID, String> lastClaim = new HashMap<>();

    public PlotListener(ProShield plugin, PlotManager plotManager, ClaimRoleManager roleManager, MessagesUtil messages) {
        this.plugin = plugin;
        this.plotManager = plotManager;
        this.roleManager = roleManager;
        this.messages = messages;

        // Schedule mob repel tasks
        new BukkitRunnable() {
            @Override
            public void run() {
                repelMobs();
            }
        }.runTaskTimer(plugin, 20L, 20L * 5); // every 5s

        new BukkitRunnable() {
            @Override
            public void run() {
                repelBorders();
            }
        }.runTaskTimer(plugin, 20L, 20L * 3); // every 3s
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
     * INTERACTIONS
     * ------------------------------------------------------ */
    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent e) {
        if (e.getHand() != EquipmentSlot.HAND) return;
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

    /* ------------------------------------------------------
     * MOB REPEL TASKS
     * ------------------------------------------------------ */
    private void repelMobs() {
        double radius = plugin.getConfig().getDouble("protection.mobs.border-repel.radius", 3.0);
        double hPush = plugin.getConfig().getDouble("protection.mobs.border-repel.horizontal-push", 0.7);
        double vPush = plugin.getConfig().getDouble("protection.mobs.border-repel.vertical-push", 0.25);

        for (Player player : Bukkit.getOnlinePlayers()) {
            Location loc = player.getLocation();
            Plot plot = plotManager.getPlot(loc);
            if (plot == null) continue;

            for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
                if (entity instanceof Mob mob) {
                    double dx = mob.getLocation().getX() - loc.getX();
                    double dz = mob.getLocation().getZ() - loc.getZ();
                    double dist = Math.sqrt(dx * dx + dz * dz);
                    if (dist < radius && dist > 0) {
                        mob.setVelocity(mob.getVelocity().setX(dx / dist * hPush).setZ(dz / dist * hPush).setY(vPush));
                    }
                }
            }
        }
    }

    private void repelBorders() {
        // future: border-specific logic if needed
    }
}
