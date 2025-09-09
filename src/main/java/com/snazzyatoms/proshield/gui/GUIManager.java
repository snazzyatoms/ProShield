package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.PlotManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class GUIManager {

    private final ProShield plugin;
    private final PlotManager plots;

    // Titles
    public static final String TITLE_MAIN  = ChatColor.AQUA + "ProShield";
    public static final String TITLE_ADMIN = ChatColor.DARK_AQUA + "ProShield • Admin";
    public static final String TITLE_HELP  = ChatColor.GREEN + "ProShield • Help";
    public static final String TITLE_TRUST = ChatColor.GOLD + "ProShield • Trust";
    public static final String TITLE_TRANSFER = ChatColor.GOLD + "ProShield • Transfer";
    public static final String TITLE_ROLES = ChatColor.GOLD + "ProShield • Roles";
    public static final String TITLE_PREVIEW = ChatColor.LIGHT_PURPLE + "ProShield • Preview";

    // Slots (read from config, but keep defaults)
    private int slotMainCreate    = 11;
    private int slotMainInfo      = 13;
    private int slotMainRemove    = 15;
    private int slotMainAdmin     = 33;
    private int slotMainHelp      = 49;
    private int slotMainBack      = 48;

    private int slotMainTrust     = 20;
    private int slotMainRoles     = 21;
    private int slotMainTransfer  = 22;
    private int slotMainPreview   = 23;
    private int slotMainSettings  = 24;

    // Admin slots
    private int slotAdminExplosions  = 10;
    private int slotAdminFire        = 12;
    private int slotAdminEntityGrief = 14;
    private int slotAdminPvp         = 16;
    private int slotAdminKeepItems   = 28;
    private int slotAdminDropIfFull  = 20;
    private int slotAdminHelp        = 22;
    private int slotAdminBack        = 31;

    public GUIManager(ProShield plugin, PlotManager plots) {
        this.plugin = plugin;
        this.plots = plots;
        loadSlots();
        registerCompassRecipe(); // keep recipe handy
    }

    public void onConfigReload() {
        loadSlots();
    }

    private void loadSlots() {
        var cfg = plugin.getConfig();

        // main
        slotMainCreate   = cfg.getInt("gui.slots.main.create", slotMainCreate);
        slotMainInfo     = cfg.getInt("gui.slots.main.info", slotMainInfo);
        slotMainRemove   = cfg.getInt("gui.slots.main.remove", slotMainRemove);
        slotMainAdmin    = cfg.getInt("gui.slots.main.admin", slotMainAdmin);
        slotMainHelp     = cfg.getInt("gui.slots.main.help", slotMainHelp);
        slotMainBack     = cfg.getInt("gui.slots.main.back", slotMainBack);

        slotMainTrust    = cfg.getInt("gui.slots.main.trust", slotMainTrust);
        slotMainRoles    = cfg.getInt("gui.slots.main.roles", slotMainRoles);
        slotMainTransfer = cfg.getInt("gui.slots.main.transfer", slotMainTransfer);
        slotMainPreview  = cfg.getInt("gui.slots.main.preview", slotMainPreview);
        slotMainSettings = cfg.getInt("gui.slots.main.settings", slotMainSettings);

        // admin
        slotAdminExplosions  = cfg.getInt("gui.slots.admin.explosions", slotAdminExplosions);
        slotAdminFire        = cfg.getInt("gui.slots.admin.fire", slotAdminFire);
        slotAdminEntityGrief = cfg.getInt("gui.slots.admin.entity-grief", slotAdminEntityGrief);
        slotAdminPvp         = cfg.getInt("gui.slots.admin.pvp", slotAdminPvp);
        slotAdminKeepItems   = cfg.getInt("gui.slots.admin.keep-items", slotAdminKeepItems);
        slotAdminDropIfFull  = cfg.getInt("gui.slots.admin.toggle-drop-if-full", slotAdminDropIfFull);
        slotAdminHelp        = cfg.getInt("gui.slots.admin.help", slotAdminHelp);
        slotAdminBack        = cfg.getInt("gui.slots.admin.back", slotAdminBack);

        // sanity boundaries (avoid OOB)
        slotMainCreate = clampSlot(slotMainCreate);
        slotMainInfo = clampSlot(slotMainInfo);
        slotMainRemove = clampSlot(slotMainRemove);
        slotMainAdmin = clampSlot(slotMainAdmin);
        slotMainHelp = clampSlot(slotMainHelp);
        slotMainBack = clampSlot(slotMainBack);
        slotMainTrust = clampSlot(slotMainTrust);
        slotMainRoles = clampSlot(slotMainRoles);
        slotMainTransfer = clampSlot(slotMainTransfer);
        slotMainPreview = clampSlot(slotMainPreview);
        slotMainSettings = clampSlot(slotMainSettings);

        slotAdminExplosions = clampSlot(slotAdminExplosions);
        slotAdminFire = clampSlot(slotAdminFire);
        slotAdminEntityGrief = clampSlot(slotAdminEntityGrief);
        slotAdminPvp = clampSlot(slotAdminPvp);
        slotAdminKeepItems = clampSlot(slotAdminKeepItems);
        slotAdminDropIfFull = clampSlot(slotAdminDropIfFull);
        slotAdminHelp = clampSlot(slotAdminHelp);
        slotAdminBack = clampSlot(slotAdminBack);
    }

    private int clampSlot(int s) {
        return Math.max(0, Math.min(53, s));
    }

    /* -----------------------------
     *  Compasses
     * ----------------------------- */
    public ItemStack createPlayerCompass() {
        ItemStack it = new ItemStack(Material.COMPASS);
        ItemMeta meta = it.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + "ProShield Compass");
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Right-click to manage your claim.");
        meta.setLore(lore);
        it.setItemMeta(meta);
        return it;
    }

    public ItemStack createAdminCompass() {
        ItemStack it = new ItemStack(Material.RECOVERY_COMPASS);
        ItemMeta meta = it.getItemMeta();
        meta.setDisplayName(ChatColor.DARK_AQUA + "ProShield Admin Compass");
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Right-click for Admin Tools");
        meta.setLore(lore);
        meta.addEnchant(Enchantment.LUCK, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        it.setItemMeta(meta);
        return it;
    }

    private void registerCompassRecipe() {
        try {
            NamespacedKey key = new NamespacedKey(plugin, "proshield_compass");
            // Player compass craft (optional; harmless if already exists)
            ShapedRecipe recipe = new ShapedRecipe(key, createPlayerCompass());
            recipe.shape("IRI", "RCR", "IRI");
            recipe.setIngredient('I', Material.IRON_INGOT);
            recipe.setIngredient('R', Material.REDSTONE);
            recipe.setIngredient('C', Material.COMPASS);
            Bukkit.addRecipe(recipe);
        } catch (Throwable ignored) {}
    }

    /* -----------------------------
     *  Inventories
     * ----------------------------- */
    public void openMain(Player p) {
        Inventory inv = Bukkit.createInventory(p, 54, TITLE_MAIN);

        // Base actions
        inv.setItem(slotMainCreate,  icon(Material.GRASS_BLOCK, ChatColor.GREEN + "Claim Chunk", "Claim the chunk you stand in."));
        inv.setItem(slotMainInfo,    icon(Material.PAPER, ChatColor.AQUA + "Claim Info", "Owner, trusted players, roles."));
        inv.setItem(slotMainRemove,  icon(Material.BARRIER, ChatColor.RED + "Unclaim", "Remove your claim here."));

        // Player QoL
        inv.setItem(slotMainTrust,    icon(Material.PLAYER_HEAD, ChatColor.GOLD + "Trust Player", "Add a nearby player or by name."));
        inv.setItem(slotMainRoles,    icon(Material.BOOK, ChatColor.GOLD + "Roles", "Adjust roles for trusted players."));
        inv.setItem(slotMainTransfer, icon(Material.NAME_TAG, ChatColor.YELLOW + "Transfer Claim", "Transfer ownership to someone."));
        inv.setItem(slotMainPreview,  icon(Material.GLOWSTONE_DUST, ChatColor.LIGHT_PURPLE + "Claim Border Preview", "Toggle a short border preview."));
        inv.setItem(slotMainSettings, icon(Material.COMPARATOR, ChatColor.BLUE + "Settings", "Open quick settings."));

        // Admin entry (visible to admins only)
        if (p.isOp() || p.hasPermission("proshield.admin") || p.hasPermission("proshield.admin.gui")) {
            inv.setItem(slotMainAdmin, icon(Material.RECOVERY_COMPASS, ChatColor.DARK_AQUA + "Admin Tools", "Open the Admin panel."));
        }

        // Help + Back
        inv.setItem(slotMainHelp, icon(Material.OAK_SIGN, ChatColor.GREEN + "Help", "Shows commands you can use."));
        inv.setItem(slotMainBack, icon(Material.ARROW, ChatColor.GRAY + "Back", "Return to previous menu"));

        p.openInventory(inv);
    }

    public void openAdmin(Player p) {
        Inventory inv = Bukkit.createInventory(p, 54, TITLE_ADMIN);

        // Toggles read from config
        boolean explosions = plugin.getConfig().getBoolean("protection.explosions.enabled", true);
        boolean fire       = plugin.getConfig().getBoolean("protection.fire.enabled", true);
        boolean entityGrief= plugin.getConfig().getBoolean("protection.entity-grief.enabled", true);
        boolean pvp        = plugin.getConfig().getBoolean("protection.pvp-in-claims", false); // false = pvp blocked
        boolean keepItems  = plugin.getConfig().getBoolean("claims.keep-items.enabled", false);
        boolean dropIfFull = plugin.getConfig().getBoolean("compass.drop-if-full", true);

        inv.setItem(slotAdminExplosions, toggle(explosions, Material.TNT, "Explosions Protection", "Protect claims from TNT/creeper/etc."));
        inv.setItem(slotAdminFire,       toggle(fire, Material.FLINT_AND_STEEL, "Fire Protection", "Block spread, burn & ignition."));
        inv.setItem(slotAdminEntityGrief,toggle(entityGrief, Material.ENDER_PEARL, "Entity Griefing", "Block Endermen, Ravagers, etc."));
        inv.setItem(slotAdminPvp,        toggle(!pvp, Material.IRON_SWORD, "Block PvP in Claims", "ON = Players safe from PvP in claims"));

        inv.setItem(slotAdminKeepItems,  toggle(keepItems, Material.ITEM_FRAME, "Keep Dropped Items", "Stop dropped items despawning in claims."));
        inv.setItem(slotAdminDropIfFull, toggle(dropIfFull, Material.CHEST, "Drop Compass If Full", "Give/join fallback when inv is full."));

        // Admin help tile
        List<String> helpLore = new ArrayList<>();
        helpLore.add(ChatColor.GRAY + "• Left-click to toggle.");
        helpLore.add(ChatColor.GRAY + "• Values save instantly.");
        helpLore.add(ChatColor.DARK_GRAY + "More admin tools coming in 2.0");
        inv.setItem(slotAdminHelp, iconWithLore(Material.WRITABLE_BOOK, ChatColor.AQUA + "Admin Help", helpLore));

        // Back
        inv.setItem(slotAdminBack, icon(Material.ARROW, ChatColor.GRAY + "Back", "Return to main menu"));

        p.openInventory(inv);
    }

    public void openHelp(Player p, boolean isAdmin) {
        Inventory inv = Bukkit.createInventory(p, 54, TITLE_HELP);

        // Build context-aware help
        List<String> items = new ArrayList<>();
        if (p.hasPermission("proshield.use")) {
            items.add("&f/proshield &7- open menu/help");
            items.add("&f/proshield claim &7- claim current chunk");
            items.add("&f/proshield unclaim &7- unclaim current chunk");
            items.add("&f/proshield info &7- show claim info");
            items.add("&f/proshield trust <player> [role] &7- trust with optional role");
            items.add("&f/proshield untrust <player> &7- remove trust");
            items.add("&f/proshield trusted &7- list trusted players");
            items.add("&f/proshield transfer <player> &7- transfer your claim");
            items.add("&f/proshield preview [seconds] &7- show border preview");
        }
        if (isAdmin) {
            items.add("&f/proshield bypass <on|off|toggle> &7- admin bypass");
            items.add("&f/proshield purgeexpired <days> [dryrun] &7- cleanup claims");
            items.add("&f/proshield reload &7- reload config");
            items.add("&f/proshield debug <on|off|toggle> &7- toggle debug logs");
        }

        // Render condensed help into book/paper stacks for readability
        inv.setItem(22, icon(Material.BOOK, ChatColor.GOLD + "Your Commands", ChatColor.GRAY + "Visible based on your permissions."));
        int rowStart = 28;
        for (int i = 0; i < items.size() && i < 14; i++) {
            String line = ChatColor.translateAlternateColorCodes('&', items.get(i));
            inv.setItem(rowStart + i, lineItem(line));
        }

        // Back
        inv.setItem(49, icon(Material.ARROW, ChatColor.GRAY + "Back", "Return to main menu"));
        p.openInventory(inv);
    }

    public void openTrustMenu(Player p) {
        Inventory inv = Bukkit.createInventory(p, 54, TITLE_TRUST);
        inv.setItem(20, icon(Material.PLAYER_HEAD, ChatColor.GOLD + "Trust Nearby", "Trust someone within 15 blocks."));
        inv.setItem(22, icon(Material.NAME_TAG, ChatColor.YELLOW + "Trust by Name", "Click to enter player name in chat."));
        inv.setItem(24, icon(Material.BOOK, ChatColor.AQUA + "Set Role for Trusted", "Pick role for an already trusted player."));
        inv.setItem(49, icon(Material.ARROW, ChatColor.GRAY + "Back", "Return to main menu"));
        p.openInventory(inv);
    }

    public void openTransferMenu(Player p) {
        Inventory inv = Bukkit.createInventory(p, 27, TITLE_TRANSFER);
        inv.setItem(11, icon(Material.NAME_TAG, ChatColor.YELLOW + "Transfer by Name", "Click and type the player name in chat."));
        inv.setItem(15, icon(Material.PLAYER_HEAD, ChatColor.GOLD + "Transfer to Nearby", "Transfer to a nearby player."));
        inv.setItem(22, icon(Material.ARROW, ChatColor.GRAY + "Back", "Return to main menu"));
        p.openInventory(inv);
    }

    /* -----------------------------
     *  Helpers
     * ----------------------------- */
    private ItemStack icon(Material m, String name, String loreLine) {
        ItemStack it = new ItemStack(m);
        ItemMeta meta = it.getItemMeta();
        meta.setDisplayName(name);
        if (loreLine != null && !loreLine.isEmpty()) {
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + loreLine);
            meta.setLore(lore);
        }
        it.setItemMeta(meta);
        return it;
    }

    private ItemStack iconWithLore(Material m, String name, List<String> lore) {
        ItemStack it = new ItemStack(m);
        ItemMeta meta = it.getItemMeta();
        meta.setDisplayName(name);
        List<String> color = new ArrayList<>();
        for (String s : lore) color.add(s);
        meta.setLore(color);
        it.setItemMeta(meta);
        return it;
    }

    private ItemStack toggle(boolean on, Material base, String name, String description) {
        ItemStack it = new ItemStack(base);
        ItemMeta meta = it.getItemMeta();
        meta.setDisplayName((on ? ChatColor.GREEN : ChatColor.RED) + name + ChatColor.GRAY + " [" + (on ? "ON" : "OFF") + "]");
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + description);
        lore.add("");
        lore.add(ChatColor.DARK_GRAY + "Click to toggle");
        meta.setLore(lore);
        if (on) {
            meta.addEnchant(Enchantment.DURABILITY, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        it.setItemMeta(meta);
        return it;
    }

    private ItemStack lineItem(String text) {
        ItemStack it = new ItemStack(Material.PAPER);
        ItemMeta meta = it.getItemMeta();
        meta.setDisplayName(text);
        it.setItemMeta(meta);
        return it;
    }
}
