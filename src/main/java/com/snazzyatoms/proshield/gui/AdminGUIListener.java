package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.expansion.ExpansionRequest;
import com.snazzyatoms.proshield.expansion.ExpansionRequestManager;
import com.snazzyatoms.proshield.plots.PlotManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AdminGUIListener implements Listener {

    private final ProShield plugin;
    private final AdminGUIManager guiManager;
    private final PlotManager plotManager;

    // Tracks which request an admin is currently reviewing
    private final Map<UUID, ExpansionRequest> selectedRequests = new HashMap<>();

    public AdminGUIListener(ProShield plugin, AdminGUIManager guiManager, PlotManager plotManager) {
        this.plugin = plugin;
        this.guiManager = guiManager;
        this.plotManager = plotManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player admin)) return;

        Inventory inv = event.getInventory();
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        String title = ChatColor.stripColor(inv.getTitle());
        String name = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());

        // --- Admin Menu ---
        if (title.equalsIgnoreCase("Admin Menu")) {
            event.setCancelled(true);

            if (name.equalsIgnoreCase("Expansion Requests")) {
                guiManager.openExpansionRequestsMenu(admin);
            } else if (name.equalsIgnoreCase("Back")) {
                // Back to player main menu
                plugin.getGuiManager().openMenu(admin, "main");
            }
        }

        // --- Expansion Requests Menu ---
        else if (title.equalsIgnoreCase("Expansion Requests")) {
            event.setCancelled(true);

            if (clicked.getType() == Material.PAPER && name.startsWith("Request from")) {
                // Select this request
                ExpansionRequest req = ExpansionRequestManager.getRequests().stream()
                        .filter(r -> {
                            String playerName = Bukkit.getOfflinePlayer(r.getPlayerId()).getName();
                            return name.contains(playerName);
                        })
                        .findFirst().orElse(null);

                if (req != null) {
                    selectedRequests.put(admin.getUniqueId(), req);
                    admin.sendMessage(ChatColor.GREEN + "Selected request from " +
                            Bukkit.getOfflinePlayer(req.getPlayerId()).getName() +
                            " (+" + req.getExtraRadius() + " blocks).");
                }
            }

            else if (name.equalsIgnoreCase("Approve Selected")) {
                ExpansionRequest req = selectedRequests.get(admin.getUniqueId());
                if (req != null) {
                    FileConfiguration cfg = plugin.getConfig();
                    boolean instant = cfg.getBoolean("claims.instant-apply", true);

                    if (instant) {
                        // Apply immediately
                        plotManager.expandClaim(req.getPlayerId(), req.getExtraRadius());
                        Player target = Bukkit.getPlayer(req.getPlayerId());
                        if (target != null) {
                            target.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                    plugin.getConfig().getString("messages.expansion-approved")
                                            .replace("{blocks}", String.valueOf(req.getExtraRadius()))));
                        }
                        admin.sendMessage(ChatColor.GREEN + "Approved expansion (+"
                                + req.getExtraRadius() + " blocks) for "
                                + Bukkit.getOfflinePlayer(req.getPlayerId()).getName());
                    } else {
                        admin.sendMessage(ChatColor.YELLOW + "Expansion queued until restart.");
                    }

                    ExpansionRequestManager.removeRequest(req);
                    selectedRequests.remove(admin.getUniqueId());
                    guiManager.openExpansionRequestsMenu(admin);
                } else {
                    admin.sendMessage(ChatColor.RED + "No request selected.");
                }
            }

            else if (name.equalsIgnoreCase("Deny Selected")) {
                ExpansionRequest req = selectedRequests.get(admin.getUniqueId());
                if (req != null) {
                    Player target = Bukkit.getPlayer(req.getPlayerId());
                    if (target != null) {
                        target.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                plugin.getConfig().getString("messages.expansion-denied")
                                        .replace("{reason}", "Too large / abusive expansion")));
                    }
                    admin.sendMessage(ChatColor.RED + "Denied request from "
                            + Bukkit.getOfflinePlayer(req.getPlayerId()).getName());
                    ExpansionRequestManager.removeRequest(req);
                    selectedRequests.remove(admin.getUniqueId());
                    guiManager.openExpansionRequestsMenu(admin);
                } else {
                    admin.sendMessage(ChatColor.RED + "No request selected.");
                }
            }

            else if (name.equalsIgnoreCase("Back")) {
                guiManager.openAdminMenu(admin);
            }
        }
    }
}
