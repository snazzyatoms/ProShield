// src/main/java/com/snazzyatoms/proshield/util/MessagesUtil.java
package com.snazzyatoms.proshield.util;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;

/**
 * MessagesUtil (ProShield v1.2.6-enhanced)
 *
 * Handles:
 *  - Loading and caching messages.yml values
 *  - Language selection from config.yml
 *  - Fallback to English if key missing
 *  - Color formatting (& → § codes)
 *  - Utility for lists and optional values
 *  - Debug logging
 */
public class MessagesUtil {

    private final ProShield plugin;
    private FileConfiguration langConfig;     // active language file
    private FileConfiguration fallbackConfig; // English fallback

    public MessagesUtil(ProShield plugin) {
        this.plugin = plugin;
        reload();
    }

    /* -------------------
     * Lifecycle
     * ------------------- */

    public void reload() {
        // Determine language
        String lang = plugin.getConfig().getString("settings.language", "en").toLowerCase();
        String fileName = lang.equals("en") ? "messages.yml" : "messages_" + lang + ".yml";

        // Ensure files exist (ProShield.java already savesResource, but double-check)
        File langFile = new File(plugin.getDataFolder(), fileName);
        if (!langFile.exists()) {
            plugin.saveResource(fileName, false);
        }

        File fallbackFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!fallbackFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }

        // Load configs
        langConfig = YamlConfiguration.loadConfiguration(langFile);
        fallbackConfig = YamlConfiguration.loadConfiguration(fallbackFile);

        debug("Loaded language: " + fileName);
    }

    /* -------------------
     * Core Getters
     * ------------------- */

    /** Get a colored message by key, with fallback. */
    public String getOrDefault(String key, String fallback) {
        String raw = langConfig.getString(key);
        if (raw == null || raw.isBlank()) {
            raw = fallbackConfig.getString(key, fallback);
        }
        if (raw == null || raw.isBlank()) raw = fallback;
        return color(raw);
    }

    /** Get a message by key, or null if not found. */
    public String getOrNull(String key) {
        String raw = langConfig.getString(key);
        if (raw == null || raw.isBlank()) {
            raw = fallbackConfig.getString(key, null);
        }
        return (raw == null || raw.isBlank()) ? null : color(raw);
    }

    /** Get a message by key, with empty string fallback. */
    public String get(String key) {
        return getOrDefault(key, "");
    }

    /** Get a list of messages by key. */
    public List<String> getList(String key) {
        List<String> list = langConfig.getStringList(key);
        if (list == null || list.isEmpty()) {
            list = fallbackConfig.getStringList(key);
        }
        return (list == null || list.isEmpty()) ? Collections.emptyList() : colorList(list);
    }

    /**
     * Get a list of messages, or null if not found.
     * (Compatibility shim for ClaimRole#getLore)
     */
    public List<String> getListOrNull(String key) {
        List<String> list = langConfig.getStringList(key);
        if (list == null || list.isEmpty()) {
            list = fallbackConfig.getStringList(key);
        }
        return (list == null || list.isEmpty()) ? null : colorList(list);
    }

    /** Get all subkeys under a given path. */
    public Set<String> getKeys(String path) {
        if (langConfig.isConfigurationSection(path)) {
            return langConfig.getConfigurationSection(path).getKeys(false);
        } else if (fallbackConfig.isConfigurationSection(path)) {
            return fallbackConfig.getConfigurationSection(path).getKeys(false);
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
