package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

/**
 * ChatListener
 * - Captures chat input for expansion deny manual reasons
 * - Captures chat input for role add/remove flows
 * - Fully synchronized with GUIManager (no redundant arguments)
 */
public class ChatListener implements Listener {

    private final ProShield plugin;
    private final GUIManager guiManager;

    public ChatListener(ProShield plugin, GUIManager guiManager) {
        this.plugin = plugin;
        this.guiManager = guiManager;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        // --- EXPANSION DENY REASONS ---
        if (guiManager.isAwaitingReason(player)) {
            event.setCancelled(true);
            String reason = event.getMessage();
            // Run on main thread
            plugin.getServer().getScheduler().runTask(plugin,
                    () -> guiManager.provideManualReason(player, reason));
            return;
        }

        // --- ROLES (trusted players add/remove via chat) ---
        if (guiManager.isAwaitingRoleAction(player)) {
            event.setCancelled(true);
            String message = event.getMessage();
            // Run on main thread
            plugin.getServer().getScheduler().runTask(plugin,
                    () -> guiManager.handleRoleChatInput(player, message));
        }
    }
}
