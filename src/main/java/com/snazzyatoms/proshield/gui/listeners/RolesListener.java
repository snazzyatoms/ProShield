package com.snazzyatoms.proshield.gui.listeners;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.roles.ClaimRole;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.UUID;

/**
 * Handles clicks inside the Roles GUI.
 */
public class RolesListener implements Listener {

    private final ProShield plugin;
    private final ClaimRoleManager roles;
    private final PlotManager plotManager;

    public RolesListener(ProShield plugin, ClaimRoleManager roles, PlotManager plotManager) {
        this.plugin = plugin;
        this.roles = roles;
        this.plotManager = plotManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getCurrentItem() == null) return;

        String title = event.getView().getTitle();
        if (!title.contains("Roles")) return;

        event.setCancelled(true);

        // get the plot at the player's current location
        Plot plot = plotManager.getPlot(player.getLocation());
        if (plot == null) {
            player.sendMessage("§cYou are not inside a claim.");
            return;
        }

        UUID plotId = plot.getId();

        // Example logic: assign roles based on clicked slot
        int slot = event.getRawSlot();
        if (slot == 11) {
            roles.assignRole(plotId, player.getUniqueId(), ClaimRole.MANAGER);
            player.sendMessage("§aYou are now Manager in this claim!");
        } else if (slot == 13) {
            roles.assignRole(plotId, player.getUniqueId(), ClaimRole.TRUSTED);
            player.sendMessage("§aYou are now Trusted in this claim!");
        } else if (slot == 15) {
            roles.clearRole(plotId, player.getUniqueId());
            player.sendMessage("§cYour role in this claim was cleared.");
        }
    }
}
