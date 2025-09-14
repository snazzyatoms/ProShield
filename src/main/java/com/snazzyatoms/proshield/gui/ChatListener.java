// src/main/java/com/snazzyatoms/proshield/gui/ChatListener.java
package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.ChatColor;
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

        if (!GUIManager.isAwaitingReason(player)) {
            return;
        }

        event.setCancelled(true);

        String msg = event.getMessage().trim();
        if (msg.equalsIgnoreCase("cancel")) {
            GUIManager.cancelAwaiting(player);
            player.sendMessage(ChatColor.RED + "❌ Manual denial cancelled.");
            return;
        }

        GUIManager.provideManualReason(player, msg, plugin);
    }
}
