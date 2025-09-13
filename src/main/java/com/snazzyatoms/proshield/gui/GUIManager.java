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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GUIManager {

    private final ProShield plugin;
    private final PlotManager plotManager;

    // Default descriptions for flags
    private static final Map<String, String> FLAG_DESCRIPTIONS = new HashMap<>();
    static {
        FLAG_DESCRIPTIONS.put("explosions", "&7Toggle TNT and creeper damage inside claim");
        FLAG_DESCRIPTIONS.put("buckets", "&7Allow/disallow bucket use (water & lava)");
        FLAG_DESCRIPTIONS.put("item-frames", "&7Protect item frames from breaking/rotation");
        FLAG_DESCRIPTIONS.put("armor-stands", "&7Prevent others moving/destroying armor stands");
        FLAG_DESCRIPTIONS.put("containers", "&7Control access to chests, hoppers, furnaces, shulkers");
        FLAG_DESCRIPTIONS.put("pets", "&7Prevent damage to tamed pets (wolves, cats, etc.)");
        FLAG_DESCRIPTIONS.put("pvp", "&7Enable or disable PvP combat inside claim");
        FLAG_DESCRIPTIONS.put("safezone", "&7Turns your claim into a safe zone (blocks hostile spawns & damage)");
    }

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
        if (size % 9 != 0) size = 27; // safeguard
        Inventory inv = Bukkit.createInventory(null, size, title);

        ConfigurationSection items = menu.getConfigurationSection("items");
        if (items != null) {
            for (String slotKey : items.getKeys(false)) {
                try {
                    int slot = Integer.parseInt(slotKey);
                    if (slot < 0 || slot >= size) continue;

                    ConfigurationSection itemSec = items.getConfigurationSection(slotKey);
                    if (itemSec == null) continue;

                    // Permission check: hide item if player lacks permission (operators always see it)
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
     * Adds default flag descriptions if lore is empty or missing.
     */
    private List<String> formatLore(List<String> input, Player player, ConfigurationSection itemSec) {
        List<String> out = new ArrayList<>();
        String action = itemSec.getString("action", "");
        String state = "";

        String flagKey = null;
        if (action.toLowerCase().startsWith("command:proshield flag ")) {
            String[] split = action.split(" ");
            if (split.length >= 3) {
                flagKey = split[2].toLowerCase();

                Plot plot = plotManager.getPlot(player.getLocation());
                boolean flagValue;

                if (plot != null) {
                    flagValue = plot.getFlag(flagKey,
                            plugin.getConfig().getBoolean("claims.default-flags." + flagKey, false));
                } else {
                    // No plot: fall back to defaults
                    flagValue = plugin.getConfig().getBoolean("claims.default-flags." + flagKey, false);
                }

                state = flagValue ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled";
            }
        }

        if (input == null || input.isEmpty()) {
            // Inject default description + state if lore not provided
            if (flagKey != null && FLAG_DESCRIPTIONS.containsKey(flagKey)) {
                out.add(ChatColor.translateAlternateColorCodes('&', FLAG_DESCRIPTIONS.get(flagKey)));
                out.add(ChatColor.GRAY + "Current: " + state);
            }
        } else {
            for (String line : input) {
                if (line.contains("{state}")) {
                    out.add(ChatColor.translateAlternateColorCodes('&', line.replace("{state}", state)));
                } else {
                    out.add(ChatColor.translateAlternateColorCodes('&', line));
                }
            }
        }
        return out;
    }
}
