package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.expansion.ExpansionRequest;
import com.snazzyatoms.proshield.expansion.ExpansionRequestManager;
import com.snazzyatoms.proshield.plots.PlotManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class AdminGUIListener implements Listener {

    private final ProShield plugin;
    private final AdminGUIManager adminGUIManager;
    private final ExpansionRequestManager requestManager;
    private final PlotManager plotManager;

    // Track which request an admin is reviewing
    private UUID selectedRequest = null;

    public AdminGUIListener(ProShield plugin,
                            AdminGUIManager adminGUIManager,
                            ExpansionRequestManager requestManager,
                            PlotManager plotManager) {
        this.plugin = plugin;
        this.adminGUIManager = adminGUIManager;
        this.requestManager = requestManager;
        this.plotManager = plotManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        String title = ChatColor.stripColor(event.getView().getTitle());
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        // Cancel all clicks in admin GUIs
        if (title.equalsIgnoreCase("Expansion Requests") ||
            title.equalsIgnoreCase("Pending Expansion Requests")) {
            event.setCancelled(true);

            // Back buttons
            if (clicked.getType() == Material.BARRIER) {
                if (title.equalsIgnoreCase("Expansion Requests")) {
                    plugin.getGuiManager().openMenu(player, "admin"); // back to main admin menu
                } else {
                    adminGUIManager.openExpansionMenu(player); // back to expansion menu
                }
                return;
            }

            // Expansion Menu root actions
            if (title.equalsIgnoreCase("Expansion Requests")) {
                switch (clicked.getType()) {
                    case PAPER -> adminGUIManager.openRequestsList(player); // Pending list
                    case EMERALD -> {
                        if (selectedRequest != null) {
                            ExpansionRequest req = requestManager.getRequestByPlayer(selectedRequest);
                            if (req != null) {
                                // Apply instantly if enabled
                                if (plugin.getConfig().getBoolean("claims.instant-apply", true)) {
                                    plotManager.expandClaim(selectedRequest, req.getExtraRadius());
                                    Player target = Bukkit.getPlayer(selectedRequest);
                                    if (target != null) {
                                        target.sendMessage(plugin.getMessagesUtil()
                                                .format("&aYour claim was expanded by +" + req.getExtraRadius() + " blocks!"));
                                    }
                                }
                                requestManager.removeRequest(selectedRequest);
                                player.sendMessage(ChatColor.GREEN + "Expansion approved and applied.");
                            } else {
                                player.sendMessage(ChatColor.RED + "No request selected.");
                            }
                        } else {
                            player.sendMessage(ChatColor.RED + "Select a request first.");
                        }
                    }
                    case REDSTONE -> {
                        if (selectedRequest != null) {
                            ExpansionRequest req = requestManager.getRequestByPlayer(selectedRequest);
                            if (req != null) {
                                Player target = Bukkit.getPlayer(selectedRequest);
                                if (target != null) {
                                    target.sendMessage(plugin.getMessagesUtil()
                                            .format("&cYour claim expansion request was denied by an admin."));
                                }
                                requestManager.removeRequest(selectedRequest);
                                player.sendMessage(ChatColor.YELLOW + "Expansion denied.");
                            } else {
                                player.sendMessage(ChatColor.RED + "No request selected.");
                            }
                        } else {
                            player.sendMessage(ChatColor.RED + "Select a request first.");
                        }
                    }
                }
            }

            // Requests list actions
            if (title.equalsIgnoreCase("Pending Expansion Requests")) {
                if (clicked.getType() == Material.PAPER && clicked.hasItemMeta()) {
                    String name = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
                    Player target = Bukkit.getPlayerExact(name);
                    if (target != null) {
                        selectedRequest = target.getUniqueId();
                        player.sendMessage(ChatColor.AQUA + "Selected request: " + name);
                    } else {
                        player.sendMessage(ChatColor.RED + "That player is offline.");
                    }
                }
            }
        }
    }
}
