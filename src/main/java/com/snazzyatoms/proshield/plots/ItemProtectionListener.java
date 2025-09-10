package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.roles.ClaimRole;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Chunk;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;

/**
 * Handles protection for items and entities such as:
 * - Item frames, armor stands, vehicles
 * Respects both global config and per-claim flags.
 * Uses role checks for trusted access.
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

    @EventHandler(ignoreCancelled = true)
    public void onItemFrameBreak(HangingBreakByEntityEvent event) {
        if (!(event.getRemover() instanceof Player player)) return;
        Chunk chunk = event.getEntity().getLocation().getChunk();
        FileConfiguration config = plugin.getConfig();

        Plot plot = plotManager.getPlot(chunk);

        // === Wilderness ===
        if (plot == null) {
            if (config.getBoolean("protection.entities.item-frames", true)) {
                event.setCancelled(true);
                messages.send(player, "protection.itemframe-break-blocked");
            }
            return;
        }

        // === Inside claim ===
        ClaimRole role = roleManager.getRole(plot, player);
        if (!roleManager.canBuild(role) || !plot.getSettings().isItemFramesAllowed()) {
            event.setCancelled(true);
            messages.send(player, "protection.itemframe-break-blocked");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemFramePlace(HangingPlaceEvent event) {
        Player player = event.getPlayer();
        Chunk chunk = event.getEntity().getLocation().getChunk();
        FileConfiguration config = plugin.getConfig();

        Plot plot = plotManager.getPlot(chunk);

        // === Wilderness ===
        if (plot == null) {
            if (config.getBoolean("protection.entities.item-frames", true)) {
                event.setCancelled(true);
                messages.send(player, "protection.itemframe-place-blocked");
            }
            return;
        }

        // === Inside claim ===
        ClaimRole role = roleManager.getRole(plot, player);
        if (!roleManager.canBuild(role) || !plot.getSettings().isItemFramesAllowed()) {
            event.setCancelled(true);
            messages.send(player, "protection.itemframe-place-blocked");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onVehicleDestroy(VehicleDestroyEvent event) {
        if (!(event.getAttacker() instanceof Player player)) return;
        Vehicle vehicle = event.getVehicle();
        Chunk chunk = vehicle.getLocation().getChunk();
        FileConfiguration config = plugin.getConfig();

        Plot plot = plotManager.getPlot(chunk);

        // === Wilderness ===
        if (plot == null) {
            if (config.getBoolean("protection.entities.vehicles", true)) {
                event.setCancelled(true);
                messages.send(player, "protection.vehicle-destroy-blocked");
            }
            return;
        }

        // === Inside claim ===
        ClaimRole role = roleManager.getRole(plot, player);
        if (!roleManager.canBuild(role) || !plot.getSettings().isVehiclesAllowed()) {
            event.setCancelled(true);
            messages.send(player, "protection.vehicle-destroy-blocked");
        }
    }
}
