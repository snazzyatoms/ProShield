package com.snazzyatoms.proshield.gui.listeners;

import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import com.snazzyatoms.proshield.roles.RolePermissions;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.UUID;

/**
 * RoleFlagsListener
 *
 * Handles toggling permissions in the Role Flags menu.
 * Syncs changes to ClaimRoleManager.savePermissions().
 */
public class RoleFlagsListener implements Listener {

    private final PlotManager plotManager;
    private final ClaimRoleManager roles;
    private final GUIManager gui;
    private final MessagesUtil messages;

    public RoleFlagsListener(PlotManager plotManager, ClaimRoleManager roles, GUIManager gui, MessagesUtil messages) {
        this.plotManager = plotManager;
        this.roles = roles;
        this.gui = gui;
        this.messages = messages;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getView().getTitle() == null) return;

        String title = event.getView().getTitle();
        if (!title.startsWith("§bRole Flags: ")) return;

        event.setCancelled(true);
        Plot plot = plotManager.getPlot(player.getLocation());
        if (plot == null) return;

        String roleId = title.replace("§bRole Flags: ", "").toLowerCase();
        UUID claimId = plot.getId();
        RolePermissions perms = roles.getRolePermissions(claimId, roleId);

        switch (event.getSlot()) {
            case 10 -> perms.setCanBuild(!perms.canBuild());
            case 11 -> perms.setCanContainers(!perms.canContainers());
            case 12 -> perms.setCanManageTrust(!perms.canManageTrust());
            case 13 -> perms.setCanUnclaim(!perms.canUnclaim());
            default -> { return; }
        }

        // Persist changes
        roles.savePermissions(claimId, roleId, perms);

        messages.send(player, "flags.toggle", roleId + " updated");

        // Refresh menu
        gui.openRoleFlagsMenu(player, plot, roleId);
    }
}
