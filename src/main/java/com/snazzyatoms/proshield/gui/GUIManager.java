package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.PlotManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class GUIManager {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final GUICache cache;

    public GUIManager(ProShield plugin, PlotManager plotManager, GUICache cache) {
        this.plugin = plugin;
        this.plotManager = plotManager;
        this.cache = cache;

        // Preload GUIs into cache
        reloadGUIs();
    }

    // === UTILITY: Item creation ===
    private ItemStack makeItem(Material mat, String name, List<String> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (lore != null) {
                meta.setLore(lore);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    // === BUILD MAIN MENU ===
    private Inventory buildMainGUI() {
        Inventory inv = Bukkit.createInventory(null, 54, "§3ProShield Menu");

        inv.setItem(11, makeItem(Material.GRASS_BLOCK, "§aClaim Land", List.of("§7Claim your current chunk")));
        inv.setItem(13, makeItem(Material.PAPER, "§eClaim Info", List.of("§7View details about this claim")));
        inv.setItem(15, makeItem(Material.BARRIER, "§cUnclaim Land", List.of("§7Remove your current claim")));

        inv.setItem(31, makeItem(Material.BOOK, "§bHelp", List.of("§7Show available commands")));
        inv.setItem(33, makeItem(Material.COMPASS, "§6Admin Menu", List.of("§7Requires permission: proshield.admin")));

        return inv;
    }

    // === BUILD PLAYER MENU (NEW IN 1.2.5) ===
    private Inventory buildPlayerGUI() {
        Inventory inv = Bukkit.createInventory(null, 54, "§aPlayer Tools");

        inv.setItem(10, makeItem(Material.PLAYER_HEAD, "§aTrust Player", List.of("§7Trust a player into your claim")));
        inv.setItem(12, makeItem(Material.REDSTONE, "§cUntrust Player", List.of("§7Remove trust from a player")));
        inv.setItem(14, makeItem(Material.IRON_SWORD, "§eManage Roles", List.of("§7Assign claim roles to players")));
        inv.setItem(16, makeItem(Material.PAINTING, "§dClaim Flags", List.of("§7Toggle claim settings")));

        inv.setItem(20, makeItem(Material.ENDER_EYE, "§bTransfer Ownership", List.of("§7Transfer claim to another player")));
        inv.setItem(22, makeItem(Material.MAP, "§6Preview Claim", List.of("§7Preview your claim boundaries")));
        inv.setItem(24, makeItem(Material.CHEST, "§eKeep Items Toggle", List.of("§7Toggle keep-drops inside claims")));

        inv.setItem(48, makeItem(Material.ARROW, "§fBack", List.of("§7Return to Main Menu")));

        return inv;
    }

    // === BUILD ADMIN MENU ===
    private Inventory buildAdminGUI() {
        Inventory inv = Bukkit.createInventory(null, 54, "§cAdmin Tools");

        inv.setItem(10, makeItem(Material.FLINT_AND_STEEL, "§cFire Toggle", List.of("§7Toggle fire spread/ignite")));
        inv.setItem(11, makeItem(Material.TNT, "§cExplosion Toggle", List.of("§7Toggle TNT, creepers, withers")));
        inv.setItem(12, makeItem(Material.ENDER_PEARL, "§cEntity Grief Toggle", List.of("§7Prevent mob griefing")));
        inv.setItem(13, makeItem(Material.REDSTONE_TORCH, "§cInteraction Toggle", List.of("§7Block/allow container use")));
        inv.setItem(14, makeItem(Material.DIAMOND_SWORD, "§cPvP Toggle", List.of("§7Enable/disable PvP")));

        inv.setItem(20, makeItem(Material.CHEST, "§eKeep Items Toggle", List.of("§7Admin toggle keep-drops")));
        inv.setItem(21, makeItem(Material.BARRIER, "§cPurge Expired", List.of("§7Force expiry cleanup")));
        inv.setItem(22, makeItem(Material.BOOK, "§bHelp", List.of("§7Admin help menu")));
        inv.setItem(23, makeItem(Material.REDSTONE, "§cDebug Mode", List.of("§7Toggle debug logs")));
        inv.setItem(24, makeItem(Material.COMPASS, "§6Compass Drop", List.of("§7Toggle compass auto-drop")));
        inv.setItem(25, makeItem(Material.REPEATER, "§dReload Configs", List.of("§7Reload ProShield configs live")));

        inv.setItem(30, makeItem(Material.ENDER_EYE, "§aTeleport Tools", List.of("§7Teleport to claims")));
        inv.setItem(31, makeItem(Material.ARROW, "§fBack", List.of("§7Return to Main Menu")));

        return inv;
    }

    // === PUBLIC OPEN METHODS ===
    public void openMain(Player player) {
        cache.open(player, GUICache.GUIType.MAIN);
    }

    public void openPlayer(Player player) {
        cache.open(player, GUICache.GUIType.PLAYER);
    }

    public void openAdmin(Player player) {
        if (player.hasPermission("proshield.admin")) {
            cache.open(player, GUICache.GUIType.ADMIN);
        } else {
            player.sendMessage("§cYou do not have permission to access Admin Tools.");
        }
    }

    // === HANDLE BUTTON CLICKS (centralized) ===
    public void handleButtonClick(Player player, String title, int slot) {
        if (title.contains("ProShield Menu")) {
            switch (slot) {
                case 11 -> player.performCommand("proshield claim");
                case 13 -> player.performCommand("proshield info");
                case 15 -> player.performCommand("proshield unclaim");
                case 31 -> player.performCommand("proshield help");
                case 33 -> openAdmin(player);
            }
        } else if (title.contains("Player Tools")) {
            switch (slot) {
                case 10 -> player.performCommand("proshield trustmenu");
                case 12 -> player.performCommand("proshield untrustmenu");
                case 14 -> player.performCommand("proshield roles");
                case 16 -> player.performCommand("proshield flags");
                case 20 -> player.performCommand("proshield transfer");
                case 22 -> player.performCommand("proshield preview");
                case 24 -> player.performCommand("proshield keepitems toggle");
                case 48 -> openMain(player);
            }
        } else if (title.contains("Admin Tools")) {
            switch (slot) {
                case 10 -> player.performCommand("proshield admin fire");
                case 11 -> player.performCommand("proshield admin explosions");
                case 12 -> player.performCommand("proshield admin entitygrief");
                case 13 -> player.performCommand("proshield admin interactions");
                case 14 -> player.performCommand("proshield admin pvp");
                case 20 -> player.performCommand("proshield admin keepitems");
                case 21 -> player.performCommand("proshield purgeexpired");
                case 22 -> player.performCommand("proshield help");
                case 23 -> player.performCommand("proshield debug toggle");
                case 24 -> player.performCommand("proshield admin compassdrop");
                case 25 -> player.performCommand("proshield reload");
                case 30 -> player.performCommand("proshield admin tp");
                case 31 -> openMain(player);
            }
        }
    }

    // === RELOAD SUPPORT ===
    public void reloadGUIs() {
        cache.clear();
        cache.put(GUICache.GUIType.MAIN, buildMainGUI());
        cache.put(GUICache.GUIType.PLAYER, buildPlayerGUI());
        cache.put(GUICache.GUIType.ADMIN, buildAdminGUI());
    }
}
