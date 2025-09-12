// src/main/java/com/snazzyatoms/proshield/gui/listeners/TrustListener.java
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
 * TrustListener
 *
 * Handles the Trust Menu GUI interactions.
 * Lets owners/co-owners assign roles to trusted players.
 */
public class TrustListener implements Listener {

    private final PlotManager plots;
    private final ClaimRoleManager roles;
    private final GUIManager gui;
    private final MessagesUtil messages;

    public TrustListener(ProShield plugin, PlotManager plots, ClaimRoleManager roles, GUIManager gui) {
        this.plots = plots;
        this.roles = roles;
        this.gui = gui;
        this.messages = plugin.getMessagesUtil();
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        String title = event.getView().getTitle();
        if (title == null || !title.contains("Trust")) return; // safer than hardcoding

        event.setCancelled(true);

        Plot plot = plots.getPlot(player.getLocation());
        if (plot == null) {
            messages.send(player, "error.no-claim");
            return;
        }

        // Get target (set earlier via rememberTarget)
        String targetName = gui.getRememberedTarget(player);
        if (targetName == null) {
            messages.send(player, "error.player-not-found", Map.of("player", "unknown"));
            return;
        }

        UUID targetId = Bukkit.getOfflinePlayer(targetName).getUniqueId();
        UUID claimId = plot.getId();

        ClaimRole assignedRole = switch (event.getSlot()) {
            case 10 -> ClaimRole.TRUSTED;
            case 11 -> ClaimRole.BUILDER;
            case 12 -> ClaimRole.MODERATOR;
            case 26 -> {
                gui.openMain(player);
                yield null;
            }
            default -> null;
        };

        if (assignedRole == null) return;

        // Apply role and persist
        roles.assignRole(claimId, targetId, assignedRole);

        // Send placeholder-based message
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("player", targetName);
        placeholders.put("claim", plot.getDisplayNameSafe());
        placeholders.put("role", assignedRole.getDisplayName());

        messages.send(player, "trust.added", placeholders);

        // Save plot and refresh
        plots.saveAsync(plot);
        gui.openRolesGUI(player, plot, player.hasPermission("proshield.admin"));
    }
}
