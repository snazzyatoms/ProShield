package com.snazzyatoms.proshield.util;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.FileConfiguration;

public class MessagesUtil {
    private final ProShield plugin;
    private final FileConfiguration messagesCfg;

    public MessagesUtil(ProShield plugin, FileConfiguration messagesCfg) {
        this.plugin = plugin;
        this.messagesCfg = messagesCfg;
    }

    // Send to player
    public void send(Player player, String message) {
        if (player == null || message == null) return;
        player.sendMessage(color(getPrefix() + message));
    }

    // Send to CommandSender (works for console too)
    public void send(CommandSender sender, String message) {
        if (sender == null || message == null) return;
        sender.sendMessage(color(getPrefix() + message));
    }

    // Send raw (used for help menus where prefix is not wanted)
    public void sendRaw(CommandSender sender, String message) {
        if (sender == null || message == null) return;
        sender.sendMessage(color(message));
    }

    // Debug log
    public void debug(String message) {
        if (plugin.isDebugEnabled()) {
            plugin.getLogger().info(color(getDebugPrefix() + message));
        }
    }

    // Look up a key from messages.yml, fall back to config.yml if not found
    public String get(String path) {
        if (messagesCfg != null && messagesCfg.contains(path)) {
            return color(messagesCfg.getString(path, ""));
        }
        if (plugin.getConfig().contains(path)) {
            return color(plugin.getConfig().getString(path, ""));
        }
        return color(path); // fallback: show path itself
    }

    // Prefix helpers
    private String getPrefix() {
        if (messagesCfg != null && messagesCfg.contains("prefix")) {
            return color(messagesCfg.getString("prefix", "&7[ProShield]&r "));
        }
        return color(plugin.getConfig().getString("messages.prefix", "&7[ProShield]&r "));
    }

    private String getDebugPrefix() {
        if (messagesCfg != null && messagesCfg.contains("debug-prefix")) {
            return color(messagesCfg.getString("debug-prefix", "&8[Debug]&r "));
        }
        return color(plugin.getConfig().getString("messages.debug-prefix", "&8[Debug]&r "));
    }

    // Colorizer
    public String color(String input) {
        if (input == null) return "";
        return ChatColor.translateAlternateColorCodes('&', input);
    }
}
