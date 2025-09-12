package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
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
 * Unified GUIListener
 * Handles ALL ProShield menus in one place:
 * - Main menu
 * - Admin menu
 * - Roles menu
 * - Trust/Untrust menus
 * - Flags / Transfer / Info menus
 */
public class GUIListener implements Listener {

    private final ProShield plugin;
    private final GUIManager gui;
    private final PlotManager plots;
    private final ClaimRoleManager roles;
    private final MessagesUtil messages;

    public GUIListener(ProShield plugin, GUIManager gui, PlotManager plots, ClaimRoleManager roles, MessagesUtil messages) {
        this.plugin = plugin;
        this.gui = gui;
        this.plots = plots;
        this.roles = roles;
        this.messages = messages;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getCurrentItem() == null) return;

        String title = event.getView().getTitle();
        if (title == null) return;

        event.setCancelled(true); // prevent taking items out of menus

        Plot plot = plots.getPlot(player.getLocation());

        // --- Main Menu ---
        if (title.contains("ProShield Menu")) {
            switch (event.getRawSlot()) {
                case 10 -> gui.runPlayerCommand(player, "proshield claim");
                case 11 -> gui.runPlayerCommand(player, "proshield unclaim");
                case 12 -> gui.openInfoMenu(player, plot);
                case 13 -> gui.openTrustMenu(player, false);
                case 14 -> gui.openUntrustMenu(player, false);
                case 15 -> gui.openRolesGUI(player, plot, false);
                case 16 -> gui.openFlagsMenu(player, false);
                case 26 -> player.closeInventory();
            }
            return;
        }

        // --- Admin Menu ---
        if (title.contains("ProShield Admin Menu")) {
            switch (event.getRawSlot()) {
                case 11 -> gui.openTrustMenu(player, true);
                case 13 -> gui.openRolesGUI(player, plot, true);
                case 15 -> gui.openFlagsMenu(player, true);
                case 26 -> gui.openMain(player);
            }
            return;
        }

        // --- Roles Menu ---
        if (title.contains("Manage Roles")) {
            // Example: open role assignment for clicked player
            if (event.getCurrentItem() != null && event.getCurrentItem().getItemMeta() != null) {
                String targetName = event.getCurrentItem().getItemMeta().getDisplayName();
                gui.rememberTarget(player, targetName);
                gui.openRoleAssignmentMenu(player, plot, targetName, player.hasPermission("proshield.admin"));
            }
            return;
        }

        // --- Trust Menu ---
        if (title.contains("Trust Player")) {
            messages.send(player, "trust.added", "Enter player name in chat...");
            player.closeInventory();
            return;
        }

        // --- Untrust Menu ---
        if (title.contains("Untrust Player")) {
            messages.send(player, "untrust.removed", "Enter player name in chat...");
            player.closeInventory();
            return;
        }

        // --- Role Assignment Menu ---
        if (title.contains("Roles for")) {
            String targetName = gui.getRememberedTarget(player);
            if (plot == null || targetName == null) return;

            UUID targetId = Bukkit.getOfflinePlayer(targetName).getUniqueId();
            UUID claimId = plot.getId();
            Map<String, String> ph = new HashMap<>();
            ph.put("player", targetName);
            ph.put("claim", plot.getDisplayNameSafe());

            switch (event.getRawSlot()) {
                case 10 -> {
                    roles.assignRole(claimId, targetId, ClaimRole.BUILDER);
                    ph.put("role", ClaimRole.BUILDER.getDisplayName());
                    messages.send(player, "roles.updated", ph);
                }
                case 12 -> {
                    roles.assignRole(claimId, targetId, ClaimRole.MODERATOR);
                    ph.put("role", ClaimRole.MODERATOR.getDisplayName());
                    messages.send(player, "roles.updated", ph);
                }
                case 14 -> {
                    roles.clearRole(claimId, targetId);
                    messages.send(player, "roles.cleared", ph);
                }
                case 26 -> gui.openRolesGUI(player, plot, player.hasPermission("proshield.admin"));
            }
            plots.saveAsync(plot);
            return;
        }

        // --- Flags Menu ---
        if (title.contains("Claim Flags")) {
            // TODO: connect slots to your flag toggles
            messages.send(player, "flags.toggle", "Feature coming soon.");
            return;
        }

        // --- Info Menu ---
        if (title.contains("Claim Info")) {
            player.closeInventory();
        }
    }
}
