package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple GUI manager with the open* menus that GUIListener expects.
 * Preserves the original menu layout we discussed and adds giveCompass(player, putInHotbar).
 */
public class GUIManager {

    private final ProShield plugin;
    private final MessagesUtil msg;

    public GUIManager(ProShield plugin) {
        this.plugin = plugin;
        this.msg = plugin.getMessagesUtil();
    }

    /* --------- Public API used by GUIListener / commands --------- */

    public void openMain(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, "§bProShield Menu");
        set(inv, 11, Material.GRASS_BLOCK, "§aClaim Chunk", "Protect your current chunk");
        set(inv, 13, Material.BOOK, "§bClaim Info", "See details about this claim");
        set(inv, 15, Material.BARRIER, "§cUnclaim", "Release your current claim");
        set(inv, 19, Material.PLAYER_HEAD, "§aTrust Player", "Grant trust to another player");
        set(inv, 20, Material.SKELETON_SKULL, "§cUntrust Player", "Remove trust from a player");
        set(inv, 21, Material.NAME_TAG, "§6Roles", "Manage player roles");
        set(inv, 22, Material.REDSTONE, "§dClaim Flags", "Toggle claim flags");
        set(inv, 23, Material.ENDER_PEARL, "§eTransfer Ownership", "Give your claim to another player");
        set(inv, 31, Material.PAPER, "§fHelp", "Show commands and tips");
        if (player.hasPermission("proshield.admin")) {
            set(inv, 33, Material.COMPASS, "§cAdmin Menu", "Admin-only tools");
        }
        player.openInventory(inv);
    }

    public void openAdmin(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, "§cProShield Admin");
        set(inv, 10, Material.FLINT_AND_STEEL, "§cToggle Fire", "Enable/disable fire spread");
        set(inv, 11, Material.TNT, "§cToggle Explosions", "Enable/disable explosions");
        set(inv, 12, Material.ENDERMAN_SPAWN_EGG, "§cEntity Grief", "Toggle mob/entity griefing");
        set(inv, 13, Material.OAK_DOOR, "§cInteractions", "Toggle interaction protections");
        set(inv, 14, Material.DIAMOND_SWORD, "§cPvP", "Enable/disable PvP");
        set(inv, 20, Material.CHEST, "§6Keep Items", "Toggle keep-drops in claims");
        set(inv, 21, Material.LAVA_BUCKET, "§6Purge Expired", "Remove old claims");
        set(inv, 23, Material.REDSTONE_TORCH, "§cDebug Mode", "Toggle debug logging");
        set(inv, 24, Material.COMPASS, "§cCompass Drop", "Toggle compass behavior");
        set(inv, 25, Material.BOOK, "§aReload Config", "Reload ProShield config");
        set(inv, 28, Material.BEDROCK, "§cSpawn Guard", "Toggle spawn guard");
        set(inv, 30, Material.ENDER_EYE, "§aTP Tools", "Teleport to claims");
        set(inv, 31, Material.ARROW, "§fBack", "Return to main menu");
        player.openInventory(inv);
    }

    public void openTrustMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "§aTrust Players");
        set(inv, 13, Material.PLAYER_HEAD, "§aTrust Player", "Choose a player to trust");
        set(inv, 26, Material.ARROW, "§fBack", "Return to main menu");
        player.openInventory(inv);
    }

    public void openUntrustMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "§cUntrust Players");
        set(inv, 13, Material.SKELETON_SKULL, "§cUntrust Player", "Choose a player to untrust");
        set(inv, 26, Material.ARROW, "§fBack", "Return to main menu");
        player.openInventory(inv);
    }

    public void openRolesMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "§6Roles Manager");
        set(inv, 10, Material.GRAY_DYE, "§7Visitor", "No interaction rights");
        set(inv, 11, Material.GREEN_DYE, "§aMember", "Basic interaction rights");
        set(inv, 12, Material.CHEST, "§6Container", "Access to containers");
        set(inv, 13, Material.IRON_PICKAXE, "§bBuilder", "Can build and break");
        set(inv, 14, Material.DIAMOND, "§dCo-Owner", "Full rights");
        set(inv, 26, Material.ARROW, "§fBack", "Return to main menu");
        player.openInventory(inv);
    }

    public void openFlagsMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "§dClaim Flags");
        set(inv, 10, Material.DIAMOND_SWORD, "§cPvP", "Toggle PvP");
        set(inv, 11, Material.TNT, "§cExplosions", "Toggle explosions");
        set(inv, 12, Material.FLINT_AND_STEEL, "§cFire", "Toggle fire");
        set(inv, 13, Material.ENDERMAN_SPAWN_EGG, "§cEntity Grief", "Toggle entity grief");
        set(inv, 14, Material.REDSTONE, "§cInteractions", "Toggle block interactions");
        set(inv, 26, Material.ARROW, "§fBack", "Return to main menu");
        player.openInventory(inv);
    }

    public void openTransferMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "§eTransfer Ownership");
        set(inv, 13, Material.ENDER_PEARL, "§eTransfer Claim", "Select player to transfer claim");
        set(inv, 26, Material.ARROW, "§fBack", "Return to main menu");
        player.openInventory(inv);
    }

    /**
     * Give the ProShield compass to a player. If putInHotbar is true, use configured slot.
     */
    public void giveCompass(Player player, boolean putInHotbar) {
        ItemStack compass = new ItemStack(Material.COMPASS);
        ItemMeta meta = compass.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.AQUA + "ProShield Compass");
            meta.setLore(List.of(ChatColor.GRAY + "Navigate claims and tools"));
            compass.setItemMeta(meta);
        }

        if (putInHotbar) {
            FileConfiguration cfg = plugin.getConfig();
            int slot = Math.max(0, Math.min(8, cfg.getInt("admin-menu.defaults.compass-slot", 8)));
            PlayerInventory inv = player.getInventory();
            ItemStack existing = inv.getItem(slot);
            if (existing == null || existing.getType() == Material.AIR) {
                inv.setItem(slot, compass);
            } else {
                inv.addItem(compass);
            }
        } else {
            player.getInventory().addItem(compass);
        }
        msg.send(player, "admin.compass-given");
    }

    public void clearCache() {
        // No cache object in this trimmed version; provided for API parity
    }

    /* ------------- small gui helper ------------- */
    private void set(Inventory inv, int slot, Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            List<String> lores = new ArrayList<>();
            for (String line : lore) lores.add("§7" + line);
            meta.setLore(lores);
            item.setItemMeta(meta);
        }
        inv.setItem(slot, item);
    }
}
