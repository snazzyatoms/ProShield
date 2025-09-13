// src/main/java/com/snazzyatoms/proshield/gui/GUIListener.java
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
        if (event.getCurrentItem() == null) return;

        Inventory inv = event.getInventory();
        if (inv == null) return;

        String title = ChatColor.stripColor(event.getView().getTitle());
        if (title == null) return;

        ConfigurationSection menus = plugin.getConfig().getConfigurationSection("gui.menus");
        if (menus == null) return;

        // Find which menu matches this inventory by title
        String menuKey = null;
        for (String key : menus.getKeys(false)) {
            String cfgTitle = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&',
                    menus.getConfigurationSection(key).getString("title", "")));
            if (title.equalsIgnoreCase(cfgTitle)) {
                menuKey = key;
                break;
            }
        }
        if (menuKey == null) return; // Not a ProShield menu

        event.setCancelled(true); // Prevent item movement

        int slot = event.getRawSlot();
        ConfigurationSection menu = menus.getConfigurationSection(menuKey);
        if (menu == null) return;

        ConfigurationSection items = menu.getConfigurationSection("items");
        if (items == null) return;

        ConfigurationSection item = items.getConfigurationSection(String.valueOf(slot));
        if (item == null) return;

        String action = item.getString("action", "").trim();
        if (action.isEmpty()) return;

        // Normalize lowercase only for comparisons
        String lower = action.toLowerCase();

        if (lower.startsWith("command:")) {
            String cmd = action.substring("command:".length()).trim();

            // Special case: reload command
            if (cmd.equalsIgnoreCase("proshield reload")) {
                if (player.isOp() || player.hasPermission("proshield.admin.reload")) {
                    plugin.reloadConfig();
                    // Play feedback sound only (no chat spam)
                    String sound = plugin.getConfig().getString("sounds.admin-action", "ENTITY_EXPERIENCE_ORB_PICKUP");
                    try {
                        player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
                    } catch (Exception ignored) {
                        // ignore invalid sound keys
                    }
                }
                return;
            }

            if (!cmd.isEmpty()) {
                Bukkit.dispatchCommand(player, cmd);
            }
        } else if (lower.startsWith("menu:")) {
            String targetMenu = action.substring("menu:".length()).trim();
            if (!targetMenu.isEmpty()) {
                guiManager.openMenu(player, targetMenu);
            }
        } else if (lower.equals("close")) {
            player.closeInventory();
        } else {
            plugin.getLogger().warning("[GUI] Unknown action in menu '" + menuKey + "' slot " + slot + ": " + action);
        }
    }
}
