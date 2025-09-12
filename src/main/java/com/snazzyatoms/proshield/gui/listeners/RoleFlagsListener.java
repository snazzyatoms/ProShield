package com.snazzyatoms.proshield.gui.listeners;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import com.snazzyatoms.proshield.roles.RolePermissions;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.UUID;

/**
 * Handles toggling permissions inside the Role Flags GUI.
 *
 * ✅ Uses ClaimRoleManager.setRolePermissions
 * ✅ Works per-claim and per-role
 */
public class RoleFlagsListener implements Listener {

    private final ProShield plugin;
    private final ClaimRoleManager roles;
    private final MessagesUtil messages;

    public RoleFlagsListener(ProShield plugin, ClaimRoleManager roles) {
        this.plugin = plugin;
        this.roles = roles;
        this.messages = plugin.getMessagesUtil();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getCurrentItem() == null || event.getView().getTitle() == null) return;

        String title = ChatColor.stripColor(event.getView().getTitle());
        if (!title.startsWith("Role Flags: ")) return;

        event.setCancelled(true);

        String roleId = title.substring("Role Flags: ".length()).toLowerCase();
        UUID claimId = plugin.getPlotManager().getPlot(player.getLocation()).getId();
        RolePermissions perms = roles.getRolePermissions(claimId, roleId);

        switch (event.getCurrentItem().getType()) {
            case STONE_PICKAXE -> perms.setCanBuild(!perms.canBuild());
            case CHEST -> perms.setCanContainers(!perms.canContainers());
            case BOOK -> perms.setCanManageTrust(!perms.canManageTrust());
            case BARRIER -> perms.setCanUnclaim(!perms.canUnclaim());
            case BARRIER: {
                // Back button
                player.closeInventory();
                return;
            }
            default -> {
                return;
            }
        }

        // Persist
        roles.setRolePermissions(claimId, roleId, perms);

        // Feedback
        messages.send(player, "flags.toggle", roleId, perms.toString());
    }
}
