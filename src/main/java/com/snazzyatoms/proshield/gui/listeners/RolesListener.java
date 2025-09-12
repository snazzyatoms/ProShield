// src/main/java/com/snazzyatoms/proshield/gui/listeners/RolesListener.java
package com.snazzyatoms.proshield.gui.listeners;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.roles.ClaimRole;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.UUID;

/**
 * Handles clicks in the Roles GUI.
 */
public class RolesListener implements Listener {

    private final ProShield plugin;
    private final ClaimRoleManager roles;

    public RolesListener(ProShield plugin, ClaimRoleManager roles) {
        this.plugin = plugin;
        this.roles = roles;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getCurrentItem() == null) return;
        if (!event.getView().getTitle().equals(ChatColor.GOLD + "Manage Roles")) return;

        event.setCancelled(true);

        Plot plot = plugin.getPlotManager().getPlot(player.getLocation());
        if (plot == null) return;

        String targetName = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());
        UUID targetId = Bukkit.getOfflinePlayer(targetName).getUniqueId();

        switch (event.getCurrentItem().getType()) {
            case STONE_PICKAXE -> roles.setRole(plot, targetId, ClaimRole.BUILDER);
            case CROSSBOW -> roles.setRole(plot, targetId, ClaimRole.MODERATOR);
            case BOOK -> roles.setRole(plot, targetId, ClaimRole.TRUSTED);
            default -> {}
        }

        plugin.getGuiManager().openRoleAssignmentMenu(player, plot, targetName, false);
    }
}
