package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

/**
 * ChatListener
 *
 * ✅ Listens for admins typing manual denial reasons after selecting "Other".
 */
public class ChatListener implements Listener {

    private final ProShield plugin;

    public ChatListener(ProShield plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        // Only care if this admin is in the "awaitingReason" map
        if (!GUIManager.awaitingReason.containsKey(player.getUniqueId())) {
            return;
        }

        event.setCancelled(true); // prevent broadcast of denial reason

        String msg = event.getMessage().trim();
        if (msg.equalsIgnoreCase("cancel")) {
            GUIManager.awaitingReason.remove(player.getUniqueId());
            player.sendMessage(ChatColor.RED + "❌ Manual denial cancelled.");
            return;
        }

        // Forward reason to GUIManager static handler
        GUIManager.provideManualReason(player, msg, plugin);
    }
}
