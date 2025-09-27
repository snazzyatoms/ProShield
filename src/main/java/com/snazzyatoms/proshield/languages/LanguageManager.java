// src/main/java/com/snazzyatoms/proshield/languages/LanguageManager.java
package com.snazzyatoms.proshield.languages;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * LanguageManager (ProShield v1.2.6.2)
 *
 * - Loads active language file (messages_xx.yml)
 * - Always loads English as fallback
 * - Provides safe getters with fallback
 * - Tracks active language for debugging/logging
 */
public class LanguageManager {

    private final ProShield plugin;

    private FileConfiguration langConfig;   // active language
    private FileConfiguration fallbackConfig; // default EN
    private String activeLanguage = "en";   // default fallback

    public LanguageManager(ProShield plugin) {
        this.plugin = plugin;
        reload();
    }

    /**
     * Reload language file from disk based on config.yml
     */
    public void reload() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        // Load chosen language
        this.activeLanguage = plugin.getConfig().getString("settings.language", "en");
        this.langConfig = loadLang(activeLanguage);

        // Always load English as fallback
        this.fallbackConfig = loadLang("en");

        plugin.getLogger().info(ChatColor.GREEN + "[ProShield] Loaded language: " + activeLanguage);
    }

    private FileConfiguration loadLang(String lang) {
        File langFile = new File(plugin.getDataFolder(), "messages_" + lang + ".yml");
        if (!langFile.exists()) {
            plugin.saveResource("messages_" + lang + ".yml", false);
        }
        return YamlConfiguration.loadConfiguration(langFile);
    }

    /**
     * Get active language code (e.g. "en", "fr", "de")
     */
    public String getActiveLanguage() {
        return activeLanguage;
    }

    /**
     * Raw configuration (active language only)
     */
    public FileConfiguration raw() {
        return langConfig;
    }

    /**
     * Get a string with fallback
     */
    public String get(String key) {
        String value = langConfig.getString(key);
        if (value == null || value.isBlank()) {
            value = fallbackConfig.getString(key);
        }
        if (value == null) return null;
        return ChatColor.translateAlternateColorCodes('&', value);
    }

    /**
     * Get a list with fallback
     */
    public List<String> getList(String key) {
        List<String> list = langConfig.getStringList(key);
        if (list == null || list.isEmpty()) {
            list = fallbackConfig.getStringList(key);
        }
        if (list == null) return Collections.emptyList();
        return list.stream()
                .map(s -> ChatColor.translateAlternateColorCodes('&', s))
                .toList();
    }

    /**
     * Format with placeholders
     */
    public String format(String key, Map<String, String> placeholders) {
        String base = get(key);
        if (base == null) return null;
        for (var entry : placeholders.entrySet()) {
            base = base.replace("%" + entry.getKey() + "%", entry.getValue());
        }
        return base;
    }
}
