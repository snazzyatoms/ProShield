// src/main/java/com/snazzyatoms/proshield/util/MessagesUtil.java
package com.snazzyatoms.proshield.util;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * MessagesUtil
 * - Unified access to messages from messages.yml and config.yml
 * - Auto-fallback: check messages.yml first, then config.yml
 * - Prefix + Debug formatting
 */
public class MessagesUtil {
    private final ProShield plugin;
    private final FileConfiguration messagesConfig; // messages.yml

    public MessagesUtil(ProShield plugin) {
        this.plugin = plugin;
        this.messagesConfig = plugin.getConfig(); // default fallback
        // TODO: if you’ve got a separate messages.yml loader, point it here.
    }

    /**
     * Send a message to a player with prefix applied.
     */
    public void send(Player player, String message) {
        if (player == null || message == null) return;
        player.sendMessage(color(getPrefix() + message));
    }

    /**
     * Send to any CommandSender (player, console, etc.).
     */
    public void send(CommandSender sender, String message) {
        if (sender == null || message == null) return;
        sender.sendMessage(color(getPrefix() + message));
    }

    /**
     * Send a raw line without prefix (useful for help menus).
     */
    public void sendRaw(CommandSender sender, String message) {
        if (sender == null || message == null) return;
        sender.sendMessage(color(message));
    }

    /**
     * Fetch a message by key with auto-fallback:
     *  1. Look in messages.yml
     *  2. If missing, look in config.yml
     *  3. If still missing, show placeholder
     */
    public String get(String path) {
        String msg = plugin.getConfig().getString(path); // fallback
        try {
            // if you have a dedicated messages.yml, swap plugin.getConfig() for it
            msg = messagesConfig.getString(path, msg);
        } catch (Exception ignored) {}
        if (msg == null) {
            msg = "&c[Missing message: " + path + "]";
        }
        return color(msg);
    }

    /**
     * Get prefix from config/messages.
     */
    private String getPrefix() {
        return color(plugin.getConfig().getString("messages.prefix", "&7[ProShield]&r "));
    }

    /**
     * Debug log to console if debug is enabled.
     */
    public void debug(String message) {
        if (plugin.isDebugEnabled()) {
            plugin.getLogger().info(color(plugin.getConfig().getString(
                    "messages.debug-prefix", "&8[Debug]&r ") + message));
        }
    }

    /**
     * Apply color codes (& → §).
     */
    public String color(String input) {
        if (input == null) return "";
        return ChatColor.translateAlternateColorCodes('&', input);
    }
}
