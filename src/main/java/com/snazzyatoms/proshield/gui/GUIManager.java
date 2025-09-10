package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Map;

/**
 * Handles creation and management of ProShield GUIs.
 * Supports both Player GUI and Admin GUI.
 * Uses GUICache for performance (avoids rebuilding items every time).
 */
public class GUIManager {

    private final ProShield plugin;
    private final GUICache cache;

    public GUIManager(ProShield plugin) {
        this.plugin = plugin;
        this.cache = new GUICache(plugin);
    }

    // =========================================================
    // COMPASS CREATION
    // =========================================================

    public ItemStack createCompass(boolean admin) {
        return admin ? cache.getAdminCompass() : cache.getPlayerCompass();
    }

    public boolean isProShieldCompass(ItemStack item) {
        return cache.isCompass(item);
    }

    // =========================================================
    // PLAYER MAIN MENU
    // =========================================================

    public void openMain(Player player, boolean admin) {
        Inventory inv = Bukkit.createInventory(player, 54, ChatColor.DARK_AQUA + "ProShield Menu");

        // Load from cache
        Map<Integer, ItemStack> layout = admin ? cache.getAdminMainLayout() : cache.getPlayerMainLayout();

        for (Map.Entry<Integer, ItemStack> entry : layout.entrySet()) {
            inv.setItem(entry.getKey(), entry.getValue());
        }

        player.openInventory(inv);
    }

    // =========================================================
    // ADMIN MENU
    // =========================================================

    public void openAdmin(Player player) {
        Inventory inv = Bukkit.createInventory(player, 54, ChatColor.RED + "ProShield Admin");

        Map<Integer, ItemStack> layout = cache.getAdminSettingsLayout();
        for (Map.Entry<Integer, ItemStack> entry : layout.entrySet()) {
            inv.setItem(entry.getKey(), entry.getValue());
        }

        player.openInventory(inv);
    }

    // =========================================================
    // HELP MENUS
    // =========================================================

    public void openHelp(Player player, boolean admin) {
        Inventory inv = Bukkit.createInventory(player, 54, ChatColor.GOLD + "ProShield Help");

        Map<Integer, ItemStack> layout = admin ? cache.getAdminHelpLayout() : cache.getPlayerHelpLayout();
        for (Map.Entry<Integer, ItemStack> entry : layout.entrySet()) {
            inv.setItem(entry.getKey(), entry.getValue());
        }

        player.openInventory(inv);
    }

    // =========================================================
    // BUTTON HANDLING
    // =========================================================

    public void handleButtonClick(Player player, String action, boolean admin) {
        switch (action.toLowerCase()) {
            case "claim":
                player.performCommand("proshield claim");
                break;
            case "unclaim":
                player.performCommand("proshield unclaim");
                break;
            case "info":
                player.performCommand("proshield info");
                break;
            case "trust":
                player.performCommand("proshield trustmenu");
                break;
            case "untrust":
                player.performCommand("proshield untrustmenu");
                break;
            case "roles":
                player.performCommand("proshield roles");
                break;
            case "transfer":
                player.performCommand("proshield transfer");
                break;
            case "flags":
                player.performCommand("proshield flags");
                break;
            case "admin":
                if (player.hasPermission("proshield.admin")) {
                    openAdmin(player);
                } else {
                    player.sendMessage(ChatColor.RED + "You don’t have permission for admin tools.");
                }
                break;
            case "reload":
                if (player.hasPermission("proshield.admin.reload")) {
                    plugin.reloadConfig();
                    player.sendMessage(ChatColor.GREEN + "✅ ProShield configuration reloaded.");
                } else {
                    player.sendMessage(ChatColor.RED + "You don’t have permission to reload.");
                }
                break;
            case "help":
                openHelp(player, admin);
                break;
            case "back":
                openMain(player, admin); // Fixed back navigation
                break;
            default:
                player.sendMessage(ChatColor.RED + "Unknown action: " + action);
                break;
        }
    }

    // =========================================================
    // RELOAD HOOK
    // =========================================================

    public void onConfigReload() {
        cache.refresh();
    }
}
