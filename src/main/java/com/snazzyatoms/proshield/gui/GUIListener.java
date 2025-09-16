package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

/**
 * GUIListener
 * - Universal Back/Exit handling
 * - Dispatches to GUIManager for each menu
 * - Cancels vanilla item movement
 */
public class GUIListener implements Listener {

    private final ProShield plugin;
    private final GUIManager gui;

    public GUIListener(ProShield plugin, GUIManager guiManager) {
        this.plugin = plugin;
        this.gui = guiManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        String title = event.getView().getTitle();
        if (title == null) return;

        String t = strip(title);
        if (!(t.contains("ProShield") ||
              t.contains("Trusted Players") ||
              t.contains("Assign Role") ||
              t.contains("Claim Flags") ||
              t.contains("Admin Tools") ||
              t.contains("Expansion Requests") ||
              t.contains("Deny Reasons") ||
              t.contains("Request Claim Expansion"))) {
            return;
        }

        // Cancel vanilla inventory behavior
        event.setCancelled(true);

        // Universal Back/Exit by item display name (color-safe)
        String itemName = clicked.hasItemMeta() && clicked.getItemMeta().hasDisplayName()
                ? strip(clicked.getItemMeta().getDisplayName())
                : "";

        if (itemName.equalsIgnoreCase("Back") || itemName.equalsIgnoreCase("Return")
                || itemName.contains("Return to")) {
            if (t.contains("Trusted Players") || t.contains("Claim Flags") || t.contains("Admin Tools")) {
                gui.openMain(player);
            } else if (t.contains("Assign Role")) {
                gui.openTrusted(player);
            } else if (t.contains("Expansion Requests")) {
                gui.openAdminTools(player);
            } else if (t.contains("Deny Reasons")) {
                gui.openExpansionReview(player);
            } else if (t.contains("Request Claim Expansion")) {
                gui.openMain(player);
            }
            return;
        }

        if (itemName.equalsIgnoreCase("Exit")) {
            player.closeInventory();
            return;
        }

        // Dispatch
        if (t.contains("ProShield Menu")) {
            handleMainClick(player, event);
        } else if (t.contains("Trusted Players")) {
            gui.handleTrustedClick(player, event);
        } else if (t.contains("Assign Role")) {
            gui.handleAssignRoleClick(player, event);
        } else if (t.contains("Claim Flags")) {
            gui.handleFlagsClick(player, event);
        } else if (t.contains("Admin Tools")) {
            gui.handleAdminClick(player, event);
        } else if (t.contains("Expansion Requests")) {
            gui.handleExpansionReviewClick(player, event);
        } else if (t.contains("Deny Reasons")) {
            gui.handleDenyReasonClick(player, event);
        } else if (t.contains("Request Claim Expansion")) {
            gui.handleExpansionRequestClick(player, event);
        }
    }

    private void handleMainClick(Player player, InventoryClickEvent event) {
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        String name = strip(clicked.getItemMeta().getDisplayName());
        if (name.isEmpty()) return;

        String lowered = name.toLowerCase();

        if (lowered.contains("claim land")) {
            plugin.getPlotManager().claimPlot(player);
            player.closeInventory();
        } else if (lowered.contains("claim info")) {
            plugin.getPlotManager().sendClaimInfo(player);
            player.closeInventory();
        } else if (lowered.contains("unclaim")) {
            plugin.getPlotManager().unclaimPlot(player);
            player.closeInventory();
        } else if (lowered.contains("trusted players")) {
            gui.openTrusted(player);
        } else if (lowered.contains("claim flags")) {
            gui.openFlags(player);
        } else if (lowered.contains("request expansion")) {
            gui.openExpansionRequestMenu(player);
        } else if (lowered.contains("admin tools")) {
            if (player.hasPermission("proshield.admin")) {
                gui.openAdminTools(player);
            } else {
                plugin.getMessagesUtil().send(player, "&cYou don’t have permission to use Admin Tools.");
            }
        } else if (lowered.contains("exit")) {
            player.closeInventory();
        }
    }

    private String strip(String s) { return s == null ? "" : ChatColor.stripColor(s).trim(); }
}
