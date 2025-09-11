package com.snazzyatoms.proshield.gui.listeners;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.GUIManager;
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

/**
 * PlayerMenuListener
 *
 * ✅ Handles clicks inside the Player GUI menu
 * ✅ Runs corresponding commands (/claim, /unclaim, /info, /trust, /flags)
 * ✅ Uses GUIManager (internally checks GUICache) to verify menus
 */
public class PlayerMenuListener implements Listener {

    private final ProShield plugin;
    private final GUIManager guiManager;
    private final MessagesUtil messages;

    public PlayerMenuListener(ProShield plugin, GUIManager guiManager) {
        this.plugin = plugin;
        this.guiManager = guiManager;
        this.messages = plugin.getMessagesUtil();

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        UUID uuid = player.getUniqueId();

        // ✅ Verify this inventory belongs to the player menu via GUIManager
        if (!guiManager.getCache().isPlayerMenu(uuid, event.getInventory())) return;

        event.setCancelled(true); // Prevent item pickup/movement

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        ItemMeta meta = clicked.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;

        String name = ChatColor.stripColor(meta.getDisplayName());

        switch (name.toLowerCase()) {
            case "claim chunk" -> player.performCommand("claim");
            case "unclaim chunk" -> player.performCommand("unclaim");
            case "claim info" -> player.performCommand("info");
            case "trust players" -> {
                player.closeInventory();
                player.sendMessage(ChatColor.YELLOW + "Use /trust <player> [role] to add players.");
            }
            case "flags" -> player.performCommand("flags");
            default -> {
                // Do nothing if it’s not one of ours
            }
        }
    }
}
