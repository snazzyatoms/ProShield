package com.snazzyatoms.proshield.gui.listeners;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.cache.GUICache;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.UUID;

/**
 * AdminMenuListener
 *
 * ✅ Handles clicks inside the Admin GUI menu
 * ✅ Toggles config/admin settings & runs admin tools
 * ✅ Uses GUICache to verify correct inventory
 */
public class AdminMenuListener implements Listener {

    private final ProShield plugin;
    private final GUICache cache;
    private final PlotManager plots;
    private final MessagesUtil messages;

    public AdminMenuListener(ProShield plugin, GUICache cache) {
        this.plugin = plugin;
        this.cache = cache;
        this.plots = plugin.getPlotManager();
        this.messages = plugin.getMessagesUtil();

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        UUID uuid = player.getUniqueId();

        // Verify this inventory belongs to our Admin GUI
        if (!cache.isAdminMenu(uuid, event.getInventory())) return;

        event.setCancelled(true); // Prevent item movement

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        ItemMeta meta = clicked.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;

        String name = ChatColor.stripColor(meta.getDisplayName());

        switch (name.toLowerCase()) {
            // --- Admin Toggles ---
            case "debug logging" -> {
                boolean state = plugin.toggleDebug();
                player.sendMessage(ChatColor.AQUA + "Debug logging: " + (state ? "ENABLED" : "DISABLED"));
            }
            case "wilderness messages" -> {
                boolean current = plugin.getConfig().getBoolean("messages.show-wilderness", false);
                plugin.getConfig().set("messages.show-wilderness", !current);
                plugin.saveConfig();
                player.sendMessage(ChatColor.GREEN + "Wilderness messages " + (!current ? "ENABLED" : "DISABLED"));
            }
            case "admin flag chat" -> {
                boolean current = plugin.getConfig().getBoolean("messages.admin-flag-chat", true);
                plugin.getConfig().set("messages.admin-flag-chat", !current);
                plugin.saveConfig();
                player.sendMessage(ChatColor.GREEN + "Admin flag chat " + (!current ? "ENABLED" : "DISABLED"));
            }

            // --- Admin Tools ---
            case "force unclaim" -> {
                player.closeInventory();
                player.performCommand("proshield forceunclaim");
            }
            case "transfer claim" -> {
                player.closeInventory();
                player.sendMessage(ChatColor.YELLOW + "Use /transfer <player> to transfer ownership.");
            }
            case "teleport to claim" -> {
                player.closeInventory();
                player.sendMessage(ChatColor.YELLOW + "Use /proshield tp <owner> to teleport to claims.");
            }
            case "purge expired claims" -> {
                player.closeInventory();
                player.performCommand("proshield purge");
            }
            default -> {
                // No action
            }
        }
    }
}
