package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

/**
 * GUIListener
 * - Unified listener for all ProShield GUIs
 * - Actions are defined in config.yml under gui.menus
 * - Supports: command:<text>, menu:<menuKey>, close
 */
public class GUIListener implements Listener {

    private final ProShield plugin;
    private final GUIManager guiManager;

    private static final String ACTION_COMMAND = "command:";
    private static final String ACTION_MENU = "menu:";
    private static final String ACTION_CLOSE = "close";

    public GUIListener(ProShield plugin, GUIManager guiManager) {
        this.plugin = plugin;
        this.guiManager = guiManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getCurrentItem() == null) return;

        String title = ChatColor.stripColor(event.getView().getTitle());
        if (title == null || title.isEmpty()) return;

        ConfigurationSection menus = plugin.getConfig().getConfigurationSection("gui.menus");
        if (menus == null) return;

        // Find which menu is open
        String menuKey = null;
        for (String key : menus.getKeys(false)) {
            String cfgTitle = menus.getConfigurationSection(key).getString("title", "");
            cfgTitle = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', cfgTitle));
            if (title.equalsIgnoreCase(cfgTitle)) {
                menuKey = key;
                break;
            }
        }
        if (menuKey == null) return; // Not a ProShield GUI

        event.setCancelled(true); // Prevent taking items

        ConfigurationSection menu = menus.getConfigurationSection(menuKey);
        if (menu == null) return;

        ConfigurationSection items = menu.getConfigurationSection("items");
        if (items == null) return;

        ConfigurationSection item = items.getConfigurationSection(String.valueOf(event.getRawSlot()));
        if (item == null) return;

        String action = item.getString("action", "").trim();
        if (action.isEmpty()) return;

        handleAction(player, action);
    }

    /**
     * Handle actions defined in config.yml
     */
    private void handleAction(Player player, String action) {
        String lower = action.toLowerCase();

        if (lower.startsWith(ACTION_COMMAND)) {
            String cmd = action.substring(ACTION_COMMAND.length()).trim();
            if (!cmd.isEmpty()) {
                Bukkit.dispatchCommand(player, cmd);
            }
        } else if (lower.startsWith(ACTION_MENU)) {
            String targetMenu = action.substring(ACTION_MENU.length()).trim();
            if (!targetMenu.isEmpty()) {
                guiManager.openMenu(player, targetMenu);
            }
        } else if (lower.equals(ACTION_CLOSE)) {
            player.closeInventory();
        } else {
            if (plugin.isDebugEnabled()) {
                plugin.getLogger().warning("Unknown GUI action: " + action);
            }
        }
    }
}
