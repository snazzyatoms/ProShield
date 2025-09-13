// src/main/java/com/snazzyatoms/proshield/gui/GUIListener.java
package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import org.bukkit.Bukkit;
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
        if (event.getCurrentItem() == null) return;

        Inventory inv = event.getInventory();
        if (inv == null) return;

        String rawTitle = event.getView().getTitle();
        if (rawTitle == null) return;
        String title = ChatColor.stripColor(rawTitle);

        event.setCancelled(true); // Always prevent taking items

        // ======================================================
        // 1. Handle Role Editor menu (dynamic)
        // ======================================================
        if (title.startsWith("Role Editor: ")) {
            String targetName = ChatColor.stripColor(rawTitle).replace("Role Editor: ", "").trim();
            Plot plot = plugin.getPlotManager().getPlot(player.getLocation());
            if (plot == null) return;

            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || !clicked.hasItemMeta()) return;

            String name = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
            ClaimRoleManager roleManager = plugin.getRoleManager();

            // Handle Close button
            if ("Close".equalsIgnoreCase(name)) {
                player.closeInventory();
                return;
            }

            // Handle toggles
            String key = name.toLowerCase();
            if (key.equals("build") || key.equals("interact") ||
                key.equals("containers") || key.equals("vehicles") ||
                key.equals("unclaim")) {

                boolean current = roleManager.getPermissions(plot.getId(), targetName)
                        .getOrDefault(key, false);
                roleManager.setPermission(plot.getId(), targetName, key, !current);

                // Refresh menu
                Bukkit.getScheduler().runTask(plugin, () -> guiManager.openRoleEditor(player, targetName));
            }
            return;
        }

        // ======================================================
        // 2. Handle Untrust menu → open Role Editor
        // ======================================================
        if (title.equalsIgnoreCase("Untrust Player")) {
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || !clicked.hasItemMeta()) return;

            String targetName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
            if (targetName == null || targetName.isEmpty()) return;

            Bukkit.getScheduler().runTask(plugin, () -> guiManager.openRoleEditor(player, targetName));
            return;
        }

        // ======================================================
        // 3. Handle config-driven menus (main, trust, flags, etc.)
        // ======================================================
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
        if (menuKey == null) return; // Not a ProShield menu

        int slot = event.getRawSlot();
        ConfigurationSection menu = menus.getConfigurationSection(menuKey);
        if (menu == null) return;

        ConfigurationSection items = menu.getConfigurationSection("items");
        if (items == null) return;

        ConfigurationSection item = items.getConfigurationSection(String.valueOf(slot));
        if (item == null) return;

        String action = item.getString("action", "").trim();
        if (action.isEmpty()) return;

        String lower = action.toLowerCase();

        if (lower.startsWith("command:")) {
            String cmd = action.substring("command:".length()).trim();

            // Special case: reload command
            if (cmd.equalsIgnoreCase("proshield reload")) {
                if (player.isOp() || player.hasPermission("proshield.admin.reload")) {
                    plugin.reloadConfig();
                    String sound = plugin.getConfig().getString("sounds.admin-action", "ENTITY_EXPERIENCE_ORB_PICKUP");
                    try {
                        player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
                    } catch (Exception ignored) {}
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
