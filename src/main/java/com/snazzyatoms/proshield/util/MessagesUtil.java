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
import java.util.Collections;
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

    /** Reloads messages.yml */
    public void reload() {
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        this.msgs = YamlConfiguration.loadConfiguration(messagesFile);

        // Defaults from jar
        InputStream defStream = plugin.getResource("messages.yml");
        if (defStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defStream));
            this.msgs.setDefaults(defConfig);
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
        return list.stream().map(this::color).toList();
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
        return msgs.getString("messages.prefix", "&3[ProShield]&r ");
    }

    private String debugPrefix() {
        return msgs.getString("messages.debug-prefix", "&8[Debug]&r ");
    }

    public String get(String path) {
        String v = msgs.getString(path, "");
        return color(v);
    }

    /** Get a string list path from messages.yml */
    public List<String> getList(String path) {
        List<String> list = msgs.getStringList(path);
        return list != null ? list : Collections.emptyList();
    }
}
