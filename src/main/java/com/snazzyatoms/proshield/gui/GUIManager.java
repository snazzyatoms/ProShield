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

import java.util.*;

public class GUIManager {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final GUICache cache;

    public GUIManager(ProShield plugin, PlotManager plotManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
        this.cache = new GUICache();
    }

    /* -----------------------------------------------------
     * Open Menus
     * --------------------------------------------------- */
    public void openMain(Player player, boolean admin) {
        String key = (admin ? "admin-main" : "player-main");
        Inventory cached = cache.get(player, key);
        if (cached != null) {
            player.openInventory(cached);
            return;
        }

        Inventory inv = Bukkit.createInventory(null, 54, ChatColor.DARK_AQUA + "ProShield Menu");

        // Core Player Options
        setItem(inv, 11, Material.GRASS_BLOCK, "&aClaim Chunk", "&7Claim the current chunk.");
        setItem(inv, 13, Material.PAPER, "&bClaim Info", "&7View information about this claim.");
        setItem(inv, 15, Material.BARRIER, "&cUnclaim Chunk", "&7Remove your claim.");

        // Expanded Features (1.2.5)
        setItem(inv, 20, Material.PLAYER_HEAD, "&eTrust Player", "&7Add a trusted player.");
        setItem(inv, 21, Material.SKELETON_SKULL, "&eUntrust Player", "&7Remove a trusted player.");
        setItem(inv, 22, Material.BOOK, "&dManage Roles", "&7Assign roles to trusted players.");
        setItem(inv, 23, Material.REDSTONE_TORCH, "&6Toggle Claim Flags", "&7Enable/disable protections.");
        setItem(inv, 24, Material.ENDER_PEARL, "&5Transfer Ownership", "&7Give your claim to another player.");

        // Navigation
        setItem(inv, 31, Material.BOOKSHELF, "&fHelp", "&7View available commands.");
        if (admin) {
            setItem(inv, 33, Material.COMPASS, "&cAdmin Tools", "&7Open admin menu.");
        }

        cache.put(player, key, inv);
        player.openInventory(inv);
    }

    public void openAdmin(Player player) {
        String key = "admin";
        Inventory cached = cache.get(player, key);
        if (cached != null) {
            player.openInventory(cached);
            return;
        }

        Inventory inv = Bukkit.createInventory(null, 54, ChatColor.DARK_RED + "ProShield Admin");

        setItem(inv, 10, Material.FIRE_CHARGE, "&cFire Toggle", "&7Toggle fire spread & ignition.");
        setItem(inv, 11, Material.TNT, "&cExplosions Toggle", "&7Toggle TNT, creeper, and wither explosions.");
        setItem(inv, 12, Material.ENDER_EYE, "&cEntity Grief Toggle", "&7Toggle Enderman, Ravager, Wither griefing.");
        setItem(inv, 13, Material.OAK_DOOR, "&cInteractions Toggle", "&7Restrict doors, levers, buttons, etc.");
        setItem(inv, 14, Material.IRON_SWORD, "&cPvP Toggle", "&7Enable/disable PvP in claims.");
        setItem(inv, 20, Material.CHEST, "&cKeep Items Toggle", "&7Toggle item-keep in claims.");
        setItem(inv, 21, Material.LAVA_BUCKET, "&cPurge Expired Claims", "&7Remove expired claims.");
        setItem(inv, 22, Material.REDSTONE, "&cReload Config", "&7Reload ProShield configuration.");
        setItem(inv, 23, Material.DEBUG_STICK, "&cDebug Mode", "&7Toggle debug logging.");
        setItem(inv, 30, Material.ENDER_PEARL, "&cTeleport Tools", "&7Teleport to player claims.");

        // Back navigation
        setItem(inv, 31, Material.ARROW, "&fBack", "&7Return to main menu.");
        setItem(inv, 49, Material.BARRIER, "&cClose", "&7Exit menu.");

        cache.put(player, key, inv);
        player.openInventory(inv);
    }

    public void openRoleMenu(Player player, Claim claim) {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.LIGHT_PURPLE + "Manage Roles");

        setItem(inv, 10, Material.PAPER, "&fVisitor", "&7Walk only, no interaction.");
        setItem(inv, 11, Material.WOODEN_SWORD, "&fMember", "&7Basic interactions.");
        setItem(inv, 12, Material.CHEST, "&fContainer", "&7Can open containers.");
        setItem(inv, 13, Material.IRON_PICKAXE, "&fBuilder", "&7Can build/break.");
        setItem(inv, 14, Material.NETHER_STAR, "&fCo-Owner", "&7Full access.");

        setItem(inv, 22, Material.ARROW, "&fBack", "&7Return to main menu.");
        player.openInventory(inv);
    }

    public void openFlagMenu(Player player, Claim claim) {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.GOLD + "Claim Flags");

        setItem(inv, 10, Material.IRON_SWORD, "&cPvP", "&7Toggle PvP inside this claim.");
        setItem(inv, 11, Material.TNT, "&cExplosions", "&7Toggle TNT/creeper explosions.");
        setItem(inv, 12, Material.FIRE_CHARGE, "&cFire", "&7Toggle fire spread/ignition.");
        setItem(inv, 13, Material.ENDER_PEARL, "&cEnderman Teleport", "&7Toggle enderman teleport.");
        setItem(inv, 14, Material.CHEST, "&cContainers", "&7Toggle chest/container use.");

        setItem(inv, 22, Material.ARROW, "&fBack", "&7Return to main menu.");
        player.openInventory(inv);
    }

    /* -----------------------------------------------------
     * Helpers
     * --------------------------------------------------- */
    private void setItem(Inventory inv, int slot, Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
            if (lore.length > 0) {
                List<String> loreList = new ArrayList<>();
                for (String l : lore) {
                    loreList.add(ChatColor.translateAlternateColorCodes('&', l));
                }
                meta.setLore(loreList);
            }
            item.setItemMeta(meta);
        }
        inv.setItem(slot, item);
    }

    public ProShield getPlugin() {
        return plugin;
    }

    public PlotManager getPlotManager() {
        return plotManager;
    }
}
