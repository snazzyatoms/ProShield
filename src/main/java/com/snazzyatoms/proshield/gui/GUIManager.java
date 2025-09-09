package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.PlotManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.ShapedRecipe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Centralized GUI builder + small utilities (compass recipe, help page, etc).
 * This class intentionally keeps backwards-compat signatures that other classes call:
 * - public static final String TITLE
 * - GUIManager(ProShield, PlotManager) ctor
 * - GUIManager(PlotManager) ctor (compat)
 * - registerCompassRecipe()
 * - onConfigReload()
 * - createAdminCompass()
 * - giveCompass(Player, boolean)
 * - openHelp(Player)
 */
public class GUIManager implements InventoryHolder {

    /** Backwards-compat title used by GUIListener */
    public static final String TITLE = ChatColor.DARK_AQUA + "ProShield";

    private final PlotManager plots;
    private final ProShield plugin; // may be null when constructed via legacy ctor

    // Cached slots from config (with sane defaults)
    private int slotCreate = 11;
    private int slotInfo   = 13;
    private int slotRemove = 15;
    private int slotAdmin  = 33;
    private int slotHelp   = 49;
    private int slotBack   = 48;

    private int adminSlotToggleDropIfFull = 20;
    private int adminSlotHelp             = 22;
    private int adminSlotBack             = 31;

    // Main/Admin/Help inventories are rebuilt fresh when opened
    public GUIManager(ProShield plugin, PlotManager plots) {
        this.plugin = plugin;
        this.plots  = plots;
        readSlotsFromConfig();
    }

    /** Legacy compat ctor — plugin is null; features requiring it are no-ops. */
    public GUIManager(PlotManager plots) {
        this.plugin = null;
        this.plots  = plots;
        readSlotsFromConfig();
    }

    /** GUI holder requirement; we don’t persist a single inventory here. */
    @Override
    public Inventory getInventory() {
        return Bukkit.createInventory(this, 54, TITLE);
    }

    /* -------------------------------------------------
     * Public API used by other classes
     * ------------------------------------------------- */

    /** Called by ProShield.onEnable() after managers are created. */
    public void registerCompassRecipe() {
        if (plugin == null) return; // legacy-safe
        ItemStack compass = createAdminCompass();

        NamespacedKey key = new NamespacedKey(plugin, "proshield_admin_compass");
        // Remove old recipe form to avoid duplicates
        Bukkit.removeRecipe(key);

        ShapedRecipe recipe = new ShapedRecipe(key, compass);
        recipe.shape("IRI", "RCR", "IRI");
        recipe.setIngredient('I', Material.IRON_INGOT);
        recipe.setIngredient('R', Material.REDSTONE);
        recipe.setIngredient('C', Material.COMPASS);
        Bukkit.addRecipe(recipe);
    }

    /** Called by /proshield reload and onEnable to refresh mapped slots & any toggles. */
    public void onConfigReload() {
        readSlotsFromConfig();
    }

    /** Used by PlayerJoinListener & commands to create the special compass item. */
    public static ItemStack createAdminCompass() {
        ItemStack it = new ItemStack(Material.COMPASS);
        ItemMeta meta = it.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.AQUA + "ProShield Compass");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Right-click to open the ProShield menu");
            lore.add(ChatColor.DARK_GRAY + "Admin/owners may receive this on join");
            meta.setLore(lore);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            it.setItemMeta(meta);
        }
        return it;
    }

    /**
     * Give the ProShield compass if there’s room; otherwise drop (if configured).
     * @param player target player
     * @param dropIfFull whether to drop at feet when inventory is full
     */
    public static void giveCompass(Player player, boolean dropIfFull) {
        ItemStack compass = createAdminCompass();
        // If they already have one that matches our display name, don’t spam
        for (ItemStack s : player.getInventory().getContents()) {
            if (s == null || s.getType() != Material.COMPASS) continue;
            ItemMeta m = s.getItemMeta();
            if (m != null && ChatColor.stripColor(Objects.toString(m.getDisplayName(), ""))
                    .equalsIgnoreCase("ProShield Compass")) {
                return; // already has one
            }
        }
        // Try add
        var left = player.getInventory().addItem(compass);
        if (!left.isEmpty() && dropIfFull) {
            player.getWorld().dropItemNaturally(player.getLocation(), compass);
        }
    }

    /** Opens the main menu (simple 6x9). */
    public void openMain(Player p) {
        Inventory inv = Bukkit.createInventory(this, 54, TITLE);
        inv.setItem(slotCreate, button(Material.EMERALD_BLOCK, ChatColor.GREEN + "Claim Chunk",
                List.of(ChatColor.GRAY + "Claim the chunk you are standing in.")));
        inv.setItem(slotInfo, button(Material.PAPER, ChatColor.AQUA + "Claim Info",
                List.of(ChatColor.GRAY + "View owner and trusted players.")));
        inv.setItem(slotRemove, button(Material.REDSTONE_BLOCK, ChatColor.RED + "Unclaim Chunk",
                List.of(ChatColor.GRAY + "Remove your claim from this chunk.")));
        inv.setItem(slotHelp, button(Material.BOOK, ChatColor.GOLD + "Help",
                List.of(ChatColor.GRAY + "Show commands you can use right now.")));
        inv.setItem(slotAdmin, button(Material.NETHER_STAR, ChatColor.LIGHT_PURPLE + "Admin",
                List.of(ChatColor.GRAY + "Admin tools & settings (permission required).")));
        inv.setItem(slotBack, button(Material.ARROW, ChatColor.YELLOW + "Back",
                List.of(ChatColor.GRAY + "Return to the previous screen.")));
        p.openInventory(inv);
    }

    /** Opens the admin menu. */
    public void openAdmin(Player p) {
        Inventory inv = Bukkit.createInventory(this, 54, ChatColor.DARK_PURPLE + "ProShield — Admin");
        boolean dropIfFull = true;
        if (plugin != null) {
            dropIfFull = plugin.getConfig().getBoolean("compass.drop-if-full", true);
        }

        inv.setItem(adminSlotToggleDropIfFull, button(
                dropIfFull ? Material.LIME_DYE : Material.GRAY_DYE,
                ChatColor.AQUA + "Compass: Drop if Full " + ChatColor.DARK_GRAY + "(" + (dropIfFull ? "ON" : "OFF") + ")",
                List.of(
                        ChatColor.GRAY + "If player inventory is full when granting a compass,",
                        ChatColor.GRAY + "drop it at their feet.",
                        ChatColor.DARK_GRAY + "Click to toggle."
                )
        ));

        inv.setItem(adminSlotHelp, button(
                Material.BOOK,
                ChatColor.GOLD + "Admin Help",
                List.of(
                        ChatColor.YELLOW + "Tips:",
                        ChatColor.GRAY + "- Use /proshield purgeexpired <days> [dryrun] to preview cleanup",
                        ChatColor.GRAY + "- Use bypass to test protections quickly",
                        ChatColor.DARK_GRAY + "More tools coming in 2.0..."
                )
        ));

        inv.setItem(adminSlotBack, button(Material.ARROW, ChatColor.YELLOW + "Back",
                List.of(ChatColor.GRAY + "Return to the main menu.")));

        p.openInventory(inv);
    }

    /** Opens a filtered help page showing only commands the player can use. */
    public void openHelp(Player p) {
        Inventory inv = Bukkit.createInventory(this, 54, ChatColor.DARK_GREEN + "ProShield — Help");
        List<ItemStack> entries = new ArrayList<>();

        // Basic
        entries.add(helpLine(Material.MAP, "/proshield", "Main help & menu", true));
        entries.add(helpLine(Material.GRASS_BLOCK, "/proshield claim", "Claim your current chunk", p.hasPermission("proshield.use")));
        entries.add(helpLine(Material.BARRIER, "/proshield unclaim", "Unclaim your current chunk", p.hasPermission("proshield.use")));
        entries.add(helpLine(Material.PAPER, "/proshield info", "View claim details", p.hasPermission("proshield.use")));
        entries.add(helpLine(Material.NAME_TAG, "/proshield trust <player> [role]", "Trust a player with optional role", p.hasPermission("proshield.use")));
        entries.add(helpLine(Material.NAME_TAG, "/proshield untrust <player>", "Remove trust", p.hasPermission("proshield.use")));
        entries.add(helpLine(Material.WRITABLE_BOOK, "/proshield trusted", "List trusted players", p.hasPermission("proshield.use")));
        entries.add(helpLine(Material.COMPASS, "/proshield compass", "Get the ProShield compass", p.hasPermission("proshield.compass") || p.isOp()));

        // Admin
        boolean isAdmin = p.hasPermission("proshield.admin") || p.isOp();
        entries.add(helpLine(Material.LEVER, "/proshield bypass <on|off|toggle>", "Toggle admin bypass", isAdmin));
        entries.add(helpLine(Material.REDSTONE, "/proshield purgeexpired <days> [dryrun]", "Purge/preview old claims", isAdmin));
        entries.add(helpLine(Material.REPEATER, "/proshield reload", "Reload configuration", p.hasPermission("proshield.admin.reload") || p.isOp()));

        // Lay them out
        int idx = 0;
        for (ItemStack it : entries) {
            if (it == null) continue;
            inv.setItem(10 + (idx % 7) + (9 * (idx / 7)), it); // grid fill
            idx++;
        }

        // back button
        inv.setItem(48, button(Material.ARROW, ChatColor.YELLOW + "Back",
                List.of(ChatColor.GRAY + "Return to the main menu.")));

        p.openInventory(inv);
    }

    /* -------------------------------------------------
     * Helpers
     * ------------------------------------------------- */

    private void readSlotsFromConfig() {
        if (plugin == null) return; // legacy-safe: keep defaults
        var cfg = plugin.getConfig();

        // main
        slotCreate = cfg.getInt("gui.slots.main.create", slotCreate);
        slotInfo   = cfg.getInt("gui.slots.main.info", slotInfo);
        slotRemove = cfg.getInt("gui.slots.main.remove", slotRemove);
        slotAdmin  = cfg.getInt("gui.slots.main.admin", slotAdmin);
        slotHelp   = cfg.getInt("gui.slots.main.help", slotHelp);
        slotBack   = cfg.getInt("gui.slots.main.back", slotBack);

        // admin
        adminSlotToggleDropIfFull = cfg.getInt("gui.slots.admin.toggle-drop-if-full", adminSlotToggleDropIfFull);
        adminSlotHelp             = cfg.getInt("gui.slots.admin.help", adminSlotHelp);
        adminSlotBack             = cfg.getInt("gui.slots.admin.back", adminSlotBack);
    }

    private static ItemStack button(Material mat, String title, List<String> lore) {
        ItemStack it = new ItemStack(mat);
        ItemMeta meta = it.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(title);
            meta.setLore(lore == null ? Collections.emptyList() : lore);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            it.setItemMeta(meta);
        }
        return it;
    }

    private static ItemStack helpLine(Material mat, String cmd, String desc, boolean allowed) {
        if (!allowed) return null;
        return button(mat, ChatColor.AQUA + cmd, List.of(ChatColor.GRAY + desc));
    }
}
