package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * GUIManager - Handles all GUI menus for ProShield
 *
 * v1.2.5:
 *  - Player GUI expanded: Trust, Untrust, Roles, Flags, Transfer Ownership
 *  - Admin GUI refined: Reload, Debug, Compass toggle, Spawn Guard, TP Tools
 *  - Integrated GUICache for performance
 *  - Back button fixed and consistent
 *  - Reload clears and rebuilds menus
 */
public class GUIManager {

    private final ProShield plugin;
    private final GUICache cache;

    public GUIManager(ProShield plugin) {
        this.plugin = plugin;
        this.cache = new GUICache(plugin);
    }

    /* ---------------------------------------------------------
     * 🔹 Main Player Menu
     * --------------------------------------------------------- */
    public void openMain(Player player) {
        String title = "§bProShield Menu";

        Inventory inv = cache.getOrBuild(title, () -> {
            Inventory fresh = Bukkit.createInventory(null, 54, title);

            // Claim
            setItem(fresh, 11, Material.GRASS_BLOCK, "§aClaim Chunk", "Protect your current chunk");

            // Info
            setItem(fresh, 13, Material.BOOK, "§bClaim Info", "See details about this claim");

            // Unclaim
            setItem(fresh, 15, Material.BARRIER, "§cUnclaim", "Release your current claim");

            // Trust Players
            setItem(fresh, 19, Material.PLAYER_HEAD, "§aTrust Player", "Grant trust to another player");

            // Untrust Players
            setItem(fresh, 20, Material.SKELETON_SKULL, "§cUntrust Player", "Remove trust from a player");

            // Roles
            setItem(fresh, 21, Material.NAME_TAG, "§6Roles", "Manage player roles inside your claim");

            // Flags
            setItem(fresh, 22, Material.REDSTONE, "§dClaim Flags", "Toggle claim protection flags");

            // Transfer Ownership
            setItem(fresh, 23, Material.ENDER_PEARL, "§eTransfer Ownership", "Give your claim to another player");

            // Help
            setItem(fresh, 31, Material.PAPER, "§fHelp", "Show commands and usage tips");

            // Admin Menu (if perms)
            setItem(fresh, 33, Material.COMPASS, "§cAdmin Menu", "Admin-only tools");

            cache.put(title, fresh);
        });

        player.openInventory(inv);
    }

    /* ---------------------------------------------------------
     * 🔹 Admin Menu
     * --------------------------------------------------------- */
    public void openAdmin(Player player) {
        String title = "§cProShield Admin";

        Inventory inv = cache.getOrBuild(title, () -> {
            Inventory fresh = Bukkit.createInventory(null, 54, title);

            // Fire toggle
            setItem(fresh, 10, Material.FLINT_AND_STEEL, "§cToggle Fire", "Enable/disable fire spread");

            // Explosions
            setItem(fresh, 11, Material.TNT, "§cToggle Explosions", "Enable/disable explosions");

            // Entity Grief
            setItem(fresh, 12, Material.ENDERMAN_SPAWN_EGG, "§cEntity Grief", "Toggle mob/entity griefing");

            // Interactions
            setItem(fresh, 13, Material.OAK_DOOR, "§cInteractions", "Toggle block interaction protections");

            // PvP
            setItem(fresh, 14, Material.DIAMOND_SWORD, "§cPvP", "Enable/disable PvP inside claims");

            // Keep Items
            setItem(fresh, 20, Material.CHEST, "§6Keep Items", "Toggle keep-drops in claims");

            // Purge expired claims
            setItem(fresh, 21, Material.LAVA_BUCKET, "§6Purge Expired", "Remove old claims");

            // Debug
            setItem(fresh, 23, Material.REDSTONE_TORCH, "§cDebug Mode", "Toggle debug logging");

            // Compass Drop
            setItem(fresh, 24, Material.COMPASS, "§cCompass Drop", "Toggle compass drop behavior");

            // Reload
            setItem(fresh, 25, Material.BOOK, "§aReload Config", "Reload ProShield configuration");

            // Spawn Guard
            setItem(fresh, 28, Material.BEDROCK, "§cSpawn Guard", "Toggle spawn protection radius");

            // TP Tools
            setItem(fresh, 30, Material.ENDER_EYE, "§aTP Tools", "Teleport to claims");

            // Help
            setItem(fresh, 22, Material.PAPER, "§fHelp", "Show admin help & usage tips");

            // Back
            setItem(fresh, 31, Material.ARROW, "§fBack", "Return to main menu");

            cache.put(title, fresh);
        });

        player.openInventory(inv);
    }

    /* ---------------------------------------------------------
     * 🔹 Sub-Menus (Player)
     * --------------------------------------------------------- */
    public void openTrustMenu(Player player) {
        String title = "§aTrust Players";

        Inventory inv = cache.getOrBuild(title, () -> {
            Inventory fresh = Bukkit.createInventory(null, 27, title);
            setItem(fresh, 13, Material.PLAYER_HEAD, "§aTrust Player", "Choose a player to trust");
            setItem(fresh, 26, Material.ARROW, "§fBack", "Return to main menu");
            cache.put(title, fresh);
        });

        player.openInventory(inv);
    }

    public void openUntrustMenu(Player player) {
        String title = "§cUntrust Players";

        Inventory inv = cache.getOrBuild(title, () -> {
            Inventory fresh = Bukkit.createInventory(null, 27, title);
            setItem(fresh, 13, Material.SKELETON_SKULL, "§cUntrust Player", "Choose a player to untrust");
            setItem(fresh, 26, Material.ARROW, "§fBack", "Return to main menu");
            cache.put(title, fresh);
        });

        player.openInventory(inv);
    }

    public void openRolesMenu(Player player) {
        String title = "§6Roles Manager";

        Inventory inv = cache.getOrBuild(title, () -> {
            Inventory fresh = Bukkit.createInventory(null, 27, title);

            setItem(fresh, 10, Material.GRAY_DYE, "§7Visitor", "No interaction rights");
            setItem(fresh, 11, Material.GREEN_DYE, "§aMember", "Basic interaction rights");
            setItem(fresh, 12, Material.CHEST, "§6Container", "Access to containers");
            setItem(fresh, 13, Material.IRON_PICKAXE, "§bBuilder", "Can build and break");
            setItem(fresh, 14, Material.DIAMOND, "§dCo-Owner", "Full rights");

            setItem(fresh, 26, Material.ARROW, "§fBack", "Return to main menu");

            cache.put(title, fresh);
        });

        player.openInventory(inv);
    }

    public void openFlagsMenu(Player player) {
        String title = "§dClaim Flags";

        Inventory inv = cache.getOrBuild(title, () -> {
            Inventory fresh = Bukkit.createInventory(null, 27, title);

            setItem(fresh, 10, Material.DIAMOND_SWORD, "§cPvP", "Toggle PvP");
            setItem(fresh, 11, Material.TNT, "§cExplosions", "Toggle explosions");
            setItem(fresh, 12, Material.FLINT_AND_STEEL, "§cFire", "Toggle fire");
            setItem(fresh, 13, Material.ENDERMAN_SPAWN_EGG, "§cEntity Grief", "Toggle entity grief");
            setItem(fresh, 14, Material.REDSTONE, "§cInteractions", "Toggle block interactions");

            setItem(fresh, 26, Material.ARROW, "§fBack", "Return to main menu");

            cache.put(title, fresh);
        });

        player.openInventory(inv);
    }

    public void openTransferMenu(Player player) {
        String title = "§eTransfer Ownership";

        Inventory inv = cache.getOrBuild(title, () -> {
            Inventory fresh = Bukkit.createInventory(null, 27, title);

            setItem(fresh, 13, Material.ENDER_PEARL, "§eTransfer Claim", "Select player to transfer claim");
            setItem(fresh, 26, Material.ARROW, "§fBack", "Return to main menu");

            cache.put(title, fresh);
        });

        player.openInventory(inv);
    }

    /* ---------------------------------------------------------
     * 🔹 Utilities
     * --------------------------------------------------------- */
    private void setItem(Inventory inv, int slot, Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            List<String> lores = new ArrayList<>();
            for (String line : lore) {
                lores.add("§7" + line);
            }
            meta.setLore(lores);
            item.setItemMeta(meta);
        }
        inv.setItem(slot, item);
    }

    /**
     * Clears all GUI caches (used on /proshield reload)
     */
    public void clearCache() {
        cache.clear();
    }
}
