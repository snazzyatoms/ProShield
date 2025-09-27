// src/main/java/com/snazzyatoms/proshield/util/MessagesUtil.java
package com.snazzyatoms.proshield.util;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.languages.LanguageManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.*;

/**
 * MessagesUtil (ProShield v1.2.6.1-polished)
 *
 * Handles:
 *  - Delegates to LanguageManager for loading active language
 *  - Fallbacks handled inside LanguageManager
 *  - Backwards-compatibility shims (getOrDefault, getOrNull)
 *  - Color formatting (& → § codes)
 *  - Utility for lists and optional values
 *  - Debug logging
 */
public class MessagesUtil {

    private final ProShield plugin;
    private final LanguageManager langs;

    public MessagesUtil(ProShield plugin) {
        this.plugin = plugin;
        this.langs = plugin.getLanguageManager();
    }

    /* -------------------
     * Lifecycle
     * ------------------- */

    public void reload() {
        langs.reload(); // delegate reload to LanguageManager
    }

    /* -------------------
     * Core Getters
     * ------------------- */

    /** Get a colored message by key (no explicit fallback). */
    public String get(String key) {
        return color(langs.get(key));
    }

    /** Get a list of messages by key. */
    public List<String> getList(String key) {
        return colorList(langs.getList(key));
    }

    /** Get a colored + formatted message with placeholders. */
    public String format(String key, Map<String, String> placeholders) {
        return color(langs.format(key, placeholders));
    }

    /** Get all subkeys under a given path. */
    public Set<String> getKeys(String path) {
        return langs.raw().isConfigurationSection(path)
                ? langs.raw().getConfigurationSection(path).getKeys(false)
                : Collections.emptySet();
    }

    /* -------------------
     * Backwards Compatibility Helpers
     * ------------------- */

    /** Get a message with fallback value if missing. */
    public String getOrDefault(String key, String fallback) {
        String value = langs.get(key);
        if (value == null || value.isBlank()) return color(fallback);
        return color(value);
    }

    /** Get a message or null if not found. */
    public String getOrNull(String key) {
        String value = langs.get(key);
        return (value == null || value.isBlank()) ? null : color(value);
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
        List<String> out = new ArrayList<>(input.size());
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
