package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

/**
 * GUIListener
 * - Central dispatcher
 * - Cancels vanilla movement
 * - Back/Exit handled safely
 * - Claim Info clicks are ignored (tooltip only)
 * - Distinguishes between player request and admin review expansion menus
 */
public class GUIListener implements Listener {

    private final ProShield plugin;
    private final GUIManager guiManager;

    public GUIListener(ProShield plugin, GUIManager guiManager) {
        this.plugin = plugin;
        this.guiManager = guiManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        String title = event.getView().getTitle();
        if (title == null) return;

        // Only handle our menus
        if (!title.contains("ProShield")
                && !title.contains("Trusted Players")
                && !title.contains("Assign Role")
                && !title.contains("Claim Flags")
                && !title.contains("Admin Tools")
                && !title.contains("Expansion Requests")
                && !title.contains("Request Expansion")
                && !title.contains("Deny Reasons")) {
            return;
        }

        event.setCancelled(true); // always cancel vanilla movement

        if (title.contains("ProShield Menu")) {
            handleMainClick(player, event);
        } else if (title.contains("Trusted Players")) {
            guiManager.handleTrustedClick(player, event);
        } else if (title.contains("Assign Role")) {
            guiManager.handleAssignRoleClick(player, event);
        } else if (title.contains("Claim Flags")) {
            guiManager.handleFlagsClick(player, event);
        } else if (title.contains("Admin Tools")) {
            guiManager.handleAdminClick(player, event);
        } else if (title.contains("Expansion Requests")) {
            // Admin review menu
            guiManager.handleExpansionReviewClick(player, event);
        } else if (title.contains("Request Expansion")) {
            // Player request menu
            plugin.getExpansionRequestManager().handlePlayerRequestClick(player, event);
        } else if (title.contains("Deny Reasons")) {
            guiManager.handleDenyReasonClick(player, event);
        }
    }

    private void handleMainClick(Player player, InventoryClickEvent event) {
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta() || !clicked.getItemMeta().hasDisplayName()) return;

        String name = clicked.getItemMeta().getDisplayName()
                .replace("§", "")
                .toLowerCase();

        PlotManager plotManager = plugin.getPlotManager();

        if (name.contains("claim land")) {
            // Create a new claim for this player at their current location
            plotManager.createPlot(player.getUniqueId(), player.getLocation());
            plugin.getMessagesUtil().send(player, "&aClaim created successfully.");
            player.closeInventory();
        } else if (name.contains("claim info")) {
            // Tooltip only – ignore click
        } else if (name.contains("unclaim")) {
            Plot plot = plotManager.getPlotAt(player.getLocation());
            if (plot != null && plot.getOwner().equals(player.getUniqueId())) {
                plotManager.deletePlot(plot.getId());
                plugin.getMessagesUtil().send(player, "&cClaim unclaimed successfully.");
            } else {
                plugin.getMessagesUtil().send(player, "&cYou don’t own a claim here.");
            }
            player.closeInventory();
        } else if (name.contains("trusted players")) {
            guiManager.openTrusted(player);
        } else if (name.contains("claim flags")) {
            guiManager.openFlags(player);
        } else if (name.contains("request expansion")) {
            plugin.getExpansionRequestManager().openPlayerRequestMenu(player);
        } else if (name.equals("back")) {
            guiManager.openMain(player);
        } else if (name.equals("exit")) {
            player.closeInventory();
        } else if (name.contains("admin tools")) {
            if (player.hasPermission("proshield.admin")) {
                guiManager.openAdminTools(player);
            } else {
                plugin.getMessagesUtil().send(player, "&cYou don’t have permission to use Admin Tools.");
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        String title = event.getView().getTitle();
        if (title != null && title.contains("Assign Role")) {
            guiManager.clearPendingRoleAssignment(player.getUniqueId());
        }
    }
}
