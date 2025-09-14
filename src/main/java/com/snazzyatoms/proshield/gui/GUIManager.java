// src/main/java/com/snazzyatoms/proshield/gui/GUIManager.java
package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GUIManager {

    private final ProShield plugin;
    private final MessagesUtil messages;

    public GUIManager(ProShield plugin) {
        this.plugin = plugin;
        this.messages = plugin.getMessagesUtil();
    }

    public void openMenu(Player player, String menuName) {
        FileConfiguration config = plugin.getConfig();
        ConfigurationSection menuSection = config.getConfigurationSection("gui.menus." + menuName);
        if (menuSection == null) {
            player.sendMessage(ChatColor.RED + "Menu not found: " + menuName);
            return;
        }

        String title = ChatColor.translateAlternateColorCodes('&', menuSection.getString("title", "&7Menu"));
        int size = menuSection.getInt("size", 27);
        Inventory inv = Bukkit.createInventory(null, size, title);

        ConfigurationSection items = menuSection.getConfigurationSection("items");
        if (items != null) {
            for (String key : items.getKeys(false)) {
                ConfigurationSection itemSec = items.getConfigurationSection(key);
                if (itemSec == null) continue;

                Material mat = Material.matchMaterial(itemSec.getString("material", "STONE"));
                if (mat == null) mat = Material.STONE;

                ItemStack stack = new ItemStack(mat);
                ItemMeta meta = stack.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', itemSec.getString("name", "&fItem")));

                    List<String> lore = new ArrayList<>();
                    for (String line : itemSec.getStringList("lore")) {
                        // ✅ Inject dynamic claim info if claim info menu
                        if (menuName.equals("main") && "Claim Info".equalsIgnoreCase(ChatColor.stripColor(itemSec.getString("name", "")))) {
                            lore.addAll(buildClaimInfoLore(player));
                        } else {
                            lore.add(ChatColor.translateAlternateColorCodes('&', line));
                        }
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

    private List<String> buildClaimInfoLore(Player player) {
        List<String> lore = new ArrayList<>();
        FileConfiguration config = plugin.getConfig();

        int radius = config.getInt("claims.default-radius", 16);
        lore.add(ChatColor.GRAY + "Shows your current claim details");
        lore.add(ChatColor.YELLOW + "Default radius: " + radius + " blocks");

        // Protected features based on default flags
        Map<String, Object> flags = config.getConfigurationSection("claims.default-flags").getValues(false);
        for (Map.Entry<String, Object> entry : flags.entrySet()) {
            boolean enabled = Boolean.parseBoolean(entry.getValue().toString());
            String flagName = entry.getKey().replace("-", " ");
            lore.add(ChatColor.AQUA + capitalize(flagName) + ": " + (enabled ? ChatColor.GREEN + "Protected" : ChatColor.RED + "Unprotected"));
        }

        return lore;
    }

    private String capitalize(String input) {
        if (input == null || input.isEmpty()) return input;
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }

    public void handleClick(Player player, InventoryClickEvent event, String menuName) {
        event.setCancelled(true);

        FileConfiguration config = plugin.getConfig();
        ConfigurationSection menuSection = config.getConfigurationSection("gui.menus." + menuName);
        if (menuSection == null) return;

        ConfigurationSection items = menuSection.getConfigurationSection("items");
        if (items == null) return;

        for (String key : items.getKeys(false)) {
            ConfigurationSection itemSec = items.getConfigurationSection(key);
            if (itemSec == null) continue;

            int slot = Integer.parseInt(key);
            if (slot != event.getSlot()) continue;

            String action = itemSec.getString("action", "");
            if (action.startsWith("command:")) {
                String cmd = action.substring("command:".length());
                player.closeInventory();
                player.performCommand(cmd);
            } else if (action.startsWith("menu:")) {
                String target = action.substring("menu:".length());
                openMenu(player, target);
            } else if (action.startsWith("flag:")) {
                String flag = action.substring("flag:".length());
                toggleFlag(player, flag);
            }
        }
    }

    private void toggleFlag(Player player, String flag) {
        Plot plot = plugin.getPlotManager().getPlot(player.getLocation());
        if (plot == null) {
            messages.send(player, "&cYou are not inside a claim.");
            return;
        }

        boolean current = plot.getFlag(flag, plugin.getConfig().getBoolean("claims.default-flags." + flag, false));
        plot.setFlag(flag, !current);

        messages.send(player, "&eFlag &6" + flag + "&e is now " + (!current ? "&aEnabled" : "&cDisabled"));
        player.playSound(player.getLocation(),
                plugin.getConfig().getString("sounds.flag-toggle", "BLOCK_NOTE_BLOCK_PLING"),
                1.0f, 1.0f);
    }
}
