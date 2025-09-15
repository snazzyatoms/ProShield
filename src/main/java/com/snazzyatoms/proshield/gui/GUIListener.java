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
                && !title.contains("Expansion Requests")) {
            return;
        }

        // Always cancel vanilla movement
        event.setCancelled(true);

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
            // hook into ExpansionRequestManager when player clicks inside Expansion menu
            plugin.getExpansionRequestManager().handleRequestClick(player, event);
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
            // open the Expansion Requests GUI
            guiManager.openExpansionRequests(player);
        } else if (stripped.contains("admin tools")) {
            if (player.hasPermission("proshield.admin")) {
                guiManager.openAdminTools(player);
            } else {
                plugin.getMessagesUtil().send(player, "&cYou don’t have permission to use Admin Tools.");
            }
        }
    }
}
