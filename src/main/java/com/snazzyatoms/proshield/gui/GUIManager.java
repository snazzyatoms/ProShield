// src/main/java/com/snazzyatoms/proshield/gui/GUIManager.java
package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.cache.GUICache;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class GUIManager {

    private final ProShield plugin;
    private final GUICache cache;

    public GUIManager(ProShield plugin, GUICache cache) {
        this.plugin = plugin;
        this.cache = cache;
    }

    /* -------------------------------------------------------
     * PLAYER MENU
     * ------------------------------------------------------- */
    public void openPlayerMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.DARK_AQUA + "ProShield Menu");

        inv.setItem(10, new ItemBuilder(Material.GRASS_BLOCK).setName("§aClaim Chunk").setLore("§7Claim your current chunk.").toItemStack());
        inv.setItem(11, new ItemBuilder(Material.BARRIER).setName("§cUnclaim Chunk").setLore("§7Unclaim your current chunk.").toItemStack());
        inv.setItem(12, new ItemBuilder(Material.PAPER).setName("§bClaim Info").setLore("§7View owner & trusted players.").toItemStack());
        inv.setItem(13, new ItemBuilder(Material.PLAYER_HEAD).setName("§eTrust Player").setLore("§7Trust someone in this claim.").toItemStack());
        inv.setItem(14, new ItemBuilder(Material.REDSTONE).setName("§cRoles").setLore("§7Manage claim roles.").toItemStack());
        inv.setItem(15, new ItemBuilder(Material.LEVER).setName("§6Flags").setLore("§7Toggle claim flags.").toItemStack());
        inv.setItem(16, new ItemBuilder(Material.COMPASS).setName("§dPreview").setLore("§7Show chunk border.").toItemStack());
        inv.setItem(22, new ItemBuilder(Material.CHEST).setName("§9Transfer Claim").setLore("§7Give ownership to another player.").toItemStack());

        player.openInventory(inv);
        cache.setOpenMenu(player.getUniqueId(), "player");
    }

    /* -------------------------------------------------------
     * ADMIN MENU
     * ------------------------------------------------------- */
    public void openAdminMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.DARK_RED + "ProShield Admin");

        inv.setItem(10, new ItemBuilder(Material.TNT).setName("§cForce Unclaim").setLore("§7Force-remove a claim.").toItemStack());
        inv.setItem(11, new ItemBuilder(Material.CHEST).setName("§6Transfer Claim").setLore("§7Transfer claim ownership.").toItemStack());
        inv.setItem(12, new ItemBuilder(Material.ENDER_PEARL).setName("§bTeleport to Claim").setLore("§7Teleport directly into claims.").toItemStack());
        inv.setItem(13, new ItemBuilder(Material.LAVA_BUCKET).setName("§cPurge Expired").setLore("§7Remove expired claims.").toItemStack());
        inv.setItem(14, new ItemBuilder(Material.REDSTONE_TORCH).setName("§cReload Config").setLore("§7Reload plugin configuration.").toItemStack());

        // Toggles
        boolean wildernessMsgs = plugin.getConfig().getBoolean("gui.admin.wilderness.toggle-messages", true);
        inv.setItem(15, new ItemBuilder(Material.OAK_SIGN).setName("§aWilderness Messages")
                .setLore("§7Currently: " + (wildernessMsgs ? "§aENABLED" : "§cDISABLED")).toItemStack());

        boolean adminFlagChat = plugin.getConfig().getBoolean("gui.admin.debug.toggle-flag-chat", true);
        inv.setItem(16, new ItemBuilder(Material.BOOK).setName("§eAdmin Flag Chat")
                .setLore("§7Currently: " + (adminFlagChat ? "§aENABLED" : "§cDISABLED")).toItemStack());

        inv.setItem(22, new ItemBuilder(Material.HOPPER).setName("§dKeep Items Toggle")
                .setLore("§7Toggle global keep-items.").toItemStack());

        player.openInventory(inv);
        cache.setOpenMenu(player.getUniqueId(), "admin");
    }

    /* -------------------------------------------------------
     * FLAGS MENU (per claim)
     * ------------------------------------------------------- */
    public void openFlagsMenu(Player player, Plot plot) {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.LIGHT_PURPLE + "Claim Flags");

        inv.setItem(10, makeFlagItem(Material.TNT, "Explosions", plot.getSettings().isExplosionsAllowed()));
        inv.setItem(11, makeFlagItem(Material.BUCKET, "Buckets", plot.getSettings().isBucketAllowed()));
        inv.setItem(12, makeFlagItem(Material.ITEM_FRAME, "Item Frames", plot.getSettings().isItemFramesAllowed()));
        inv.setItem(13, makeFlagItem(Material.ARMOR_STAND, "Armor Stands", plot.getSettings().isArmorStandsAllowed()));
        inv.setItem(14, makeFlagItem(Material.COW_SPAWN_EGG, "Animals", plot.getSettings().isAnimalAccessAllowed()));
        inv.setItem(15, makeFlagItem(Material.BONE, "Pets", plot.getSettings().isPetAccessAllowed()));
        inv.setItem(16, makeFlagItem(Material.CHEST, "Containers", plot.getSettings().isContainersAllowed()));
        inv.setItem(19, makeFlagItem(Material.MINECART, "Vehicles", plot.getSettings().isVehiclesAllowed()));
        inv.setItem(20, makeFlagItem(Material.FLINT_AND_STEEL, "Fire", plot.getSettings().isFireAllowed()));
        inv.setItem(21, makeFlagItem(Material.REDSTONE, "Redstone", plot.getSettings().isRedstoneAllowed()));
        inv.setItem(22, makeFlagItem(Material.CREEPER_HEAD, "Entity Griefing", plot.getSettings().isEntityGriefingAllowed()));
        inv.setItem(23, makeFlagItem(Material.IRON_SWORD, "PvP", plot.getSettings().isPvpEnabled()));
        inv.setItem(24, makeFlagItem(Material.SHIELD, "Mob Repel", plot.getSettings().isMobRepelEnabled()));
        inv.setItem(25, makeFlagItem(Material.BARRIER, "Mob Despawn", plot.getSettings().isMobDespawnInsideEnabled()));

        player.openInventory(inv);
        cache.setOpenMenu(player.getUniqueId(), "flags");
    }

    private ItemStack makeFlagItem(Material mat, String name, boolean enabled) {
        return new ItemBuilder(mat)
                .setName("§e" + name)
                .setLore("§7Now: " + (enabled ? "§aENABLED" : "§cDISABLED"))
                .toItemStack();
    }
}
