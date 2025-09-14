// src/main/java/com/snazzyatoms/proshield/gui/GUIManager.java
package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class GUIManager {
    private final ProShield plugin;
    private final PlotManager plotManager;

    public GUIManager(ProShield plugin) {
        this.plugin = plugin;
        this.plotManager = plugin.getPlotManager();
    }

    public void openMenu(Player player, String menu) {
        switch (menu.toLowerCase()) {
            case "main" -> player.openInventory(buildMainMenu());
            case "flags" -> player.openInventory(buildFlagsMenu(player));
            case "roles" -> player.openInventory(buildRolesMenu(player));
            case "untrust" -> player.openInventory(buildUntrustMenu(player));
            default -> player.sendMessage(ChatColor.RED + "Unknown menu: " + menu);
        }
    }

    /* ---------------------- GUI Builders ---------------------- */

    private Inventory buildMainMenu() {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.AQUA + "ProShield Menu");

        inv.setItem(11, createItem(Material.GRASS_BLOCK, ChatColor.GREEN + "Claim Land", "Claim this area", "command:claim"));
        inv.setItem(12, createItem(Material.BOOK, ChatColor.YELLOW + "Claim Info", "View details about this claim", "command:proshield info"));
        inv.setItem(13, createItem(Material.BARRIER, ChatColor.RED + "Unclaim", "Remove your claim", "command:unclaim"));
        inv.setItem(14, createItem(Material.OAK_SIGN, ChatColor.BLUE + "Manage Flags", "Toggle claim settings", "menu:flags"));
        inv.setItem(15, createItem(Material.PLAYER_HEAD, ChatColor.LIGHT_PURPLE + "Manage Roles", "Manage trusted players", "menu:roles"));

        return inv;
    }

    private Inventory buildFlagsMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.LIGHT_PURPLE + "Claim Flags");

        Plot plot = plotManager.getPlot(player.getLocation());
        if (plot == null) {
            inv.setItem(13, createItem(Material.BARRIER, ChatColor.RED + "No Claim", "You are not standing in a claim", "static:none"));
            return inv;
        }

        boolean pvp = plot.getFlag("pvp", false);
        inv.setItem(11, createItem(Material.IRON_SWORD, ChatColor.RED + "PvP",
                "Enable/disable PvP in this claim", "command:proshield flag pvp",
                "&7Current: " + (pvp ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled")));

        boolean mobSpawns = plot.getFlag("mobspawns", true);
        inv.setItem(12, createItem(Material.ZOMBIE_HEAD, ChatColor.DARK_GREEN + "Mob Spawns",
                "Toggle hostile mob spawning", "command:proshield flag mobspawns",
                "&7Current: " + (mobSpawns ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled")));

        inv.setItem(26, createItem(Material.ARROW, ChatColor.GRAY + "Back", "Return to main menu", "menu:main"));
        return inv;
    }

    private Inventory buildRolesMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.AQUA + "Trusted Roles");

        Plot plot = plotManager.getPlot(player.getLocation());
        if (plot == null) {
            inv.setItem(13, createItem(Material.BARRIER, ChatColor.RED + "No Claim", "You are not standing in a claim", "static:none"));
            return inv;
        }

        Set<UUID> trusted = plot.getTrusted();
        if (trusted.isEmpty()) {
            inv.setItem(13, createItem(Material.BARRIER, ChatColor.GRAY + "No trusted players", "No players have been trusted yet", "static:none"));
        } else {
            int slot = 10;
            for (UUID uuid : trusted) {
                String name = Bukkit.getOfflinePlayer(uuid).getName();
                inv.setItem(slot++, createItem(Material.PLAYER_HEAD, ChatColor.YELLOW + name, "Trusted player", "command:untrust " + name));
            }
        }

        inv.setItem(26, createItem(Material.ARROW, ChatColor.GRAY + "Back", "Return to main menu", "menu:main"));
        return inv;
    }

    private Inventory buildUntrustMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.RED + "Untrust Players");

        Plot plot = plotManager.getPlot(player.getLocation());
        if (plot == null) {
            inv.setItem(13, createItem(Material.BARRIER, ChatColor.RED + "No Claim", "You are not standing in a claim", "static:none"));
            return inv;
        }

        Set<UUID> trusted = plot.getTrusted();
        if (trusted.isEmpty()) {
            inv.setItem(13, createItem(Material.BARRIER, ChatColor.GRAY + "No trusted players", "No players to untrust", "static:none"));
        } else {
            int slot = 10;
            for (UUID uuid : trusted) {
                String name = Bukkit.getOfflinePlayer(uuid).getName();
                inv.setItem(slot++, createItem(Material.PLAYER_HEAD, ChatColor.YELLOW + name, "Click to untrust", "command:untrust " + name));
            }
        }

        inv.setItem(26, createItem(Material.ARROW, ChatColor.GRAY + "Back", "Return to roles menu", "menu:roles"));
        return inv;
    }

    /* ---------------------- Utility ---------------------- */

    private ItemStack createItem(Material mat, String name, String desc, String action, String... extraLore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);

            List<String> lore = new ArrayList<>();
            if (desc != null && !desc.isEmpty()) {
                lore.add(ChatColor.GRAY + desc);
            }
            if (extraLore != null) {
                for (String s : extraLore) {
                    lore.add(ChatColor.translateAlternateColorCodes('&', s));
                }
            }
            meta.setLore(lore);

            // ✅ hide vanilla attributes so only lore shows
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE);

            item.setItemMeta(meta);
        }
        return item;
    }
}
