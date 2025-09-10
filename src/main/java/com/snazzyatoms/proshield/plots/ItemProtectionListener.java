package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.roles.ClaimRole;
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

/**
 * Handles protections for entities inside claims:
 * - Item frames
 * - Armor stands
 * - Containers (via PlayerInteractEntityEvent)
 * - Vehicles (boats, minecarts)
 * - Passive animals & pets
 *
 * Uses global + per-claim rules from PlotSettings.
 */
public class ItemProtectionListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final ClaimRoleManager roleManager;
    private final MessagesUtil messages;

    public ItemProtectionListener(ProShield plugin, PlotManager plotManager, ClaimRoleManager roleManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
        this.roleManager = roleManager;
        this.messages = plugin.getMessagesUtil();
    }

    /* ------------------------------
     * Item Frames & Armor Stands
     * ------------------------------ */
    @EventHandler(ignoreCancelled = true)
    public void onHangingBreak(HangingBreakByEntityEvent event) {
        if (!(event.getRemover() instanceof Player player)) return;

        Chunk chunk = event.getEntity().getLocation().getChunk();
        Plot plot = plotManager.getPlot(chunk);

        if (plot == null) {
            if (!plugin.getConfig().getBoolean("protection.entities.item-frames", true)) {
                event.setCancelled(true);
                messages.send(player, "item-frames-deny");
            }
            return;
        }

        if (!plot.getSettings().isItemFramesAllowed()) {
            event.setCancelled(true);
            messages.send(player, "item-frames-deny");
            messages.debug("&cPrevented item frame break inside claim: " + plot.getDisplayNameSafe());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onHangingPlace(HangingPlaceEvent event) {
        Player player = event.getPlayer();
        Chunk chunk = event.getEntity().getLocation().getChunk();
        Plot plot = plotManager.getPlot(chunk);

        if (plot == null) return;

        if (!plot.getSettings().isItemFramesAllowed()) {
            event.setCancelled(true);
            messages.send(player, "item-frames-deny");
        }
    }

    /* ------------------------------
     * Interacting with entities (Armor Stands, Animals, Containers)
     * ------------------------------ */
    @EventHandler(ignoreCancelled = true)
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();
        Chunk chunk = entity.getLocation().getChunk();
        Plot plot = plotManager.getPlot(chunk);

        if (plot == null) return; // wilderness - handled by config elsewhere

        // Role currently unused, but left for expansion (e.g., allow builders to access containers)
        ClaimRole role = roleManager.getRole(plot, player.getUniqueId());

        // Armor stands
        if (entity instanceof ArmorStand && !plot.getSettings().isArmorStandsAllowed()) {
            event.setCancelled(true);
            messages.send(player, "armor-stands-deny");
            return;
        }

        // Passive animals
        if (entity instanceof Animals && !plot.getSettings().isAnimalAccessAllowed()) {
            event.setCancelled(true);
            messages.send(player, "animals-deny");
            return;
        }

        // Tamed pets
        if (entity instanceof Tameable tameable && tameable.isTamed() && !plot.getSettings().isPetAccessAllowed()) {
            event.setCancelled(true);
            messages.send(player, "pets-deny");
            return;
        }

        // Containers (minecarts with chest, etc.)
        if (entity instanceof Minecart && !plot.getSettings().isContainersAllowed()) {
            event.setCancelled(true);
            messages.send(player, "containers-deny");
        }
    }

    /* ------------------------------
     * Vehicle Protections
     * ------------------------------ */
    @EventHandler(ignoreCancelled = true)
    public void onVehicleDestroy(VehicleDestroyEvent event) {
        if (!(event.getAttacker() instanceof Player player)) return;

        Chunk chunk = event.getVehicle().getLocation().getChunk();
        Plot plot = plotManager.getPlot(chunk);

        if (plot == null) {
            if (!plugin.getConfig().getBoolean("protection.entities.vehicles", true)) {
                event.setCancelled(true);
                messages.send(player, "vehicles-deny");
            }
            return;
        }

        if (!plot.getSettings().isVehiclesAllowed()) {
            event.setCancelled(true);
            messages.send(player, "vehicles-deny");
            messages.debug("&cPrevented vehicle destroy in claim: " + plot.getDisplayNameSafe());
        }
    }
}
