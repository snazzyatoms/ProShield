// src/main/java/com/snazzyatoms/proshield/gui/listeners/RolesListener.java
package com.snazzyatoms.proshield.gui.listeners;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.roles.ClaimRole;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * RolesListener
 *
 * Handles the Roles GUI interactions.
 * Allows assigning / clearing roles for trusted players.
 */
public class RolesListener implements Listener {

    private final ClaimRoleManager roles;
    private final PlotManager plots;
    private final GUIManager gui;
    private final MessagesUtil messages;

    public RolesListener(ProShield plugin, ClaimRoleManager roles, PlotManager plots, GUIManager gui) {
        this.roles = roles;
        this.plots = plots;
        this.gui = gui;
        this.messages = plugin.getMessagesUtil();
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getCurrentItem() == null) return;

        String title = event.getView().getTitle();
        if (title == null || !title.toLowerCase().contains("roles")) return;

        event.setCancelled(true);

        Plot plot = plots.getPlot(player.getLocation());
        if (plot == null) {
            messages.send(player, "roles.no-claim");
            return;
        }

        String targetName = gui.getRememberedTarget(player);
        if (targetName == null) {
            messages.send(player, "error.player-not-found", Map.of("player", "unknown"));
            return;
        }

        UUID targetId = Bukkit.getOfflinePlayer(targetName).getUniqueId();
        UUID claimId = plot.getId();

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("player", targetName);
        placeholders.put("claim", plot.getDisplayNameSafe());

        switch (event.getRawSlot()) {
            case 11 -> {
                roles.assignRole(claimId, targetId, ClaimRole.MANAGER);
                placeholders.put("role", ClaimRole.MANAGER.getDisplayName());
                messages.send(player, "trust.added", placeholders);
            }
            case 13 -> {
                roles.assignRole(claimId, targetId, ClaimRole.TRUSTED);
                placeholders.put("role", ClaimRole.TRUSTED.getDisplayName());
                messages.send(player, "trust.added", placeholders);
            }
            case 15 -> {
                roles.clearRole(claimId, targetId);
                messages.send(player, "roles.cleared", placeholders);
            }
            case 26 -> {
                gui.openMain(player);
                return;
            }
            default -> { return; }
        }

        plots.saveAsync(plot);
        gui.openRolesGUI(player, plot, player.hasPermission("proshield.admin"));
    }
}
