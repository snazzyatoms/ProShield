package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

/**
 * GUIListener
 * - Central dispatcher for GUIManager
 * - Cancels vanilla item movement
 * - Routes clicks to correct handlers
 * - Claim Info is tooltip-only (ignored on click)
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
                && !title.contains("Deny Reasons")) {
            return;
        }

        event.setCancelled(true); // always cancel vanilla behavior

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
            // If it's the admin view
            if (player.hasPermission("proshield.admin")) {
                guiManager.handleExpansionReviewClick(player, event);
            } else {
                // player’s own expansion request menu
                plugin.getExpansionRequestManager().handleRequestClick(player, event);
            }
        } else if (title.contains("Deny Reasons")) {
            guiManager.handleDenyReasonClick(player, event);
        }
    }

    private void handleMainClick(Player player, InventoryClickEvent event) {
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta() || !clicked.getItemMeta().hasDisplayName()) return;

        String name = clicked.getItemMeta().getDisplayName().replace("§", "").toLowerCase();

        if (name.contains("claim land")) {
            plugin.getPlotManager().claimPlot(player);
            player.closeInventory();
        } else if (name.contains("claim info")) {
            // Tooltip only – ignore
        } else if (name.contains("unclaim")) {
            plugin.getPlotManager().unclaimPlot(player);
            player.closeInventory();
        } else if (name.contains("trusted players")) {
            guiManager.openTrusted(player);
        } else if (name.contains("claim flags")) {
            guiManager.openFlags(player);
        } else if (name.contains("request expansion")) {
            plugin.getExpansionRequestManager().openRequestMenu(player);
        } else if (name.equalsIgnoreCase("back")) {
            guiManager.openMain(player);
        } else if (name.equalsIgnoreCase("exit")) {
            player.closeInventory();
        } else if (name.contains("admin tools")) {
            if (player.hasPermission("proshield.admin")) {
                guiManager.openAdminTools(player);
            } else {
                plugin.getMessagesUtil().send(player, "&cYou don’t have permission to use Admin Tools.");
            }
        }
    }

    // Extra safety: if a player closes "Assign Role", clear pending state so nothing lingers.
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        String title = event.getView().getTitle();
        if (title != null && title.contains("Assign Role")) {
            guiManager.clearPendingRoleAssignment(player.getUniqueId());
        }
    }
}
