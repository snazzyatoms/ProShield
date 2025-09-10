package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.cache.GUICache;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class GUIManager {

    private final ProShield plugin;
    private final GUICache cache;

    public GUIManager(ProShield plugin, GUICache cache) {
        this.plugin = plugin;
        this.cache = cache;
    }

    // ==========================
    // COMPASS HANDLING
    // ==========================

    public void giveCompass(Player player, boolean admin) {
        ItemStack compass = admin ? cache.getAdminCompass() : cache.getPlayerCompass();
        if (compass == null) return;

        // Place in inventory if free slot exists, otherwise drop
        if (player.getInventory().firstEmpty() != -1) {
            player.getInventory().addItem(compass.clone());
        } else {
            player.getWorld().dropItemNaturally(player.getLocation(), compass.clone());
        }
    }

    public boolean isProShieldCompass(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        String display = ChatColor.stripColor(item.getItemMeta().getDisplayName());
        return display != null && (display.contains("ProShield") || display.contains("Admin ProShield"));
    }

    // ==========================
    // MAIN MENU
    // ==========================

    public void openMain(Player player, boolean admin) {
        Inventory inv = Bukkit.createInventory(null, 54, ChatColor.DARK_AQUA + "ProShield Menu");

        if (admin) {
            setupAdminGUI(inv);
        } else {
            setupPlayerGUI(inv);
        }

        player.openInventory(inv);
    }

    private void setupPlayerGUI(Inventory inv) {
        inv.setItem(11, cache.buildItem(Material.GRASS_BLOCK, "&aClaim Chunk", "&7Protect the current chunk."));
        inv.setItem(13, cache.buildItem(Material.BOOK, "&eClaim Info", "&7View owner and trusted players."));
        inv.setItem(15, cache.buildItem(Material.BARRIER, "&cUnclaim", "&7Release this claim."));

        // New: trust/untrust/roles
        inv.setItem(19, cache.buildItem(Material.PLAYER_HEAD, "&bTrust Player", "&7Grant a player access to your claim."));
        inv.setItem(20, cache.buildItem(Material.SKELETON_SKULL, "&cUntrust Player", "&7Remove a player’s access."));
        inv.setItem(21, cache.buildItem(Material.PAPER, "&eManage Roles", "&7Assign Visitor, Member, Builder, etc."));

        // New: claim transfer & flags
        inv.setItem(23, cache.buildItem(Material.CHEST, "&6Transfer Claim", "&7Give this claim to another player."));
        inv.setItem(24, cache.buildItem(Material.REDSTONE_TORCH, "&cClaim Flags", "&7Toggle PvP, explosions, fire, etc."));

        inv.setItem(31, cache.buildItem(Material.OAK_SIGN, "&9Help", "&7Show available commands."));

        // Back button (always functional)
        inv.setItem(48, cache.buildItem(Material.ARROW, "&7Back", "&7Return to previous menu."));
    }

    private void setupAdminGUI(Inventory inv) {
        inv.setItem(10, cache.buildItem(Material.FLINT_AND_STEEL, "&cFire Toggle", "&7Toggle fire spread and ignition."));
        inv.setItem(11, cache.buildItem(Material.TNT, "&cExplosion Toggle", "&7Toggle TNT, creepers, withers, etc."));
        inv.setItem(12, cache.buildItem(Material.ENDER_PEARL, "&cEntity Grief", "&7Toggle Endermen, ravagers, silverfish."));
        inv.setItem(13, cache.buildItem(Material.LEVER, "&eInteractions", "&7Toggle door/button/fence gate use."));
        inv.setItem(14, cache.buildItem(Material.IRON_SWORD, "&4PvP Toggle", "&7Enable or disable PvP in claims."));

        inv.setItem(20, cache.buildItem(Material.HOPPER, "&aKeep Items", "&7Toggle item keep inside claims."));
        inv.setItem(21, cache.buildItem(Material.LAVA_BUCKET, "&cPurge Expired", "&7Remove old claims."));
        inv.setItem(22, cache.buildItem(Material.BOOK, "&bHelp", "&7Show admin commands."));
        inv.setItem(23, cache.buildItem(Material.COMMAND_BLOCK, "&dDebug Mode", "&7Toggle debug logging."));
        inv.setItem(24, cache.buildItem(Material.CHEST_MINECART, "&6Compass Drop Policy", "&7Toggle compass drop if full."));

        // Reload added here
        inv.setItem(25, cache.buildItem(Material.REDSTONE_BLOCK, "&4Reload Config", "&7Reloads ProShield configuration."));

        inv.setItem(30, cache.buildItem(Material.ENDER_EYE, "&dTP Tools", "&7Teleport to claims."));
        inv.setItem(31, cache.buildItem(Material.ARROW, "&7Back", "&7Return to previous menu."));
    }

    // ==========================
    // UTILS
    // ==========================

    public ItemStack buildItem(Material mat, String name, String... lore) {
        return cache.buildItem(mat, name, lore);
    }

}
