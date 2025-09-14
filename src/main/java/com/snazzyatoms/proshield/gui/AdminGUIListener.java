package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.expansion.ExpansionQueue;
import com.snazzyatoms.proshield.expansion.ExpansionRequest;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;
import java.util.Locale;

public class AdminGUIListener implements Listener {

    private final GUIManager guiManager;

    public AdminGUIListener(GUIManager guiManager) {
        this.guiManager = guiManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getCurrentItem() == null || event.getCurrentItem().getItemMeta() == null) return;

        String title = event.getView().getTitle();
        String name = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());

        if (title.contains("Expansion Requests")) {
            event.setCancelled(true);

            List<ExpansionRequest> pending = ExpansionQueue.getPendingRequests();

            switch (name.toLowerCase(Locale.ROOT)) {
                case "approve selected" -> {
                    if (pending.isEmpty()) {
                        player.sendMessage(ChatColor.YELLOW + "No requests to approve.");
                        return;
                    }
                    ExpansionRequest req = pending.get(0);
                    Bukkit.getServer().dispatchCommand(player, "proshield approve " + req.getPlayerId());
                }
                case "deny selected" -> {
                    if (pending.isEmpty()) {
                        player.sendMessage(ChatColor.YELLOW + "No requests to deny.");
                        return;
                    }
                    ExpansionRequest req = pending.get(0);
                    Bukkit.getServer().dispatchCommand(player, "proshield deny " + req.getPlayerId() + " other");
                }
                case "back" -> guiManager.openMenu(player, "main");
            }
        }
    }
}
