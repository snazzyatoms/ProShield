package com.snazzyatoms.proshield.util;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

/**
 * Utility for sending messages to players/console with placeholder support.
 *
 * Preserves prior logic and extends:
 * - debug(ProShield, String) overload (fixes compile errors in listeners)
 * - colorizing, placeholders, and lists
 * - on/off helpers for flags
 */
public class MessagesUtil {

    private final ProShield plugin;
    private final FileConfiguration messages;

    public MessagesUtil(ProShield plugin, FileConfiguration messages) {
        this.plugin = plugin;
        this.messages = messages;
    }

    /* -------------------------------------------------------
     * Message Sending
     * ------------------------------------------------------- */

    public void send(CommandSender sender, String path, Object... placeholders) {
        String msg = get(path, placeholders);
        if (msg != null && !msg.isEmpty()) {
            sender.sendMessage(color(msg));
        }
    }

    public void sendList(CommandSender sender, String path, Object... placeholders) {
        List<String> list = messages.getStringList(path);
        if (list == null || list.isEmpty()) return;
        for (String line : list) {
            sender.sendMessage(color(applyPlaceholders(line, placeholders)));
        }
    }

    public void broadcast(String path, Object... placeholders) {
        String msg = get(path, placeholders);
        if (msg != null && !msg.isEmpty()) {
            plugin.getServer().broadcastMessage(color(msg));
        }
    }

    /* -------------------------------------------------------
     * Debug Logging
     * ------------------------------------------------------- */

    /** Debug log to console (single arg). */
    public void debug(String message) {
        if (plugin.getConfig().getBoolean("debug", false)) {
            plugin.getLogger().info(color(message));
        }
    }

    /** Debug log overload used by many listeners (accepts plugin + msg). */
    public void debug(ProShield plugin, String message) {
        if (plugin.getConfig().getBoolean("debug", false)) {
            plugin.getLogger().info(color(message));
        }
    }

    /* -------------------------------------------------------
     * Internal Helpers
     * ------------------------------------------------------- */

    public String get(String path, Object... placeholders) {
        String raw = messages.getString(path);
        if (raw == null) return null;
        return applyPlaceholders(raw, placeholders);
    }

    public String color(String msg) {
        if (msg == null) return "";
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    private String applyPlaceholders(String msg, Object... placeholders) {
        if (msg == null || placeholders == null) return msg;
        String result = msg;
        for (int i = 0; i + 1 < placeholders.length; i += 2) {
            Object key = placeholders[i];
            Object val = placeholders[i + 1];
            if (key != null && val != null) {
                result = result.replace("{" + key + "}", val.toString());
            }
        }
        return result;
    }

    /* -------------------------------------------------------
     * Flag Helpers
     * ------------------------------------------------------- */

    public String onOff(boolean value) {
        return value ? ChatColor.GREEN + "ON" : ChatColor.RED + "OFF";
    }
}
