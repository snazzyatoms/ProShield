package com.snazzyatoms.proshield.gui.listeners;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.cache.GUICache;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

/**
 * AdminMenuListener
 *
 * - Handles admin GUI interactions (bypass, force unclaim, teleport, etc).
 * - Uses GUICache to track open admin menus.
 * - Clears state when inventory is closed.
 */
public class AdminMenuListener implements Listener {

    private final ProShield plugin;
    private final GUICache cache;

    public AdminMenuListener(ProShield plugin, GUICache cache) {
        this.plugin = plugin;
        this.cache = cache;
    }

    /* -------------------------
     * Handle menu interactions
     * ------------------------- */
    @EventHandler(ignoreCancelled = true)
    public void onMenuClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        if (e.getCurrentItem() == null) return;

        String menu = cache.getOpenMenu(player.getUniqueId());
        if (menu == null || !menu.equalsIgnoreCase("admin")) return; // not admin GUI
        e.setCancelled(true);

        ItemStack item = e.getCurrentItem();
        if (item.getType() == Material.AIR || !item.hasItemMeta()) return;

        String name = ChatColor.stripColor(item.getItemMeta().getDisplayName());
        if (name == null) return;

        switch (name.toLowerCase()) {
            case "toggle bypass" -> player.performCommand("proshield bypass");
            case "force unclaim" -> player.performCommand("proshield forceunclaim");
            case "transfer claim" -> player.performCommand("transfer <player>");
            case "teleport to claim" -> player.performCommand("proshield tp");
            case "purge expired" -> player.performCommand("proshield purge");
            case "reload" -> player.performCommand("reload");
            case "flags" -> player.performCommand("flags");
            case "debug toggle" -> player.performCommand("proshield debug");
            case "wilderness messages" -> {
                boolean enabled = plugin.getConfig().getBoolean("messages.wilderness.enabled", true);
                plugin.getConfig().set("messages.wilderness.enabled", !enabled);
                plugin.saveConfig();
                player.sendMessage(ChatColor.YELLOW + "Wilderness messages: " +
                        (enabled ? ChatColor.RED + "DISABLED" : ChatColor.GREEN + "ENABLED"));
            }
            default -> player.sendMessage(ChatColor.RED + "Unknown admin option: " + name);
        }
    }

    /* -------------------------
     * Cleanup on close
     * ------------------------- */
    @EventHandler
    public void onMenuClose(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player player)) return;
        cache.clearOpenMenu(player);
    }
}
