package com.snazzyatoms.proshield.util;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class MessagesUtil {

    private final ProShield plugin;
    private FileConfiguration msgs;
    private File messagesFile;

    public MessagesUtil(ProShield plugin) {
        this.plugin = plugin;
        reload();
    }

    /** Reloads language file (messages_xx.yml) based on config.yml */
    public void reload() {
        // normalize language code
        String lang = plugin.getConfig().getString("settings.language", "en")
                .toLowerCase(Locale.ROOT)
                .replace("-", "_");

        File langFolder = new File(plugin.getDataFolder(), "languages");
        if (!langFolder.exists()) langFolder.mkdirs();

        // choose filename
        String filename = lang.equals("en") ? "messages.yml" : "messages_" + lang + ".yml";
        messagesFile = new File(langFolder, filename);

        // ensure file exists
        if (!messagesFile.exists()) {
            InputStream resource = plugin.getResource("languages/" + filename);
            if (resource != null) {
                plugin.saveResource("languages/" + filename, false);
            } else {
                plugin.getLogger().warning("[ProShield] No translation for '" + lang + "', falling back to English.");
                filename = "messages.yml";
                messagesFile = new File(langFolder, filename);
                if (!messagesFile.exists()) {
                    plugin.saveResource("messages.yml", false);
                }
            }
        }

        // load user config
        msgs = YamlConfiguration.loadConfiguration(messagesFile);

        // load defaults from jar
        InputStream defStream = plugin.getResource(lang.equals("en") ? "messages.yml" : "languages/" + filename);
        if (defStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defStream));
            msgs.setDefaults(defConfig);
        }
    }

    public FileConfiguration getConfig() {
        return msgs;
    }

    public void send(Player player, String message) {
        if (player == null || message == null) return;
        player.sendMessage(color(prefix() + message));
    }

    public void send(CommandSender sender, String message) {
        if (sender == null || message == null) return;
        sender.sendMessage(color(prefix() + message));
    }

    /** For pre-colored lines (strings coming from config help lists, etc.) */
    public void sendRaw(CommandSender sender, String coloredLine) {
        if (sender == null || coloredLine == null) return;
        sender.sendMessage(color(coloredLine));
    }

    /** Send a list of lines (e.g., help pages) */
    public void sendList(CommandSender sender, List<String> lines) {
        if (sender == null || lines == null) return;
        for (String line : colorList(lines)) {
            sender.sendMessage(line);
        }
    }

    /** Colorize every string in a list */
    public List<String> colorList(List<String> list) {
        if (list == null) return Collections.emptyList();
        List<String> out = new ArrayList<>(list.size());
        for (String s : list) out.add(color(s));
        return out;
    }

    public void debug(String message) {
        if (plugin.isDebugEnabled()) {
            plugin.getLogger().info(color(debugPrefix() + message));
        }
    }

    public String color(String input) {
        return ChatColor.translateAlternateColorCodes('&', Objects.requireNonNullElse(input, ""));
    }

    private String prefix() {
        return msgs.getString("prefix", "&3[ProShield]&r ");
    }

    private String debugPrefix() {
        return msgs.getString("debug-prefix", "&8[Debug]&r ");
    }

    /** Get a colored string from messages.yml (or "" if missing) */
    public String get(String path) {
        String v = msgs.getString(path, "");
        return color(v);
    }

    /** Overload: get with default fallback (colored). */
    public String get(String path, String def) {
        String v = msgs.getString(path);
        return color(v != null ? v : def);
    }

    /** Preferred: fallback helper. */
    public String getOrDefault(String path, String def) {
        String v = msgs.getString(path);
        return color(v == null || v.isEmpty() ? def : v);
    }

    /** Get a string list path from messages.yml */
    public List<String> getList(String path) {
        List<String> list = msgs.getStringList(path);
        return list != null ? list : Collections.emptyList();
    }

    /** Get child keys (non-recursive) at a section path (used for deny reasons). */
    public Set<String> getKeys(String path) {
        ConfigurationSection sec = msgs.getConfigurationSection(path);
        if (sec == null) return Collections.emptySet();
        return sec.getKeys(false);
    }
}
