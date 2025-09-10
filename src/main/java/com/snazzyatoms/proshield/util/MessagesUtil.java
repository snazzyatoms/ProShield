package com.snazzyatoms.proshield.util;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.logging.Level;

/**
 * MessagesUtil - Central utility for ProShield messages.
 * 
 * ✅ Preserves all prior logic
 * ✅ Adds clean debug() method (no plugin arg needed anymore)
 * ✅ Reload support for messages.yml
 * ✅ Console + broadcast helpers
 */
public class MessagesUtil {

    private final ProShield plugin;
    private YamlConfiguration config;

    public MessagesUtil(ProShield plugin) {
        this.plugin = plugin;
        reload();
    }

    /** Reloads messages.yml from disk. */
    public void reload() {
        File file = new File(plugin.getDataFolder(), "messages.yml");
        if (!file.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    /* =========================================================
     * Core message sending
     * ========================================================= */

    public void send(CommandSender sender, String path, Object... args) {
        String msg = get(path, args);
        if (msg != null && !msg.isEmpty()) {
            sender.sendMessage(colorize(msg));
        }
    }

    public void send(Player player, String path, Object... args) {
        send((CommandSender) player, path, args);
    }

    public void broadcast(String path, Object... args) {
        String msg = get(path, args);
        if (msg != null && !msg.isEmpty()) {
            Bukkit.broadcastMessage(colorize(msg));
        }
    }

    public void broadcastConsole(String path, Object... args) {
        String msg = get(path, args);
        if (msg != null && !msg.isEmpty()) {
            plugin.getLogger().info(ChatColor.stripColor(colorize(msg)));
        }
    }

    /* =========================================================
     * Debug logging
     * ========================================================= */

    /**
     * Sends a debug message if debug mode is enabled.
     * Usage: messages.debug("&cSomething happened!");
     */
    public void debug(String message) {
        if (plugin.isDebugEnabled()) {
            plugin.getLogger().log(Level.INFO, ChatColor.translateAlternateColorCodes('&',
                    config.getString("messages.debug-prefix", "&8[Debug] &r") + message));
        }
    }

    /* =========================================================
     * Helpers
     * ========================================================= */

    private String get(String path, Object... args) {
        String msg = config.getString(path);
        if (msg == null) {
            return null;
        }
        if (args != null && args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                msg = msg.replace("{" + i + "}", String.valueOf(args[i]));
            }
        }
        return msg;
    }

    private String colorize(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    public String onOff(boolean value) {
        return value ? ChatColor.GREEN + "ON" + ChatColor.RESET : ChatColor.RED + "OFF" + ChatColor.RESET;
    }
}
