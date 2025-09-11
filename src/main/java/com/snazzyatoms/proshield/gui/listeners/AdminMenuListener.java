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

public class AdminMenuListener implements Listener {

    private final ProShield plugin;
    private final GUICache cache;
    private final GUIManager guiManager;
    private final MessagesUtil messages;

    public AdminMenuListener(ProShield plugin, GUIManager guiManager) {
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

        // ✅ Only handle admin menus
        if (!cache.isAdminMenu(uuid, event.getInventory())) return;

        event.setCancelled(true); // Prevent movement of icons

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        ItemMeta meta = clicked.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;

        String name = ChatColor.stripColor(meta.getDisplayName()).toLowerCase();

        switch (name) {
            case "teleport to claim" -> {
                player.sendMessage(ChatColor.YELLOW + "Use /proshield tp <owner> to teleport.");
                playClick(player, true);
            }
            case "force unclaim" -> {
                player.performCommand("proshield forceunclaim");
                playClick(player, true);
            }
            case "manage flags" -> {
                guiManager.openFlagsMenu(player, true);
                playClick(player, false);
            }
            case "purge claims" -> {
                player.performCommand("proshield purge");
                playClick(player, true);
            }
            case "transfer claim" -> {
                guiManager.openTransferMenu(player);
                playClick(player, false);
            }
            case "back" -> {
                guiManager.openAdminMain(player);
                playClick(player, false);
            }
            default -> {}
        }
    }

    private void playClick(Player player, boolean notify) {
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
        if (notify && plugin.getConfig().getBoolean("messages.admin-flag-chat", true)) {
            messages.send(player, "prefix", "&7Admin action executed.");
        }
    }
}
