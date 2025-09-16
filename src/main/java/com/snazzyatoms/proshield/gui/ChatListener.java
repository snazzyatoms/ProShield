package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

/**
 * ChatListener
 * Placeholder for future chat-based inputs.
 * Currently, all ProShield features (claims, roles, flags, expansions)
 * are fully GUI-driven, so no chat capture is required.
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

        // No chat-based inputs are active.
        // This listener remains registered as a safe placeholder.
    }
}
