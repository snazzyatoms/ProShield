package com.snazzyatoms.proshield.util;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.logging.Level;

/**
 * Central utility for handling messages from messages.yml
 * Provides overloaded send/debug methods for commands & listeners.
 */
public class MessagesUtil {

    private final ProShield plugin;
    private FileConfiguration config;

    public MessagesUtil(ProShield plugin) {
        this.plugin = plugin;
        reload();
    }

    /* -----------------------------
     * Reload messages.yml
     * ----------------------------- */
    public void reload() {
        plugin.reloadConfig(); // reloads all configs
        plugin.reloadConfig(); // ensure sync with Bukkit API
        config = plugin.getConfig(); // fallback in case messages.yml not found
        try {
            config = plugin.getConfig(); // prefer plugin-provided
            plugin.reloadConfig();
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Could not reload messages.yml", e);
        }
    }

    /* -----------------------------
     * Internal fetch
     * ----------------------------- */
    private String get(String key) {
        if (config == null) return ChatColor.RED + "Missing messages.yml";
        String raw = config.getString(key, "&cMissing message: " + key);
        return ChatColor.translateAlternateColorCodes('&', raw);
    }

    /* -----------------------------
     * Sending methods (overloads)
     * ----------------------------- */
    public void send(CommandSender sender, String key) {
        sender.sendMessage(get(key));
    }

    public void send(CommandSender sender, String key, String arg1) {
        sender.sendMessage(get(key).replace("{0}", arg1));
    }

    public void send(CommandSender sender, String key, String arg1, String arg2) {
        sender.sendMessage(get(key).replace("{0}", arg1).replace("{1}", arg2));
    }

    public void send(CommandSender sender, String key, String arg1, String arg2, String arg3) {
        sender.sendMessage(
                get(key)
                        .replace("{0}", arg1)
                        .replace("{1}", arg2)
                        .replace("{2}", arg3)
        );
    }

    public void send(CommandSender sender, String key, String arg1, String arg2, String arg3, String arg4, String arg5) {
        sender.sendMessage(
                get(key)
                        .replace("{0}", arg1)
                        .replace("{1}", arg2)
                        .replace("{2}", arg3)
                        .replace("{3}", arg4)
                        .replace("{4}", arg5)
        );
    }

    /* -----------------------------
     * Console + Broadcast helpers
     * ----------------------------- */
    public void broadcastConsole(String key, CommandSender console) {
        console.sendMessage(get(key));
    }

    public void broadcastConsole(String key, String arg1, CommandSender console) {
        console.sendMessage(get(key).replace("{0}", arg1));
    }

    /* -----------------------------
     * Debug
     * ----------------------------- */
    public void debug(ProShield plugin, String message) {
        if (plugin.isDebugEnabled()) {
            plugin.getLogger().info("[DEBUG] " + message);
        }
    }
}
