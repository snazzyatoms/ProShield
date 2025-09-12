// src/main/java/com/snazzyatoms/proshield/plots/PlotListener.java
package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

/**
 * PlotListener
 * - Unified protection listener for claims
 * - Handles block, bucket, interaction, fire, explosions, PvP, and entities
 */
public class PlotListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final MessagesUtil messages;

    public PlotListener(ProShield plugin, PlotManager plotManager, MessagesUtil messages) {
        this.plugin = plugin;
        this.plotManager = plotManager;
        this.messages = messages;
    }

    /* ======================================================
     * BLOCK BREAK / PLACE
     * ====================================================== */
    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e) {
        Player p = e.getPlayer();
        if (!canBuild(p.getUniqueId(), e.getBlock())) {
            e.setCancelled(true);
            messages.send(p, "error.not-trusted");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent e) {
        Player p = e.getPlayer();
        if (!canBuild(p.getUniqueId(), e.getBlock())) {
            e.setCancelled(true);
            messages.send(p, "error.not-trusted");
        }
    }

    /* ======================================================
     * BUCKETS
     * ====================================================== */
    @EventHandler(ignoreCancelled = true)
    public void onBucketEmpty(PlayerBucketEmptyEvent e) {
        if (!canBuild(e.getPlayer().getUniqueId(), e.getBlockClicked())) {
            e.setCancelled(true);
            messages.send(e.getPlayer(), "error.not-trusted");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBucketFill(PlayerBucketFillEvent e) {
        if (!canBuild(e.getPlayer().getUniqueId(), e.getBlockClicked())) {
            e.setCancelled(true);
            messages.send(e.getPlayer(), "error.not-trusted");
        }
    }

    /* ======================================================
     * INTERACTIONS
     * ====================================================== */
    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent e) {
        if (e.getClickedBlock() == null) return;
        Player p = e.getPlayer();
        if (!canInteract(p.getUniqueId(), e.getClickedBlock())) {
            e.setCancelled(true);
            messages.send(p, "error.not-trusted");
        }
    }

    /* ======================================================
     * FIRE
     * ====================================================== */
    @EventHandler(ignoreCancelled = true)
    public void onIgnite(BlockIgniteEvent e) {
        if (e.getPlayer() != null && !canBuild(e.getPlayer().getUniqueId(), e.getBlock())) {
            e.setCancelled(true);
            messages.send(e.getPlayer(), "error.not-trusted");
        }
    }

    /* ======================================================
     * EXPLOSIONS
     * ====================================================== */
    @EventHandler(ignoreCancelled = true)
    public void onExplode(EntityExplodeEvent e) {
        e.blockList().removeIf(block -> {
            Plot plot = plotManager.getPlot(block.getLocation());
            return plot != null; // remove block destruction inside claims
        });
    }

    /* ======================================================
     * PVP + ENTITY DAMAGE
     * ====================================================== */
    @EventHandler(ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof Player victim && e.getDamager() instanceof Player attacker) {
            Plot plot = plotManager.getPlot(victim.getLocation());
            if (plot != null && !plugin.getConfig().getBoolean("claims.default-flags.pvp", false)) {
                e.setCancelled(true);
                messages.send(attacker, "error.not-trusted");
            }
        }
    }

    /* ======================================================
     * ENTITY SPAWN / MOB CONTROL
     * ====================================================== */
    @EventHandler(ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent e) {
        Plot plot = plotManager.getPlot(e.getLocation());
        if (plot != null && !plugin.getConfig().getBoolean("claims.default-flags.animals", false)) {
            if (e.getEntity() instanceof Animals || e.getEntity() instanceof Villager) {
                e.setCancelled(true);
            }
        }
    }

    /* ======================================================
     * HELPERS
     * ====================================================== */
    private boolean canBuild(UUID playerId, Block block) {
        return plotManager.isTrustedOrOwner(playerId, block.getLocation());
    }

    private boolean canInteract(UUID playerId, Block block) {
        return plotManager.canInteract(playerId, block.getLocation());
    }
}
