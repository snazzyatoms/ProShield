// src/main/java/com/snazzyatoms/proshield/gui/listeners/PlayerMenuListener.java
package com.snazzyatoms.proshield.gui.listeners;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.gui.cache.GUICache;
import com.snazzyatoms.proshield.plots.PlotManager;
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

public class PlayerMenuListener implements Listener {

    private final ProShield plugin;
    private final GUIManager gui;
    private final GUICache cache;
    private final PlotManager plots;
    private final MessagesUtil messages;

    public PlayerMenuListener(ProShield plugin, GUIManager gui) {
        this.plugin = plugin;
        this.gui = gui;
        this.cache = gui.getCache();
        this.plots = plugin.getPlotManager();
        this.messages = plugin.getMessagesUtil();
    }

    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        // Only handle our tracked player menus
        UUID uuid = player.getUniqueId();
        if (!cache.isPlayerMenu(uuid, event.getInventory())) return;

        // Make items static (no moving)
        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        ItemMeta meta = clicked.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;

        String name = ChatColor.stripColor(meta.getDisplayName()).toLowerCase();

        switch (name) {
            case "claim chunk" -> {
                player.closeInventory();
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.7f, 1.2f);
                player.performCommand("claim");
            }
            case "unclaim chunk" -> {
                player.closeInventory();
                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.7f, 1.0f);
                player.performCommand("unclaim");
            }
            case "trust menu" -> {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.0f);
                gui.openTrustMenu(player);
            }
            case "flags" -> {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.0f);
                gui.openFlagsMenu(player);
            }
            case "roles" -> {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.0f);
                gui.openRolesGUI(player, plots.getPlot(player.getLocation()));
            }
            case "transfer claim" -> {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.0f);
                gui.openTransferMenu(player);
            }
            case "untrust menu" -> {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.0f);
                gui.openUntrustMenu(player);
            }
            case "coming in proshield 2.0" -> {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.7f, 1.3f);
            }
            case "back" -> {
                // Back returns to main player menu (simple & predictable)
                player.playSound(player.getLocation(), Sound.BLOCK_CHEST_CLOSE, 0.7f, 1.0f);
                gui.openMain(player);
            }
            // Submenu items with info-only (actual flag/role logic handled elsewhere)
            case "pvp", "explosions", "fire", "redstone", "containers",
                 "builder", "moderator", "manager", "trusted list" -> {
                // Feedback sound so the click feels responsive
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.7f, 1.1f);
                // The actual toggling/assignment should be wired in your existing listeners/commands.
                // Keeping chat silent for players per your request.
            }
            default -> {
                // Do nothing
            }
        }
    }
}
