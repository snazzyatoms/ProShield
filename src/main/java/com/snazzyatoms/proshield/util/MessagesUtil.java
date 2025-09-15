package com.snazzyatoms.proshield.util;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.FileConfiguration;

public class MessagesUtil {
    private final ProShield plugin;
    private final FileConfiguration msgs; // messages.yml loaded into ProShield#getMessagesConfig

    public MessagesUtil(ProShield plugin) {
        this.plugin = plugin;
        this.msgs = plugin.getMessagesConfig() != null ? plugin.getMessagesConfig() : plugin.getConfig();
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

    public void debug(String message) {
        if (plugin.isDebugEnabled()) {
            plugin.getLogger().info(color(debugPrefix() + message));
        }
    }

    public String color(String input) {
        if (input == null) return "";
        return ChatColor.translateAlternateColorCodes('&', input);
    }

    private String prefix() {
        return msgs.getString("messages.prefix",
                msgs.getString("prefix", "&3[ProShield]&r "));
    }

    private String debugPrefix() {
        return msgs.getString("messages.debug-prefix",
                msgs.getString("debug-prefix", "&8[Debug]&r "));
    }

    public String get(String path) {
        String v = msgs.getString(path, null);
        if (v == null) v = plugin.getConfig().getString(path, "");
        return color(v);
    }
}
