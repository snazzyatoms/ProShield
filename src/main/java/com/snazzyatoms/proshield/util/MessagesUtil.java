// src/main/java/com/snazzyatoms/proshield/util/MessagesUtil.java
package com.snazzyatoms.proshield.util;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

/**
 * MessagesUtil
 *
 * ✅ Preserves prior logic
 * ✅ Expanded with reload() and broadcastConsole()
 * ✅ Provides message lookup, formatting, and debug helpers
 */
public class MessagesUtil {

    private final ProShield plugin;
    private FileConfiguration config;
    private final File file;

    public MessagesUtil(ProShield plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "messages.yml");
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    /* -------------------------------------------------------
     * Reload Support
     * ------------------------------------------------------- */

    /** Reloads messages.yml into memory. */
    public void reload() {
        if (!file.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    /* -------------------------------------------------------
     * Messaging API
     * ------------------------------------------------------- */

    /** Send a formatted message from messages.yml to a sender. */
    public void send(CommandSender sender, String path, String def) {
        String msg = config.getString(path, def);
        if (msg != null && !msg.isEmpty()) {
            sender.sendMessage(colorize(msg));
        }
    }

    /** Send a raw message directly (with color codes). */
    public void sendRaw(CommandSender sender, String raw) {
        if (raw != null && !raw.isEmpty()) {
            sender.sendMessage(colorize(raw));
        }
    }

    /** Broadcast a message to console (with key + fallback). */
    public void broadcastConsole(String path, ConsoleCommandSender console) {
        String msg = config.getString(path, "&e[ProShield] " + path);
        if (msg != null && !msg.isEmpty()) {
            console.sendMessage(colorize(msg));
        }
    }

    /** Debug logging to console only. */
    public void debug(String message) {
        if (plugin.isDebugEnabled()) {
            plugin.getLogger().info("[DEBUG] " + ChatColor.stripColor(message));
        }
    }

    /* -------------------------------------------------------
     * Utility
     * ------------------------------------------------------- */

    private String colorize(String input) {
        return ChatColor.translateAlternateColorCodes('&', input);
    }
}
