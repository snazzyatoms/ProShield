package com.snazzyatoms.proshield.util;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for handling plugin messages.
 * - Centralizes message sending
 * - Supports placeholders
 * - Preserves legacy logic while extending for 1.2.5
 */
public class MessagesUtil {

    private final ProShield plugin;
    private final FileConfiguration messages;

    public MessagesUtil(ProShield plugin) {
        this.plugin = plugin;
        this.messages = plugin.getMessagesConfig();
    }

    /* ---------------------------------------------------------
     * 🔹 Core Send Methods
     * --------------------------------------------------------- */

    public void send(CommandSender sender, String key) {
        String msg = get(key);
        if (msg != null && !msg.isEmpty()) {
            sender.sendMessage(applyPrefix(msg));
        }
    }

    public void send(CommandSender sender, String key, String... replacements) {
        String msg = get(key);
        if (msg == null || msg.isEmpty()) return;

        for (int i = 0; i < replacements.length; i++) {
            msg = msg.replace("{" + i + "}", replacements[i]);
        }
        sender.sendMessage(applyPrefix(msg));
    }

    public void send(CommandSender sender, String key, Map<String, String> placeholders) {
        String msg = get(key);
        if (msg == null || msg.isEmpty()) return;

        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            msg = msg.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        sender.sendMessage(applyPrefix(msg));
    }

    /* ---------------------------------------------------------
     * 🔹 Debug / Admin Messages
     * --------------------------------------------------------- */

    public void debug(ProShield plugin, String message) {
        if (plugin.isDebug()) {
            plugin.getLogger().info("[DEBUG] " + ChatColor.stripColor(message));
        }
    }

    public void adminBroadcast(String key, String... replacements) {
        String msg = get(key);
        if (msg == null || msg.isEmpty()) return;

        for (int i = 0; i < replacements.length; i++) {
            msg = msg.replace("{" + i + "}", replacements[i]);
        }

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (player.hasPermission("proshield.admin")) {
                player.sendMessage(applyPrefix(msg));
            }
        }
    }

    /* ---------------------------------------------------------
     * 🔹 Message Lookup
     * --------------------------------------------------------- */

    private String get(String key) {
        if (messages.contains(key)) {
            return ChatColor.translateAlternateColorCodes('&', messages.getString(key, ""));
        }
        return null;
    }

    private String applyPrefix(String msg) {
        String prefix = messages.getString("prefix", "&3[ProShield]&r ");
        return ChatColor.translateAlternateColorCodes('&', prefix) + msg;
    }

    /* ---------------------------------------------------------
     * 🔹 Placeholder Builder
     * --------------------------------------------------------- */
    public Map<String, String> buildPlaceholders(Object... pairs) {
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < pairs.length - 1; i += 2) {
            map.put(String.valueOf(pairs[i]), String.valueOf(pairs[i + 1]));
        }
        return map;
    }
}
