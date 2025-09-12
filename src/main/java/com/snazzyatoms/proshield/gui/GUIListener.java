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

import java.util.List;

public class GUIListener implements Listener {

    private final ProShield plugin;
    private final GUIManager guiManager;

    public GUIListener(ProShield plugin, GUIManager guiManager) {
        this.plugin = plugin;
        this.guiManager = guiManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        Inventory inv = event.getInventory();
        if (inv == null || event.getCurrentItem() == null) return;

        String title = ChatColor.stripColor(event.getView().getTitle());
        if (title == null) return;

        // Check against config menus
        ConfigurationSection menus = plugin.getConfig().getConfigurationSection("gui.menus");
        if (menus == null) return;

        String menuKey = null;
        for (String key : menus.getKeys(false)) {
            String cfgTitle = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&',
                    menus.getConfigurationSection(key).getString("title", "")));
            if (title.equalsIgnoreCase(cfgTitle)) {
                menuKey = key;
                break;
            }
        }
        if (menuKey == null) return; // not a ProShield GUI

        event.setCancelled(true); // always cancel GUI clicks

        int slot = event.getRawSlot();
        ConfigurationSection menu = menus.getConfigurationSection(menuKey);
        if (menu == null) return;

        ConfigurationSection items = menu.getConfigurationSection("items");
        if (items == null) return;

        ConfigurationSection item = items.getConfigurationSection(String.valueOf(slot));
        if (item == null) return;

        String action = item.getString("action", "").toLowerCase();
        if (action.isEmpty()) return;

        if (action.startsWith("command:")) {
            String cmd = action.substring("command:".length()).trim();
            if (!cmd.isEmpty()) {
                Bukkit.dispatchCommand(player, cmd);
            }
        } else if (action.startsWith("menu:")) {
            String targetMenu = action.substring("menu:".length()).trim();
            if (!targetMenu.isEmpty()) {
                guiManager.openMenu(player, targetMenu);
            }
        } else if (action.equalsIgnoreCase("close")) {
            player.closeInventory();
        }
    }
}
