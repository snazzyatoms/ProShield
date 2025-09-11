package com.snazzyatoms.proshield.gui.listeners;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.cache.GUICache;
import com.snazzyatoms.proshield.plots.PlotManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

/**
 * PlayerMenuListener
 *
 * - Handles player menu clicks (flags, trust, claim tools).
 * - Uses GUICache to track open menus.
 * - Clears cache on close to prevent stale state.
 */
public class PlayerMenuListener implements Listener {

    private final ProShield plugin;
    private final GUICache cache;
    private final PlotManager plots;

    public PlayerMenuListener(ProShield plugin, GUICache cache, PlotManager plots) {
        this.plugin = plugin;
        this.cache = cache;
        this.plots = plots;
    }

    /* -------------------------
     * Handle menu interactions
     * ------------------------- */
    @EventHandler(ignoreCancelled = true)
    public void onMenuClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        if (e.getCurrentItem() == null) return;

        String menu = cache.getOpenMenu(player.getUniqueId());
        if (menu == null) return; // not a ProShield menu
        e.setCancelled(true);

        ItemStack item = e.getCurrentItem();
        if (item.getType() == Material.AIR || !item.hasItemMeta()) return;

        String name = ChatColor.stripColor(item.getItemMeta().getDisplayName());
        if (name == null) return;

        switch (menu.toLowerCase()) {
            case "main" -> handleMainMenu(player, name);
            case "flags" -> handleFlagsMenu(player, name);
            default -> { /* future menus */ }
        }
    }

    private void handleMainMenu(Player player, String name) {
        switch (name.toLowerCase()) {
            case "claim chunk" -> player.performCommand("claim");
            case "unclaim chunk" -> player.performCommand("unclaim");
            case "claim info" -> player.performCommand("info");
            case "flags" -> player.performCommand("flags");
            case "roles" -> player.performCommand("roles");
            case "trusted players" -> player.performCommand("trusted");
            default -> player.sendMessage(ChatColor.RED + "Unknown option: " + name);
        }
    }

    private void handleFlagsMenu(Player player, String name) {
        // Delegated to FlagsListener; here we just prevent dragging
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
