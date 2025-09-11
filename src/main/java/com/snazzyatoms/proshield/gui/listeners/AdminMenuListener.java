package com.snazzyatoms.proshield.gui.listeners;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.gui.cache.GUICache;
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

public class AdminMenuListener implements Listener {

    private final ProShield plugin;
    private final GUICache cache;
    private final GUIManager guiManager;
    private final MessagesUtil messages;

    public AdminMenuListener(ProShield plugin, GUIManager guiManager) {
        this.plugin = plugin;
        this.cache = guiManager.getCache();
        this.guiManager = guiManager;
        this.messages = plugin.getMessagesUtil();

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        UUID uuid = player.getUniqueId();

        if (!cache.isAdminMenu(uuid, event.getInventory())) return;

        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        ItemMeta meta = clicked.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;

        String name = ChatColor.stripColor(meta.getDisplayName()).toLowerCase();

        switch (name) {
            case "debug logging" -> {
                boolean state = plugin.toggleDebug();
                player.sendMessage(ChatColor.AQUA + "Debug logging: " + (state ? "ENABLED" : "DISABLED"));
            }
            case "wilderness tools" -> {
                boolean current = plugin.getConfig().getBoolean("messages.show-wilderness", false);
                plugin.getConfig().set("messages.show-wilderness", !current);
                plugin.saveConfig();
                player.sendMessage(ChatColor.GREEN + "Wilderness messages " + (!current ? "ENABLED" : "DISABLED"));
            }
            case "force unclaim" -> player.performCommand("proshield forceunclaim");
            case "transfer claim" -> guiManager.openTransferMenu(player);
            case "teleport to claim" -> player.sendMessage(ChatColor.YELLOW + "Use /proshield tp <owner> to teleport to claims.");
            case "purge expired claims" -> player.performCommand("proshield purge");

            // ✅ Player tools for admins
            case "claim chunk" -> player.performCommand("claim");
            case "unclaim chunk" -> player.performCommand("unclaim");

            case "back" -> guiManager.openAdminMain(player); // ✅ Back button
        }
    }
}
