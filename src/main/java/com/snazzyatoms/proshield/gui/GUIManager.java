package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.PlotManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.Collections;

public class GUIManager {

    private final ProShield plugin;
    private final PlotManager plots;

    // Titles
    public static final String TITLE_MAIN  = ChatColor.AQUA + "ProShield";
    public static final String TITLE_ADMIN = ChatColor.RED + "ProShield Admin";
    public static final String TITLE_HELP  = ChatColor.GOLD + "ProShield Help";

    // Main slots (configurable)
    private int slotMainCreate   = 11;
    private int slotMainInfo     = 13;
    private int slotMainRemove   = 15;
    private int slotMainTrust    = 21;
    private int slotMainUntrust  = 22;
    private int slotMainTrusted  = 23;
    private int slotMainRoles    = 24;
    private int slotMainTransfer = 25;
    private int slotMainPreview  = 29;
    private int slotMainKeep     = 30;
    private int slotMainHelp     = 49;
    private int slotMainBack     = 48;
    private int slotMainAdmin    = 33;

    // Admin slots (configurable)
    private int slotAdminHelp    = 22;
    private int slotAdminBack    = 31;

    // Compass display names
    private final String playerCompassName = ChatColor.AQUA + "ProShield Compass";
    private final String adminCompassName  = ChatColor.RED + "ProShield Admin Compass";

    public GUIManager(ProShield plugin, PlotManager plots) {
        this.plugin = plugin;
        this.plots = plots;
        readSlotsFromConfig();
    }

    /* -------------------- Public accessors -------------------- */
    public String getPlayerCompassName() { return playerCompassName; }
    public String getAdminCompassName()  { return adminCompassName; }

    /* -------------------- Compass creation -------------------- */

    public ItemStack createPlayerCompass() {
        ItemStack it = new ItemStack(Material.COMPASS);
        ItemMeta meta = it.getItemMeta();
        meta.setDisplayName(playerCompassName);
        meta.setLore(Arrays.asList(
                ChatColor.GRAY + "Right-click to manage claims",
                ChatColor.DARK_GRAY + "(Claim / Trust / Roles / Help)"
        ));
        it.setItemMeta(meta);
        return it;
    }

    public ItemStack createAdminCompass() {
        ItemStack it = new ItemStack(Material.COMPASS);
        ItemMeta meta = it.getItemMeta();
        meta.setDisplayName(adminCompassName);
        meta.setLore(Arrays.asList(
                ChatColor.GRAY + "Right-click for admin tools",
                ChatColor.DARK_GRAY + "(Toggles / Expiry / Teleport / Stats)"
        ));
        it.setItemMeta(meta);
        return it;
    }

    /** Optional crafting recipe for the player compass. Call once in onEnable(). */
    public void registerCompassRecipe() {
        try {
            NamespacedKey key = new NamespacedKey(plugin, "proshield_compass");
            if (Bukkit.getRecipe(key) != null) return;

            ShapedRecipe recipe = new ShapedRecipe(key, createPlayerCompass());
            recipe.shape("IRI", "RCR", "IRI");
            recipe.setIngredient('I', Material.IRON_INGOT);
            recipe.setIngredient('R', Material.REDSTONE);
            recipe.setIngredient('C', Material.COMPASS);
            Bukkit.addRecipe(recipe);
        } catch (Throwable ignored) { }
    }

    /* -------------------- GUIs -------------------- */

    public void openMainGUI(Player p) {
        final boolean isOwnerHere = plots.isOwner(p.getUniqueId(), p.getLocation());

        Inventory inv = Bukkit.createInventory(p, 54, TITLE_MAIN);

        inv.setItem(slotMainCreate, named(Material.OAK_DOOR, ChatColor.GREEN + "Claim Chunk",
                ChatColor.GRAY + "Claim the current chunk."));
        inv.setItem(slotMainInfo, named(Material.PAPER, ChatColor.AQUA + "Claim Info",
                ChatColor.GRAY + "Owner, trusted, roles."));
        inv.setItem(slotMainRemove, named(Material.BARRIER, ChatColor.RED + "Unclaim",
                ChatColor.GRAY + "Remove your claim. " + ownerOnly(isOwnerHere)));

        inv.setItem(slotMainTrust, named(Material.PLAYER_HEAD, ChatColor.GREEN + "Trust Nearby",
                ChatColor.GRAY + "Quickly trust players within radius.",
                ownerOnly(isOwnerHere)));
        inv.setItem(slotMainUntrust, named(Material.SHEARS, ChatColor.RED + "Untrust Player",
                ChatColor.GRAY + "Revoke a player's access.",
                ownerOnly(isOwnerHere)));
        inv.setItem(slotMainTrusted, named(Material.BOOK, ChatColor.GOLD + "View Trusted",
                ChatColor.GRAY + "List trusted players on this claim."));

        inv.setItem(slotMainRoles, named(Material.IRON_HOE, ChatColor.AQUA + "Manage Roles",
                ChatColor.GRAY + "Assign Visitor/Member/Container/Builder/Co-Owner.",
                ownerOnly(isOwnerHere)));

        inv.setItem(slotMainTransfer, named(Material.NAME_TAG, ChatColor.YELLOW + "Transfer Ownership",
                ChatColor.GRAY + "Give this claim to another player.",
                ownerOnly(isOwnerHere)));

        inv.setItem(slotMainPreview, named(Material.MAP, ChatColor.AQUA + "Borders Preview",
                ChatColor.GRAY + "Show a temporary outline of the claim."));

        inv.setItem(slotMainKeep, named(Material.CHEST, ChatColor.GOLD + "Keep Items",
                ChatColor.GRAY + "Toggle dropped-item protection for this claim.",
                ownerOnly(isOwnerHere)));

        inv.setItem(slotMainHelp, named(Material.BOOK, ChatColor.GOLD + "Help",
                ChatColor.GRAY + "Shows commands available to you."));
        inv.setItem(slotMainAdmin, named(Material.REDSTONE_TORCH, ChatColor.RED + "Admin Tools",
                ChatColor.GRAY + "Open the admin menu (permission required)."));
        inv.setItem(slotMainBack, named(Material.ARROW, ChatColor.YELLOW + "Back",
                ChatColor.GRAY + "Go back to previous menu."));

        p.openInventory(inv);
    }

    public void openAdminGUI(Player p) {
        if (!(p.isOp() || p.hasPermission("proshield.admin") || p.hasPermission("proshield.admin.gui"))) {
            p.sendMessage(plugin.msg("&cYou don't have permission to open Admin Tools."));
            return;
        }

        Inventory inv = Bukkit.createInventory(p, 45, TITLE_ADMIN);

        inv.setItem(10, named(Material.BEACON, ChatColor.AQUA + "Global Toggles",
                ChatColor.GRAY + "Fire / Explosions / Interactions / Mob grief / PvP"));
        inv.setItem(11, named(Material.CLOCK, ChatColor.YELLOW + "Claim Expiry",
                ChatColor.GRAY + "Run purge preview or commit"));
        inv.setItem(12, named(Material.ENDER_PEARL, ChatColor.GREEN + "Teleport Tools",
                ChatColor.GRAY + "Jump to claims (admin.tp)"));
        inv.setItem(13, named(Material.CHEST, ChatColor.GOLD + "Item Keep / Drops",
                ChatColor.GRAY + "Toggle keep-items & pickup rules"));
        inv.setItem(14, named(Material.MAP, ChatColor.AQUA + "Borders Preview",
                ChatColor.GRAY + "Show temporary claim borders"));
        inv.setItem(19, named(Material.PAPER, ChatColor.BLUE + "Messages",
                ChatColor.GRAY + "Entry / Exit messages"));
        inv.setItem(20, named(Material.BOOK, ChatColor.GOLD + "Admin Help",
                ChatColor.GRAY + "Short guide & tips"));
        inv.setItem(slotAdminHelp, named(Material.WRITABLE_BOOK, ChatColor.GOLD + "Help",
                ChatColor.GRAY + "View admin command reference"));
        inv.setItem(slotAdminBack, named(Material.ARROW, ChatColor.YELLOW + "Back",
                ChatColor.GRAY + "Return to main menu"));

        // teaser for 2.0
        inv.setItem(24, named(Material.NETHER_STAR, ChatColor.LIGHT_PURPLE + "Towns (2.0 Teaser)",
                ChatColor.GRAY + "Stay tuned…"));

        p.openInventory(inv);
    }

    public void openHelp(Player p) {
        Inventory inv = Bukkit.createInventory(p, 54, TITLE_HELP);
        inv.setItem(22, named(Material.BOOK, ChatColor.GOLD + "Your Commands",
                ChatColor.GRAY + "This page shows commands you can actually use."));
        inv.setItem(49, named(Material.ARROW, ChatColor.YELLOW + "Back",
                ChatColor.GRAY + "Return to previous menu"));
        p.openInventory(inv);
    }

    /* -------------------- Config reload support -------------------- */

    public void onConfigReload() {
        readSlotsFromConfig();
    }

    private void readSlotsFromConfig() {
        // main
        slotMainCreate   = getInt("gui.slots.main.create", 11);
        slotMainInfo     = getInt("gui.slots.main.info", 13);
        slotMainRemove   = getInt("gui.slots.main.remove", 15);
        slotMainTrust    = getInt("gui.slots.main.trust", 21);
        slotMainUntrust  = getInt("gui.slots.main.untrust", 22);
        slotMainTrusted  = getInt("gui.slots.main.trusted", 23);
        slotMainRoles    = getInt("gui.slots.main.roles", 24);
        slotMainTransfer = getInt("gui.slots.main.transfer", 25);
        slotMainPreview  = getInt("gui.slots.main.preview", 29);
        slotMainKeep     = getInt("gui.slots.main.keep", 30);
        slotMainHelp     = getInt("gui.slots.main.help", 49);
        slotMainBack     = getInt("gui.slots.main.back", 48);
        slotMainAdmin    = getInt("gui.slots.main.admin", 33);
        // admin
        slotAdminHelp    = getInt("gui.slots.admin.help", 22);
        slotAdminBack    = getInt("gui.slots.admin.back", 31);
    }

    private int getInt(String path, int def) {
        return plugin.getConfig().getInt(path, def);
    }

    /* -------------------- Items -------------------- */

    private ItemStack named(Material mat, String name, String... loreLines) {
        ItemStack it = new ItemStack(mat);
        ItemMeta meta = it.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(loreLines == null || loreLines.length == 0
                ? Collections.emptyList()
                : Arrays.asList(loreLines));
        it.setItemMeta(meta);
        return it;
    }

    private String ownerOnly(boolean isOwnerHere) {
        return isOwnerHere ? "" : ChatColor.DARK_GRAY + "(Owner-only here)";
    }
}
