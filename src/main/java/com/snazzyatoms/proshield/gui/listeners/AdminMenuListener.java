// src/main/java/com/snazzyatoms/proshield/gui/listeners/AdminMenuListener.java
package com.snazzyatoms.proshield.gui.listeners;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.gui.cache.GUICache;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
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
 * ✅ Supports back button → returns to admin main
 * ✅ Plays sounds for feedback
 */
public class AdminMenuListener implements Listener {

    private final ProShield plugin;
    private final GUIManager gui;
    private final GUICache cache;
    private final MessagesUtil messages;

    public AdminMenuListener(ProShield plugin, GUIManager gui) {
        this.plugin = plugin;
        this.gui = gui;
        this.cache = gui.getCache();
        this.messages = plugin.getMessagesUtil();

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        UUID uuid = player.getUniqueId();

        // Verify this is a ProShield admin menu
        if (!cache.isAdminMenu(uuid, event.getInventory())) return;

        event.setCancelled(true); // Prevent item movement

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        ItemMeta meta = clicked.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;

        String name = ChatColor.stripColor(meta.getDisplayName()).toLowerCase();

        switch (name) {
            // --- Admin Toggles ---
            case "debug logging" -> {
                boolean state = plugin.toggleDebug();
                if (plugin.isDebugEnabled()) {
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2f);
                } else {
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);
                }
                messages.send(player, "prefix", "&bDebug logging: " + (state ? "&aENABLED" : "&cDISABLED"));
            }
            case "wilderness tools" -> {
                player.performCommand("proshield wilderness");
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            }
            case "admin flag chat" -> {
                boolean current = plugin.getConfig().getBoolean("messages.admin-flag-chat", true);
                plugin.getConfig().set("messages.admin-flag-chat", !current);
                plugin.saveConfig();
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                messages.send(player, "prefix", "&eAdmin flag chat " + (!current ? "&aENABLED" : "&cDISABLED"));
            }

            // --- Admin Tools ---
            case "force unclaim" -> {
                player.closeInventory();
                player.performCommand("proshield forceunclaim");
                player.playSound(player.getLocation(), Sound.ENTITY_WITHER_DEATH, 1f, 0.5f);
            }
            case "transfer claim" -> {
                player.closeInventory();
                player.performCommand("transfer");
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            }
            case "teleport to claim" -> {
                player.closeInventory();
                player.sendMessage(ChatColor.YELLOW + "Use /proshield tp <owner> to teleport to claims.");
                player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1.2f);
            }
            case "purge expired claims" -> {
                player.closeInventory();
                player.performCommand("proshield purge");
                player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1f, 1f);
            }

            case "back" -> {
                gui.openAdminMain(player);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.2f);
            }
            default -> {
                // No action
            }
        }
    }
}
