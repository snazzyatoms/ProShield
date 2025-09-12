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

import java.util.UUID;

/**
 * UntrustListener
 *
 * Handles the Untrust Menu GUI interactions.
 * Lets owners/co-owners remove trusted players from claims.
 */
public class UntrustListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plots;
    private final ClaimRoleManager roles;
    private final GUIManager gui;
    private final MessagesUtil messages;

    public UntrustListener(ProShield plugin, PlotManager plots, ClaimRoleManager roles, GUIManager gui) {
        this.plugin = plugin;
        this.plots = plots;
        this.roles = roles;
        this.gui = gui;
        this.messages = plugin.getMessagesUtil();
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getView().getTitle() == null) return;

        String title = event.getView().getTitle();
        if (!title.contains("Untrust")) return; // safe even if colors change

        event.setCancelled(true);
        Plot plot = plots.getPlot(player.getLocation());
        if (plot == null) {
            messages.send(player, "error.not-in-claim");
            return;
        }

        // Get target (set earlier via rememberTarget)
        String targetName = gui.getRememberedTarget(player);
        if (targetName == null) {
            messages.send(player, "error.player-not-found");
            return;
        }

        UUID targetId = Bukkit.getOfflinePlayer(targetName).getUniqueId();
        UUID claimId = plot.getId();

        switch (event.getSlot()) {
            case 10, 11, 12 -> { // Untrust action
                roles.clearRole(claimId, targetId); // handles persistence
                messages.send(player, "untrust.removed", targetName);
            }
            case 26 -> { // Back button
                gui.openMain(player);
                return;
            }
            default -> { return; }
        }

        // Refresh roles GUI
        gui.openRolesGUI(player, plot, player.hasPermission("proshield.admin"));
    }
}
