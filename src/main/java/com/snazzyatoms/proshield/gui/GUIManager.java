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
     * MAIN MENUS (Player + Admin parity)
     * --------------------------------------------------------- */

    public void openMain(Player player) {
        Inventory inv = Bukkit.createInventory(player, 27, ChatColor.AQUA + "⚔ ProShield Menu");
        ItemStack filler = createItem(Material.GRAY_STAINED_GLASS_PANE, ChatColor.DARK_GRAY + " ");

        // Fill everything with placeholders (static look)
        for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, filler);

        // Action buttons (kept centered / consistent)
        inv.setItem(10, createItem(Material.GRASS_BLOCK, ChatColor.GREEN + "Claim Chunk",
                "§7Secure and protect your current chunk.",
                "§eLeft-click: Claim this chunk"));

        inv.setItem(11, createItem(Material.BARRIER, ChatColor.RED + "Unclaim Chunk",
                "§7Release your claim on this chunk.",
                "§cLeft-click: Unclaim current chunk"));

        inv.setItem(12, createItem(Material.BOOK, ChatColor.BLUE + "Trust Menu",
                "§7Trust friends to build with you.",
                "§7Assign roles for permissions."));

        inv.setItem(13, createItem(Material.IRON_SWORD, ChatColor.YELLOW + "Flags",
                "§7Toggle claim settings:",
                "§ePvP, explosions, fire, redstone, containers."));

        inv.setItem(14, createItem(Material.NAME_TAG, ChatColor.GOLD + "Roles",
                "§7Manage who can build, fight, or manage your claim."));

        inv.setItem(15, createItem(Material.ENDER_PEARL, ChatColor.LIGHT_PURPLE + "Transfer Claim",
                "§7Give ownership of this claim to another player."));

        inv.setItem(16, createItem(Material.NETHER_STAR, ChatColor.DARK_PURPLE + "Coming in ProShield 2.0",
                "§8★ Global claim map with markers",
                "§8★ Player statistics",
                "§8★ More flags & utilities"));

        // Back (always present for consistency)
        inv.setItem(22, createItem(Material.ARROW, ChatColor.RED + "Back",
                "§7Return to the previous menu."));

        cache.setPlayerMenu(player, inv);
        player.openInventory(inv);
    }

    public void openAdminMain(Player player) {
        Inventory inv = Bukkit.createInventory(player, 27, ChatColor.DARK_RED + "⚙ ProShield Admin");
        ItemStack filler = createItem(Material.GRAY_STAINED_GLASS_PANE, ChatColor.DARK_GRAY + " ");

        for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, filler);

        // Admin still gets player tools (parity)
        inv.setItem(9, createItem(Material.GRASS_BLOCK, ChatColor.GREEN + "Claim Chunk",
                "§7Admins can also claim land for themselves."));
        inv.setItem(10, createItem(Material.BARRIER, ChatColor.RED + "Unclaim Chunk",
                "§7Unclaim your current chunk."));
        inv.setItem(11, createItem(Material.BOOK, ChatColor.BLUE + "Trust Menu",
                "§7Manage trust the same as players."));
        inv.setItem(12, createItem(Material.IRON_SWORD, ChatColor.YELLOW + "Flags",
                "§7Open claim flags menu."));
        inv.setItem(13, createItem(Material.NAME_TAG, ChatColor.GOLD + "Roles",
                "§7Open claim roles menu."));
        inv.setItem(14, createItem(Material.ENDER_PEARL, ChatColor.LIGHT_PURPLE + "Transfer Claim",
                "§7Transfer claim ownership."));

        // Admin-only tools
        inv.setItem(15, createItem(Material.COMPASS, ChatColor.AQUA + "Teleport to Claim",
                "§7Admin-only teleport tools (via commands)."));
        inv.setItem(16, createItem(Material.TNT, ChatColor.RED + "Force Unclaim",
                "§7Force-remove any claim (command dispatched)."));
        inv.setItem(17, createItem(Material.HOPPER, ChatColor.YELLOW + "Toggle Keep-Items",
                "§7Toggle global keep-drops in config."));

        inv.setItem(22, createItem(Material.BOOK, ChatColor.DARK_PURPLE + "Wilderness Tools",
                "§7Manage wilderness settings (messages, etc)."));

        inv.setItem(23, createItem(Material.BONE, ChatColor.GRAY + "Debug Logging",
                "§7Toggle plugin debug mode."));

        inv.setItem(25, createItem(Material.LAVA_BUCKET, ChatColor.DARK_RED + "Purge Expired Claims",
                "§7Clean up claims marked as expired."));

        // Back
        inv.setItem(26, createItem(Material.ARROW, ChatColor.RED + "Back",
                "§7Return to the previous menu."));

        cache.setAdminMenu(player, inv);
        player.openInventory(inv);
    }

    /* ---------------------------------------------------------
     * SUB MENUS (always include Back)
     * --------------------------------------------------------- */

    public void openFlagsMenu(Player player) {
        Inventory inv = Bukkit.createInventory(player, 27, ChatColor.YELLOW + "⚑ Claim Flags");
        ItemStack filler = createItem(Material.GRAY_STAINED_GLASS_PANE, ChatColor.DARK_GRAY + " ");
        for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, filler);

        inv.setItem(10, createItem(Material.IRON_SWORD, ChatColor.RED + "PvP",
                "§7Toggle player vs player combat in this claim."));
        inv.setItem(11, createItem(Material.TNT, ChatColor.DARK_RED + "Explosions",
                "§7Toggle TNT/creeper damage in this claim."));
        inv.setItem(12, createItem(Material.FLINT_AND_STEEL, ChatColor.GOLD + "Fire",
                "§7Toggle fire spread & ignition."));
        inv.setItem(13, createItem(Material.REDSTONE, ChatColor.RED + "Redstone",
                "§7Toggle redstone mechanics."));
        inv.setItem(14, createItem(Material.CHEST, ChatColor.GREEN + "Containers",
                "§7Toggle chest/furnace access."));

        inv.setItem(22, createItem(Material.ARROW, ChatColor.RED + "Back", "§7Return to main."));

        cache.setPlayerMenu(player, inv);
        player.openInventory(inv);
    }

    public void openRolesGUI(Player player, Plot plot) {
        Inventory inv = Bukkit.createInventory(player, 27, ChatColor.AQUA + "♜ Claim Roles");
        ItemStack filler = createItem(Material.GRAY_STAINED_GLASS_PANE, ChatColor.DARK_GRAY + " ");
        for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, filler);

        inv.setItem(10, createItem(Material.STONE_PICKAXE, ChatColor.GREEN + "Builder",
                "§7Build & break blocks, use doors, buttons."));
        inv.setItem(11, createItem(Material.IRON_SWORD, ChatColor.RED + "Moderator",
                "§7Combat & entity interactions."));
        inv.setItem(12, createItem(Material.DIAMOND, ChatColor.GOLD + "Manager",
                "§7Full management of the claim."));
        inv.setItem(14, createItem(Material.BOOK, ChatColor.YELLOW + "Trusted List",
                "§7View all trusted players."));

        inv.setItem(22, createItem(Material.ARROW, ChatColor.RED + "Back", "§7Return to main."));

        cache.setPlayerMenu(player, inv);
        player.openInventory(inv);
    }

    public void openTransferMenu(Player player) {
        Inventory inv = Bukkit.createInventory(player, 27, ChatColor.LIGHT_PURPLE + "⇄ Transfer Claim");
        ItemStack filler = createItem(Material.GRAY_STAINED_GLASS_PANE, ChatColor.DARK_GRAY + " ");
        for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, filler);

        inv.setItem(13, createItem(Material.ENDER_PEARL, ChatColor.AQUA + "Transfer Claim",
                "§7Use /transfer <player> to confirm."));

        inv.setItem(22, createItem(Material.ARROW, ChatColor.RED + "Back", "§7Return to main."));

        cache.setPlayerMenu(player, inv);
        player.openInventory(inv);
    }

    public void openTrustMenu(Player player) {
        Inventory inv = Bukkit.createInventory(player, 27, ChatColor.GREEN + "✔ Trust Player");
        ItemStack filler = createItem(Material.GRAY_STAINED_GLASS_PANE, ChatColor.DARK_GRAY + " ");
        for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, filler);

        inv.setItem(13, createItem(Material.BOOK, ChatColor.GREEN + "Trust Player",
                "§7Use /trust <name> [role] to add players."));

        inv.setItem(22, createItem(Material.ARROW, ChatColor.RED + "Back", "§7Return to main."));

        cache.setPlayerMenu(player, inv);
        player.openInventory(inv);
    }

    public void openUntrustMenu(Player player) {
        Inventory inv = Bukkit.createInventory(player, 27, ChatColor.RED + "✖ Untrust Player");
        ItemStack filler = createItem(Material.GRAY_STAINED_GLASS_PANE, ChatColor.DARK_GRAY + " ");
        for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, filler);

        inv.setItem(13, createItem(Material.BARRIER, ChatColor.RED + "Untrust Player",
                "§7Use /untrust <name> to remove trust."));

        inv.setItem(22, createItem(Material.ARROW, ChatColor.RED + "Back", "§7Return to main."));

        cache.setPlayerMenu(player, inv);
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
