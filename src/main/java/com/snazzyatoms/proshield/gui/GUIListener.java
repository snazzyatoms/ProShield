// src/main/java/com/snazzyatoms/proshield/gui/GUIListener.java
package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

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

        ItemStack clicked = event.getCurrentItem();

        switch (menuKey.toLowerCase()) {
            case "trust" -> handleTrustClick(player, clicked);
            case "untrust" -> handleUntrustClick(player, clicked);
            case "roles" -> handleRolesClick(player, clicked);
            case "flags" -> handleFlagsClick(player, clicked, event.getRawSlot(), menus.getConfigurationSection("flags"));
            default -> handleStaticClick(player, clicked, menuKey, event.getRawSlot(), menus);
        }
    }

    /* ======================================================
     * TRUST MENU
     * ====================================================== */
    private void handleTrustClick(Player player, ItemStack clicked) {
        if (!(clicked.getItemMeta() instanceof SkullMeta meta)) return;
        OfflinePlayer target = meta.getOwningPlayer();
        if (target == null) return;

        // Operators bypass permissions
        if (!player.hasPermission("proshield.trust") && !player.isOp()) return;

        Bukkit.dispatchCommand(player, "trust " + target.getName());
        player.closeInventory();
    }

    /* ======================================================
     * UNTRUST MENU
     * ====================================================== */
    private void handleUntrustClick(Player player, ItemStack clicked) {
        if (!(clicked.getItemMeta() instanceof SkullMeta meta)) return;
        OfflinePlayer target = meta.getOwningPlayer();
        if (target == null) return;

        if (!player.hasPermission("proshield.untrust") && !player.isOp()) return;

        Bukkit.dispatchCommand(player, "untrust " + target.getName());
        player.closeInventory();
    }

    /* ======================================================
     * ROLES MENU
     * ====================================================== */
    private void handleRolesClick(Player player, ItemStack clicked) {
        if (!(clicked.getItemMeta() instanceof SkullMeta meta)) return;
        OfflinePlayer target = meta.getOwningPlayer();
        if (target == null) return;

        if (!player.hasPermission("proshield.roles") && !player.isOp()) return;

        Bukkit.dispatchCommand(player, "roles " + target.getName());
        player.closeInventory();
    }

    /* ======================================================
     * FLAGS MENU
     * ====================================================== */
    private void handleFlagsClick(Player player, ItemStack clicked, int slot, ConfigurationSection menu) {
        if (menu == null || clicked.getItemMeta() == null) return;

        PlotManager plotManager = plugin.getPlotManager();
        Plot plot = plotManager.getPlotAt(player.getLocation());
        if (plot == null) {
            player.sendMessage(ChatColor.RED + "You are not standing in a claim.");
            return;
        }

        String displayName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
        String flag = getFlagFromName(displayName);
        if (flag == null) return;

        if (!player.hasPermission("proshield.flags") && !player.isOp()) return;

        boolean newValue = !plotManager.getFlag(plot, flag);
        plotManager.setFlag(plot, flag, newValue);

        player.sendMessage(ChatColor.YELLOW + "Flag " + flag + " set to " + newValue);
        Bukkit.dispatchCommand(player, "proshield flag " + flag); // trigger proper handlers
        player.closeInventory();
    }

    private String getFlagFromName(String displayName) {
        if (displayName == null) return null;
        return displayName.toLowerCase().replace("§a", "").replace("§c", "").trim()
                .replace(" ", "-");
    }

    /* ======================================================
     * STATIC MENUS
     * ====================================================== */
    private void handleStaticClick(Player player, ItemStack clicked, String menuKey, int slot, ConfigurationSection menus) {
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
        }
    }
}
