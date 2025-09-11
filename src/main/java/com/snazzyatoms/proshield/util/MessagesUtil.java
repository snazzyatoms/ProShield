// src/main/java/com/snazzyatoms/proshield/util/MessagesUtil.java
package com.snazzyatoms.proshield.util;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;

/**
 * MessagesUtil - Handles sending and formatting messages.
 *
 * ✅ Preserves prior logic (send to players, console)
 * ✅ Added reload() to fix build errors
 * ✅ Added broadcastConsole() to fix build errors
 */
public class MessagesUtil {

    private final ProShield plugin;
    private FileConfiguration messagesConfig;
    private File file;

    public MessagesUtil(ProShield plugin) {
        this.plugin = plugin;
        load();
    }

    /** Load or reload messages.yml */
    private void load() {
        file = new File(plugin.getDataFolder(), "messages.yml");
        if (!file.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messagesConfig = YamlConfiguration.loadConfiguration(file);
    }

    /** Reload messages.yml (used in /proshield reload). */
    public void reload() {
        load();
    }

    /** Send a message by key, or fallback if not found. */
    public void send(CommandSender sender, String key, String fallback) {
        String msg = messagesConfig.getString(key, fallback);
        if (msg != null && !msg.isEmpty()) {
            sender.sendMessage(color(msg));
        }
    }

    /** Convenience: send message only by key (no fallback). */
    public void send(CommandSender sender, String key) {
        send(sender, key, key);
    }

    /** Broadcast to console (used after reloads, etc.). */
    public void broadcastConsole(String key, ConsoleCommandSender console) {
        String msg = messagesConfig.getString(key, key);
        if (msg != null && !msg.isEmpty()) {
            console.sendMessage(color(msg));
        }
    }

    /** Debug logging (if enabled). */
    public void debug(String msg) {
        if (plugin.isDebugEnabled()) {
            Bukkit.getLogger().info("[ProShield Debug] " + msg);
        }
    }

    /** Apply Bukkit color codes. */
    private String color(String input) {
        return input.replace("&", "§");
    }

    /** Save changes back to file (if plugin ever edits messages). */
    public void save() {
        try {
            messagesConfig.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public FileConfiguration getConfig() {
        return messagesConfig;
    }
}
