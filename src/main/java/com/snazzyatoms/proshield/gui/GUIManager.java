package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * GUIManager – Handles all ProShield GUI menus.
 * Reads from config.yml → gui.menus
 * Supports dynamic placeholders (e.g. {state})
 */
public class GUIManager {

    private final ProShield plugin;
    private final MessagesUtil messages;
    private final PlotManager plotManager;

    public GUIManager(ProShield plugin) {
        this.plugin = plugin;
        this.messages = plugin.getMessagesUtil();
        this.plotManager = plugin.getPlotManager();
    }

    /**
     * Opens a menu defined in config.yml (gui.menus.<menuKey>)
     */
    public void openMenu(Player player, String menuKey) {
        FileConfiguration cfg = plugin.getConfig();
        ConfigurationSection menuSec = cfg.getConfigurationSection("gui.menus." + menuKey);
        if (menuSec == null) {
            player.sendMessage(ChatColor.RED + "Menu not found: " + menuKey);
            return;
        }

        String title = ChatColor.translateAlternateColorCodes('&',
                menuSec.getString("title", "&7Menu"));
        int size = menuSec.getInt("size", 27);
        Inventory inv = Bukkit.createInventory(null, size, title);

        if (menuSec.isConfigurationSection("items")) {
            for (String slotKey : menuSec.getConfigurationSection("items").getKeys(false)) {
                int slot = Integer.parseInt(slotKey);
                ConfigurationSection itemSec = menuSec.getConfigurationSection("items." + slotKey);

                if (itemSec == null) continue;
                String name = ChatColor.translateAlternateColorCodes('&',
                        itemSec.getString("name", "Unnamed"));
                String materialName = itemSec.getString("material", "STONE");
                Material material = Material.matchMaterial(materialName.toUpperCase(Locale.ROOT));
                if (material == null) material = Material.STONE;

                ItemStack item = new ItemStack(material);
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(name);

                    List<String> lore = new ArrayList<>();
                    if (itemSec.isList("lore")) {
                        for (String line : itemSec.getStringList("lore")) {
                            lore.add(ChatColor.translateAlternateColorCodes('&',
                                    replacePlaceholders(player, line)));
                        }
                    }
                    meta.setLore(lore);
                    meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES); // ✅ hide vanilla attributes
                    item.setItemMeta(meta);
                }

                inv.setItem(slot, item);
            }
        }

        player.openInventory(inv);
    }

    /**
     * Handle inventory clicks
     */
    public void handleClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        event.setCancelled(true);
        String rawName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
        String menuTitle = event.getView().getTitle();

        // Lookup config section for action
        FileConfiguration cfg = plugin.getConfig();
        String menuKey = getMenuKeyByTitle(cfg, menuTitle);
        if (menuKey == null) return;

        ConfigurationSection menuSec = cfg.getConfigurationSection("gui.menus." + menuKey + ".items");
        if (menuSec == null) return;

        for (String slotKey : menuSec.getKeys(false)) {
            String expectedName = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&',
                    menuSec.getString(slotKey + ".name", "")));
            if (!expectedName.equalsIgnoreCase(rawName)) continue;

            String action = menuSec.getString(slotKey + ".action", "");
            handleAction(player, action);
            return;
        }
    }

    /**
     * Handle actions (command:, menu:, toggle:, expansion:)
     */
    private void handleAction(Player player, String action) {
        if (action.startsWith("command:")) {
            String cmd = action.substring("command:".length());
            player.performCommand(cmd);
        } else if (action.startsWith("menu:")) {
            String target = action.substring("menu:".length());
            openMenu(player, target);
        } else if (action.startsWith("toggle:claim.")) {
            String flag = action.substring("toggle:claim.".length());
            Plot plot = plotManager.getPlot(player.getLocation());
            if (plot == null) {
                messages.send(player, "&cYou are not in a claim.");
                return;
            }
            boolean current = plot.getFlag(flag, false);
            plot.setFlag(flag, !current);
            messages.send(player, "&eToggled &f" + flag + " &7→ " + (!current ? "&aEnabled" : "&cDisabled"));
            openMenu(player, "flags"); // refresh
        } else if (action.startsWith("toggle:world.")) {
            String flag = action.substring("toggle:world.".length());
            boolean current = plugin.getConfig().getBoolean("protection.world-controls.defaults." + flag, false);
            plugin.getConfig().set("protection.world-controls.defaults." + flag, !current);
            plugin.saveConfig();
            messages.send(player, "&eWorld flag &f" + flag + " &7→ " + (!current ? "&aEnabled" : "&cDisabled"));
            openMenu(player, "world-controls");
        } else if (action.startsWith("expansion:")) {
            int blocks = Integer.parseInt(action.substring("expansion:".length()));
            requestExpansion(player, blocks);
        } else if (action.equalsIgnoreCase("close")) {
            player.closeInventory();
        }
    }

    /**
     * Replace placeholders in lore lines
     */
    private String replacePlaceholders(Player player, String line) {
        if (!line.contains("{state}")) return line;
        Plot plot = plotManager.getPlot(player.getLocation());
        boolean state = false;

        if (line.toLowerCase().contains("explosions")) {
            state = plot != null && plot.getFlag("explosions", false);
        } else if (line.toLowerCase().contains("fire spread")) {
            state = plot != null && plot.getFlag("fire-spread", false);
        } else if (line.toLowerCase().contains("mob damage")) {
            state = plot != null && plot.getFlag("mob-damage", false);
        } else if (line.toLowerCase().contains("pvp")) {
            state = plot != null && plot.getFlag("pvp", true);
        }

        return line.replace("{state}", state ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled");
    }

    /**
     * Expansion request → notifies admins
     */
    private void requestExpansion(Player player, int blocks) {
        String msg = plugin.getConfig().getString("messages.expansion-request",
                "&eYour expansion request for +{blocks} blocks has been sent to admins.");
        messages.send(player, msg.replace("{blocks}", String.valueOf(blocks)));

        String adminMsg = ChatColor.translateAlternateColorCodes('&',
                "&c[ProShield]&e " + player.getName() + " requested a +" + blocks + " block expansion.");
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.hasPermission("proshield.admin.expansions")) {
                online.sendMessage(adminMsg);
            }
        }
    }

    /**
     * Helper: match menu key from title
     */
    private String getMenuKeyByTitle(FileConfiguration cfg, String title) {
        ConfigurationSection menus = cfg.getConfigurationSection("gui.menus");
        if (menus == null) return null;
        for (String key : menus.getKeys(false)) {
            String cfgTitle = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&',
                    menus.getString(key + ".title", "")));
            if (ChatColor.stripColor(title).equalsIgnoreCase(cfgTitle)) {
                return key;
            }
        }
        return null;
    }
}
