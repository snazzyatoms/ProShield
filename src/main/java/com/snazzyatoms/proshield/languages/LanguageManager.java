// src/main/java/com/snazzyatoms/proshield/languages/LanguageManager.java
package com.snazzyatoms.proshield.languages;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.logging.Level;

public class LanguageManager {

    private final ProShield plugin;
    private FileConfiguration messages;      // selected language (with defaults)
    private String languageCode;             // e.g. "en", "fr", "pl"

    public LanguageManager(ProShield plugin) {
        this.plugin = plugin;
    }

    /** Call onEnable and whenever /proshield reload runs */
    public void reload() {
        this.languageCode = plugin.getConfig().getString("language", "en").toLowerCase(Locale.ROOT);
        String fileName = "messages_" + languageCode + ".yml";

        // Destination = plugin folder ROOT (no languages/ subfolder)
        File dest = new File(plugin.getDataFolder(), fileName);

        // Ensure chosen language file exists on disk; copy from JAR if needed
        ensureFileFromJar("languages/" + fileName, dest, /*fallbackToEnglish=*/true);

        // Load selected language from disk
        this.messages = YamlConfiguration.loadConfiguration(utf8Reader(dest));

        // Load English defaults straight from the JAR and hook as defaults
        try (InputStream enStream = plugin.getResource("languages/messages_en.yml")) {
            if (enStream != null) {
                YamlConfiguration en = YamlConfiguration.loadConfiguration(new InputStreamReader(enStream, StandardCharsets.UTF_8));
                this.messages.setDefaults(en);
                this.messages.options().copyDefaults(true); // allows fallback lookups
            } else {
                plugin.getLogger().warning("[Lang] English fallback not found in JAR. Keys will not have defaults.");
            }
        } catch (IOException ioe) {
            plugin.getLogger().log(Level.WARNING, "[Lang] Failed reading English fallback from JAR.", ioe);
        }

        plugin.getLogger().info("[Lang] Loaded language: " + languageCode + " (" + dest.getName() + ")");
    }

    private void ensureFileFromJar(String jarPath, File dest, boolean fallbackToEnglish) {
        if (dest.exists()) return;

        // Try to copy requested language from JAR
        if (!copyFromJar(jarPath, dest)) {
            plugin.getLogger().warning("[Lang] Requested '" + jarPath + "' not found in JAR.");

            if (fallbackToEnglish) {
                // Fall back to English
                File enDest = new File(plugin.getDataFolder(), "messages_en.yml");
                if (!enDest.exists()) {
                    if (!copyFromJar("languages/messages_en.yml", enDest)) {
                        plugin.getLogger().severe("[Lang] English fallback missing from JAR. Creating empty messages_en.yml to avoid crash.");
                        try {
                            plugin.getDataFolder().mkdirs();
                            enDest.createNewFile();
                        } catch (IOException e) {
                            plugin.getLogger().log(Level.SEVERE, "[Lang] Could not create messages_en.yml", e);
                        }
                    }
                }
                // Also create an empty file for the requested language so admins can edit it
                try {
                    plugin.getDataFolder().mkdirs();
                    dest.createNewFile();
                } catch (IOException e) {
                    plugin.getLogger().log(Level.WARNING, "[Lang] Could not create " + dest.getName(), e);
                }
            }
        }
    }

    private boolean copyFromJar(String jarPath, File dest) {
        try (InputStream in = plugin.getResource(jarPath)) {
            if (in == null) return false;
            plugin.getDataFolder().mkdirs();
            try (OutputStream out = Files.newOutputStream(dest.toPath())) {
                in.transferTo(out);
            }
            return true;
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "[Lang] Failed to copy '" + jarPath + "' to " + dest.getName(), e);
            return false;
        }
    }

    private static Reader utf8Reader(File f) {
        try {
            return new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8);
        } catch (FileNotFoundException e) {
            // Shouldn't happen because ensureFileFromJar created it; return empty reader
            return new StringReader("");
        }
    }

    /** Colorized single string */
    public String get(String key) {
        String raw = messages.getString(key, key);
        return ChatColor.translateAlternateColorCodes('&', raw);
    }

    /** Colorized list */
    public List<String> getList(String key) {
        List<String> raw = messages.getStringList(key);
        if (raw == null) return Collections.emptyList();
        List<String> colored = new ArrayList<>(raw.size());
        for (String s : raw) {
            colored.add(ChatColor.translateAlternateColorCodes('&', s));
        }
        return colored;
    }

    /** Simple placeholder replacement: %key% → value */
    public String format(String key, Map<String, String> placeholders) {
        String s = get(key);
        if (placeholders != null) {
            for (Map.Entry<String, String> e : placeholders.entrySet()) {
                s = s.replace("%" + e.getKey() + "%", e.getValue());
            }
        }
        return s;
    }

    public FileConfiguration raw() {
        return messages;
    }

    public String currentLanguage() {
        return languageCode;
    }
}
