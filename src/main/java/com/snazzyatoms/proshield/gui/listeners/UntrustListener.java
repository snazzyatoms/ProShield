// src/main/java/com/snazzyatoms/proshield/gui/listeners/UntrustListener.java
package com.snazzyatoms.proshield.gui.listeners;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
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
 * UntrustListener
 *
 * Handles the Untrust Menu GUI interactions.
 * Lets owners/co-owners remove trusted players from claims.
 */
public class UntrustListener implements Listener {

    private final PlotManager plots;
    private final ClaimRoleManager roles;
    private final GUIManager gui;
    private final MessagesUtil messages;

    public UntrustListener(ProShield plugin, PlotManager plots, ClaimRoleManager roles, GUIManager gui) {
        this.plots = plots;
        this.roles = roles;
        this.gui = gui;
        this.messages = plugin.getMessagesUtil();
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        String title = event.getView().getTitle();
        if (title == null || !title.toLowerCase().contains("untrust")) return;

        event.setCancelled(true);

        Plot plot = plots.getPlot(player.getLocation());
        if (plot == null) {
            messages.send(player, "error.no-claim");
            return;
        }

        String targetName = gui.getRememberedTarget(player);
        if (targetName == null) {
            messages.send(player, "error.player-not-found", Map.of("player", "unknown"));
            return;
        }

        UUID targetId = Bukkit.getOfflinePlayer(targetName).getUniqueId();
        UUID claimId = plot.getId();

        boolean updated = switch (event.getSlot()) {
            case 10, 11, 12 -> {
                roles.clearRole(claimId, targetId);

                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("player", targetName);
                placeholders.put("claim", plot.getDisplayNameSafe());

                // Use roles.cleared for consistency
                messages.send(player, "roles.cleared", placeholders);
                yield true;
            }
            case 26 -> {
                gui.openMain(player);
                yield false;
            }
            default -> false;
        };

        if (!updated) return;

        // Refresh roles GUI
        gui.openRolesGUI(player, plot, player.hasPermission("proshield.admin"));
    }
}
