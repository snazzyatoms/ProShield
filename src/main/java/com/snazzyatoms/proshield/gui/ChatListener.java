// src/main/java/com/snazzyatoms/proshield/gui/ChatListener.java
package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

/**
 * ChatListener
 *
 * ✅ Currently a placeholder (no chat capture required).
 * ✅ Registered safely for potential future features (e.g., naming towns,
 *    entering custom deny reasons, etc).
 * ✅ Kept in sync with ProShield’s GUI-first design.
 */
public class ChatListener implements Listener {

    private final ProShield plugin;
    private final GUIManager guiManager;

    public ChatListener(ProShield plugin) {
        this.plugin = plugin;
        this.guiManager = plugin.getGuiManager();
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        // --- Future use case ---
        // If we ever introduce chat-based inputs (e.g. player enters
        // a custom expansion deny reason, or names a town), we can
        // intercept here.
        //
        // Example (future):
        // if (guiManager.isAwaitingInput(player)) {
        //     event.setCancelled(true);
        //     guiManager.handleChatInput(player, event.getMessage());
        // }
    }
}
