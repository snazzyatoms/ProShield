package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.plots.PlotManager.Claim;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class GUIManager {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final GUICache cache;

    public GUIManager(ProShield plugin, PlotManager plotManager, GUICache cache) {
        this.plugin = plugin;
        this.plotManager = plotManager;
        this.cache = cache;
    }

    public ProShield getPlugin() {
        return plugin;
    }

    // === Utility to build menu items ===
    private ItemStack button(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.AQUA + name);
            meta.setLore(java.util.Arrays.asList(lore));
            item.setItemMeta(meta);
        }
        return item;
    }

    // === MAIN MENU ===
    public void openMain(Player player, boolean admin) {
        Inventory inv = Bukkit.createInventory(null, 54, "ProShield Menu");

        inv.setItem(11, button(Material.GRASS_BLOCK, "Claim Chunk", "Protect this chunk"));
        inv.setItem(13, button(Material.PAPER, "Claim Info", "View claim details"));
        inv.setItem(15, button(Material.BARRIER, "Unclaim Chunk", "Remove this claim"));

        inv.setItem(20, button(Material.PLAYER_HEAD, "Trust Player", "Add a trusted player"));
        inv.setItem(21, button(Material.SKELETON_SKULL, "Untrust Player", "Remove a trusted player"));
        inv.setItem(22, button(Material.BOOK, "Manage Roles", "Assign claim roles"));
        inv.setItem(23, button(Material.REDSTONE_TORCH, "Toggle Claim Flags", "PvP, Fire, Explosions..."));
        inv.setItem(24, button(Material.ENDER_PEARL, "Transfer Ownership", "Give claim to another player"));

        inv.setItem(31, button(Material.COMPASS, "Help", "Command reference"));
        if (admin) {
            inv.setItem(33, button(Material.NETHER_STAR, "Admin Tools", "Open admin controls"));
        }

        cache.set(player, inv);
        player.openInventory(inv);
    }

    // === ADMIN MENU ===
    public void openAdmin(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, "ProShield Admin");

        inv.setItem(10, button(Material.FLINT_AND_STEEL, "Fire Toggle"));
        inv.setItem(11, button(Material.TNT, "Explosions Toggle"));
        inv.setItem(12, button(Material.ENDERMAN_SPAWN_EGG, "Entity Grief Toggle"));
        inv.setItem(13, button(Material.LEVER, "Interactions Toggle"));
        inv.setItem(14, button(Material.IRON_SWORD, "PvP Toggle"));
        inv.setItem(20, button(Material.CHEST, "Keep Items Toggle"));
        inv.setItem(21, button(Material.PAPER, "Purge Expired Claims"));
        inv.setItem(22, button(Material.COMMAND_BLOCK, "Reload Config"));
        inv.setItem(23, button(Material.REDSTONE, "Debug Mode"));
        inv.setItem(24, button(Material.COMPASS, "Teleport Tools"));
        inv.setItem(31, button(Material.ARROW, "Back"));

        cache.set(player, inv);
        player.openInventory(inv);
    }

    // === ROLE MENU ===
    public void openRoleMenu(Player player, Claim claim) {
        Inventory inv = Bukkit.createInventory(null, 27, "Manage Roles");

        inv.setItem(10, button(Material.BARRIER, "Visitor"));
        inv.setItem(11, button(Material.WOODEN_SWORD, "Member"));
        inv.setItem(12, button(Material.CHEST, "Container"));
        inv.setItem(13, button(Material.STONE, "Builder"));
        inv.setItem(14, button(Material.NETHER_STAR, "Co-Owner"));
        inv.setItem(22, button(Material.ARROW, "Back"));

        cache.set(player, inv);
        player.openInventory(inv);
    }

    // === FLAG MENU ===
    public void openFlagMenu(Player player, Claim claim) {
        Inventory inv = Bukkit.createInventory(null, 27, "Claim Flags");

        inv.setItem(10, button(Material.IRON_SWORD, "PvP"));
        inv.setItem(11, button(Material.TNT, "Explosions"));
        inv.setItem(12, button(Material.FLINT_AND_STEEL, "Fire"));
        inv.setItem(13, button(Material.ENDER_PEARL, "Enderman Teleport"));
        inv.setItem(14, button(Material.CHEST, "Containers"));
        inv.setItem(22, button(Material.ARROW, "Back"));

        cache.set(player, inv);
        player.openInventory(inv);
    }
}
