package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

/**
 * GUIListener
 * Dispatches clicks to GUIManager handlers.
 * Cancels vanilla movement to keep menus safe.
 * Includes global handling for Back + Exit buttons.
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
        if (!title.contains("ProShield") &&
            !title.contains("Trusted Players") &&
            !title.contains("Assign Role") &&
            !title.contains("Claim Flags") &&
            !title.contains("Admin Tools") &&
            !title.contains("Expansion Requests") &&
            !title.contains("Deny Reasons")) {
            return;
        }

        // Always cancel vanilla movement
        event.setCancelled(true);

        // Handle universal Back / Exit buttons
        String rawName = clicked.hasItemMeta() && clicked.getItemMeta().hasDisplayName()
                ? clicked.getItemMeta().getDisplayName().replace("§", "").toLowerCase()
                : "";

        if (rawName.equalsIgnoreCase("back") || rawName.contains("return to")) {
            if (title.contains("Trusted Players") ||
                title.contains("Claim Flags") ||
                title.contains("Admin Tools")) {
                guiManager.openMain(player);
            } else if (title.contains("Assign Role")) {
                guiManager.openTrusted(player);
            } else if (title.contains("Expansion Requests")) {
                guiManager.openAdminTools(player); // back from requests → Admin Tools
            } else if (title.contains("Deny Reasons")) {
                guiManager.openExpansionReview(player); // back from deny → expansion review
            }
            return;
        }

        if (rawName.equalsIgnoreCase("exit")) {
            player.closeInventory();
            return;
        }

        // Dispatch to correct handler
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
            guiManager.handleExpansionReviewClick(player, event);
        } else if (title.contains("Deny Reasons")) {
            guiManager.handleDenyReasonClick(player, event);
        }
    }

    private void handleMainClick(Player player, InventoryClickEvent event) {
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        String name = clicked.getItemMeta().getDisplayName();
        if (name == null) return;

        String stripped = name.replace("§", "").toLowerCase();

        if (stripped.contains("claim land")) {
            plugin.getPlotManager().claimPlot(player);
            player.closeInventory();
        } else if (stripped.contains("claim info")) {
            plugin.getPlotManager().sendClaimInfo(player);
            player.closeInventory();
        } else if (stripped.contains("unclaim")) {
            plugin.getPlotManager().unclaimPlot(player);
            player.closeInventory();
        } else if (stripped.contains("trusted players")) {
            guiManager.openTrusted(player);
        } else if (stripped.contains("claim flags")) {
            guiManager.openFlags(player);
        } else if (stripped.contains("request expansion")) {
            plugin.getExpansionRequestManager().openRequestMenu(player);
        } else if (stripped.contains("admin tools")) {
            if (player.hasPermission("proshield.admin")) {
                guiManager.openAdminTools(player);
            } else {
                plugin.getMessagesUtil().send(player, "&cYou don’t have permission to use Admin Tools.");
            }
        } else if (stripped.contains("exit")) {
            player.closeInventory();
        }
    }
}
