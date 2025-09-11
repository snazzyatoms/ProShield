// src/main/java/com/snazzyatoms/proshield/gui/listeners/PlayerMenuListener.java
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

public class PlayerMenuListener implements Listener {

    private final ProShield plugin;
    private final GUIManager gui;
    private final GUICache cache;

    public PlayerMenuListener(ProShield plugin, GUIManager guiManager) {
        this.plugin = plugin;
        this.gui = guiManager;
        this.cache = guiManager.getCache();
    }

    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        UUID uuid = player.getUniqueId();

        // Only handle our player menu
        if (!cache.isPlayerMenu(uuid, event.getInventory())) return;

        event.setCancelled(true); // ✅ make items static/unmovable

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        ItemMeta meta = clicked.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;

        String name = ChatColor.stripColor(meta.getDisplayName()).toLowerCase();

        switch (name) {
            case "claim chunk" -> player.performCommand("claim");
            case "unclaim chunk" -> player.performCommand("unclaim");
            case "claim info" -> player.performCommand("info");
            case "trust menu" -> gui.openTrustMenu(player);
            case "untrust menu" -> gui.openUntrustMenu(player);
            case "flags" -> gui.openFlagsMenu(player);
            case "roles" -> player.performCommand("roles"); // lets command do in-claim check
            case "transfer claim" -> gui.openTransferMenu(player);
            case "admin tools" -> {
                if (player.isOp() || player.hasPermission("proshield.admin")) {
                    gui.openAdminMain(player);
                } else {
                    plugin.getMessagesUtil().send(player, "error.no-permission");
                }
            }
            default -> { /* ignore filler clicks */ }
        }
    }
}
