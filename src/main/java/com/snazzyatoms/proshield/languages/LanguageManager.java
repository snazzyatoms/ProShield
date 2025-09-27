// src/main/java/com/snazzyatoms/proshield/languages/LanguageManager.java
package com.snazzyatoms.proshield.languages;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.util.*;

/**
 * LanguageManager (ProShield v1.2.6.4)
 *
 * - Manages /localization/messages_<code>.yml files
 * - No more plain messages.yml confusion
 * - Extracts from JAR → /plugins/ProShield/localization/
 * - Always loads English as fallback
 * - Optional auto-clean of unused language files (configurable)
 */
public class LanguageManager {

    private static final String JAR_DIR   = "localization";
    private static final String PREFIX    = "messages_";
    private static final String EXT       = ".yml";
    private static final String FALLBACK  = "en";

    private final ProShield plugin;

    private FileConfiguration activeCfg;     // selected language
    private FileConfiguration fallbackCfg;   // English
    private String activeLanguage = FALLBACK;

    public LanguageManager(ProShield plugin) {
        this.plugin = plugin;
        reload();
    }

    /* -------------------------------------------------------------
     * Public API
     * ------------------------------------------------------------- */

    /** Reload according to config settings.language and refresh caches. */
    public void reload() {
        if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();

        String requested = normalize(plugin.getConfig().getString("settings.language", FALLBACK));

        // Load active language (fallback → English)
        this.activeCfg = loadOrExtract(requested);
        if (this.activeCfg == null) {
            plugin.getLogger().warning("[ProShield][Lang] Could not load '" + requested + "'. Falling back to English.");
            this.activeCfg = loadOrExtract(FALLBACK);
            this.activeLanguage = FALLBACK;
        } else {
            this.activeLanguage = requested;
        }

        // Load English fallback
        this.fallbackCfg = loadOrExtract(FALLBACK);
        if (this.fallbackCfg == null) {
            plugin.getLogger().warning("[ProShield][Lang] English fallback missing. Some keys may be empty.");
            this.fallbackCfg = new YamlConfiguration();
        }

        plugin.getLogger().info(ChatColor.GREEN + "[ProShield] Loaded language: " + this.activeLanguage
                + " (" + PREFIX + this.activeLanguage + EXT + ")");

        // 🔹 Auto-clean unused languages if enabled
        if (plugin.getConfig().getBoolean("settings.language-auto-clean", true)) {
            cleanupLanguages();
        }
    }

    /** Language code currently in use (e.g., "en", "fr", "pl"). */
    public String getActiveLanguage() {
        return activeLanguage;
    }

    /** Raw configuration for the active language. */
    public FileConfiguration raw() {
        return activeCfg;
    }

    /** Get a colorized string for key, with English fallback. */
    public String get(String key) {
        String s = activeCfg.getString(key);
        if (s == null || s.isBlank()) {
            plugin.getLogger().warning("[ProShield][Lang] Missing key: " + key + " (using English fallback)");
            s = fallbackCfg.getString(key);
        }
        if (s == null) return null;
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    /** Get a colorized list for key, with English fallback. */
    public List<String> getList(String key) {
        List<String> list = activeCfg.getStringList(key);
        if (list == null || list.isEmpty()) {
            plugin.getLogger().warning("[ProShield][Lang] Missing list key: " + key + " (using English fallback)");
            list = fallbackCfg.getStringList(key);
        }
        if (list == null) return Collections.emptyList();
        return list.stream()
                .map(s -> ChatColor.translateAlternateColorCodes('&', s))
                .toList();
    }

    /** Format a message with placeholders (%key%). */
    public String format(String key, Map<String, String> placeholders) {
        String base = get(key);
        if (base == null) return null;
        for (var e : placeholders.entrySet()) {
            base = base.replace("%" + e.getKey() + "%", e.getValue());
        }
        return base;
    }

    /* -------------------------------------------------------------
     * Internals
     * ------------------------------------------------------------- */

    private String normalize(String code) {
        if (code == null || code.isBlank()) return FALLBACK;
        return code.trim().toLowerCase(Locale.ROOT).replace('-', '_');
    }

    /**
     * Load a language file; if not extracted yet, copy it from the JAR.
     */
    private FileConfiguration loadOrExtract(String code) {
        String jarPath  = JAR_DIR + "/" + PREFIX + code + EXT;
        File   outFile  = new File(plugin.getDataFolder(), jarPath);

        if (!outFile.exists()) {
            if (!outFile.getParentFile().exists()) outFile.getParentFile().mkdirs();

            if (hasResource(jarPath)) {
                plugin.saveResource(jarPath, false);
                plugin.getLogger().info("[ProShield][Lang] Extracted: " + jarPath);
            } else {
                String altUnderscore = JAR_DIR + "/" + PREFIX + code.replace('-', '_') + EXT;
                String altHyphen     = JAR_DIR + "/" + PREFIX + code.replace('_', '-') + EXT;

                boolean extracted = false;
                if (hasResource(altUnderscore)) {
                    plugin.saveResource(altUnderscore, false);
                    plugin.getLogger().info("[ProShield][Lang] Extracted: " + altUnderscore + " (variant)");
                    outFile = new File(plugin.getDataFolder(), altUnderscore);
                    extracted = true;
                } else if (hasResource(altHyphen)) {
                    plugin.saveResource(altHyphen, false);
                    plugin.getLogger().info("[ProShield][Lang] Extracted: " + altHyphen + " (variant)");
                    outFile = new File(plugin.getDataFolder(), altHyphen);
                    extracted = true;
                }

                if (!extracted) {
                    plugin.getLogger().warning("[ProShield][Lang] Resource not in JAR: " + jarPath);
                    return null;
                }
            }
        }

        return YamlConfiguration.loadConfiguration(outFile);
    }

    private boolean hasResource(String path) {
        try (InputStream is = plugin.getResource(path)) {
            return is != null;
        } catch (Exception ignored) {
            return false;
        }
    }

    /** 🔹 Delete all localization files except active + English. */
    private void cleanupLanguages() {
        File locDir = new File(plugin.getDataFolder(), JAR_DIR);
        if (!locDir.exists() || !locDir.isDirectory()) return;

        File[] files = locDir.listFiles((dir, name) -> name.startsWith(PREFIX) && name.endsWith(EXT));
        if (files == null) return;

        for (File f : files) {
            String fname = f.getName().toLowerCase(Locale.ROOT);
            if (!fname.equals(PREFIX + activeLanguage + EXT) &&
                !fname.equals(PREFIX + FALLBACK + EXT)) {
                if (f.delete()) {
                    plugin.getLogger().info("[ProShield][Lang] Cleaned unused language file: " + f.getName());
                }
            }
        }
    }
}
