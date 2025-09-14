// src/main/java/com/snazzyatoms/proshield/gui/AdminGUIListener.java
package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.expansion.ExpansionRequest;
import com.snazzyatoms.proshield.expansion.ExpansionRequestManager;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Locale;

public class AdminGUIListener implements Listener {

    private final ProShield plugin;
    private final AdminGUIManager adminGUIManager;
    private final MessagesUtil messages;
    private final PlotManager plotManager;

    public AdminGUIListener(ProShield plugin, AdminGUIManager adminGUIManager) {
        this.plugin = plugin;
        this.adminGUIManager = adminGUIManager;
        this.messages = plugin.getMessagesUtil();
        this.plotManager = plugin.getPlotManager();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getCurrentItem() == null) return;

        String title = event.getView().getTitle();
        ItemStack clicked = event.getCurrentItem();
        ItemMeta meta = clicked.getItemMeta();
        if (meta == null) return;

        event.setCancelled(true); // always cancel GUI item taking

        String name = ChatColor.stripColor(meta.getDisplayName()).toLowerCase(Locale.ROOT);

        // ------------------------
        // Expansion Requests menu
        // ------------------------
        if (title.contains("Expansion Requests")) {
            switch (name) {
                case "approve selected" -> {
                    if (ExpansionRequestManager.hasRequests()) {
                        ExpansionRequest req = ExpansionRequestManager.getRequests().get(0); // handle first for now
                        Player target = Bukkit.getPlayer(req.getPlayerId());
                        if (target != null) {
                            plotManager.expandClaim(target, req.getExtraRadius());
                            messages.send(target, plugin.getConfig().getString("messages.expansion-approved")
                                    .replace("{blocks}", String.valueOf(req.getExtraRadius())));
                        }
                        ExpansionRequestManager.removeRequest(req);
                        messages.send(player, "&aExpansion approved.");
                    } else {
                        messages.send(player, "&7No requests to approve.");
                    }
                }
                case "deny selected" -> {
                    if (ExpansionRequestManager.hasRequests()) {
                        // open deny reasons menu
                        adminGUIManager.openMenu(player, "deny-reasons");
                    } else {
                        messages.send(player, "&7No requests to deny.");
                    }
                }
                case "back" -> adminGUIManager.openMenu(player, "admin");
            }
        }

        // ------------------------
        // Deny Reasons menu
        // ------------------------
        if (title.contains("Deny Reasons")) {
            if (ExpansionRequestManager.hasRequests()) {
                ExpansionRequest req = ExpansionRequestManager.getRequests().get(0); // same handling, first in queue
                Player target = Bukkit.getPlayer(req.getPlayerId());

                // Deny with chosen reason
                if (!name.equals("back")) {
                    if (target != null) {
                        messages.send(target, plugin.getConfig().getString("messages.expansion-denied")
                                .replace("{reason}", meta.getDisplayName()));
                    }
                    ExpansionRequestManager.removeRequest(req);
                    messages.send(player, "&cRequest denied: " + meta.getDisplayName());
                }
            }

            if (name.equals("back")) {
                adminGUIManager.openMenu(player, "admin-expansions");
            }
        }
    }
}
