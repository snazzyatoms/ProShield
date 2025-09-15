package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

/**
 * ChatListener
 * - Delegates all special chat input (deny reasons, role input, etc.)
 *   into GUIManager.handleChatInput.
 * - Keeps main thread safe by rescheduling any GUI work.
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

        // If GUIManager expects chat input from this player
        if (guiManager.isAwaitingChatInput(player)) {
            event.setCancelled(true);
            String message = event.getMessage();

            // Switch back to main thread for GUI actions
            plugin.getServer().getScheduler().runTask(plugin,
                    () -> guiManager.handleChatInput(player, message));
        }
    }
}
