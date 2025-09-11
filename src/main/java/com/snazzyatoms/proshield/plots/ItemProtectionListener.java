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

import java.util.UUID;

/**
 * ItemProtectionListener
 *
 * ✅ Uses standalone PlotSettings
 * ✅ Protects item frames, armor stands, animals, pets, containers, vehicles
 * ✅ Uses ClaimRoleManager for trust/role checks
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
     * Item Frames
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

        UUID uid = player.getUniqueId();
        ClaimRole role = roleManager.getRole(plot, uid);

        if (!plot.getSettings().isItemFramesAllowed() || !roleManager.canBuild(role)) {
            event.setCancelled(true);
            messages.send(player, "item-frames-deny");
            messages.debug("&cPrevented item frame break inside claim: " + plot.getDisplayNameSafe() +
                    " by " + player.getName());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onHangingPlace(HangingPlaceEvent event) {
        Player player = event.getPlayer();
        Chunk chunk = event.getEntity().getLocation().getChunk();
        Plot plot = plotManager.getPlot(chunk);

        if (plot == null) return;

        UUID uid = player.getUniqueId();
        ClaimRole role = roleManager.getRole(plot, uid);

        if (!plot.getSettings().isItemFramesAllowed() || !roleManager.canBuild(role)) {
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
        Plot plot = plotManager.getPlot(chunk);

        if (plot == null) return; // wilderness – handled by config

        UUID uid = player.getUniqueId();
        ClaimRole role = roleManager.getRole(plot, uid);

        // Armor stands
        if (entity instanceof ArmorStand && !plot.getSettings().isArmorStandsAllowed()) {
            if (!roleManager.canInteract(role)) {
                event.setCancelled(true);
                messages.send(player, "armor-stands-deny");
                return;
            }
        }

        // Passive animals
        if (entity instanceof Animals && !plot.getSettings().isAnimalAccessAllowed()) {
            if (!roleManager.canInteract(role)) {
                event.setCancelled(true);
                messages.send(player, "animals-deny");
                return;
            }
        }

        // Tamed pets
        if (entity instanceof Tameable tameable && tameable.isTamed() && !plot.getSettings().isPetAccessAllowed()) {
            if (!roleManager.canInteract(role)) {
                event.setCancelled(true);
                messages.send(player, "pets-deny");
                return;
            }
        }

        // Containers (like chest minecarts)
        if (entity instanceof Minecart && !plot.getSettings().isContainersAllowed()) {
            if (!roleManager.canInteract(role)) {
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
        Plot plot = plotManager.getPlot(chunk);

        if (plot == null) {
            if (!plugin.getConfig().getBoolean("protection.entities.vehicles", true)) {
                event.setCancelled(true);
                messages.send(player, "vehicles-deny");
            }
            return;
        }

        UUID uid = player.getUniqueId();
        ClaimRole role = roleManager.getRole(plot, uid);

        if (!plot.getSettings().isVehiclesAllowed() || !roleManager.canBuild(role)) {
            event.setCancelled(true);
            messages.send(player, "vehicles-deny");
            messages.debug("&cPrevented vehicle destroy in claim: " + plot.getDisplayNameSafe() +
                    " by " + player.getName());
        }
    }
}
