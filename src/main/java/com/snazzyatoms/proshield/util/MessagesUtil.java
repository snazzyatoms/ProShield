// src/main/java/com/snazzyatoms/proshield/util/MessagesUtil.java
package com.snazzyatoms.proshield.util;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

/**
 * MessagesUtil (ProShield v1.2.6-enhanced)
 *
 * Handles:
 *  - Loading and caching messages.yml values
 *  - Color formatting (& → § codes)
 *  - Safe fallback lookups
 *  - Utility for lists and optional values
 *  - Debug logging
 */
public class MessagesUtil {

    private final ProShield plugin;
    private FileConfiguration config;

    public MessagesUtil(ProShield plugin) {
        this.plugin = plugin;
        reload();
    }

    /* -------------------
     * Lifecycle
     * ------------------- */

    public void reload() {
        plugin.saveDefaultConfig(); // ensures config exists
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }

    /* -------------------
     * Core Getters
     * ------------------- */

    /** Get a colored message by key, with fallback. */
    public String getOrDefault(String key, String fallback) {
        String raw = config.getString(key, fallback);
        if (raw == null || raw.isBlank()) raw = fallback;
        return color(raw);
    }

    /** Get a message by key, or null if not found. */
    public String getOrNull(String key) {
        String raw = config.getString(key, null);
        return (raw == null || raw.isBlank()) ? null : color(raw);
    }

    /** Get a message by key, with empty string fallback. */
    public String get(String key) {
        return getOrDefault(key, "");
    }

    /** Get a list of messages by key. */
    public List<String> getList(String key) {
        List<String> list = config.getStringList(key);
        if (list == null || list.isEmpty()) return Collections.emptyList();
        return colorList(list);
    }

    /**
     * Get a list of messages, or null if not found.
     * (Compatibility shim for ClaimRole#getLore)
     */
    public List<String> getListOrNull(String key) {
        if (!config.contains(key)) return null;
        List<String> list = config.getStringList(key);
        return (list == null || list.isEmpty()) ? null : colorList(list);
    }

    /** Get all subkeys under a given path. */
    public Set<String> getKeys(String path) {
        if (config.isConfigurationSection(path)) {
            return config.getConfigurationSection(path).getKeys(false);
        }
        return Collections.emptySet();
    }

    /* -------------------
     * Sending Helpers
     * ------------------- */

    public void send(CommandSender sender, String msg) {
        if (msg == null || msg.isBlank()) return;
        sender.sendMessage(color(msg));
    }

    public void sendList(CommandSender sender, List<String> msgs) {
        if (msgs == null || msgs.isEmpty()) return;
        for (String m : msgs) {
            send(sender, m);
        }
    }

    /* -------------------
     * Color Utility
     * ------------------- */

    public String color(String input) {
        if (input == null) return "";
        return ChatColor.translateAlternateColorCodes('&', input);
    }

    public List<String> colorList(List<String> input) {
        if (input == null) return Collections.emptyList();
        List<String> out = new ArrayList<>();
        for (String s : input) {
            out.add(color(s));
        }
        return out;
    }

    /* -------------------
     * Debug Utility
     * ------------------- */

    public void debug(String msg) {
        if (plugin.isDebugEnabled()) {
            plugin.getLogger().info("[Debug] " + msg);
        }
    }
}
