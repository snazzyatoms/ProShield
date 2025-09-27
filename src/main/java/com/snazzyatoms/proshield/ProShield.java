package com.snazzyatoms.proshield.languages;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * LanguageManager (ProShield v1.2.6.3)
 *
 * - Looks for language files INSIDE the jar at: localization/messages_<code>.yml
 * - Extracts them to the plugin data folder: /plugins/ProShield/localization/messages_<code>.yml
 * - Always loads English (messages_en.yml) as a fallback
 * - Auto-clean support: removes unused languages if enabled
 * - Safe getters with logging for missing keys
 */
public class LanguageManager {

    private static final String JAR_DIR   = "localization";
    private static final String PREFIX    = "messages_";
    private static final String EXT       = ".yml";
    private static final String FALLBACK  = "en";

    private final ProShield plugin;

    private FileConfiguration activeCfg;     // selected language
    private FileConfiguration fallbackCfg;   // English
    private String activeLanguage = FALLBACK; // the language actually loaded

    public LanguageManager(ProShield plugin) {
        this.plugin = plugin;
        reload();
    }

    /* -------------------------------------------------------------
     * Public API
     * ------------------------------------------------------------- */

    /** Reload according to config settings.language and refresh caches. */
    public void reload() {
        // Ensure data folder exists
        if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();

        String requested = normalize(plugin.getConfig().getString("settings.language", FALLBACK));

        // Load active language (with graceful fallback to English)
        this.activeCfg = loadOrExtract(requested);
        if (this.activeCfg == null) {
            plugin.getLogger().warning("[ProShield][Lang] Could not load '" + requested + "'. Falling back to English.");
            this.activeCfg = loadOrExtract(FALLBACK);
            this.activeLanguage = FALLBACK;
        } else {
            this.activeLanguage = requested;
        }

        // Always load English fallback too
        this.fallbackCfg = loadOrExtract(FALLBACK);
        if (this.fallbackCfg == null) {
            // Should never happen if messages_en.yml is in the JAR, but guard anyway
            plugin.getLogger().warning("[ProShield][Lang] English fallback missing. Some keys may be empty.");
            this.fallbackCfg = new YamlConfiguration(); // empty
        }

        plugin.getLogger().info(ChatColor.GREEN + "[ProShield] Loaded language: " + this.activeLanguage
                + " (" + PREFIX + this.activeLanguage + EXT + ")");
    }

    /** Removes all language files except the active one (if auto-clean is enabled). */
    public void cleanInactiveLanguages() {
        File locDir = new File(plugin.getDataFolder(), JAR_DIR);
        if (!locDir.exists() || !locDir.isDirectory()) return;

        File[] files = locDir.listFiles((dir, name) -> name.startsWith(PREFIX) && name.endsWith(EXT));
        if (files == null) return;

        for (File f : files) {
            String name = f.getName().toLowerCase(Locale.ROOT);
            if (!name.equals(PREFIX + activeLanguage + EXT)) {
                if (f.delete()) {
                    plugin.getLogger().info("[ProShield][Lang] Auto-clean removed unused: " + f.getName());
                }
            }
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
            s = fallbackCfg.getString(key);
            if (s == null) {
                plugin.getLogger().warning("[ProShield][Lang] Missing key: " + key + " (no fallback found)");
                return null;
            }
            plugin.getLogger().warning("[ProShield][Lang] Missing key in " + activeLanguage + ": " + key + " (using English)");
        }
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    /** Get a colorized list for key, with English fallback. */
    public List<String> getList(String key) {
        List<String> list = activeCfg.getStringList(key);
        if (list == null || list.isEmpty()) {
            list = fallbackCfg.getStringList(key);
            if (list == null || list.isEmpty()) {
                plugin.getLogger().warning("[ProShield][Lang] Missing list key: " + key);
                return Collections.emptyList();
            }
            plugin.getLogger().warning("[ProShield][Lang] Missing list in " + activeLanguage + ": " + key + " (using English)");
        }
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
        // Normalize language codes to lowercase and prefer underscore (pt_br, zh_cn, etc.)
        return code.trim().toLowerCase(Locale.ROOT).replace('-', '_');
    }

    /**
     * Load a language file; if it isn't extracted yet, try to extract it from the JAR.
     * Looks for a matching file in the JAR at: localization/messages_<code>.yml
     * Returns a loaded YamlConfiguration, or null if neither disk nor JAR had the file.
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
                    outFile = new File(plugin.getDataFolder(), altUnderscore);
                    plugin.getLogger().info("[ProShield][Lang] Extracted: " + altUnderscore + " (variant)");
                    extracted = true;
                } else if (hasResource(altHyphen)) {
                    plugin.saveResource(altHyphen, false);
                    outFile = new File(plugin.getDataFolder(), altHyphen);
                    plugin.getLogger().info("[ProShield][Lang] Extracted: " + altHyphen + " (variant)");
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

    /** Check if a resource exists inside the plugin JAR. */
    private boolean hasResource(String path) {
        try (InputStream is = plugin.getResource(path)) {
            return is != null;
        } catch (Exception ignored) {
            return false;
        }
    }
}
