// src/main/java/com/snazzyatoms/proshield/util/MessagesUtil.java
package com.snazzyatoms.proshield.util;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class MessagesUtil {
    private final ProShield plugin;

    public MessagesUtil(ProShield plugin) {
        this.plugin = plugin;
    }

    public void send(Player player, String message) {
        if (player == null || message == null) return;
        player.sendMessage(color(plugin.getConfig().getString("messages.prefix", "&7[ProShield]&r ") + message));
    }

    public void debug(String message) {
        if (plugin.isDebugEnabled()) {
            plugin.getLogger().info(color(plugin.getConfig().getString("messages.debug-prefix", "&8[Debug]&r ") + message));
        }
    }

    // ✅ Added helper to fix "cannot find symbol: color"
    public String color(String input) {
        if (input == null) return "";
        return ChatColor.translateAlternateColorCodes('&', input);
    }
}
