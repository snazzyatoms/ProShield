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
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class AdminGUIListener implements Listener {

    private final ProShield plugin;
    private final AdminGUIManager adminGUIManager;
    private final ExpansionRequestManager requestManager;
    private final PlotManager plotManager;

    // Track which request an admin is reviewing
    private UUID selectedRequest = null;

    // Track admins waiting to enter denial reason
    private final Map<UUID, UUID> awaitingReason = new HashMap<>();

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
                                                .format(plugin.getConfig().getString("messages.expansion-approved")
                                                        .replace("{blocks}", String.valueOf(req.getExtraRadius()))));
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
                                // Prompt admin for denial reason
                                awaitingReason.put(player.getUniqueId(), selectedRequest);
                                player.closeInventory();
                                player.sendMessage(ChatColor.YELLOW + "Type a reason in chat for denying this request.");
                                player.sendMessage(ChatColor.GRAY + "(Type 'cancel' to abort.)");
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

    @EventHandler
    public void onAdminChat(AsyncPlayerChatEvent event) {
        Player admin = event.getPlayer();
        UUID adminId = admin.getUniqueId();

        if (!awaitingReason.containsKey(adminId)) return;

        event.setCancelled(true); // block from global chat
        String msg = event.getMessage();
        UUID playerId = awaitingReason.remove(adminId);

        if (msg.equalsIgnoreCase("cancel")) {
            admin.sendMessage(ChatColor.RED + "Denial cancelled.");
            return;
        }

        ExpansionRequest req = requestManager.getRequestByPlayer(playerId);
        if (req != null) {
            Player target = Bukkit.getPlayer(playerId);
            if (target != null) {
                target.sendMessage(plugin.getMessagesUtil()
                        .format(plugin.getConfig().getString("messages.expansion-denied")
                                .replace("{reason}", msg)));
            }
            requestManager.removeRequest(playerId);
            admin.sendMessage(ChatColor.YELLOW + "Expansion denied with reason: " + msg);
        } else {
            admin.sendMessage(ChatColor.RED + "That request no longer exists.");
        }
    }
}
