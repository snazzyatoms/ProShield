// src/main/java/com/snazzyatoms/proshield/gui/GUIManager.java
package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.cache.GUICache;
import com.snazzyatoms.proshield.plots.Plot;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class GUIManager {

    private final ProShield plugin;
    private final GUICache cache;

    public GUIManager(ProShield plugin, GUICache cache) {
        this.plugin = plugin;
        this.cache = cache;
    }

    /* ---------------------------------------------------------
     * MAIN MENUS
     * --------------------------------------------------------- */

    public void openMain(Player player) {
        Inventory inv = Bukkit.createInventory(player, 27, ChatColor.AQUA + "★ ProShield Menu");

        fill(inv);

        inv.setItem(10, createItem(Material.GRASS_BLOCK, ChatColor.GREEN + "Claim Chunk",
                "§7Protect this land from griefers.", "§eClick to claim your current chunk."));
        inv.setItem(11, createItem(Material.BARRIER, ChatColor.RED + "Unclaim Chunk",
                "§7Release your land back to the wilderness.", "§eClick to unclaim your chunk."));
        inv.setItem(12, createItem(Material.BOOK, ChatColor.YELLOW + "Claim Info",
                "§7View information about this claim.", "§eOwner, trusted players, roles."));
        inv.setItem(13, createItem(Material.PAPER, ChatColor.GOLD + "Trust Menu",
                "§7Trust new players to your land.", "§eClick to manage trust."));
        inv.setItem(14, createItem(Material.BOOKSHELF, ChatColor.RED + "Untrust Menu",
                "§7Remove trusted players.", "§eClick to manage untrust."));
        inv.setItem(15, createItem(Material.IRON_SWORD, ChatColor.DARK_RED + "Flags",
                "§7Toggle claim settings.", "§ePvP, explosions, fire, redstone, etc."));
        inv.setItem(16, createItem(Material.NAME_TAG, ChatColor.LIGHT_PURPLE + "Roles",
                "§7Assign roles to trusted players.", "§eBuilder, Moderator, Manager."));

        cache.setPlayerMenu(player, inv);
        player.openInventory(inv);
    }

    public void openAdminMain(Player player) {
        Inventory inv = Bukkit.createInventory(player, 27, ChatColor.DARK_RED + "★ ProShield Admin Menu");

        fill(inv);

        // Admins also have player actions
        openMain(player);
        player.closeInventory(); // reset back to admin menu

        inv.setItem(10, createItem(Material.COMPASS, ChatColor.AQUA + "Teleport to Claim",
                "§7Teleport directly to a player’s claim.", "§eUse admin powers to jump around."));
        inv.setItem(11, createItem(Material.TNT, ChatColor.RED + "Force Unclaim",
                "§7Remove a claim without permission.", "§eAdmin-only tool."));
        inv.setItem(12, createItem(Material.CHEST, ChatColor.GOLD + "Manage Flags",
                "§7Override flags in any claim.", "§eForce changes as admin."));
        inv.setItem(13, createItem(Material.HOPPER, ChatColor.YELLOW + "Toggle Keep-Items",
                "§7Force keep-drops mode.", "§eEnable/disable drops in claims."));
        inv.setItem(14, createItem(Material.BOOK, ChatColor.DARK_PURPLE + "Wilderness Tools",
                "§7Manage wilderness settings.", "§eAdmin controls for wilderness."));

        cache.setAdminMenu(player, inv);
        player.openInventory(inv);
    }

    /* ---------------------------------------------------------
     * SUB MENUS
     * --------------------------------------------------------- */

    public void openFlagsMenu(Player player, boolean fromAdmin) {
        Inventory inv = Bukkit.createInventory(player, 27, ChatColor.YELLOW + "⚑ Claim Flags");

        fill(inv);

        inv.setItem(10, createItem(Material.IRON_SWORD, ChatColor.RED + "PvP",
                "§7Toggle combat between players.", "§eClick to toggle PvP."));
        inv.setItem(11, createItem(Material.TNT, ChatColor.DARK_RED + "Explosions",
                "§7Toggle TNT & creeper damage.", "§eClick to toggle explosions."));
        inv.setItem(12, createItem(Material.FLINT_AND_STEEL, ChatColor.GOLD + "Fire",
                "§7Toggle fire spread & ignition.", "§eClick to toggle fire."));
        inv.setItem(13, createItem(Material.REDSTONE, ChatColor.RED + "Redstone",
                "§7Toggle redstone mechanics.", "§eClick to toggle redstone."));
        inv.setItem(14, createItem(Material.CHEST, ChatColor.GREEN + "Containers",
                "§7Toggle chest/furnace/hopper access.", "§eClick to toggle container access."));

        inv.setItem(22, createItem(Material.ARROW, ChatColor.RED + "Back", "§7Return to previous menu."));

        if (fromAdmin) cache.setAdminMenu(player, inv); else cache.setPlayerMenu(player, inv);
        player.openInventory(inv);
    }

    public void openRolesGUI(Player player, Plot plot, boolean fromAdmin) {
        Inventory inv = Bukkit.createInventory(player, 27, ChatColor.AQUA + "♜ Claim Roles");

        fill(inv);

        inv.setItem(11, createItem(Material.STONE_PICKAXE, ChatColor.GREEN + "Builder",
                "§7Build inside this claim.", "§eTrusted player rights."));
        inv.setItem(12, createItem(Material.IRON_SWORD, ChatColor.RED + "Moderator",
                "§7PvP & entity rights.", "§eEnforce rules inside claims."));
        inv.setItem(13, createItem(Material.DIAMOND, ChatColor.GOLD + "Manager",
                "§7Full management rights.", "§eNearly full owner powers."));
        inv.setItem(15, createItem(Material.BOOK, ChatColor.YELLOW + "Trusted List",
                "§7View all trusted players.", "§eShows current trust list."));

        inv.setItem(22, createItem(Material.ARROW, ChatColor.RED + "Back", "§7Return to previous menu."));

        if (fromAdmin) cache.setAdminMenu(player, inv); else cache.setPlayerMenu(player, inv);
        player.openInventory(inv);
    }

    public void openTrustMenu(Player player, boolean fromAdmin) {
        Inventory inv = Bukkit.createInventory(player, 9, ChatColor.GREEN + "Trust Player");

        fill(inv);

        inv.setItem(4, createItem(Material.BOOK, ChatColor.GREEN + "Trust Player",
                "§7Use /trust <name> [role]", "§eGrant rights to another player."));
        inv.setItem(8, createItem(Material.ARROW, ChatColor.RED + "Back", "§7Return to previous menu."));

        if (fromAdmin) cache.setAdminMenu(player, inv); else cache.setPlayerMenu(player, inv);
        player.openInventory(inv);
    }

    public void openUntrustMenu(Player player, boolean fromAdmin) {
        Inventory inv = Bukkit.createInventory(player, 9, ChatColor.RED + "Untrust Player");

        fill(inv);

        inv.setItem(4, createItem(Material.BARRIER, ChatColor.RED + "Untrust Player",
                "§7Use /untrust <name>", "§eRemove trust from a player."));
        inv.setItem(8, createItem(Material.ARROW, ChatColor.RED + "Back", "§7Return to previous menu."));

        if (fromAdmin) cache.setAdminMenu(player, inv); else cache.setPlayerMenu(player, inv);
        player.openInventory(inv);
    }

    /* ---------------------------------------------------------
     * HELPERS
     * --------------------------------------------------------- */

    private ItemStack createItem(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(Arrays.asList(lore));
            item.setItemMeta(meta);
        }
        return item;
    }

    private void fill(Inventory inv) {
        ItemStack filler = createItem(Material.GRAY_STAINED_GLASS_PANE, ChatColor.DARK_GRAY + " ");
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, filler);
        }
    }

    public void clearCache() {
        cache.clearCache();
    }

    public GUICache getCache() {
        return cache;
    }

    public ProShield getPlugin() {
        return plugin;
    }
}
