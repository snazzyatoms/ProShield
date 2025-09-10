// src/main/java/com/snazzyatoms/proshield/util/MessagesUtil.java
package com.snazzyatoms.proshield.util;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class MessagesUtil {
    private final ProShield plugin;
    private YamlConfiguration messages;

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

    public String get(String path, String def) {
        String v = messages.getString(path, def);
        if (v == null) v = def;
        return ChatColor.translateAlternateColorCodes('&', v);
    }

    public void send(CommandSender to, String path, String... placeholders) {
        String msg = get(path, "");
        if (msg.isEmpty()) return;
        msg = applyPlaceholders(msg, placeholders);
        to.sendMessage(msg);
    }

    public void broadcastConsole(String path, CommandSender console) {
        String msg = get(path, "");
        if (!msg.isEmpty()) console.sendMessage(msg);
    }

    private String applyPlaceholders(String msg, String... ph) {
        // expects pairs: {key, value}
        if (ph == null) return msg;
        for (int i = 0; i + 1 < ph.length; i += 2) {
            msg = msg.replace("%" + ph[i] + "%", ph[i + 1]);
        }
        return msg;
    }

    // Debug helpers (overloads to satisfy older call sites)
    public void debug(String message) {
        if (plugin.isDebugEnabled()) {
            plugin.getLogger().info("[DEBUG] " + message);
        }
    }
    public void debug(ProShield ignored, String message) {
        debug(message);
    }

    public String onOff(boolean state) {
        return state ? get("generic.on", "&aON") : get("generic.off", "&cOFF");
    }
}
