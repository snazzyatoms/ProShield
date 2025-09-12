// src/main/java/com/snazzyatoms/proshield/gui/GUIListener.java
package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

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
        ConfigurationSection menus = plugin.getConfig().getConfigurationSection("ui.menus");
        if (menus == null) return;

        // Match by menu title
        ConfigurationSection matchedMenu = null;
        for (String key : menus.getKeys(false)) {
            String menuTitle = ChatColor.stripColor(
                ChatColor.translateAlternateColorCodes('&', menus.getString(key + ".title", ""))
            );
            if (menuTitle.equalsIgnoreCase(title)) {
                matchedMenu = menus.getConfigurationSection(key);
                break;
            }
        }
        if (matchedMenu == null) return;

        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();

        ConfigurationSection items = matchedMenu.getConfigurationSection("items");
        if (items == null) return;

        // Find matching item by slot
        for (String itemKey : items.getKeys(false)) {
            ConfigurationSection item = items.getConfigurationSection(itemKey);
            if (item == null) continue;

            int slot = item.getInt("slot", -1);
            if (slot == event.getSlot()) {
                String action = item.getString("action", "");
                handleAction(player, action);
                return;
            }
        }
    }

    private void handleAction(Player player, String action) {
        if (action == null || action.isEmpty()) return;

        if (action.startsWith("command:")) {
            String cmd = action.substring("command:".length());
            player.performCommand(cmd.startsWith("/") ? cmd.substring(1) : cmd);
        } else if (action.startsWith("open:")) {
            String menuKey = action.substring("open:".length());
            guiManager.openMenu(player, menuKey);
        } else if (action.equalsIgnoreCase("close")) {
            player.closeInventory();
        } else {
            plugin.getLogger().warning("Unknown GUI action: " + action);
        }
    }
}
