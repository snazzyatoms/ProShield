package com.snazzyatoms.proshield.util;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * MessagesUtil — unified messaging with placeholders and debug helpers.
 * Adds overloads referenced throughout the codebase.
 */
public class MessagesUtil {

    private final ProShield plugin;
    private File messagesFile;
    private FileConfiguration config;

    public MessagesUtil(ProShield plugin) {
        this.plugin = plugin;
        load();
    }

    private void load() {
        // Ensure default messages.yml exists
        plugin.saveResource("messages.yml", false);
        messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        config = YamlConfiguration.loadConfiguration(messagesFile);
    }

    public void reload() {
        config = YamlConfiguration.loadConfiguration(messagesFile);
    }

    public FileConfiguration getMessagesConfig() {
        return config;
    }

    /* -------- Sending -------- */

    private String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s == null ? "" : s);
    }

    private String prefix() {
        return color(config.getString("messages.prefix", "&3[ProShield]&r "));
    }

    public void send(CommandSender to, String key) {
        if (to == null) return;
        String msg = color(config.getString(key, key));
        if (!msg.isEmpty()) {
            to.sendMessage(prefix() + msg);
        }
    }

    public void send(CommandSender to, String key, String... placeholders) {
        if (to == null) return;
        String msg = config.getString(key, key);
        msg = apply(placeholders, msg);
        to.sendMessage(prefix() + color(msg));
    }

    public void send(CommandSender to, String key, Map<String, String> placeholders) {
        if (to == null) return;
        String msg = config.getString(key, key);
        msg = apply(placeholders, msg);
        to.sendMessage(prefix() + color(msg));
    }

    public void broadcastConsole(String key, CommandSender console) {
        send(console, key);
    }

    /* -------- Helpers -------- */

    public void debug(String message) {
        if (plugin.isDebugEnabled()) {
            plugin.getLogger().info("[DEBUG] " + message);
        }
    }

    public String onOff(boolean value) {
        return value ? color("&aON") : color("&cOFF");
    }

    private String apply(String[] pairs, String msg) {
        if (pairs == null) return msg;
        // pairs: "player", "%player%", "role", "%role%"
        for (int i = 0; i + 1 < pairs.length; i += 2) {
            String k = pairs[i];
            String v = pairs[i + 1];
            if (k != null && v != null) {
                msg = msg.replace("%" + k + "%", v);
            }
        }
        return msg;
    }

    private String apply(Map<String, String> map, String msg) {
        if (map == null) return msg;
        for (Map.Entry<String, String> e : map.entrySet()) {
            msg = msg.replace("%" + e.getKey() + "%", e.getValue());
        }
        return msg;
    }

    /* Convenience builders */
    public Map<String, String> of(String k, String v) {
        Map<String, String> m = new LinkedHashMap<>();
        m.put(k, v);
        return m;
    }

    public Map<String, String> of(String k1, String v1, String k2, String v2) {
        Map<String, String> m = new LinkedHashMap<>();
        m.put(k1, v1); m.put(k2, v2);
        return m;
    }

    public Map<String, String> of(String k1, String v1, String k2, String v2, String k3, String v3) {
        Map<String, String> m = new LinkedHashMap<>();
        m.put(k1, v1); m.put(k2, v2); m.put(k3, v3);
        return m;
    }
}
