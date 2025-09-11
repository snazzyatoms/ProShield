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

public class PlayerMenuListener implements Listener {

    private final ProShield plugin;
    private final GUICache cache;
    private final GUIManager guiManager;
    private final MessagesUtil messages;

    public PlayerMenuListener(ProShield plugin, GUIManager guiManager) {
        this.plugin = plugin;
        this.guiManager = guiManager;
        this.cache = guiManager.getCache();
        this.messages = plugin.getMessagesUtil();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        UUID uuid = player.getUniqueId();

        // ✅ Only handle player menus
        if (!cache.isPlayerMenu(uuid, event.getInventory())) return;

        event.setCancelled(true); // Prevent movement of icons

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        ItemMeta meta = clicked.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;

        String name = ChatColor.stripColor(meta.getDisplayName()).toLowerCase();

        switch (name) {
            case "claim chunk" -> {
                player.performCommand("claim");
                playClick(player, true);
            }
            case "unclaim chunk" -> {
                player.performCommand("unclaim");
                playClick(player, true);
            }
            case "claim info" -> {
                player.performCommand("info");
                playClick(player, true);
            }
            case "trust menu" -> {
                guiManager.openTrustMenu(player, false);
                playClick(player, false);
            }
            case "untrust menu" -> {
                guiManager.openUntrustMenu(player, false);
                playClick(player, false);
            }
            case "flags" -> {
                guiManager.openFlagsMenu(player, false);
                playClick(player, false);
            }
            case "roles" -> {
                guiManager.openRolesGUI(player, plugin.getPlotManager().getPlotAt(player.getLocation()), false);
                playClick(player, false);
            }
            case "back" -> {
                guiManager.openMain(player);
                playClick(player, false);
            }
            default -> {}
        }
    }

    private void playClick(Player player, boolean notify) {
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
        if (notify && plugin.getConfig().getBoolean("messages.admin-flag-chat", false) && player.hasPermission("proshield.admin")) {
            messages.send(player, "prefix", "&7Action executed successfully.");
        }
    }
}
