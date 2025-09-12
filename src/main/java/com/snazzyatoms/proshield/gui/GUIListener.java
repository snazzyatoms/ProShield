package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

/**
 * Unified GUIListener
 * 
 * Handles all ProShield GUI clicks in one place:
 * - Main menu
 * - Admin menu
 * - Roles
 * - Trust / Untrust
 * - Flags
 * - Info
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

        Inventory inv = event.getInventory();
        String title = event.getView().getTitle();
        if (title == null) return;

        // ✅ Ensure clicks in ProShield menus don’t move items
        if (title.contains("ProShield") || title.contains("Claim") || title.contains("Roles")) {
            event.setCancelled(true);
        }

        Plot plot = plugin.getPlotManager().getPlot(player.getLocation());

        switch (title) {
            case "ProShield Menu" -> {
                switch (event.getSlot()) {
                    case 10 -> plugin.getCommand("claim").execute(player, "claim", new String[0]);
                    case 11 -> plugin.getCommand("unclaim").execute(player, "unclaim", new String[0]);
                    case 12 -> guiManager.openInfoMenu(player, plot);
                    case 13 -> guiManager.openTrustMenu(player, false);
                    case 14 -> guiManager.openUntrustMenu(player, false);
                    case 15 -> guiManager.openRolesGUI(player, plot, false);
                    case 16 -> guiManager.openFlagsMenu(player, false);
                    case 26 -> player.closeInventory();
                }
            }
            case "ProShield Admin Menu" -> {
                switch (event.getSlot()) {
                    case 11 -> guiManager.openTrustMenu(player, true);
                    case 13 -> guiManager.openRolesGUI(player, plot, true);
                    case 15 -> guiManager.openFlagsMenu(player, true);
                    case 26 -> player.closeInventory();
                }
            }
            case "Manage Roles" -> {
                // Clicking a skull opens role assignment
                if (event.getCurrentItem() != null && event.getCurrentItem().getType().toString().contains("HEAD")) {
                    String targetName = event.getCurrentItem().getItemMeta().getDisplayName();
                    guiManager.openRoleAssignmentMenu(player, plot, targetName, false);
                }
                if (event.getSlot() == 26) guiManager.openMain(player);
            }
            case "Trust Player" -> {
                if (event.getSlot() == 26) guiManager.openMain(player);
            }
            case "Untrust Player" -> {
                if (event.getSlot() == 26) guiManager.openMain(player);
            }
            case "Claim Flags" -> {
                if (event.getSlot() == 26) guiManager.openMain(player);
            }
            case "Claim Info" -> {
                if (event.getSlot() == 26) guiManager.openMain(player);
            }
            default -> {
                // No-op for other inventories
            }
        }
    }
}
