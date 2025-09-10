package com.snazzyatoms.proshield.util;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

/**
 * Utility for managing messages from messages.yml
 * - Centralizes message retrieval
 * - Handles prefixes & color codes
 * - Provides convenience methods for sending to players/console
 */
public class MessagesUtil {

    private final ProShield plugin;
    private final String prefix;

    public MessagesUtil(ProShield plugin) {
        this.plugin = plugin;
        // Load prefix from messages.yml
        this.prefix = colorize(plugin.getMessages().getString("messages.prefix", "&7[ProShield]&r "));
    }

    /**
     * Get raw message string (no prefix, but with color formatting).
     *
     * @param path Path inside messages.yml
     * @return Message string, or fallback if missing
     */
    public String getRaw(String path) {
        String msg = plugin.getMessages().getString(path, "&cMissing message: " + path);
        return colorize(msg);
    }

    /**
     * Get formatted message string (with prefix + colors).
     *
     * @param path Path inside messages.yml
     * @return Prefixed & colored message
     */
    public String get(String path) {
        return prefix + getRaw(path);
    }

    /**
     * Send a message to any CommandSender (player, console, etc.)
     *
     * @param sender Target sender
     * @param path   Path inside messages.yml
     */
    public void send(CommandSender sender, String path) {
        sender.sendMessage(get(path));
    }

    /**
     * Send a formatted message with placeholders replaced.
     *
     * @param sender       Target sender
     * @param path         Path in messages.yml
     * @param replacements Key-value pairs ("{key}", "value")
     */
    public void sendFormatted(CommandSender sender, String path, String... replacements) {
        sender.sendMessage(format(path, replacements));
    }

    /**
     * Get a formatted message with placeholders replaced.
     *
     * @param path         Path in messages.yml
     * @param replacements Key-value pairs ("{key}", "value")
     * @return Formatted string
     */
    public String format(String path, String... replacements) {
        String msg = getRaw(path);
        for (int i = 0; i < replacements.length - 1; i += 2) {
            msg = msg.replace(replacements[i], replacements[i + 1]);
        }
        return prefix + msg;
    }

    /**
     * Translate & colorize `&` codes into Minecraft § codes.
     */
    private String colorize(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }
}
