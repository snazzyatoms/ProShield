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
        if (message == null || message.isBlank()) return;
        String prefix = plugin.getConfig().getString("messages.prefix", "&3[ProShield]&r ");
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + message));
    }

    public void debug(String msg) {
        if (plugin.isDebugEnabled()) {
            plugin.getLogger().info(ChatColor.stripColor(msg));
        }
    }
}
