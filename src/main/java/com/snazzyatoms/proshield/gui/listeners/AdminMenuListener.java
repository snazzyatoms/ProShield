// src/main/java/com/snazzyatoms/proshield/gui/listeners/AdminMenuListener.java
package com.snazzyatoms.proshield.gui.listeners;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.gui.cache.GUICache;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.UUID;

public class AdminMenuListener implements Listener {

    private final ProShield plugin;
    private final GUIManager gui;
    private final GUICache cache;

    public AdminMenuListener(ProShield plugin, GUIManager guiManager) {
        this.plugin = plugin;
        this.gui = guiManager;
        this.cache = guiManager.getCache();
    }

    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        UUID uuid = player.getUniqueId();

        // Only our admin menu
        if (!cache.isAdminMenu(uuid, event.getInventory())) return;

        event.setCancelled(true); // ✅ make items static/unmovable

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        ItemMeta meta = clicked.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;

        String name = ChatColor.stripColor(meta.getDisplayName()).toLowerCase();

        switch (name) {
            // Give admins player-level actions too
            case "claim chunk" -> player.performCommand("claim");
            case "unclaim chunk" -> player.performCommand("unclaim");
            case "claim info" -> player.performCommand("info");

            // Admin tools
            case "teleport to claim" -> {
                player.closeInventory();
                player.sendMessage(ChatColor.YELLOW + "Use /proshield tp <owner> to teleport to claims.");
            }
            case "force unclaim" -> {
                player.closeInventory();
                player.performCommand("proshield forceunclaim");
            }
            case "manage flags (admin)" -> {
                // Use the existing flags GUI for now; admins have override power anyway
                gui.openFlagsMenu(player);
            }
            case "toggle keep-items" -> {
                boolean current = plugin.getConfig().getBoolean("keep-items.enabled", false);
                plugin.getConfig().set("keep-items.enabled", !current);
                plugin.saveConfig();
                player.sendMessage(ChatColor.GREEN + "Keep-Items is now " + (!current ? "ENABLED" : "DISABLED"));
            }
            case "wilderness tools" -> {
                boolean current = plugin.getConfig().getBoolean("messages.show-wilderness", false);
                plugin.getConfig().set("messages.show-wilderness", !current);
                plugin.saveConfig();
                player.sendMessage(ChatColor.GREEN + "Wilderness messages " + (!current ? "ENABLED" : "DISABLED"));
            }
            case "back to player menu" -> gui.openMain(player);
            default -> { /* ignore filler clicks */ }
        }
    }
}
