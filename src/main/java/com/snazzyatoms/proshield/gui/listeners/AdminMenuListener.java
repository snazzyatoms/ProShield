// src/main/java/com/snazzyatoms/proshield/gui/listeners/AdminMenuListener.java
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

public class AdminMenuListener implements Listener {

    private final ProShield plugin;
    private final GUIManager gui;
    private final GUICache cache;
    private final PlotManager plots;
    private final MessagesUtil messages;

    public AdminMenuListener(ProShield plugin, GUIManager gui) {
        this.plugin = plugin;
        this.gui = gui;
        this.cache = gui.getCache();
        this.plots = plugin.getPlotManager();
        this.messages = plugin.getMessagesUtil();
    }

    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!player.hasPermission("proshield.admin")) return;

        UUID uuid = player.getUniqueId();
        if (!cache.isAdminMenu(uuid, event.getInventory())) return;

        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        ItemMeta meta = clicked.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;

        String name = ChatColor.stripColor(meta.getDisplayName()).toLowerCase();

        switch (name) {
            // Admin also has player actions
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

            // Admin-only tools (dispatch or toggle config)
            case "teleport to claim" -> {
                player.closeInventory();
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.0f);
                player.sendMessage(ChatColor.YELLOW + "Use /proshield tp <owner> to teleport to claims.");
            }
            case "force unclaim" -> {
                player.closeInventory();
                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.7f, 1.0f);
                player.performCommand("proshield forceunclaim");
            }
            case "toggle keep-items" -> {
                boolean current = plugin.getConfig().getBoolean("claims.keep-items.enabled", false);
                plugin.getConfig().set("claims.keep-items.enabled", !current);
                plugin.saveConfig();
                player.playSound(player.getLocation(), Sound.BLOCK_LEVER_CLICK, 0.7f, 1.1f);
                messages.send(player, "prefix", "&eKeep-drops: " + (!current ? "&aENABLED" : "&cDISABLED"));
            }
            case "wilderness tools" -> {
                boolean current = plugin.getConfig().getBoolean("messages.show-wilderness", false);
                plugin.getConfig().set("messages.show-wilderness", !current);
                plugin.saveConfig();
                player.playSound(player.getLocation(), Sound.BLOCK_LEVER_CLICK, 0.7f, 1.1f);
                messages.send(player, "prefix", "&eWilderness messages: " + (!current ? "&aENABLED" : "&cDISABLED"));
            }
            case "debug logging" -> {
                boolean state = plugin.toggleDebug();
                player.playSound(player.getLocation(), Sound.BLOCK_LEVER_CLICK, 0.7f, 1.0f);
                messages.send(player, "prefix", "&eDebug logging: " + (state ? "&aENABLED" : "&cDISABLED"));
            }
            case "purge expired claims" -> {
                player.closeInventory();
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.0f);
                player.performCommand("proshield purge");
            }

            case "back" -> {
                player.playSound(player.getLocation(), Sound.BLOCK_CHEST_CLOSE, 0.7f, 1.0f);
                gui.openAdminMain(player);
            }

            default -> {
                // no-op
            }
        }
    }
}
