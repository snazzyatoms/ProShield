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
 * ✅ Registered safely for potential future features:
 *    - Naming towns (coming in 2.0 multiple claims)
 *    - Entering custom deny reasons
 *    - Other text-based inputs that don’t fit well in GUIs
 *
 * ✅ Synced with GUIManager so expansion is seamless.
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

        // --- Future-proof ---
        // Currently, all ProShield features are GUI-driven.
        // This listener stays as a hook for future features.
        //
        // Example for later (pseudo-code):
        // if (guiManager.isAwaitingChatInput(player)) {
        //     event.setCancelled(true);
        //     guiManager.handleChatInput(player, event.getMessage());
        // }
    }
}
