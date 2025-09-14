package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

/**
 * ChatListener
 * -----------------
 * Listens for admin input when they pick "Other" as deny reason
 * in the Expansion GUI. Forwards the typed reason to GUIManager.
 */
public class ChatListener implements Listener {

    private final ProShield plugin;

    public ChatListener(ProShield plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;

        // Only handle if admin is flagged as awaiting a reason
        if (GUIManager.isAwaitingReason(player)) {
            event.setCancelled(true); // block from appearing in chat

            String reason = ChatColor.stripColor(event.getMessage()).trim();
            if (reason.isEmpty()) {
                plugin.getMessagesUtil().send(player, "&cDenial reason cannot be empty. Try again.");
                return;
            }

            // Pass reason to GUIManager and process denial
            GUIManager.provideManualReason(player, reason, plugin);
        }
    }
}
