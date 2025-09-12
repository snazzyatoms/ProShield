package com.snazzyatoms.proshield.gui.listeners;

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
 * RolesListener
 *
 * Handles assigning / clearing roles in the Roles GUI.
 * Syncs changes to ClaimRoleManager.assignRole() and clearRole().
 */
public class RolesListener implements Listener {

    private final PlotManager plots;
    private final ClaimRoleManager roles;
    private final GUIManager gui;
    private final MessagesUtil messages;

    public RolesListener(PlotManager plots, ClaimRoleManager roles, GUIManager gui, MessagesUtil messages) {
        this.plots = plots;
        this.roles = roles;
        this.gui = gui;
        this.messages = messages;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getView().getTitle() == null) return;

        String title = event.getView().getTitle();
        if (!title.startsWith("§bRoles for ")) return;

        event.setCancelled(true);
        Plot plot = plots.getPlot(player.getLocation());
        if (plot == null) return;

        String targetName = title.replace("§bRoles for ", "");
        UUID claimId = plot.getId();
        UUID targetId = Bukkit.getOfflinePlayer(targetName).getUniqueId();

        switch (event.getSlot()) {
            case 10 -> {
                roles.assignRole(claimId, targetId, "builder");
                messages.send(player, "roles.assigned", targetName + " -> BUILDER");
            }
            case 12 -> {
                roles.assignRole(claimId, targetId, "moderator");
                messages.send(player, "roles.assigned", targetName + " -> MODERATOR");
            }
            case 14 -> {
                roles.clearRole(claimId, targetId);
                messages.send(player, "roles.cleared", targetName);
            }
            case 22 -> {
                gui.openRoleFlagsMenu(player, plot, "moderator");
                return;
            }
            case 26 -> {
                gui.openRolesGUI(player, plot, player.hasPermission("proshield.admin"));
                return;
            }
            default -> { return; }
        }

        // Refresh assignment menu
        gui.openRoleAssignmentMenu(player, plot, targetName, player.hasPermission("proshield.admin"));
    }
}
