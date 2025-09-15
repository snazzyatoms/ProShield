// src/main/java/com/snazzyatoms/proshield/util/MessagesUtil.java
package com.snazzyatoms.proshield.util;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MessagesUtil {
    private final ProShield plugin;

    public MessagesUtil(ProShield plugin) {
        this.plugin = plugin;
    }

    /**
     * Send a prefixed, colored message to a player.
     */
    public void send(Player player, String message) {
        if (player == null || message == null) return;
        player.sendMessage(color(getPrefix() + message));
    }

    /**
     * Send a prefixed, colored message to any CommandSender (player or console).
     */
    public void send(CommandSender sender, String message) {
        if (sender == null || message == null) return;
        sender.sendMessage(color(getPrefix() + message));
    }

    /**
     * Send a raw (no prefix) colored message — useful for help menus.
     */
    public void sendRaw(CommandSender sender, String message) {
        if (sender == null || message == null) return;
        sender.sendMessage(color(message));
    }

    /**
     * Debug output (only prints if debug mode enabled).
     */
    public void debug(String message) {
        if (plugin.isDebugEnabled() && message != null) {
            plugin.getLogger().info(color(getDebugPrefix() + message));
        }
    }

    /**
     * Colorize a string with Bukkit-style '&' codes.
     */
    public String color(String input) {
        if (input == null) return "";
        return ChatColor.translateAlternateColorCodes('&', input);
    }

    private String getPrefix() {
        return plugin.getConfig().getString("messages.prefix", "&7[ProShield]&r ");
    }

    private String getDebugPrefix() {
        return plugin.getConfig().getString("messages.debug-prefix", "&8[Debug]&r ");
    }
}
