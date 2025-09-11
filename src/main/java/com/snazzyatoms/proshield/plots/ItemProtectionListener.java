package com.snazzyatoms.proshield.plots;

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

import java.util.UUID;

/**
 * ItemProtectionListener
 *
 * ✅ Preserves all prior logic
 * ✅ Updated constructor style (PlotManager, MessagesUtil, ClaimRoleManager)
 * ✅ Matches expanded PlotSettings (item frames, armor stands, animals, pets, containers, vehicles)
 */
public class ItemProtectionListener implements Listener {

    private final PlotManager plots;
    private final MessagesUtil messages;
    private final ClaimRoleManager roles;

    public ItemProtectionListener(PlotManager plots, MessagesUtil messages, ClaimRoleManager roles) {
        this.plots = plots;
        this.messages = messages;
        this.roles = roles;
    }

    /* ------------------------------
     * Item Frames & Armor Stands
     * ------------------------------ */
    @EventHandler(ignoreCancelled = true)
    public void onHangingBreak(HangingBreakByEntityEvent event) {
        if (!(event.getRemover() instanceof Player player)) return;

        Chunk chunk = event.getEntity().getLocation().getChunk();
        Plot plot = plots.getPlot(chunk);

        if (plot == null) {
            if (!player.hasPermission("proshield.admin")) {
                event.setCancelled(true);
                messages.send(player, "item-frames-deny");
            }
            return;
        }

        ClaimRole role = roles.getRole(plot, player.getUniqueId());

        if (!plot.getSettings().isItemFramesAllowed() || !roles.canBuild(role)) {
            event.setCancelled(true);
            messages.send(player, "item-frames-deny");
            messages.debug("&cPrevented item frame break inside claim: "
                    + plot.getDisplayNameSafe() + " by " + player.getName());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onHangingPlace(HangingPlaceEvent event) {
        Player player = event.getPlayer();
        Chunk chunk = event.getEntity().getLocation().getChunk();
        Plot plot = plots.getPlot(chunk);

        if (plot == null) return;

        ClaimRole role = roles.getRole(plot, player.getUniqueId());

        if (!plot.getSettings().isItemFramesAllowed() || !roles.canBuild(role)) {
            event.setCancelled(true);
            messages.send(player, "item-frames-deny");
        }
    }

    /* ------------------------------
     * Interacting with entities (Armor Stands, Animals, Pets, Containers)
     * ------------------------------ */
    @EventHandler(ignoreCancelled = true)
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();
        Chunk chunk = entity.getLocation().getChunk();
        Plot plot = plots.getPlot(chunk);

        if (plot == null) return; // wilderness → skip

        ClaimRole role = roles.getRole(plot, player.getUniqueId());

        // Armor stands
        if (entity instanceof ArmorStand && !plot.getSettings().isArmorStandsAllowed()) {
            if (!roles.canInteract(role)) {
                event.setCancelled(true);
                messages.send(player, "armor-stands-deny");
                return;
            }
        }

        // Passive animals
        if (entity instanceof Animals && !plot.getSettings().isAnimalAccessAllowed()) {
            if (!roles.canInteract(role)) {
                event.setCancelled(true);
                messages.send(player, "animals-deny");
                return;
            }
        }

        // Tamed pets
        if (entity instanceof Tameable tameable && tameable.isTamed() && !plot.getSettings().isPetAccessAllowed()) {
            if (!roles.canInteract(role)) {
                event.setCancelled(true);
                messages.send(player, "pets-deny");
                return;
            }
        }

        // Containers (e.g., chest minecarts)
        if (entity instanceof Minecart && !plot.getSettings().isContainersAllowed()) {
            if (!roles.canAccessContainers(role)) {
                event.setCancelled(true);
                messages.send(player, "containers-deny");
            }
        }
    }

    /* ------------------------------
     * Vehicle Protections
     * ------------------------------ */
    @EventHandler(ignoreCancelled = true)
    public void onVehicleDestroy(VehicleDestroyEvent event) {
        if (!(event.getAttacker() instanceof Player player)) return;

        Chunk chunk = event.getVehicle().getLocation().getChunk();
        Plot plot = plots.getPlot(chunk);

        if (plot == null) {
            if (!player.hasPermission("proshield.admin")) {
                event.setCancelled(true);
                messages.send(player, "vehicles-deny");
            }
            return;
        }

        ClaimRole role = roles.getRole(plot, player.getUniqueId());

        if (!plot.getSettings().isVehiclesAllowed() || !roles.canBuild(role)) {
            event.setCancelled(true);
            messages.send(player, "vehicles-deny");
            messages.debug("&cPrevented vehicle destroy in claim: "
                    + plot.getDisplayNameSafe() + " by " + player.getName());
        }
    }
}
