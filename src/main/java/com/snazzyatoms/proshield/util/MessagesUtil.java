package com.snazzyatoms.proshield.util;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Messages utility for ProShield.
 *
 * - Backward compatible with older sendMessage(...) overloads used across listeners/commands.
 * - New flexible send(...) with vararg placeholder pairs: key, "{p1}", "Alice", "{n}", "3"
 * - Honors messages.prefix from messages.yml (or config.yml fallback)
 * - Debug helper respects proshield.debug (config.yml)
 */
public class MessagesUtil {

    private final ProShield plugin;
    private File file;
    private FileConfiguration messages;

    public MessagesUtil(ProShield plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "messages.yml");
        reload(); // load or create
    }

    /* =========================================================
     * Loading / Reloading
     * ========================================================= */

    public void reload() {
        // Ensure folder exists
        if (!plugin.getDataFolder().exists()) {
            //noinspection ResultOfMethodCallIgnored
            plugin.getDataFolder().mkdirs();
        }

        if (!file.exists()) {
            // First-run bootstrap: save default from jar if present
            plugin.saveResource("messages.yml", false);
        }

        messages = YamlConfiguration.loadConfiguration(file);

        // Minimal safety: ensure prefix exists
        if (!messages.isSet("messages.prefix")) {
            String fallback = plugin.getConfig().getString("messages.prefix", "&3[ProShield]&r ");
            messages.set("messages.prefix", fallback);
            saveQuietly();
        }
    }

    private void saveQuietly() {
        try {
            messages.save(file);
        } catch (Exception ignored) {}
    }

    /* =========================================================
     * Basic getters / utilities
     * ========================================================= */

    public String prefix() {
        return colorize(messages.getString("messages.prefix", "&3[ProShield]&r "));
    }

    public String getRaw(String key) {
        return messages.getString(key);
    }

    public boolean exists(String key) {
        return messages.isSet(key);
    }

    public String tr(String key, Object... placeholders) {
        String base = messages.getString(key, key);
        return applyPlaceholders(colorize(base), placeholders);
    }

    public void send(CommandSender to, String key, Object... placeholders) {
        String msg = tr(key, placeholders);
        if (msg == null || msg.isEmpty()) return;
        to.sendMessage(prefix() + msg);
    }

    public void send(Player to, String key, Object... placeholders) {
        send((CommandSender) to, key, placeholders);
    }

    public void sendRaw(CommandSender to, String rawColoredMessage) {
        if (rawColoredMessage == null || rawColoredMessage.isEmpty()) return;
        to.sendMessage(colorize(rawColoredMessage));
    }

    public void broadcast(String key, Object... placeholders) {
        String msg = prefix() + tr(key, placeholders);
        Bukkit.getOnlinePlayers().forEach(p -> p.sendMessage(msg));
        Bukkit.getConsoleSender().sendMessage(msg);
    }

    /* =========================================================
     * Legacy compatibility overloads (used by your current code)
     * ========================================================= */

    /** Legacy: sendMessage(player, key) */
    public void sendMessage(Player player, String key) {
        send(player, key);
    }

    /** Legacy: sendMessage(player, key, "{a}", "x") */
    public void sendMessage(Player player, String key, String p1, String v1) {
        send(player, key, p1, v1);
    }

    /** Legacy: sendMessage(player, key, "{a}","x","{b}","y","{c}","z") */
    public void sendMessage(Player player, String key,
                            String p1, String v1,
                            String p2, String v2,
                            String p3, String v3) {
        send(player, key, p1, v1, p2, v2, p3, v3);
    }

    /** Some places call messages.send(sender, key) with NO placeholders */
    public void send(CommandSender sender, String key) {
        send(sender, key, new Object[0]);
    }

    /* =========================================================
     * Debug helpers
     * ========================================================= */

    public void debug(String message) {
        boolean enabled = plugin.getConfig().getBoolean("proshield.debug", false);
        if (!enabled) return;
        Bukkit.getConsoleSender().sendMessage(prefix() + ChatColor.DARK_GRAY + "[debug] " + ChatColor.GRAY + message);
    }

    public void debug(ProShield pluginRef, String message) {
        // Some callers pass pluginRef explicitly; honor it
        boolean enabled = pluginRef.getConfig().getBoolean("proshield.debug", false);
        if (!enabled) return;
        Bukkit.getConsoleSender().sendMessage(prefix() + ChatColor.DARK_GRAY + "[debug] " + ChatColor.GRAY + message);
    }

    /* =========================================================
     * Placeholder & color helpers
     * ========================================================= */

    private String applyPlaceholders(String msg, Object... placeholders) {
        if (msg == null) return null;
        if (placeholders == null || placeholders.length == 0) return msg;

        // Accept either pairs varargs or a single Map<?,?>
        if (placeholders.length == 1 && placeholders[0] instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) placeholders[0];
            for (Map.Entry<String, Object> e : map.entrySet()) {
                msg = msg.replace(e.getKey(), String.valueOf(e.getValue()));
            }
            return msg;
        }

        // Pairs: "{key}", value, "{key2}", value2, ...
        if (placeholders.length % 2 != 0) {
            // If odd, ignore the last dangling token for safety
            Object[] trimmed = new Object[placeholders.length - 1];
            System.arraycopy(placeholders, 0, trimmed, 0, trimmed.length);
            placeholders = trimmed;
        }

        for (int i = 0; i < placeholders.length; i += 2) {
            Object k = placeholders[i];
            Object v = placeholders[i + 1];
            if (k == null) continue;
            msg = msg.replace(String.valueOf(k), String.valueOf(v));
        }
        return msg;
    }

    private String colorize(String input) {
        if (input == null) return null;
        return ChatColor.translateAlternateColorCodes('&', input);
    }

    /* =========================================================
     * Convenience builders
     * ========================================================= */

    public Map<String, Object> map(Object... kv) {
        Map<String, Object> out = new LinkedHashMap<>();
        if (kv == null) return out;
        int n = kv.length - (kv.length % 2);
        for (int i = 0; i < n; i += 2) {
            out.put(String.valueOf(kv[i]), kv[i + 1]);
        }
        return out;
    }
}
