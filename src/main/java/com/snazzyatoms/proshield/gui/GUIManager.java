// src/main/java/com/snazzyatoms/proshield/gui/GUIManager.java
package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class GUIManager {

    private final ProShield plugin;

    public GUIManager(ProShield plugin) {
        this.plugin = plugin;
    }

    public void openMenu(Player player, String menuName) {
        ConfigurationSection menuCfg = plugin.getConfig().getConfigurationSection("gui.menus." + menuName);
        if (menuCfg == null) {
            player.sendMessage(ChatColor.RED + "Menu not found: " + menuName);
            return;
        }

        String title = ChatColor.translateAlternateColorCodes('&', menuCfg.getString("title", "&cUnknown Menu"));
        int size = menuCfg.getInt("size", 27);

        Inventory inv = Bukkit.createInventory(null, size, title);

        ConfigurationSection itemsCfg = menuCfg.getConfigurationSection("items");
        if (itemsCfg != null) {
            for (String key : itemsCfg.getKeys(false)) {
                ConfigurationSection itemCfg = itemsCfg.getConfigurationSection(key);
                if (itemCfg == null) continue;

                Material mat = Material.matchMaterial(itemCfg.getString("material", "STONE"));
                if (mat == null) mat = Material.STONE;
                ItemStack stack = new ItemStack(mat);

                ItemMeta meta = stack.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', itemCfg.getString("name", "&cUnnamed")));

                    List<String> lore = new ArrayList<>();
                    if (itemCfg.contains("lore")) {
                        for (String line : itemCfg.getStringList("lore")) {
                            lore.add(ChatColor.translateAlternateColorCodes('&', line));
                        }
                    }

                    // === Dynamic Claim Info ===
                    if (itemCfg.getString("action", "").equalsIgnoreCase("command:proshield info")) {
                        Plot plot = plugin.getPlotManager().getPlot(player.getLocation());
                        if (plot != null) {
                            lore.add(ChatColor.YELLOW + "Claim Size: " + PlotManager.getClaimRadius() + " blocks radius");

                            Map<String, Boolean> flags = plot.getFlags();
                            if (flags.isEmpty()) {
                                lore.add(ChatColor.GRAY + "No custom flags set.");
                            } else {
                                lore.add(ChatColor.GOLD + "Protections:");
                                for (Map.Entry<String, Boolean> entry : flags.entrySet()) {
                                    String state = entry.getValue() ? ChatColor.GREEN + "ON" : ChatColor.RED + "OFF";
                                    lore.add(ChatColor.GRAY + "- " + entry.getKey() + ": " + state);
                                }
                            }
                        } else {
                            lore.add(ChatColor.RED + "You are not standing in a claim.");
                        }
                    }

                    // === Dynamic Flags (Toggle) ===
                    if (itemCfg.getString("action", "").startsWith("command:proshield flag")) {
                        String flagName = itemCfg.getString("action").replace("command:proshield flag ", "").trim();
                        Plot plot = plugin.getPlotManager().getPlot(player.getLocation());

                        boolean state = false;
                        if (plot != null) {
                            state = plot.getFlag(flagName,
                                    plugin.getConfig().getBoolean("claims.default-flags." + flagName, false));
                        }

                        lore.add(ChatColor.AQUA + "Flag: " + flagName);
                        lore.add(ChatColor.YELLOW + "Current State: " +
                                (state ? ChatColor.GREEN + "ENABLED" : ChatColor.RED + "DISABLED"));

                        // Clear attack damage text for swords/axes/etc.
                        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                    }

                    meta.setLore(lore);
                    stack.setItemMeta(meta);
                }

                int slot = Integer.parseInt(key);
                inv.setItem(slot, stack);
            }
        }

        player.openInventory(inv);
    }
}
