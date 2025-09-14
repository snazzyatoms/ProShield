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
        if (!(event.getPlayer() instanceof Player)) return;
        Player player = event.getPlayer();

        if (GUIManager.isAwaitingReason(player)) {
            event.setCancelled(true);
            String reason = event.getMessage();
            GUIManager.provideManualReason(player, reason, plugin);
            player.sendMessage(ChatColor.YELLOW + "You denied a request with reason: " + reason);
        }
    }
}
