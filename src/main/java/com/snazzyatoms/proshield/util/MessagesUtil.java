package com.snazzyatoms.proshield.util;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Objects;

public class MessagesUtil {

    private final ProShield plugin;
    private FileConfiguration msgs;
    private final File messagesFile;

    public MessagesUtil(ProShield plugin) {
        this.plugin = plugin;
        this.messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        reload();
    }

    /** Reload messages.yml (with defaults from jar) */
    public void reload() {
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        this.msgs = YamlConfiguration.loadConfiguration(messagesFile);

        InputStream defStream = plugin.getResource("messages.yml");
        if (defStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defStream));
            this.msgs.setDefaults(defConfig);
        }
    }

    public FileConfiguration getConfig() {
        return msgs;
    }

    /* -----------------------------
     * Sending Helpers
     * ----------------------------- */

    public void send(Player player, String message) {
        if (player == null || message == null || message.isEmpty()) return;
        player.sendMessage(color(prefix() + message));
    }

    public void send(CommandSender sender, String message) {
        if (sender == null || message == null || message.isEmpty()) return;
        sender.sendMessage(color(prefix() + message));
    }

    /** For already prefixed or pre-colored lines (e.g. help pages) */
    public void sendRaw(CommandSender sender, String coloredLine) {
        if (sender == null || coloredLine == null || coloredLine.isEmpty()) return;
        sender.sendMessage(color(coloredLine));
    }

    /** Sends a list of lines (e.g. help pages) */
    public void sendList(CommandSender sender, List<String> lines) {
        if (sender == null || lines == null) return;
        for (String line : lines) {
            sendRaw(sender, line);
        }
    }

    public void debug(String message) {
        if (plugin.isDebugEnabled() && message != null && !message.isEmpty()) {
            plugin.getLogger().info(color(debugPrefix() + message));
        }
    }

    /* -----------------------------
     * Utility
     * ----------------------------- */

    public String color(String input) {
        return ChatColor.translateAlternateColorCodes('&', Objects.requireNonNullElse(input, ""));
    }

    private String prefix() {
        return msgs.getString("messages.prefix", "&3[ProShield]&r ");
    }

    private String debugPrefix() {
        return msgs.getString("messages.debug-prefix", "&8[Debug]&r ");
    }

    /**
     * Fetches a message by path, with color codes.
     * Example: get("messages.error.player-only")
     */
    public String get(String path) {
        String v = msgs.getString(path, "");
        return color(v);
    }

    /**
     * Fetches a list of messages (help pages, lore, etc.)
     */
    public List<String> getList(String path) {
        return msgs.getStringList(path).stream().map(this::color).toList();
    }
}
