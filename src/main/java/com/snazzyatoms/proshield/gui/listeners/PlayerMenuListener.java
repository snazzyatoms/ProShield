package com.snazzyatoms.proshield.gui.listeners;

import com.snazzyatoms.proshield.ProShield;
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

/**
 * PlayerMenuListener
 *
 * ✅ Handles clicks inside the Player GUI menu
 * ✅ Runs corresponding commands (/claim, /unclaim, /info, /trust, /flags)
 * ✅ Prevents icon movement
 * ✅ Plays sounds for feedback
 */
public class PlayerMenuListener implements Listener {

    private final ProShield plugin;
    private final GUICache cache;
    private final PlotManager plots;
    private final MessagesUtil messages;

    public PlayerMenuListener(ProShield plugin, GUICache cache) {
        this.plugin = plugin;
        this.cache = cache;
        this.plots = plugin.getPlotManager();
        this.messages = plugin.getMessagesUtil();

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        UUID uuid = player.getUniqueId();

        // Verify this inventory belongs to our player GUI
        if (!cache.isPlayerMenu(uuid, event.getInventory())) return;

        event.setCancelled(true); // ✅ Prevent icon movement

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        ItemMeta meta = clicked.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;

        String name = ChatColor.stripColor(meta.getDisplayName()).toLowerCase();

        switch (name) {
            case "claim chunk" -> {
                boolean success = player.performCommand("claim");
                if (success) {
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.2f);
                } else {
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.6f);
                }
            }
            case "unclaim chunk" -> {
                boolean success = player.performCommand("unclaim");
                if (success) {
                    player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1f, 1f);
                } else {
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.6f);
                }
            }
            case "claim info" -> {
                player.performCommand("info");
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.1f);
            }
            case "trust menu" -> {
                player.closeInventory();
                player.performCommand("trust");
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            }
            case "untrust menu" -> {
                player.closeInventory();
                player.performCommand("untrust");
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            }
            case "flags" -> {
                player.performCommand("flags");
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            }
            case "roles" -> {
                player.performCommand("roles");
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            }
            case "back" -> {
                player.closeInventory();
                plugin.getGuiManager().openMain(player);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 0.9f);
            }
            default -> {
                // Do nothing if it’s not one of ours
            }
        }
    }
}
