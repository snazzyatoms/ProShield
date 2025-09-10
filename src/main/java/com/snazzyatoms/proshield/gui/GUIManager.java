package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.cache.GUICache;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import static com.snazzyatoms.proshield.cache.GUICache.createInventory;
import static com.snazzyatoms.proshield.cache.GUICache.createItem;
import org.bukkit.Material;

public class GUIManager {

    private final ProShield plugin;
    private final GUICache cache;

    public GUIManager(ProShield plugin, GUICache cache) {
        this.plugin = plugin;
        this.cache = cache;
    }

    // ==========================
    // OPEN MAIN MENU
    // ==========================
    public void openMain(Player player, boolean admin) {
        if (admin) {
            openAdminMenu(player);
        } else {
            openPlayerMenu(player);
        }
    }

    // ==========================
    // PLAYER MENU
    // ==========================
    public void openPlayerMenu(Player player) {
        Inventory inv = cache.getGUI("playerMain");
        if (inv == null) {
            inv = createInventory("&bProShield Menu", 54);

            inv.setItem(11, createItem(Material.GRASS_BLOCK, "&aClaim Chunk", "&7Claim your current chunk."));
            inv.setItem(13, createItem(Material.BOOK, "&bClaim Info", "&7View claim owner & trusted players."));
            inv.setItem(15, createItem(Material.BARRIER, "&cUnclaim", "&7Remove your claim."));

            inv.setItem(20, createItem(Material.PLAYER_HEAD, "&eTrust Player", "&7Add a player to your claim."));
            inv.setItem(21, createItem(Material.SKELETON_SKULL, "&cUntrust Player", "&7Remove a player from your claim."));
            inv.setItem(22, createItem(Material.PAPER, "&dManage Roles", "&7Assign roles (Visitor, Member, Builder, etc.)."));
            inv.setItem(23, createItem(Material.REDSTONE_TORCH, "&6Claim Flags", "&7Toggle protections (PvP, explosions, etc.)."));
            inv.setItem(24, createItem(Material.CHEST, "&bTransfer Ownership", "&7Give your claim to another player."));

            inv.setItem(31, createItem(Material.OAK_SIGN, "&aHelp", "&7List of available commands."));
            inv.setItem(48, createItem(Material.ARROW, "&fBack", "&7Return to main menu."));

            cache.storeGUI("playerMain", inv);
        }
        player.openInventory(inv);
    }

    // ==========================
    // ADMIN MENU
    // ==========================
    public void openAdminMenu(Player player) {
        Inventory inv = cache.getGUI("adminMain");
        if (inv == null) {
            inv = createInventory("&cProShield Admin Menu", 54);

            inv.setItem(10, createItem(Material.FLINT_AND_STEEL, "&cFire Protection", "&7Toggle fire spread/ignite."));
            inv.setItem(11, createItem(Material.TNT, "&cExplosion Protection", "&7Toggle TNT/creeper/etc."));
            inv.setItem(12, createItem(Material.ENDER_PEARL, "&cEntity Grief", "&7Toggle Enderman, Ravagers, etc."));
            inv.setItem(13, createItem(Material.LEVER, "&cInteractions", "&7Toggle redstone, doors, buttons."));
            inv.setItem(14, createItem(Material.IRON_SWORD, "&cPvP Toggle", "&7Enable or disable PvP in claims."));

            inv.setItem(20, createItem(Material.HOPPER, "&aKeep Items", "&7Toggle item keep in claims."));
            inv.setItem(21, createItem(Material.LAVA_BUCKET, "&cPurge Expired", "&7Remove expired claims."));
            inv.setItem(22, createItem(Material.BOOK, "&bAdmin Help", "&7List admin commands."));
            inv.setItem(23, createItem(Material.COMMAND_BLOCK, "&6Debug", "&7Toggle debug mode."));
            inv.setItem(24, createItem(Material.CHEST_MINECART, "&eCompass Policy", "&7Adjust compass settings."));
            inv.setItem(25, createItem(Material.REDSTONE_BLOCK, "&cReload Config", "&7Reload plugin configuration."));

            inv.setItem(30, createItem(Material.ENDER_EYE, "&dTeleport Tools", "&7Teleport to claims."));
            inv.setItem(31, createItem(Material.ARROW, "&fBack", "&7Return to main menu."));

            cache.storeGUI("adminMain", inv);
        }
        player.openInventory(inv);
    }
}
