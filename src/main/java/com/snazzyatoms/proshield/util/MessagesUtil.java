package com.snazzyatoms.proshield.util;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.FileConfiguration;

public class MessagesUtil {
    private final ProShield plugin;

    public MessagesUtil(ProShield plugin) {
        this.plugin = plugin;
    }

    private FileConfiguration msgs() {
        return plugin.getMessagesConfig(); // ✅ always fetch from ProShield
    }

    public void send(Player player, String message) {
        if (player == null || message == null) return;
        String prefix = msgs().getString("prefix", "&7[ProShield]&r ");
        player.sendMessage(color(prefix + message));
    }

    public void send(CommandSender sender, String message) {
        if (sender == null || message == null) return;
        String prefix = msgs().getString("prefix", "&7[ProShield]&r ");
        sender.sendMessage(color(prefix + message));
    }

    public void sendRaw(CommandSender sender, String message) {
        if (sender == null || message == null) return;
        sender.sendMessage(color(message));
    }

    public void debug(String message) {
        if (plugin.isDebugEnabled()) {
            String prefix = msgs().getString("debug-prefix", "&8[Debug]&r ");
            plugin.getLogger().info(color(prefix + message));
        }
    }

    public String get(String path) {
        return color(msgs().getString(path, path));
    }

    public String color(String input) {
        if (input == null) return "";
        return ChatColor.translateAlternateColorCodes('&', input);
    }
}
