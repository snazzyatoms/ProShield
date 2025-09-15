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
        GUIManager gui = plugin.getGuiManager();

        // --- EXPANSION DENY REASONS ---
        if (gui.isAwaitingReason(player)) {
            event.setCancelled(true);
            String reason = event.getMessage();
            plugin.getServer().getScheduler().runTask(plugin,
                    () -> gui.provideManualReason(player, reason));
            return;
        }

        // --- ROLES (trusted players add/remove via chat) ---
        if (gui.isAwaitingRoleAction(player)) {
            event.setCancelled(true);
            String message = event.getMessage();
            plugin.getServer().getScheduler().runTask(plugin,
                    () -> gui.handleRoleChatInput(player, message));
        }
    }
}
