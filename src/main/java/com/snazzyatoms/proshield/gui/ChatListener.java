package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {

    private final ProShield plugin;

    public ChatListener(ProShield plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        // --- EXPANSION DENY REASONS ---
        if (GUIManager.isAwaitingReason(player)) {
            event.setCancelled(true);
            String reason = event.getMessage();
            plugin.getServer().getScheduler().runTask(plugin,
                    () -> GUIManager.provideManualReason(player, reason, plugin));
            return;
        }

        // --- ROLES (trusted players add/remove) ---
        if (GUIManager.isAwaitingRoleAction(player)) {
            event.setCancelled(true);
            String message = event.getMessage();
            plugin.getServer().getScheduler().runTask(plugin,
                    () -> GUIManager.handleRoleChatInput(player, message, plugin));
        }
    }
}
