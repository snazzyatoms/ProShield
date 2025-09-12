// src/main/java/com/snazzyatoms/proshield/gui/listeners/RoleFlagsListener.java
package com.snazzyatoms.proshield.gui.listeners;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import com.snazzyatoms.proshield.roles.RolePermissions;
import com.snazzyatoms.proshield.plots.Plot;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.UUID;

/**
 * Handles toggling role flags in the GUI.
 */
public class RoleFlagsListener implements Listener {

    private final ProShield plugin;
    private final ClaimRoleManager roles;

    public RoleFlagsListener(ProShield plugin, ClaimRoleManager roles) {
        this.plugin = plugin;
        this.roles = roles;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getCurrentItem() == null) return;
        if (!event.getView().getTitle().startsWith(ChatColor.AQUA + "Role Flags:")) return;

        event.setCancelled(true);

        UUID claimId = plugin.getPlotManager().getPlot(player.getLocation()).getId();
        String roleId = ChatColor.stripColor(event.getView().getTitle().replace("Role Flags: ", ""));
        RolePermissions perms = roles.getRolePermissions(claimId, roleId);

        Material clicked = event.getCurrentItem().getType();
        switch (clicked) {
            case STONE_PICKAXE -> perms.setCanBuild(!perms.canBuild());
            case CHEST -> perms.setCanContainers(!perms.canContainers());
            case BOOK -> perms.setCanManageTrust(!perms.canManageTrust());
            case BARRIER -> perms.setCanUnclaim(!perms.canUnclaim());
            case REDSTONE_BLOCK -> {
                player.closeInventory();
                return;
            }
            default -> {}
        }

        roles.setRolePermissions(claimId, roleId, perms);
        plugin.getGuiManager().openRoleFlagsMenu(player,
                plugin.getPlotManager().getPlot(player.getLocation()), roleId);
    }
}
