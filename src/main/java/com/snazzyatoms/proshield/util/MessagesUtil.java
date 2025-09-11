// src/main/java/com/snazzyatoms/proshield/util/MessagesUtil.java
package com.snazzyatoms.proshield.util;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.List;
import java.util.logging.Level;

/**
 * MessagesUtil
 * - Handles message loading, prefixing, placeholders, broadcasting
 * - Expanded with multiple send(...) overloads to match all plugin calls
 * - Preserves reload() and broadcastConsole() logic
 */
public class MessagesUtil {

    private final ProShield plugin;
    private FileConfiguration config;

    /** Global prefix (legacy support for listeners) */
    public static final String PREFIX = ChatColor.DARK_AQUA + "[ProShield] " + ChatColor.RESET;

    public MessagesUtil(ProShield plugin) {
        this.plugin = plugin;
        reload();
    }

    /** Reload messages.yml */
    public void reload() {
        try {
            File file = new File(plugin.getDataFolder(), "messages.yml");
            if (!file.exists()) {
                plugin.saveResource("messages.yml", false);
            }
            config = YamlConfiguration.loadConfiguration(file);
            plugin.getLogger().info("Messages reloaded.");
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to reload messages.yml", e);
        }
    }

    /** Get a message by key, with default fallback. */
    public String get(String key, String def) {
        if (config == null) return def;
        String raw = config.getString(key, def);
        return ChatColor.translateAlternateColorCodes('&', raw != null ? raw : def);
    }

    /* ======================================================
     * SEND METHODS (overloaded)
     * ====================================================== */

    /** Full signature (core): key + default message */
    public void send(CommandSender sender, String key, String def) {
        if (sender == null) return;
        String msg = get(key, def);
        if (msg != null && !msg.isEmpty()) {
            sender.sendMessage(PREFIX + msg);
        }
    }

    /** Overload: key only (no default, uses key if missing). */
    public void send(CommandSender sender, String key) {
        send(sender, key, "&cMissing message: " + key);
    }

    /** Overload: key + replacements ({} placeholders). */
    public void send(CommandSender sender, String key, String... replacements) {
        if (sender == null) return;
        String msg = get(key, "&cMissing message: " + key);

        if (replacements != null && replacements.length > 0) {
            for (String r : replacements) {
                msg = msg.replaceFirst("\\{\\}", r);
            }
        }

        sender.sendMessage(PREFIX + msg);
    }

    /* ======================================================
     * BROADCAST / CONSOLE
     * ====================================================== */

    /** Broadcast to console with optional console sender. */
    public void broadcastConsole(String key, ConsoleCommandSender console) {
        String msg = get(key, "&cMissing broadcast: " + key);
        if (console != null) {
            console.sendMessage(PREFIX + msg);
        } else {
            plugin.getLogger().info(ChatColor.stripColor(msg));
        }
    }

    /** Broadcast to all players and console. */
    public void broadcastAll(String key) {
        String msg = get(key, "&cMissing broadcast: " + key);
        plugin.getServer().broadcastMessage(PREFIX + msg);
    }

    /** Send multi-line messages (for help menus, etc.). */
    public void sendList(CommandSender sender, String key) {
        if (sender == null) return;
        List<String> lines = config.getStringList(key);
        if (lines != null && !lines.isEmpty()) {
            for (String line : lines) {
                sender.sendMessage(PREFIX + ChatColor.translateAlternateColorCodes('&', line));
            }
        }
    }
}
