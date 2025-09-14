// src/main/java/com/snazzyatoms/proshield/plots/PlotListener.java
package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Material;
import org.bukkit.block.Container;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

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

    // -------------------
    // PVP
    // -------------------
    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;
        if (!(event.getDamager() instanceof Player attacker)) return;

        Plot plot = plotManager.getPlot(victim.getLocation());
        if (plot != null && !plot.getFlag("pvp", false)) {
            event.setCancelled(true);
            messages.send(attacker, "&cPVP is disabled in this claim.");
        }
    }

    // -------------------
    // EXPLOSIONS
    // -------------------
    @EventHandler
    public void onExplosion(EntityExplodeEvent event) {
        Plot plot = plotManager.getPlot(event.getLocation());
        if (plot != null && !plot.getFlag("explosions", false)) {
            event.blockList().clear(); // no block damage
        }
    }

    // -------------------
    // BUCKET USE
    // -------------------
    @EventHandler
    public void onBucketUse(PlayerBucketEmptyEvent event) {
        Plot plot = plotManager.getPlot(event.getBlockClicked().getLocation());
        if (plot != null && !plot.getFlag("buckets", false)) {
            event.setCancelled(true);
            messages.send(event.getPlayer(), "&cBuckets are disabled in this claim.");
        }
    }

    // -------------------
    // CONTAINERS
    // -------------------
    @EventHandler
    public void onContainerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;
        if (!(event.getClickedBlock().getState() instanceof Container)) return;

        Plot plot = plotManager.getPlot(event.getClickedBlock().getLocation());
        if (plot != null && !plot.getFlag("containers", false)) {
            event.setCancelled(true);
            messages.send(event.getPlayer(), "&cYou cannot open containers in this claim.");
        }
    }

    // -------------------
    // PETS & ANIMALS
    // -------------------
    @EventHandler
    public void onPetDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Tameable pet)) return;
        Plot plot = plotManager.getPlot(event.getEntity().getLocation());
        if (plot != null && !plot.getFlag("pets", false)) {
            event.setCancelled(true);
            if (event.getDamager() instanceof Player player) {
                messages.send(player, "&cPets are protected in this claim.");
            }
        }
    }

    // -------------------
    // ITEM FRAMES / ARMOR STANDS
    // -------------------
    @EventHandler
    public void onFrameBreak(HangingBreakByEntityEvent event) {
        Plot plot = plotManager.getPlot(event.getEntity().getLocation());
        if (plot != null && event.getRemover() instanceof Player player) {
            if (event.getEntity() instanceof ItemFrame && !plot.getFlag("item-frames", false)) {
                event.setCancelled(true);
                messages.send(player, "&cItem frames are protected.");
            }
        }
    }

    @EventHandler
    public void onArmorStandInteract(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof ArmorStand)) return;
        Plot plot = plotManager.getPlot(event.getRightClicked().getLocation());
        if (plot != null && !plot.getFlag("armor-stands", false)) {
            event.setCancelled(true);
            messages.send(event.getPlayer(), "&cArmor stands are protected.");
        }
    }

    // -------------------
    // BLOCK PLACE INSIDE CLAIM
    // -------------------
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Plot plot = plotManager.getPlot(event.getBlock().getLocation());
        if (plot != null && !plot.isOwner(event.getPlayer().getUniqueId()) && !plot.isTrusted(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
            messages.send(event.getPlayer(), "&cYou cannot place blocks in this claim.");
        }
    }
}
