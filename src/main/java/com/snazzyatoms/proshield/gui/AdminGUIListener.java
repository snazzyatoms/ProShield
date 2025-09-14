package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.expansion.ExpansionRequest;
import com.snazzyatoms.proshield.expansion.ExpansionRequestManager;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class AdminGUIListener implements Listener {

    private final ProShield plugin;
    private final GUIManager guiManager;
    private final ExpansionRequestManager requestManager;
    private final PlotManager plotManager;
    private final MessagesUtil messages;

    public AdminGUIListener(ProShield plugin, GUIManager guiManager,
                            ExpansionRequestManager requestManager,
                            PlotManager plotManager, MessagesUtil messages) {
        this.plugin = plugin;
        this.guiManager = guiManager;
        this.requestManager = requestManager;
        this.plotManager = plotManager;
        this.messages = messages;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getClickedInventory() == null) return;

        String title = event.getView().getTitle();
        ItemStack item = event.getCurrentItem();
        if (item == null || !item.hasItemMeta()) return;

        // ✅ Only handle inside Admin Expansion Requests GUI
        if (!title.contains("Expansion Requests")) return;

        event.setCancelled(true);

        String action = guiManager.getAction(item);
        if (action == null) return;

        switch (action) {
            case "expansion:approve" -> {
                ExpansionRequest req = requestManager.getSelectedRequest(player.getUniqueId());
                if (req == null) {
                    messages.send(player, "&cNo request selected.");
                    return;
                }

                // Call PlotManager to apply radius expansion
                UUID targetId = req.getPlayerId();
                int extra = req.getExtraRadius();
                plotManager.expandClaim(targetId, extra);

                // Notify player if online
                Player target = Bukkit.getPlayer(targetId);
                if (target != null) {
                    String msg = plugin.getConfig()
                        .getString("messages.expansion-approved", "&aYour claim expansion was approved!")
                        .replace("{blocks}", String.valueOf(extra));
                    messages.send(target, msg);
                }

                // Clean up
                requestManager.removeRequest(targetId);
                messages.send(player, "&aApproved expansion of +" + extra + " blocks for " + targetId);
                guiManager.openMenu(player, "admin-expansions");
            }

            case "expansion:deny" -> {
                ExpansionRequest req = requestManager.getSelectedRequest(player.getUniqueId());
                if (req == null) {
                    messages.send(player, "&cNo request selected.");
                    return;
                }

                UUID targetId = req.getPlayerId();
                Player target = Bukkit.getPlayer(targetId);

                // Prompt admin in chat for reason (simple for now)
                messages.send(player, "&eType the reason for denial in chat...");

                // Simple one-off listener
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    // For now, fallback: auto-deny with generic reason
                    String reason = "Too large / not permitted";
                    if (target != null) {
                        String msg = plugin.getConfig()
                            .getString("messages.expansion-denied", "&cYour expansion was denied.")
                            .replace("{reason}", reason);
                        messages.send(target, msg);
                    }
                    requestManager.removeRequest(targetId);
                    messages.send(player, "&cDenied expansion for " + targetId + " with reason: " + reason);
                    guiManager.openMenu(player, "admin-expansions");
                }, 20L * 5); // waits 5s for simplicity
            }

            case "menu:main" -> {
                guiManager.openMenu(player, "main");
            }
        }
    }
}
