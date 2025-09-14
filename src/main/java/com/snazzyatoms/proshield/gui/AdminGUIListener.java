// src/main/java/com/snazzyatoms/proshield/gui/AdminGUIListener.java
package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.expansion.ExpansionQueue;
import com.snazzyatoms.proshield.expansion.ExpansionRequest;
import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class AdminGUIListener implements Listener {

    private final ProShield plugin;
    private final MessagesUtil messages;

    public AdminGUIListener(ProShield plugin) {
        this.plugin = plugin;
        this.messages = plugin.getMessagesUtil();
    }

    @EventHandler
    public void onAdminClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        if (e.getView().getTitle() == null) return;

        if (!ChatColor.stripColor(e.getView().getTitle()).equalsIgnoreCase("Expansion Requests")) return;

        e.setCancelled(true);
        ItemStack clicked = e.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        String name = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
        ExpansionRequest request = ExpansionQueue.getPendingRequests().stream()
                .filter(r -> Bukkit.getOfflinePlayer(r.getPlayerId()).getName().equalsIgnoreCase(name.replace("Request: ", "")))
                .findFirst()
                .orElse(null);

        if (request == null) return;

        switch (e.getClick()) {
            case LEFT: {
                ExpansionQueue.approveRequest(request);
                messages.send(player, "&aApproved expansion for &f" + name);
                break;
            }
            case RIGHT: {
                ExpansionQueue.denyRequest(request, "Denied by admin"); // later we can add custom input
                messages.send(player, "&cDenied expansion for &f" + name);
                break;
            }
        }

        // Refresh menu
        Bukkit.getScheduler().runTask(plugin, () -> {
            new AdminGUIManager(plugin).openExpansionRequests(player);
        });
    }
}
