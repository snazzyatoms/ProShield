// src/main/java/com/snazzyatoms/proshield/plots/PlotListener.java
package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.roles.ClaimRole;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * PlotListener
 * Unified protection/event listener for ProShield.
 * Combines all old listeners into one place (block, entity, interaction, flags).
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

    /* ------------------------------------------------------
     * Claim Enter/Exit Messages
     * ------------------------------------------------------ */
    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();

        String fromClaim = plotManager.getClaimName(e.getFrom());
        String toClaim   = plotManager.getClaimName(e.getTo());

        if (!fromClaim.equals(toClaim)) {
            // Leaving claim
            if (!fromClaim.equalsIgnoreCase("Wilderness")) {
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("claim", fromClaim);
                messages.send(p, "claim.leaving", placeholders);
            }

            // Entering claim or wilderness
            if ("Wilderness".equalsIgnoreCase(toClaim)) {
                if (plugin.getConfig().getBoolean("messages.show-wilderness", false)) {
                    messages.send(p, "claim.wilderness");
                }
            } else {
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("claim", toClaim);
                messages.send(p, "claim.entering", placeholders);
            }
        }
    }

    /* ------------------------------------------------------
     * Block Break / Place
     * ------------------------------------------------------ */
    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if (!canModify(e.getPlayer(), e.getBlock().getLocation())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        if (!canModify(e.getPlayer(), e.getBlock().getLocation())) {
            e.setCancelled(true);
        }
    }

    /* ------------------------------------------------------
     * Player Interactions (doors, redstone, etc.)
     * ------------------------------------------------------ */
    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getHand() != EquipmentSlot.HAND) return;
        if (e.getClickedBlock() == null) return;

        if (!canInteract(e.getPlayer(), e.getClickedBlock().getLocation())) {
            e.setCancelled(true);
        }
    }

    /* ------------------------------------------------------
     * Buckets (water/lava)
     * ------------------------------------------------------ */
    @EventHandler
    public void onBucketEmpty(PlayerBucketEmptyEvent e) {
        if (!canModify(e.getPlayer(), e.getBlock().getLocation())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBucketFill(PlayerBucketFillEvent e) {
        if (!canModify(e.getPlayer(), e.getBlock().getLocation())) {
            e.setCancelled(true);
        }
    }

    /* ------------------------------------------------------
     * Explosions (TNT, Creeper, etc.)
     * ------------------------------------------------------ */
    @EventHandler
    public void onEntityExplode(EntityExplodeEvent e) {
        Plot plot = plotManager.getPlot(e.getLocation());
        if (plot != null && !plot.getFlag("explosions")) {
            e.blockList().clear(); // prevent block damage
        }
    }

    /* ------------------------------------------------------
     * Fire spread & ignite
     * ------------------------------------------------------ */
    @EventHandler
    public void onBlockIgnite(BlockIgniteEvent e) {
        Plot plot = plotManager.getPlot(e.getBlock().getLocation());
        if (plot != null && !plot.getFlag("fire")) {
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

    /* ------------------------------------------------------
     * Entity grief (endermen, villagers, etc.)
     * ------------------------------------------------------ */
    @EventHandler
    public void onEntityChangeBlock(EntityChangeBlockEvent e) {
        Plot plot = plotManager.getPlot(e.getBlock().getLocation());
        if (plot != null && !plot.getFlag("entity-grief")) {
            e.setCancelled(true);
        }
    }

    /* ------------------------------------------------------
     * PvP Toggle
     * ------------------------------------------------------ */
    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player victim)) return;

        Player attacker = null;
        if (e.getDamager() instanceof Player p) attacker = p;
        if (e.getDamager() instanceof Projectile proj && proj.getShooter() instanceof Player p) attacker = p;
        if (attacker == null) return;

        Plot plot = plotManager.getPlot(victim.getLocation());
        if (plot != null && !plot.getFlag("pvp")) {
            e.setCancelled(true);
        }
    }

    /* ------------------------------------------------------
     * Helpers
     * ------------------------------------------------------ */
    private boolean canModify(Player player, org.bukkit.Location loc) {
        if (player.isOp()) return true;
        UUID uuid = player.getUniqueId();
        return plotManager.isTrustedOrOwner(uuid, loc);
    }

    private boolean canInteract(Player player, org.bukkit.Location loc) {
        if (player.isOp()) return true;
        UUID uuid = player.getUniqueId();
        return plotManager.canInteract(uuid, loc);
    }
}
