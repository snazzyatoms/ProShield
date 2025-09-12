package com.snazzyatoms.proshield.util;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * MessagesUtil
 * - Handles message loading, prefixing, placeholders, broadcasting
 * - Provides multiple send(...) overloads for flexibility
 * - Supports Map-based placeholders {player}, {claim}, {role}, etc.
 */
public class MessagesUtil {

    private final ProShield plugin;
    private FileConfiguration config;

    /** Global prefix (legacy fallback) */
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

    /** Core: key + default message */
    public void send(CommandSender sender, String key, String def) {
        if (sender == null) return;
        String msg = get(key, def);
        if (msg != null && !msg.isEmpty()) {
            sender.sendMessage(PREFIX + msg);
        }
    }

    /** Overload: key only */
    public void send(CommandSender sender, String key) {
        send(sender, key, "&cMissing message: " + key);
    }

    /** Overload: positional replacements ({}) */
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

    /** ✅ Overload: Map-based replacements ({key}) */
    public void send(CommandSender sender, String key, Map<String, String> placeholders) {
        if (sender == null) return;
        String msg = get(key, "&cMissing message: " + key);

        if (placeholders != null && !placeholders.isEmpty()) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                msg = msg.replace("{" + entry.getKey() + "}", entry.getValue());
            }
        }

        sender.sendMessage(PREFIX + msg);
    }

    /* ======================================================
     * BROADCAST / CONSOLE
     * ====================================================== */

    public void broadcastConsole(String key, ConsoleCommandSender console) {
        String msg = get(key, "&cMissing broadcast: " + key);
        if (console != null) {
            console.sendMessage(PREFIX + msg);
        } else {
            plugin.getLogger().info(ChatColor.stripColor(msg));
        }
    }

    public void broadcastAll(String key) {
        String msg = get(key, "&cMissing broadcast: " + key);
        plugin.getServer().broadcastMessage(PREFIX + msg);
    }

    public void sendList(CommandSender sender, String key) {
        if (sender == null) return;
        List<String> lines = config.getStringList(key);
        if (lines != null && !lines.isEmpty()) {
            for (String line : lines) {
                sender.sendMessage(PREFIX + ChatColor.translateAlternateColorCodes('&', line));
            }
        }
    }

    /* ======================================================
     * DEBUG SUPPORT
     * ====================================================== */

    private String getDebugPrefix() {
        if (config != null && config.isString("messages.debug-prefix")) {
            return ChatColor.translateAlternateColorCodes('&',
                    config.getString("messages.debug-prefix", "&8[Debug] &r"));
        }
        return ChatColor.DARK_GRAY + "[Debug] " + ChatColor.RESET;
    }

    public void debug(ProShield plugin, String message) {
        if (plugin == null || message == null) return;
        if (plugin.isDebugEnabled()) {
            plugin.getLogger().info(ChatColor.stripColor(message));
        }
    }

    public void debug(String message) {
        if (message == null) return;
        if (plugin.isDebugEnabled()) {
            plugin.getLogger().info(ChatColor.stripColor(message));
        }
    }

    public void debug(CommandSender sender, String message) {
        if (sender == null || message == null) return;
        if (plugin.isDebugEnabled()) {
            sender.sendMessage(getDebugPrefix() + ChatColor.translateAlternateColorCodes('&', message));
        }
    }
}
