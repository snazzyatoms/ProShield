// src/main/java/com/snazzyatoms/proshield/languages/LanguageManager.java
package com.snazzyatoms.proshield.languages;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * LanguageManager (ProShield v1.2.6.1)
 *
 * - Handles loading of active language file
 * - Provides safe getters for strings/lists
 * - Tracks currently active language for debugging/logging
 */
public class LanguageManager {

    private final ProShield plugin;

    private FileConfiguration langConfig;
    private String activeLanguage = "en"; // default fallback

    public LanguageManager(ProShield plugin) {
        this.plugin = plugin;
        reload();
    }

    /**
     * Reload language file from disk based on config.yml
     */
    public void reload() {
        // Ensure plugin/data folder exists
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        // Get language setting from config.yml
        this.activeLanguage = plugin.getConfig().getString("settings.language", "en");

        // Load the matching messages file (e.g. messages_en.yml)
        File langFile = new File(plugin.getDataFolder(), "messages_" + activeLanguage + ".yml");

        // If it doesn’t exist, copy from resources
        if (!langFile.exists()) {
            plugin.saveResource("messages_" + activeLanguage + ".yml", false);
        }

        this.langConfig = YamlConfiguration.loadConfiguration(langFile);

        plugin.getLogger().info(ChatColor.GREEN + "[ProShield] Loaded language: " + activeLanguage);
    }

    /**
     * Get active language code (e.g. "en", "fr", "de")
     */
    public String getActiveLanguage() {
        return activeLanguage;
    }

    /**
     * Raw configuration access
     */
    public FileConfiguration raw() {
        return langConfig;
    }

    /**
     * Get a colored string from language file
     */
    public String get(String key) {
        String value = langConfig.getString(key);
        if (value == null) return null;
        return ChatColor.translateAlternateColorCodes('&', value);
    }

    /**
     * Get a string list from language file
     */
    public List<String> getList(String key) {
        List<String> list = langConfig.getStringList(key);
        if (list == null) return Collections.emptyList();
        return list.stream()
                .map(s -> ChatColor.translateAlternateColorCodes('&', s))
                .toList();
    }

    /**
     * Format with placeholders
     */
    public String format(String key, java.util.Map<String, String> placeholders) {
        String base = get(key);
        if (base == null) return null;
        for (var entry : placeholders.entrySet()) {
            base = base.replace("%" + entry.getKey() + "%", entry.getValue());
        }
        return base;
    }
}
