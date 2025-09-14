package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * ChatListener
 *
 * ✅ Listens for admins typing manual denial reasons after selecting "Other".
 */
public class ChatListener implements Listener {

    private final ProShield plugin;

    // Track which admin is currently typing a manual reason
    private static final Map<UUID, String> awaitingReason = new HashMap<>();

    public ChatListener(ProShield plugin) {
        this.plugin = plugin;
    }

    /**
     * Called by GUIManager when an admin chooses "Other".
     */
    public static void awaitReason(Player admin, String targetName) {
        awaitingReason.put(admin.getUniqueId(), targetName);
        admin.sendMessage(ChatColor.YELLOW + "✎ Please type your denial reason in chat...");
        admin.sendMessage(ChatColor.GRAY + "(Type 'cancel' to abort.)");
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID adminId = player.getUniqueId();

        if (!awaitingReason.containsKey(adminId)) return;

        event.setCancelled(true); // don't broadcast this chat

        String msg = event.getMessage().trim();
        if (msg.equalsIgnoreCase("cancel")) {
            awaitingReason.remove(adminId);
            player.sendMessage(ChatColor.RED + "❌ Manual denial cancelled.");
            return;
        }

        // Get the request target from the map
        String targetName = awaitingReason.remove(adminId);

        // Send the denial to the target if they are online
        Player target = plugin.getServer().getPlayerExact(targetName);
        if (target != null) {
            target.sendMessage(ChatColor.RED + "Your expansion request was denied: " + msg);
        }

        // Notify the admin
        player.sendMessage(ChatColor.GREEN + "✔ Denial reason sent: " + msg);
    }
}
