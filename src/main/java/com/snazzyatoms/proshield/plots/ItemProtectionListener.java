// src/main/java/com/snazzyatoms/proshield/plots/ItemProtectionListener.java
package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Chunk;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;

import java.util.UUID;

/**
 * ItemProtectionListener
 *
 * ✅ Protects item frames, armor stands, animals, pets, containers, and vehicles
 * ✅ Uses per-claim PlotSettings + global config
 * ✅ Respects claim roles via ClaimRoleManager
 */
public class ItemProtectionListener implements Listener {

    private final PlotManager plots;
    private final ClaimRoleManager roles;
    private final MessagesUtil messages;

    public ItemProtectionListener(PlotManager plots, ClaimRoleManager roles, MessagesUtil messages) {
        this.plots = plots;
        this.roles = roles;
        this.messages = messages;
    }

    /* ------------------------------
     * Item Frames
     * ------------------------------ */
    @EventHandler(ignoreCancelled = true)
    public void onHangingBreak(HangingBreakByEntityEvent event) {
        if (!(event.getRemover() instanceof Player player)) return;

        Chunk chunk = event.getEntity().getLocation().getChunk();
        Plot plot = plots.getPlot(chunk);

        if (plot == null) {
            // Wilderness: use global toggle
            if (!player.hasPermission("proshield.admin") &&
                !player.getServer().getPluginManager().getPlugin("ProShield")
                      .getConfig().getBoolean("protection.entities.item-frames", true)) {
                event.setCancelled(true);
                messages.send(player, "item-frames-deny");
            }
            return;
        }

        UUID uid = player.getUniqueId();
        if (!plot.getSettings().isItemFramesAllowed() || !roles.canBuild(uid, plot)) {
            event.setCancelled(true);
            messages.send(player, "item-frames-deny");
            messages.debug("&cPrevented item frame break in claim [" + plot.getName() + "] by " + player.getName());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onHangingPlace(HangingPlaceEvent event) {
        Player player = event.getPlayer();
        Chunk chunk = event.getEntity().getLocation().getChunk();
        Plot plot = plots.getPlot(chunk);

        if (plot == null) return;

        UUID uid = player.getUniqueId();
        if (!plot.getSettings().isItemFramesAllowed() || !roles.canBuild(uid, plot)) {
            event.setCancelled(true);
            messages.send(player, "item-frames-deny");
        }
    }

    /* ------------------------------
     * Interacting (Armor Stands, Animals, Pets, Containers)
     * ------------------------------ */
    @EventHandler(ignoreCancelled = true)
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();
        Chunk chunk = entity.getLocation().getChunk();
        Plot plot = plots.getPlot(chunk);

        if (plot == null) return; // wilderness → config handles it

        UUID uid = player.getUniqueId();

        // Armor stands
        if (entity instanceof ArmorStand && !plot.getSettings().isArmorStandsAllowed()) {
            if (!roles.canInteract(uid, plot)) {
                event.setCancelled(true);
                messages.send(player, "armor-stands-deny");
                return;
            }
        }

        // Passive animals
        if (entity instanceof Animals && !plot.getSettings().isAnimalAccessAllowed()) {
            if (!roles.canInteract(uid, plot)) {
                event.setCancelled(true);
                messages.send(player, "animals-de
