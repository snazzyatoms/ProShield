package com.snazzyatoms.proshield.util;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

/**
 * Centralized messages loader + helpers.
 * Preserves previous keys and adds utility overloads used across code.
 */
public class MessagesUtil {

    private final ProShield plugin;
    private FileConfiguration messages;

    public MessagesUtil(ProShield plugin) {
        this.plugin = plugin;
        load();
    }

    private void load() {
        File file = new File(plugin.getDataFolder(), "messages.yml");
        if (!file.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messages = YamlConfiguration.loadConfiguration(file);
    }

    public void reload() {
        load();
    }

    public FileConfiguration getMessagesConfig() {
        return messages;
    }

    /* -------------------------
     * Sending helpers
     * ------------------------- */
    public void send(CommandSender to, String key) {
        to.sendMessage(colorize(get(key)));
    }

    public void send(CommandSender to, String key, String... params) {
        to.sendMessage(colorize(format(get(key), params)));
    }

    public void broadcastConsole(String key, CommandSender console) {
        console.sendMessage(colorize(get(key)));
    }

    public String get(String key) {
        String def = "&7" + key;
        return messages.getString(key, def);
    }

    public String onOff(boolean value) {
        return value ? ChatColor.GREEN + "ON" + ChatColor.RESET : ChatColor.RED + "OFF" + ChatColor.RESET;
    }

    public void debug(String msg) {
        if (plugin.isDebugEnabled()) {
            plugin.getLogger().info(ChatColor.translateAlternateColorCodes('&', "[DEBUG] " + msg));
        }
    }

    public static String colorize(String in) {
        return ChatColor.translateAlternateColorCodes('&', in);
    }

    private String format(String base, String... params) {
        String out = base;
        // Replace {0}..{n}
        for (int i = 0; i < params.length; i++) {
            out = out.replace("{" + i + "}", String.valueOf(params[i]));
        }
        return out;
    }
}
