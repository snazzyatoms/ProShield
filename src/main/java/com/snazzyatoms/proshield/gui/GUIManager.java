package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * GUIManager - Handles all GUI menus for ProShield
 *
 * v1.2.5:
 *  - Player GUI expanded: Trust, Untrust, Roles, Flags, Transfer Ownership
 *  - Admin GUI refined: Reload, Debug, Compass toggle, Spawn Guard, TP Tools
 *  - Integrated GUICache for performance
 *  - Back button fixed and consistent
 *  - Reload clears and rebuilds menus
 *  - Compass creation/distribution helpers (player & admin)
 */
public class GUIManager {

    private final ProShield plugin;
    private final GUICache cache;

    private final NamespacedKey compassKey;
    private final NamespacedKey compassKindKey;

    public GUIManager(ProShield plugin) {
        this.plugin = plugin;
        this.cache = new GUICache(plugin);
        this.compassKey = new NamespacedKey(plugin, "proshield_compass");
        this.compassKindKey = new NamespacedKey(plugin, "kind");
    }

    /* ---------------------------------------------------------
     * 🔹 Compass Utilities
     * --------------------------------------------------------- */

    public void giveCompass(Player player, boolean adminCompass) {
        FileConfiguration cfg = plugin.getConfig();
        boolean dropIfFull = cfg.getBoolean("compass.drop-if-full", true);

        ItemStack compass = adminCompass ? createAdminCompass() : createPlayerCompass();

        int preferredSlot = cfg.getInt("admin.defaults.compass-slot",
                cfg.getInt("autogive.compass-slot", 8));
        preferredSlot = Math.max(0, Math.min(preferredSlot, 35));

        ItemStack existing = player.getInventory().getItem(preferredSlot);
        if (existing == null || existing.getType() == Material.AIR) {
            player.getInventory().setItem(preferredSlot, compass);
            return;
        }

        var leftover = player.getInventory().addItem(compass);
        if (leftover.isEmpty()) {
            return;
        }

        if (dropIfFull) {
            player.getWorld().dropItemNaturally(player.getLocation(), compass);
        }
    }

    public ItemStack createPlayerCompass() {
        return createCompass("§bProShield Compass", "player", ChatColor.GRAY + "Right-click to open ProShield");
    }

    public ItemStack createAdminCompass() {
        return createCompass("§cProShield Admin Compass", "admin", ChatColor.GRAY + "Right-click to open Admin Tools");
    }

    private ItemStack createCompass(String name, String kind, String loreLine) {
        ItemStack item = new ItemStack(Material.COMPASS, 1);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            List<String> lore = new ArrayList<>();
            lore.add(loreLine);
            meta.setLore(lore);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            pdc.set(compassKey, PersistentDataType.INTEGER, 1);
            pdc.set(compassKindKey, PersistentDataType.STRING, kind);

            item.setItemMeta(meta);
        }
        return item;
    }

    public boolean isProShieldCompass(ItemStack stack) {
        if (stack == null || stack.getType() != Material.COMPASS) return false;
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return false;
        Integer mark = meta.getPersistentDataContainer().get(compassKey, PersistentDataType.INTEGER);
        return Objects.equals(mark, 1);
    }

    public boolean isAdminCompass(ItemStack stack) {
        if (!isProShieldCompass(stack)) return false;
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return false;
        String kind = meta.getPersistentDataContainer().get(compassKindKey, PersistentDataType.STRING);
        return "admin".equalsIgnoreCase(kind);
    }

    /* ---------------------------------------------------------
     * 🔹 Main Player Menu
     * --------------------------------------------------------- */
    public void openMain(Player player) {
        String title = "§bProShield Menu";

        Inventory inv = cache.getOrBuild(title, () -> {
            Inventory fresh = Bukkit.createInventory(null, 54, title);

            setItem(fresh, 11, Material.GRASS_BLOCK, "§aClaim Chunk", "Protect your current chunk");
            setItem(fresh, 13, Material.BOOK, "§bClaim Info", "See details about this claim");
            setItem(fresh, 15, Material.BARRIER, "§cUnclaim", "Release your current claim");
            setItem(fresh, 19, Material.PLAYER_HEAD, "§aTrust Player", "Grant trust to another player");
            setItem(fresh, 20, Material.SKELETON_SKULL, "§cUntrust Player", "Remove trust from a player");
            setItem(fresh, 21, Material.NAME_TAG, "§6Roles", "Manage player roles inside your claim");
            setItem(fresh, 22, Material.REDSTONE, "§dClaim Flags", "Toggle claim protection flags");
            setItem(fresh, 23, Material.ENDER_PEARL, "§eTransfer Ownership", "Give your claim to another player");
            setItem(fresh, 31, Material.PAPER, "§fHelp", "Show commands and usage tips");
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

            setItem(fresh, 10, Material.FLINT_AND_STEEL, "§cToggle Fire", "Enable/disable fire spread");
            setItem(fresh, 11, Material.TNT, "§cToggle Explosions", "Enable/disable explosions");
            setItem(fresh, 12, Material.ENDERMAN_SPAWN_EGG, "§cEntity Grief", "Toggle mob/entity griefing");
            setItem(fresh, 13, Material.OAK_DOOR, "§cInteractions", "Toggle block interaction protections");
            setItem(fresh, 14, Material.DIAMOND_SWORD, "§cPvP", "Enable/disable PvP inside claims");
            setItem(fresh, 20, Material.CHEST, "§6Keep Items", "Toggle keep-drops in claims");
            setItem(fresh, 21, Material.LAVA_BUCKET, "§6Purge Expired", "Remove old claims");
            setItem(fresh, 23, Material.REDSTONE_TORCH, "§cDebug Mode", "Toggle debug logging");
            setItem(fresh, 24, Material.COMPASS, "§cCompass Drop", "Toggle compass drop behavior");
            setItem(fresh, 25, Material.BOOK, "§aReload Config", "Reload ProShield configuration");
            setItem(fresh, 28, Material.BEDROCK, "§cSpawn Guard", "Toggle spawn protection radius");
            setItem(fresh, 30, Material.ENDER_EYE, "§aTP Tools", "Teleport to claims");
            setItem(fresh, 22, Material.PAPER, "§fHelp", "Show admin help & usage tips");
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
