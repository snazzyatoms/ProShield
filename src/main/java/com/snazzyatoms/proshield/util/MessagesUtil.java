package com.snazzyatoms.proshield.util;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

/**
 * MessagesUtil - unified message handler for ProShield.
 *
 * - Reads from messages.yml
 * - Provides color formatting, placeholders, debug, broadcast
 * - Overloaded send() methods to prevent build errors
 */
public class MessagesUtil {

    private final ProShield plugin;
    private FileConfiguration messages;

    public MessagesUtil(ProShield plugin) {
        this.plugin = plugin;
        reload(); // load messages.yml
    }

    /**
     * Reload messages.yml from disk.
     */
    public void reload() {
        plugin.reloadConfig(); // reloads config.yml
        plugin.reloadConfig(); // safeguard for future extension
        plugin.reloadResource("messages.yml", false);
        plugin.reloadConfig();
        messages = plugin.getConfig(); // messages.yml is loaded as config resource
    }

    /**
     * Get a message by key with color codes translated.
     */
    public String get(String key) {
        if (messages == null) return ChatColor.RED + "Missing messages.yml";
        String raw = messages.getString(key, key);
        return ChatColor.translateAlternateColorCodes('&', raw);
    }

    /**
     * Send a single message to a CommandSender.
     */
    public void send(CommandSender sender, String key) {
        sender.sendMessage(get("messages.prefix") + get(key));
    }

    /**
     * Send with dynamic placeholders (up to 5 args).
     */
    public void send(CommandSender sender, String key, String... args) {
        String msg = get("messages.prefix") + get(key);
        for (int i = 0; i < args.length; i++) {
            msg = msg.replace("{" + (i + 1) + "}", args[i]);
        }
        sender.sendMessage(msg);
    }

    /**
     * Broadcast message to console.
     */
    public void broadcastConsole(String key, CommandSender console) {
        console.sendMessage(get("messages.prefix") + get(key));
    }

    /**
     * Debug logging.
     */
    public void debug(String message) {
        if (plugin.isDebugEnabled()) {
            plugin.getLogger().info("[DEBUG] " + message);
        }
    }

    /**
     * Get a list of strings (for multi-line messages).
     */
    public List<String> getList(String key) {
        return messages.getStringList(key);
    }
}
