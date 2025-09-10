package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.PlotManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public class GUIManager {

    private final ProShield plugin;
    private final PlotManager plots;
    private final GUICache cache;

    public GUIManager(ProShield plugin, PlotManager plots, GUICache cache) {
        this.plugin = plugin;
        this.plots = plots;
        this.cache = cache;
    }

    /* ===============================
     * COMPASS CREATION
     * =============================== */

    public ItemStack createAdminCompass() {
        ItemStack compass = new ItemStack(Material.COMPASS);
        ItemMeta meta = compass.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.RED + "ProShield Admin Compass");
            meta.setLore(Arrays.asList(
                    ChatColor.GRAY + "Right-click to open the Admin Menu",
                    ChatColor.DARK_RED + "Admin only!"
            ));
            compass.setItemMeta(meta);
        }
        return compass;
    }

    public ItemStack createPlayerCompass() {
        ItemStack compass = new ItemStack(Material.COMPASS);
        ItemMeta meta = compass.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GREEN + "ProShield Compass");
            meta.setLore(Arrays.asList(
                    ChatColor.GRAY + "Right-click to open your Claim Menu",
                    ChatColor.YELLOW + "Manage land, trust, flags"
            ));
            compass.setItemMeta(meta);
        }
        return compass;
    }

    public boolean isProShieldCompass(ItemStack item) {
        if (item == null || item.getType() != Material.COMPASS) return false;
        if (!item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.hasDisplayName() &&
                (meta.getDisplayName().contains("ProShield"));
    }

    /* ===============================
     * MAIN MENUS
     * =============================== */

    public void openMain(Player player, boolean isAdmin) {
        Inventory cached = cache.getCachedMainMenu(player, isAdmin);
        if (cached != null) {
            player.openInventory(cached);
            return;
        }

        String title = isAdmin ? "ProShield Admin Menu" : "ProShield Menu";
        Inventory inv = Bukkit.createInventory(null, 54, ChatColor.DARK_GREEN + title);

        if (isAdmin) {
            setupAdminMenu(inv);
        } else {
            setupPlayerMenu(inv);
        }

        cache.storeMainMenu(player, isAdmin, inv);
        player.openInventory(inv);
    }

    /* ===============================
     * PLAYER MENU
     * =============================== */

    private void setupPlayerMenu(Inventory inv) {
        inv.setItem(11, makeItem(Material.GRASS_BLOCK, "&aClaim Land", "&7Claim this chunk"));
        inv.setItem(13, makeItem(Material.BOOK, "&bClaim Info", "&7View details of this claim"));
        inv.setItem(15, makeItem(Material.BARRIER, "&cUnclaim", "&7Remove your claim"));

        inv.setItem(20, makeItem(Material.PLAYER_HEAD, "&eTrust Player", "&7Grant access to a player"));
        inv.setItem(21, makeItem(Material.SKELETON_SKULL, "&eUntrust Player", "&7Remove a trusted player"));
        inv.setItem(22, makeItem(Material.WRITABLE_BOOK, "&eManage Roles", "&7Assign roles to trusted players"));

        inv.setItem(29, makeItem(Material.LEVER, "&6Claim Flags", "&7Toggle PvP, fire, explosions, etc."));
        inv.setItem(31, makeItem(Material.MAP, "&eHelp", "&7View available commands"));
        inv.setItem(33, makeItem(Material.NAME_TAG, "&dTransfer Claim", "&7Transfer claim to another player"));

        inv.setItem(48, makeItem(Material.ARROW, "&7Back", "&7Return to previous menu"));
    }

    /* ===============================
     * ADMIN MENU
     * =============================== */

    private void setupAdminMenu(Inventory inv) {
        inv.setItem(10, makeItem(Material.FLINT_AND_STEEL, "&cFire Toggle", "&7Enable/disable fire in claims"));
        inv.setItem(11, makeItem(Material.TNT, "&cExplosion Toggle", "&7Enable/disable explosions"));
        inv.setItem(12, makeItem(Material.ENDER_PEARL, "&cEntity Grief Toggle", "&7Prevent mob grief"));
        inv.setItem(13, makeItem(Material.REDSTONE, "&cInteractions Toggle", "&7Block or allow interactions"));
        inv.setItem(14, makeItem(Material.IRON_SWORD, "&cPvP Toggle", "&7Allow/disallow PvP in claims"));

        inv.setItem(20, makeItem(Material.CHEST, "&6Keep Items Toggle", "&7Prevent item despawn in claims"));
        inv.setItem(21, makeItem(Material.BOOK, "&6Purge Expired Claims", "&7Remove old inactive claims"));
        inv.setItem(22, makeItem(Material.PAPER, "&eHelp", "&7Admin commands help"));

        inv.setItem(23, makeItem(Material.COMMAND_BLOCK, "&dDebug Toggle", "&7Enable/disable debug mode"));
        inv.setItem(24, makeItem(Material.COMPASS, "&aCompass Drop Toggle", "&7Toggle compass drop if full"));
        inv.setItem(25, makeItem(Material.REPEATER, "&bReload Configs", "&7Reload plugin configs"));

        inv.setItem(30, makeItem(Material.ENDER_EYE, "&dTeleport Tools", "&7Teleport to player claims"));
        inv.setItem(31, makeItem(Material.ARROW, "&7Back", "&7Return to previous menu"));
    }

    /* ===============================
     * CLICK HANDLING
     * =============================== */

    public void handleInventoryClick(Player player, int slot, ItemStack item, InventoryClickEvent event, boolean isAdmin) {
        if (item == null || !item.hasItemMeta()) return;

        String name = ChatColor.stripColor(item.getItemMeta().getDisplayName());
        if (name == null) return;

        event.setCancelled(true);

        if (isAdmin) {
            handleAdminClick(player, name);
        } else {
            handlePlayerClick(player, name);
        }
    }

    private void handlePlayerClick(Player player, String name) {
        switch (name.toLowerCase()) {
            case "claim land":
                player.performCommand("proshield claim");
                break;
            case "claim info":
                player.performCommand("proshield info");
                break;
            case "unclaim":
                player.performCommand("proshield unclaim");
                break;
            case "trust player":
                player.performCommand("proshield trustmenu");
                break;
            case "untrust player":
                player.performCommand("proshield untrustmenu");
                break;
            case "manage roles":
                player.performCommand("proshield rolemenu");
                break;
            case "claim flags":
                player.performCommand("proshield flagmenu");
                break;
            case "transfer claim":
                player.performCommand("proshield transfermenu");
                break;
            case "help":
                player.performCommand("proshield help");
                break;
            case "back":
                openMain(player, false);
                break;
        }
    }

    private void handleAdminClick(Player player, String name) {
        switch (name.toLowerCase()) {
            case "fire toggle":
                player.performCommand("proshield admin fire");
                break;
            case "explosion toggle":
                player.performCommand("proshield admin explosions");
                break;
            case "entity grief toggle":
                player.performCommand("proshield admin entitygrief");
                break;
            case "interactions toggle":
                player.performCommand("proshield admin interactions");
                break;
            case "pvp toggle":
                player.performCommand("proshield admin pvp");
                break;
            case "keep items toggle":
                player.performCommand("proshield admin keepdrops");
                break;
            case "purge expired claims":
                player.performCommand("proshield purgeexpired");
                break;
            case "help":
                player.performCommand("proshield help admin");
                break;
            case "debug toggle":
                player.performCommand("proshield debug toggle");
                break;
            case "compass drop toggle":
                player.performCommand("proshield admin compassdrop");
                break;
            case "reload configs":
                player.performCommand("proshield reload");
                break;
            case "teleport tools":
                player.performCommand("proshield admin tp");
                break;
            case "back":
                openMain(player, true);
                break;
        }
    }

    /* ===============================
     * HELPERS
     * =============================== */

    private ItemStack makeItem(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
            meta.setLore(Arrays.asList(lore));
            item.setItemMeta(meta);
        }
        return item;
    }
}
