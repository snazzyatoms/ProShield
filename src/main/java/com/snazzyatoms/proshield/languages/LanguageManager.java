package com.snazzyatoms.proshield.languages;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * LanguageManager (ProShield v1.2.6.1+)
 *
 * Handles:
 *  - Loading language files from /resources/localization/
 *  - Copying missing language files into /plugins/ProShield/
 *  - Selecting the active language (from config.yml)
 *  - English fallback if keys are missing
 *  - Runtime reload
 */
public class LanguageManager {

    private final ProShield plugin;

    private YamlConfiguration activeLang;   // selected language
    private YamlConfiguration fallbackLang; // English fallback

    private String currentCode;

    public LanguageManager(ProShield plugin) {
        this.plugin = plugin;
    }

    /* -------------------
     * Lifecycle
     * ------------------- */

    public void reload() {
        String langCode = plugin.getConfig().getString("settings.language", "en").toLowerCase();
        this.currentCode = langCode;

        // Target file in /plugins/ProShield/
        File targetFile = new File(plugin.getDataFolder(), "messages_" + langCode + ".yml");

        // Resource path inside JAR
        String resourcePath = "localization/messages_" + langCode + ".yml";

        // Ensure file exists locally, copy from JAR if missing
        if (!targetFile.exists()) {
            if (plugin.getResource(resourcePath) != null) {
                plugin.saveResource(resourcePath, false);
                plugin.getLogger().info("[ProShield][Lang] Extracted " + resourcePath + " to plugin folder.");
            } else {
                plugin.getLogger().warning("[ProShield][Lang] Language file not found in JAR: " + resourcePath);
            }
        }

        // Load chosen language
        this.activeLang = YamlConfiguration.loadConfiguration(targetFile);

        // Always load fallback English directly from JAR
        InputStream enStream = plugin.getResource("localization/messages_en.yml");
        if (enStream != null) {
            this.fallbackLang = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(enStream, StandardCharsets.UTF_8)
            );
        } else {
            plugin.getLogger().warning("[ProShield][Lang] English fallback not found in JAR. Keys will use defaults.");
            this.fallbackLang = new YamlConfiguration();
        }

        plugin.getLogger().info("[ProShield][Lang] Loaded language: " + langCode +
                " (fallback=en, file=" + targetFile.getName() + ")");
    }

    /* -------------------
     * Getters
     * ------------------- */

    public String get(String key) {
        String val = activeLang.getString(key);
        if (val == null || val.isBlank()) {
            val = fallbackLang.getString(key, "&cMissing: " + key);
        }
        return val;
    }

    public List<String> getList(String key) {
        List<String> list = activeLang.getStringList(key);
        if (list == null || list.isEmpty()) {
            list = fallbackLang.getStringList(key);
        }
        return list != null ? list : Collections.emptyList();
    }

    public String format(String key, Map<String, String> placeholders) {
        String msg = get(key);
        if (placeholders != null) {
            for (Map.Entry<String, String> e : placeholders.entrySet()) {
                msg = msg.replace("%" + e.getKey() + "%", e.getValue());
            }
        }
        return msg;
    }

    public Set<String> getKeys(String path) {
        if (activeLang.isConfigurationSection(path)) {
            return activeLang.getConfigurationSection(path).getKeys(false);
        }
        if (fallbackLang.isConfigurationSection(path)) {
            return fallbackLang.getConfigurationSection(path).getKeys(false);
        }
        return Collections.emptySet();
    }

    /* -------------------
     * Info
     * ------------------- */

    public String getCurrentCode() {
        return currentCode;
    }

    public YamlConfiguration raw() {
        return activeLang;
    }
}
