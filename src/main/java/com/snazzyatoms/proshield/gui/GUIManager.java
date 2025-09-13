// src/main/java/com/snazzyatoms/proshield/gui/GUIManager.java
package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class GUIManager {

    private final ProShield plugin;
    private final PlotManager plotManager;

    public GUIManager(ProShield plugin) {
        this.plugin = plugin;
        this.plotManager = plugin.getPlotManager();
    }

    /**
     * Opens a GUI menu defined in config.yml under gui.menus.<menuKey>
     */
    public void openMenu(Player player, String menuKey) {
        ConfigurationSection menus = plugin.getConfig().getConfigurationSection("gui.menus");
        if (menus == null) {
            plugin.getLogger().warning("No menus defined in config.yml (gui.menus missing).");
            return;
        }

        ConfigurationSection menu = menus.getConfigurationSection(menuKey);
        if (menu == null) {
            plugin.getLogger().warning("Menu not found in config.yml: " + menuKey);
            return;
        }

        String title = ChatColor.translateAlternateColorCodes('&', menu.getString("title", "&7Menu"));

        int size = menu.getInt("size", 27);
        if (size % 9 != 0) size = 27;
        Inventory inv = Bukkit.createInventory(null, size, title);

        ConfigurationSection items = menu.getConfigurationSection("items");
        if (items != null) {
            for (String slotKey : items.getKeys(false)) {
                try {
                    int slot = Integer.parseInt(slotKey);
                    if (slot < 0 || slot >= size) continue;

                    ConfigurationSection itemSec = items.getConfigurationSection(slotKey);
                    if (itemSec == null) continue;

                    // Permission check: hide item if player lacks permission (unless op)
                    String perm = itemSec.getString("permission");
                    if (perm != null && !perm.isBlank() &&
                            !player.hasPermission(perm) && !player.isOp()) {
                        continue;
                    }

                    Material mat = Material.matchMaterial(itemSec.getString("material", "BARRIER"));
                    if (mat == null) mat = Material.BARRIER;

                    String name = ChatColor.translateAlternateColorCodes('&', itemSec.getString("name", ""));
                    List<String> lore = formatLore(itemSec.getStringList("lore"), player, itemSec);

                    ItemStack item = new ItemStack(mat);
                    ItemMeta meta = item.getItemMeta();
                    if (meta != null) {
                        meta.setDisplayName(name);
                        meta.setLore(lore);
                        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                        item.setItemMeta(meta);
                    }

                    inv.setItem(slot, item);
                } catch (NumberFormatException ignored) {
                }
            }
        }

        player.openInventory(inv);
    }

    /**
     * Format lore lines with color codes and replace placeholders like {state}.
     */
    private List<String> formatLore(List<String> input, Player player, ConfigurationSection itemSec) {
        List<String> out = new ArrayList<>();
        String action = itemSec.getString("action", "");
        String state = "";

        // Detect if it's a flag item
        if (action.startsWith("command:proshield flag")) {
            String[] split = action.split(" ");
            if (split.length >= 3) {
                String flagKey = split[2].toLowerCase();

                Plot plot = plotManager.getPlot(player.getLocation());
                boolean flagValue = (plot != null) && plot.getFlag(flagKey, plugin.getConfig()
                        .getBoolean("claims.default-flags." + flagKey, false));

                state = flagValue ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled";
            }
        }

        for (String line : input) {
            if (line.contains("{state}")) {
                out.add(ChatColor.translateAlternateColorCodes('&', line.replace("{state}", state)));
            } else {
                out.add(ChatColor.translateAlternateColorCodes('&', line));
            }
        }
        return out;
    }
}
