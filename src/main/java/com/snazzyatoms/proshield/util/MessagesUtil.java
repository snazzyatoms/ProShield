// src/main/java/com/snazzyatoms/proshield/util/MessagesUtil.java
package com.snazzyatoms.proshield.util;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

/**
 * MessagesUtil
 * - Unified message + debug system
 * - Supports nested keys in config.yml/messages.yml (e.g., messages.error.no-permission)
 * - Colorizes & codes and applies prefixes
 *
 * v1.2.5 Features:
 *   ✅ Config-driven messages (synced with messages.yml)
 *   ✅ Dynamic nested key lookups
 *   ✅ Prefix + Debug prefix
 *   ✅ Debug logging (to console and optionally admins)
 *   ✅ Null-safe and fallback-friendly
 */
public class MessagesUtil {

    private final FileConfiguration config;
    private final String prefix;
    private final String debugPrefix;

    public MessagesUtil(FileConfiguration config) {
        this.config = config;

        this.prefix = color(config.getString("messages.prefix", "&3[ProShield]&r "));
        this.debugPrefix = color(config.getString("messages.debug-prefix", "&8[Debug]&r "));
    }

    /** Translate & color codes */
    public String color(String msg) {
        return (msg == null) ? "" : ChatColor.translateAlternateColorCodes('&', msg);
    }

    /**
     * Look up a message from config.yml/messages.yml
     * e.g. get("messages.error.no-permission")
     */
    public String get(String path) {
        String raw = config.getString(path);
        return raw != null ? color(raw) : "§c<Missing: " + path + ">";
    }

    /** Send message with plugin prefix */
    public void send(CommandSender sender, String msg) {
        if (sender == null || msg == null || msg.isEmpty()) return;
        sender.sendMessage(prefix + color(msg));
    }

    /** Send config-defined message by key (with prefix) */
    public void sendKey(CommandSender sender, String path) {
        if (sender == null) return;
        sender.sendMessage(prefix + get(path));
    }

    /** Send without prefix */
    public void sendRaw(CommandSender sender, String msg) {
        if (sender == null || msg == null || msg.isEmpty()) return;
        sender.sendMessage(color(msg));
    }

    /** Debug message to a player or console */
    public void debug(CommandSender sender, String msg) {
        if (sender == null || msg == null || msg.isEmpty()) return;
        sender.sendMessage(debugPrefix + color(msg));
    }

    /** Debug message to console */
    public void debug(String msg) {
        if (msg == null || msg.isEmpty()) return;
        Bukkit.getConsoleSender().sendMessage(debugPrefix + color(msg));
    }

    /** Broadcast message to all online operators */
    public void broadcastToOps(String msg) {
        if (msg == null || msg.isEmpty()) return;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.isOp()) {
                send(player, msg);
            }
        }
        debug(msg);
    }
}
