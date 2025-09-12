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

import java.util.UUID;

/**
 * TrustListener
 *
 * Handles the Trust Menu GUI interactions.
 * Lets owners/co-owners assign roles to trusted players.
 */
public class TrustListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plots;
    private final ClaimRoleManager roles;
    private final GUIManager gui;
    private final MessagesUtil messages;

    public TrustListener(ProShield plugin, PlotManager plots, ClaimRoleManager roles, GUIManager gui) {
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
        if (!title.contains("Trust")) return; // safer than hardcoding color

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
            case 10 -> { // Trust as Trusted
                roles.assignRole(claimId, targetId, ClaimRole.TRUSTED);
                messages.send(player, "trust.added", targetName);
            }
            case 11 -> { // Trust as Builder
                roles.assignRole(claimId, targetId, ClaimRole.BUILDER);
                messages.send(player, "trust.added", targetName + " as Builder");
            }
            case 12 -> { // Trust as Moderator
                roles.assignRole(claimId, targetId, ClaimRole.MODERATOR);
                messages.send(player, "trust.added", targetName + " as Moderator");
            }
            case 26 -> { // Back button
                gui.openMain(player);
                return;
            }
            default -> { return; }
        }

        // Save plot and refresh
        plots.saveAsync(plot);
        gui.openRolesGUI(player, plot, player.hasPermission("proshield.admin"));
    }
}
