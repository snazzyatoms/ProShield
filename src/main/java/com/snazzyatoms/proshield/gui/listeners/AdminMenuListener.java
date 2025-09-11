package com.snazzyatoms.proshield.gui.listeners;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.gui.cache.GUICache;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class AdminMenuListener implements Listener {

    private final ProShield plugin;
    private final GUIManager gui;
    private final GUICache cache;
    private final PlotManager plots;
    private final MessagesUtil messages;

    public AdminMenuListener(ProShield plugin, GUIManager gui, GUICache cache, PlotManager plots) {
        this.plugin = plugin;
        this.gui = gui;
        this.cache = cache;
        this.plots = plots;
        this.messages = plugin.getMessagesUtil();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        if (e.getCurrentItem() == null) return;

        String menu = cache.getOpenMenu(player.getUniqueId());
        if (menu == null || !menu.equals("admin")) return;

        e.setCancelled(true); // prevent taking items
        ItemStack clicked = e.getCurrentItem();
        Material mat = clicked.getType();

        switch (mat) {
            case COMPASS -> {
                // Teleport tool / claim locator
                Bukkit.dispatchCommand(player, "proshield tp");
                player.closeInventory();
            }
            case BARRIER -> {
                // Force unclaim
                Bukkit.dispatchCommand(player, "proshield forceunclaim");
                player.closeInventory();
            }
            case CHEST -> {
                // Transfer claim ownership
                Bukkit.dispatchCommand(player, "proshield transfer");
                player.closeInventory();
            }
            case BOOK -> {
                // Reload configuration
                Bukkit.dispatchCommand(player, "proshield reload");
                player.closeInventory();
            }
            case REDSTONE -> {
                // Toggle debug logging
                boolean enabled = plugin.toggleDebug();
                player.sendMessage(ChatColor.YELLOW + "Debug mode: " + (enabled ? ChatColor.GREEN + "ON" : ChatColor.RED + "OFF"));
            }
            case PAPER -> {
                // Wilderness entry toggle
                boolean toggle = plugin.getConfig().getBoolean("messages.wilderness.enabled", true);
                plugin.getConfig().set("messages.wilderness.enabled", !toggle);
                plugin.saveConfig();
                player.sendMessage(ChatColor.YELLOW + "Wilderness messages: " + (!toggle ? ChatColor.GREEN + "ENABLED" : ChatColor.RED + "DISABLED"));
            }
            case LEVER -> {
                // Toggle admin-flag-chat
                boolean toggle = plugin.getConfig().getBoolean("messages.admin-flag-chat", true);
                plugin.getConfig().set("messages.admin-flag-chat", !toggle);
                plugin.saveConfig();
                player.sendMessage(ChatColor.YELLOW + "Admin flag-chat: " + (!toggle ? ChatColor.GREEN + "ENABLED" : ChatColor.RED + "DISABLED"));
            }
            default -> { /* do nothing */ }
        }
    }
}
