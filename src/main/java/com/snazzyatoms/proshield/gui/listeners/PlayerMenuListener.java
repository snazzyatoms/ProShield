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

public class PlayerMenuListener implements Listener {

    private final ProShield plugin;
    private final GUICache cache;
    private final GUIManager guiManager;
    private final MessagesUtil messages;

    public PlayerMenuListener(ProShield plugin, GUIManager guiManager) {
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

        // Verify this inventory belongs to our player GUI
        if (!cache.isPlayerMenu(uuid, event.getInventory())) return;

        event.setCancelled(true); // Prevent item pickup/movement

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        ItemMeta meta = clicked.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;

        String name = ChatColor.stripColor(meta.getDisplayName()).toLowerCase();

        switch (name) {
            case "claim chunk" -> player.performCommand("claim");
            case "unclaim chunk" -> player.performCommand("unclaim");
            case "claim info" -> player.performCommand("info");
            case "trust menu" -> guiManager.openTrustMenu(player);
            case "flags" -> guiManager.openFlagsMenu(player);
            case "roles" -> guiManager.openRolesGUI(player, plugin.getPlotManager().getPlot(player.getLocation()));
            case "transfer claim" -> guiManager.openTransferMenu(player);
            case "back" -> guiManager.openMain(player); // ✅ Back button
        }
    }
}
