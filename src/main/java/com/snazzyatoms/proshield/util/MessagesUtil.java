// src/main/java/com/snazzyatoms/proshield/util/MessagesUtil.java
package com.snazzyatoms.proshield.util;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.languages.LanguageManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.*;

/**
 * MessagesUtil (ProShield v1.2.6.1)
 *
 * Handles:
 *  - Delegates to LanguageManager for loading active language
 *  - Fallbacks handled inside LanguageManager
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

    /** Get a colored message by key, with fallback. */
    public String get(String key) {
        return langs.get(key);
    }

    /** Get a list of messages by key. */
    public List<String> getList(String key) {
        return langs.getList(key);
    }

    /** Get a colored + formatted message with placeholders. */
    public String format(String key, Map<String, String> placeholders) {
        return langs.format(key, placeholders);
    }

    /** Get all subkeys under a given path. */
    public Set<String> getKeys(String path) {
        return langs.raw().isConfigurationSection(path)
                ? langs.raw().getConfigurationSection(path).getKeys(false)
                : Collections.emptySet();
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
