package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;

/**
 * GUIManager builds and manages all ProShield GUIs (Player + Admin).
 * Uses GUICache to register actions for fast lookups in GUIListener.
 */
public class GUIManager {

    private final ProShield plugin;

    public GUIManager(ProShield plugin) {
        this.plugin = plugin;
    }

    // =========================================================
    // MAIN PLAYER MENU
    // =========================================================

    public void openMain(Player player, boolean admin) {
        Inventory inv = Bukkit.createInventory(null, 54, "§bProShield Menu");

        Map<ItemStack, String> buttonMap = new HashMap<>();

        ItemStack claim = createButton(Material.GRASS_BLOCK, "§aClaim Land", "Claim your current chunk");
        inv.setItem(11, claim);
        buttonMap.put(claim, "claim");

        ItemStack info = createButton(Material.PAPER, "§eClaim Info", "View details of your current claim");
        inv.setItem(13, info);
        buttonMap.put(info, "info");

        ItemStack unclaim = createButton(Material.BARRIER, "§cUnclaim Land", "Unclaim your current chunk");
        inv.setItem(15, unclaim);
        buttonMap.put(unclaim, "unclaim");

        ItemStack trust = createButton(Material.PLAYER_HEAD, "§aTrust Player", "Trust a player in your claim");
        inv.setItem(20, trust);
        buttonMap.put(trust, "trust");

        ItemStack untrust = createButton(Material.SKELETON_SKULL, "§cUntrust Player", "Remove a player from your claim");
        inv.setItem(21, untrust);
        buttonMap.put(untrust, "untrust");

        ItemStack roles = createButton(Material.BOOK, "§bManage Roles", "Manage trusted player roles");
        inv.setItem(22, roles);
        buttonMap.put(roles, "roles");

        ItemStack flags = createButton(Material.REDSTONE, "§dClaim Flags", "Toggle claim protections (PvP, fire, mobs)");
        inv.setItem(23, flags);
        buttonMap.put(flags, "flags");

        ItemStack transfer = createButton(Material.ENDER_PEARL, "§6Transfer Ownership", "Transfer this claim to another player");
        inv.setItem(24, transfer);
        buttonMap.put(transfer, "transfer");

        ItemStack help = createButton(Material.BOOK, "§eHelp", "View available commands");
        inv.setItem(31, help);
        buttonMap.put(help, "help");

        if (admin) {
            ItemStack adminMenu = createButton(Material.COMPASS, "§cAdmin Menu", "Open admin tools");
            inv.setItem(33, adminMenu);
            buttonMap.put(adminMenu, "admin");
        }

        ItemStack back = createButton(Material.ARROW, "§7Back", "Return to previous menu");
        inv.setItem(48, back);
        buttonMap.put(back, "back");

        // Register all buttons in cache
        GUICache.registerAll(buttonMap);

        player.openInventory(inv);
    }

    // =========================================================
    // ADMIN MENU
    // =========================================================

    public void openAdmin(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, "§cProShield Admin Menu");

        Map<ItemStack, String> buttonMap = new HashMap<>();

        ItemStack fire = createButton(Material.FLINT_AND_STEEL, "§cToggle Fire", "Enable/disable fire spread/burning");
        inv.setItem(10, fire);
        buttonMap.put(fire, "toggle_fire");

        ItemStack explosions = createButton(Material.TNT, "§cToggle Explosions", "Enable/disable explosions in claims");
        inv.setItem(11, explosions);
        buttonMap.put(explosions, "toggle_explosions");

        ItemStack entityGrief = createButton(Material.ENDERMAN_SPAWN_EGG, "§cToggle Entity Grief", "Enable/disable mob griefing");
        inv.setItem(12, entityGrief);
        buttonMap.put(entityGrief, "toggle_entity_grief");

        ItemStack interactions = createButton(Material.LEVER, "§cToggle Interactions", "Control door/button/lever access");
        inv.setItem(13, interactions);
        buttonMap.put(interactions, "toggle_interactions");

        ItemStack pvp = createButton(Material.IRON_SWORD, "§cToggle PvP", "Enable/disable PvP inside claims");
        inv.setItem(14, pvp);
        buttonMap.put(pvp, "toggle_pvp");

        ItemStack keepItems = createButton(Material.CHEST, "§6Toggle Item Keep", "Prevent dropped items in claims from despawning");
        inv.setItem(20, keepItems);
        buttonMap.put(keepItems, "toggle_keepitems");

        ItemStack purge = createButton(Material.LAVA_BUCKET, "§cPurge Expired Claims", "Remove inactive player claims");
        inv.setItem(21, purge);
        buttonMap.put(purge, "purge_expired");

        ItemStack debug = createButton(Material.REDSTONE_TORCH, "§dToggle Debug", "Enable/disable debug logging");
        inv.setItem(23, debug);
        buttonMap.put(debug, "toggle_debug");

        ItemStack reload = createButton(Material.SUNFLOWER, "§eReload Config", "Reload ProShield configuration");
        inv.setItem(25, reload);
        buttonMap.put(reload, "reload");

        ItemStack tpTools = createButton(Material.ENDER_EYE, "§bTeleport Tools", "Teleport to claims");
        inv.setItem(30, tpTools);
        buttonMap.put(tpTools, "tp_tools");

        ItemStack spawnGuard = createButton(Material.BEACON, "§aSpawn Guard", "Toggle spawn protection radius");
        inv.setItem(32, spawnGuard);
        buttonMap.put(spawnGuard, "toggle_spawnguard");

        ItemStack back = createButton(Material.ARROW, "§7Back", "Return to main menu");
        inv.setItem(48, back);
        buttonMap.put(back, "back");

        ItemStack help = createButton(Material.BOOK, "§eHelp", "Admin help commands");
        inv.setItem(49, help);
        buttonMap.put(help, "help");

        GUICache.registerAll(buttonMap);

        player.openInventory(inv);
    }

    // =========================================================
    // UTILS
    // =========================================================

    private ItemStack createButton(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (lore.length > 0) {
                meta.setLore(java.util.Arrays.asList(lore));
            }
            item.setItemMeta(meta);
        }
        return item;
    }
}
