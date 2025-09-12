package com.snazzyatoms.proshield.gui.listeners;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.roles.ClaimRole;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.UUID;

/**
 * Handles clicks inside the Roles and Role Assignment GUIs.
 *
 * ✅ Uses ClaimRoleManager.assignRole / clearRole
 * ✅ Persists changes via PlotManager
 * ✅ Sends messages back to players
 */
public class RolesListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final ClaimRoleManager roles;
    private final GUIManager guiManager;
    private final MessagesUtil messages;

    public RolesListener(ProShield plugin, PlotManager plotManager, ClaimRoleManager roles, GUIManager guiManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
        this.roles = roles;
        this.guiManager = guiManager;
        this.messages = plugin.getMessagesUtil();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getCurrentItem() == null || event.getView().getTitle() == null) return;

        String title = ChatColor.stripColor(event.getView().getTitle());
        Plot plot = plotManager.getPlot(player.getLocation());
        if (plot == null) return;

        // Roles overview
        if (title.equalsIgnoreCase("Manage Roles")) {
            event.setCancelled(true);

            if (event.getCurrentItem().getType().toString().contains("PLAYER_HEAD")) {
                String targetName = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());
                guiManager.openRoleAssignmentMenu(player, plot, targetName, player.hasPermission("proshield.admin"));
            }
        }

        // Role assignment
        if (title.startsWith("Roles for ")) {
            event.setCancelled(true);

            String targetName = title.substring("Roles for ".length());
            OfflinePlayer targetOP = Bukkit.getOfflinePlayer(targetName);
            if (targetOP == null || targetOP.getUniqueId() == null) return;
            UUID targetId = targetOP.getUniqueId();

            switch (event.getCurrentItem().getType()) {
                case STONE_PICKAXE -> { // Builder
                    roles.assignRole(plot.getId(), targetId, ClaimRole.BUILDER);
                    messages.send(player, "roles.assigned", targetName, "Builder");
                    plotManager.saveAsync(plot);
                }
                case CROSSBOW -> { // Moderator
                    roles.assignRole(plot.getId(), targetId, ClaimRole.MODERATOR);
                    messages.send(player, "roles.assigned", targetName, "Moderator");
                    plotManager.saveAsync(plot);
                }
                case BOOK -> { // Clear role
                    roles.clearRole(plot.getId(), targetId);
                    messages.send(player, "roles.cleared", targetName);
                    plotManager.saveAsync(plot);
                }
                case LEVER -> { // Open role flags
                    guiManager.openRoleFlagsMenu(player, plot, roles.getRole(plot, targetId).name());
                }
                case BARRIER -> { // Back
                    guiManager.openRolesGUI(player, plot, player.hasPermission("proshield.admin"));
                }
                default -> {
                }
            }
        }
    }
}
