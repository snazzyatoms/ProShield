// src/main/java/com/snazzyatoms/proshield/gui/listeners/PlayerMenuListener.java
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
 * PlayerMenuListener
 *
 * ✅ Handles clicks inside the Player GUI menu
 * ✅ Runs corresponding commands (/claim, /unclaim, /info, /trust, /flags, /roles)
 * ✅ Supports back button → returns to main menu
 * ✅ Plays sounds for feedback
 */
public class PlayerMenuListener implements Listener {

    private final ProShield plugin;
    private final GUIManager gui;
    private final GUICache cache;
    private final MessagesUtil messages;

    public PlayerMenuListener(ProShield plugin, GUIManager gui) {
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

        // Verify this is a ProShield player menu
        if (!cache.isPlayerMenu(uuid, event.getInventory())) return;

        event.setCancelled(true); // Prevent moving/removing items

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        ItemMeta meta = clicked.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;

        String name = ChatColor.stripColor(meta.getDisplayName()).toLowerCase();

        switch (name) {
            case "claim chunk" -> {
                player.performCommand("claim");
                player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
            }
            case "unclaim chunk" -> {
                player.performCommand("unclaim");
                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_BREAK, 1f, 1f);
            }
            case "claim info" -> {
                player.performCommand("info");
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            }
            case "trust menu" -> {
                gui.openTrustMenu(player, false);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            }
            case "untrust menu" -> {
                gui.openUntrustMenu(player, false);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            }
            case "flags" -> {
                gui.openFlagsMenu(player, false);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            }
            case "roles" -> {
                gui.openRolesGUI(player, plugin.getPlotManager().getPlot(player.getLocation()), false);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            }
            case "transfer claim" -> {
                player.performCommand("transfer");
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            }
            case "back" -> {
                gui.openMain(player);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.2f);
            }
            default -> {
                // Do nothing
            }
        }
    }
}
