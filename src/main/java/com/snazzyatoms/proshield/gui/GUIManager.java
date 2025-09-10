package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.ClaimRoleManager;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GUIManager {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final ClaimRoleManager roleManager;
    private final GUICache cache;

    public GUIManager(ProShield plugin, PlotManager plotManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
        this.roleManager = plugin.getRoleManager();
        this.cache = new GUICache();
    }

    // === MAIN MENU ===
    public void openMain(Player player) {
        boolean isAdmin = player.hasPermission("proshield.admin");
        String key = "main_" + isAdmin;

        Inventory inv = cache.getInventory(key);
        if (inv == null) {
            inv = Bukkit.createInventory(null, 54, "🛡️ ProShield Menu");

            inv.setItem(11, makeItem(Material.GRASS_BLOCK, "§aClaim Chunk", "Protect this chunk"));
            inv.setItem(13, makeItem(Material.PAPER, "§bClaim Info", "View owner, trusted players"));
            inv.setItem(15, makeItem(Material.BARRIER, "§cUnclaim", "Release this chunk"));

            inv.setItem(29, makeItem(Material.PLAYER_HEAD, "§eTrust Player", "Trust a player into your claim"));
            inv.setItem(30, makeItem(Material.SKELETON_SKULL, "§eUntrust Player", "Remove a player from your claim"));
            inv.setItem(31, makeItem(Material.BOOK, "§bHelp", "View commands"));
            inv.setItem(32, makeItem(Material.WRITABLE_BOOK, "§6Role Manager", "Assign roles to trusted players"));
            inv.setItem(33, makeItem(Material.REDSTONE_TORCH, "§6Flags", "Toggle claim-specific protections"));
            inv.setItem(34, makeItem(Material.ENDER_EYE, "§dTransfer Claim", "Give this claim to another player"));

            if (isAdmin) {
                inv.setItem(49, makeItem(Material.COMPASS, "§cAdmin Menu", "Access advanced tools"));
            }

            cache.putInventory(key, inv);
        }

        player.openInventory(inv);
    }

    // === TRUST MENU ===
    public void openTrustMenu(Player player, Plot plot) {
        String key = "trust_" + plot.getId();
        Inventory inv = cache.getInventory(key);

        if (inv == null) {
            inv = Bukkit.createInventory(null, 54, "👥 Trust Players");
            inv.setItem(49, makeItem(Material.ARROW, "§7Back", "Return to ProShield Menu"));
            cache.putInventory(key, inv);
        }

        player.openInventory(inv);
    }

    // === UNTRUST MENU ===
    public void openUntrustMenu(Player player, Plot plot) {
        String key = "untrust_" + plot.getId();
        Inventory inv = cache.getInventory(key);

        if (inv == null) {
            inv = Bukkit.createInventory(null, 54, "🚫 Untrust Players");
            inv.setItem(49, makeItem(Material.ARROW, "§7Back", "Return to ProShield Menu"));
            cache.putInventory(key, inv);
        }

        player.openInventory(inv);
    }

    // === ROLE MENU ===
    public void openRoleMenu(Player player, Plot plot) {
        String key = "roles_" + plot.getId();
        Inventory inv = cache.getInventory(key);

        if (inv == null) {
            inv = Bukkit.createInventory(null, 54, "⚙️ Role Manager");

            inv.setItem(10, makeItem(Material.GRAY_DYE, "§7Visitor", "Default: no interactions"));
            inv.setItem(12, makeItem(Material.GREEN_DYE, "§aMember", "Can use doors, buttons, etc."));
            inv.setItem(14, makeItem(Material.CHEST, "§6Container", "Can access chests, furnaces"));
            inv.setItem(16, makeItem(Material.IRON_PICKAXE, "§bBuilder", "Can build and break"));
            inv.setItem(28, makeItem(Material.NETHER_STAR, "§dCo-Owner", "Full access"));

            inv.setItem(49, makeItem(Material.ARROW, "§7Back", "Return to ProShield Menu"));

            cache.putInventory(key, inv);
        }

        player.openInventory(inv);
    }

    // === FLAG MENU ===
    public void openFlagMenu(Player player, Plot plot) {
        String key = "flags_" + plot.getId();
        Inventory inv = cache.getInventory(key);

        if (inv == null) {
            inv = Bukkit.createInventory(null, 54, "🚩 Claim Flags");

            inv.setItem(10, makeItem(Material.DIAMOND_SWORD, "§cPvP", "Toggle PvP inside claim"));
            inv.setItem(12, makeItem(Material.TNT, "§cExplosions", "Toggle TNT/creeper/wither"));
            inv.setItem(14, makeItem(Material.FLINT_AND_STEEL, "§cFire", "Toggle fire spread/ignite"));
            inv.setItem(16, makeItem(Material.ENDERMAN_SPAWN_EGG, "§cMob Grief", "Toggle mob griefing"));

            inv.setItem(49, makeItem(Material.ARROW, "§7Back", "Return to ProShield Menu"));

            cache.putInventory(key, inv);
        }

        player.openInventory(inv);
    }

    // === TRANSFER MENU ===
    public void openTransferMenu(Player player, Plot plot) {
        String key = "transfer_" + plot.getId();
        Inventory inv = cache.getInventory(key);

        if (inv == null) {
            inv = Bukkit.createInventory(null, 54, "📦 Transfer Ownership");
            inv.setItem(22, makeItem(Material.ENDER_EYE, "§dTransfer", "Give claim to another player"));
            inv.setItem(49, makeItem(Material.ARROW, "§7Back", "Return to ProShield Menu"));
            cache.putInventory(key, inv);
        }

        player.openInventory(inv);
    }

    // === ADMIN MENU (unchanged from 1.2.4) ===
    public void openAdmin(Player player) {
        String key = "admin";
        Inventory inv = cache.getInventory(key);

        if (inv == null) {
            inv = Bukkit.createInventory(null, 54, "⚙️ ProShield Admin");

            inv.setItem(10, makeItem(Material.FLINT_AND_STEEL, "§cToggle Fire", "Enable/disable fire spread"));
            inv.setItem(11, makeItem(Material.TNT, "§cToggle Explosions", "Enable/disable explosions"));
            inv.setItem(12, makeItem(Material.ENDERMAN_SPAWN_EGG, "§cToggle Entity Grief", "Endermen, Ravagers, etc."));
            inv.setItem(13, makeItem(Material.LEVER, "§cToggle Interactions", "Buttons, levers, doors"));
            inv.setItem(14, makeItem(Material.DIAMOND_SWORD, "§cToggle PvP", "Enable/disable PvP"));
            inv.setItem(20, makeItem(Material.CHEST, "§6Keep Items", "Toggle item-keep inside claims"));
            inv.setItem(21, makeItem(Material.BARRIER, "§cPurge Expired", "Remove expired claims"));
            inv.setItem(22, makeItem(Material.BOOK, "§eHelp", "Admin command guide"));
            inv.setItem(23, makeItem(Material.REDSTONE, "§cDebug Mode", "Enable/disable debug logging"));
            inv.setItem(24, makeItem(Material.COMPASS, "§aCompass Drop", "Toggle drop-if-inventory-full"));
            inv.setItem(25, makeItem(Material.REPEATER, "§bReload Config", "Reload plugin configs"));
            inv.setItem(30, makeItem(Material.ENDER_PEARL, "§dTeleport Tools", "Teleport to claims"));
            inv.setItem(31, makeItem(Material.ARROW, "§7Back", "Return to Main Menu"));

            cache.putInventory(key, inv);
        }

        player.openInventory(inv);
    }

    // === HELPER ===
    private ItemStack makeItem(Material mat, String name, String lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(List.of(lore));
        item.setItemMeta(meta);
        return item;
    }

    // Called after /proshield reload
    public void onConfigReload() {
        cache.clear();
    }
}
