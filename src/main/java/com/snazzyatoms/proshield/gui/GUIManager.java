// path: src/main/java/com/snazzyatoms/proshield/gui/GUIManager.java
package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.PlotManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class GUIManager {

    public static final String TITLE_MAIN  = ChatColor.DARK_AQUA + "ProShield";
    public static final String TITLE_ADMIN = ChatColor.DARK_RED  + "ProShield • Admin";
    public static final String TITLE_HELP  = ChatColor.BLUE      + "ProShield • Help";

    private final ProShield plugin;
    private final PlotManager plots;

    // Compass constants
    private static final String COMPASS_NAME = ChatColor.AQUA + "ProShield Compass";
    private static final NamespacedKey COMPASS_RECIPE_KEY;

    static {
        // NamespacedKey requires plugin at runtime; we create a placeholder and rebind in registerCompassRecipe.
        // We’ll still need a non-null key object here; the actual registration happens in ProShield.
        COMPASS_RECIPE_KEY = new NamespacedKey("proshield", "proshield_compass");
    }

    public GUIManager(ProShield plugin, PlotManager plots) {
        this.plugin = plugin;
        this.plots = plots;
    }

    /* =========================================================
     * Public helpers used by listeners/commands
     * ========================================================= */

    /** True if this view title belongs to any ProShield GUI we open. */
    public boolean isOurInventory(InventoryView view) {
        if (view == null) return false;
        String t = ChatColor.stripColor(view.getTitle());
        return t != null && (
                t.equals(ChatColor.stripColor(TITLE_MAIN)) ||
                t.equals(ChatColor.stripColor(TITLE_ADMIN)) ||
                t.equals(ChatColor.stripColor(TITLE_HELP))
        );
    }

    /** Handle clicks routed by GUIListener. */
    public void handleInventoryClick(Player p, int slot, ItemStack clicked, InventoryView view) {
        if (view == null) return;
        String raw = ChatColor.stripColor(view.getTitle());
        if (raw == null) return;

        if (ChatColor.stripColor(TITLE_MAIN).equals(raw)) {
            handleMainClick(p, slot);
        } else if (ChatColor.stripColor(TITLE_ADMIN).equals(raw)) {
            handleAdminClick(p, slot);
        } else if (ChatColor.stripColor(TITLE_HELP).equals(raw)) {
            handleHelpClick(p, slot);
        }
    }

    /** Build & give a ProShield compass. */
    public void giveCompass(Player p, boolean adminFlavor) {
        ItemStack it = createAdminCompass(); // single flavor (title differentiates admin via perms)
        // try add
        var inv = p.getInventory();
        var rest = inv.addItem(it);
        if (!rest.isEmpty()) {
            // fallback: drop or ignore based on config
            boolean drop = plugin.getConfig().getBoolean("compass.drop-if-full", true);
            if (drop) {
                p.getWorld().dropItemNaturally(p.getLocation(), it);
            } else {
                p.sendMessage(prefix() + ChatColor.YELLOW + "Inventory full. Use /proshield compass again after freeing a slot.");
            }
        }
        p.playSound(p.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.8f, 1.1f);
    }

    /** True if given item is our ProShield compass. */
    public boolean isProShieldCompass(ItemStack stack) {
        if (stack == null) return false;
        if (stack.getType() != Material.COMPASS) return false;
        if (!stack.hasItemMeta()) return false;
        var m = stack.getItemMeta();
        return m.hasDisplayName() && ChatColor.stripColor(m.getDisplayName())
                .equals(ChatColor.stripColor(COMPASS_NAME));
    }

    /* =========================================================
     * Open GUIs
     * ========================================================= */

    public void openMain(Player p) {
        Inventory inv = Bukkit.createInventory(p, 54, TITLE_MAIN);

        // Fill glass background
        fill(inv, glass(Material.CYAN_STAINED_GLASS_PANE, " "));

        // Slots (readable defaults; if you want, load from config.gui.slots)
        int slotCreate = plugin.getConfig().getInt("gui.slots.main.create", 11);
        int slotInfo   = plugin.getConfig().getInt("gui.slots.main.info",   13);
        int slotRemove = plugin.getConfig().getInt("gui.slots.main.remove", 15);
        int slotAdmin  = plugin.getConfig().getInt("gui.slots.main.admin",  33);
        int slotHelp   = plugin.getConfig().getInt("gui.slots.main.help",   49);
        int slotBack   = plugin.getConfig().getInt("gui.slots.main.back",   48);

        inv.setItem(slotCreate, button(Material.GREEN_WOOL, "&aClaim Chunk",
                "&7Claim the chunk you are standing in."));
        inv.setItem(slotInfo, button(Material.MAP, "&bClaim Info",
                "&7View owner, trusted players, and roles."));
        inv.setItem(slotRemove, button(Material.RED_WOOL, "&cUnclaim Chunk",
                "&7Remove your claim from this chunk."));

        // Player tools (quality-of-life)
        inv.setItem(20, button(Material.PLAYER_HEAD, "&eTrust Nearby Player",
                "&7Quickly trust a nearby player."));
        inv.setItem(22, button(Material.BOOK, "&eManage Roles",
                "&7Adjust roles for trusted players."));
        inv.setItem(24, button(Material.ENDER_EYE, "&ePreview Borders",
                "&7Show temporary particle border preview."));

        // Help + Back
        inv.setItem(slotHelp, button(Material.KNOWLEDGE_BOOK, "&bHelp",
                "&7Shows commands you can use based on your permissions."));

        inv.setItem(slotBack, backButton());

        // Admin entry (only if has permission)
        if (p.hasPermission("proshield.admin") || p.hasPermission("proshield.admin.gui")) {
            inv.setItem(slotAdmin, button(Material.NETHER_STAR, "&cAdmin Menu",
                    "&7Open ProShield Admin Tools."));
        } else {
            inv.setItem(slotAdmin, disabledButton("&7Admin Menu",
                    "&8You don't have permission to use this."));
        }

        p.openInventory(inv);
    }

    public void openAdmin(Player p) {
        if (!(p.hasPermission("proshield.admin") || p.hasPermission("proshield.admin.gui"))) {
            p.sendMessage(prefix() + ChatColor.RED + "You don't have permission to open the Admin Menu.");
            return;
        }

        Inventory inv = Bukkit.createInventory(p, 54, TITLE_ADMIN);
        fill(inv, glass(Material.RED_STAINED_GLASS_PANE, " "));

        // Default admin slots
        int toggleFire        = 10;
        int toggleExplosions  = 11;
        int toggleEntityGrief = 12;
        int toggleInteractions= 13;
        int togglePvp         = 14;

        int toggleKeepDrops   = 20; // keep-items
        int toggleDebug       = 21;

        int claimTools        = 29; // transfer, purge expired, etc.
        int teleportTools     = 30;
        int statsTools        = 31;

        int helpSlot          = plugin.getConfig().getInt("gui.slots.admin.help", 22);
        int backSlot          = plugin.getConfig().getInt("gui.slots.admin.back", 31);

        // Protection master switches (booleans)
        boolean pvpInClaims   = plugin.getConfig().getBoolean("protection.pvp-in-claims", false);
        boolean fireEnabled   = plugin.getConfig().getBoolean("protection.fire.enabled", true);
        boolean expEnabled    = plugin.getConfig().getBoolean("protection.explosions.enabled", true);
        boolean egEnabled     = plugin.getConfig().getBoolean("protection.entity-grief.enabled", true);
        boolean interEnabled  = plugin.getConfig().getBoolean("protection.interactions.enabled", true);

        inv.setItem(toggleFire, toggleButton(fireEnabled, "&6Fire Protection",
                "&7Currently: " + onOff(fireEnabled),
                "&8Blocks spread/ignite in claims"));
        inv.setItem(toggleExplosions, toggleButton(expEnabled, "&6Explosion Protection",
                "&7Currently: " + onOff(expEnabled)));
        inv.setItem(toggleEntityGrief, toggleButton(egEnabled, "&6Entity Grief",
                "&7Currently: " + onOff(egEnabled)));
        inv.setItem(toggleInteractions, toggleButton(interEnabled, "&6Interactions Guard",
                "&7Currently: " + onOff(interEnabled)));
        inv.setItem(togglePvp, toggleButton(!pvpInClaims, "&6PvP Block in Claims",
                "&7Currently: " + onOff(!pvpInClaims)));

        // Keep drops + debug
        boolean keepItemsEnabled = plugin.getConfig().getBoolean("claims.keep-items.enabled", false);
        inv.setItem(toggleKeepDrops, toggleButton(keepItemsEnabled, "&6Keep Items in Claims",
                "&7Prevent item despawn inside claims.",
                "&7Currently: " + onOff(keepItemsEnabled)));

        boolean debug = plugin.isDebug();
        inv.setItem(toggleDebug, toggleButton(debug, "&dDebug Logging",
                "&7Currently: " + onOff(debug)));

        // Utility groups
        inv.setItem(claimTools, button(Material.AMETHYST_SHARD, "&bClaim Utilities",
                "&7Transfer ownership, preview expiry, purge expired."));
        inv.setItem(teleportTools, button(Material.ENDER_PEARL, "&bTeleport Tools",
                "&7Teleport to claims (admin only)."));
        inv.setItem(statsTools, button(Material.PAPER, "&bStatistics",
                "&7Global claim stats overview."));

        // Help tip + back
        inv.setItem(helpSlot, button(Material.OAK_SIGN, "&eAdmin Help",
                "&7Left-click toggles; Right-click opens detail.",
                "&8More admin modules arriving in ProShield 2.0."));
        inv.setItem(backSlot, backButton());

        p.openInventory(inv);
    }

    public void openHelp(Player p) {
        Inventory inv = Bukkit.createInventory(p, 54, TITLE_HELP);
        fill(inv, glass(Material.LIGHT_BLUE_STAINED_GLASS_PANE, " "));

        // Build role/permission-aware command list
        List<String> lines = new ArrayList<>();
        lines.add("&bAvailable Commands:");
        lines.add(" ");
        // base
        lines.add("&7/&fproshield &8- open menu");
        lines.add("&7/&fproshield claim &8- claim current chunk");
        lines.add("&7/&fproshield unclaim &8- unclaim current chunk");
        lines.add("&7/&fproshield info &8- view claim details");
        lines.add("&7/&fproshield compass &8- get ProShield compass");
        lines.add("&7/&fproshield trust &f<player> [role] &8- trust player");
        lines.add("&7/&fproshield untrust &f<player> &8- revoke trust");
        lines.add("&7/&fproshield trusted &8- list trusted");

        if (p.hasPermission("proshield.admin") || p.isOp()) {
            lines.add(" ");
            lines.add("&cAdmin:");
            lines.add("&7/&fproshield bypass &8- toggle bypass");
            lines.add("&7/&fproshield purgeexpired &8- purge inactive claims");
            lines.add("&7/&fproshield debug &8- toggle debug logging");
            lines.add("&7/&fproshield reload &8- reload configuration");
        }

        // Place book with lore
        inv.setItem(22, loreBook("&bYour Commands", lines));

        // Back
        int backSlot = plugin.getConfig().getInt("gui.slots.main.back", 48);
        inv.setItem(backSlot, backButton());

        p.openInventory(inv);
    }

    /* =========================================================
     * Internal click handlers
     * ========================================================= */

    private void handleMainClick(Player p, int slot) {
        int slotCreate = plugin.getConfig().getInt("gui.slots.main.create", 11);
        int slotInfo   = plugin.getConfig().getInt("gui.slots.main.info",   13);
        int slotRemove = plugin.getConfig().getInt("gui.slots.main.remove", 15);
        int slotAdmin  = plugin.getConfig().getInt("gui.slots.main.admin",  33);
        int slotHelp   = plugin.getConfig().getInt("gui.slots.main.help",   49);
        int slotBack   = plugin.getConfig().getInt("gui.slots.main.back",   48);

        if (slot == slotCreate) {
            p.performCommand("proshield claim");
            p.closeInventory();
        } else if (slot == slotInfo) {
            p.performCommand("proshield info");
            p.closeInventory();
        } else if (slot == slotRemove) {
            p.performCommand("proshield unclaim");
            p.closeInventory();
        } else if (slot == slotHelp) {
            openHelp(p);
        } else if (slot == slotAdmin) {
            if (p.hasPermission("proshield.admin") || p.hasPermission("proshield.admin.gui")) {
                openAdmin(p);
            } else {
                p.sendMessage(prefix() + ChatColor.RED + "You don’t have permission to open the Admin Menu.");
            }
        } else if (slot == slotBack) {
            p.closeInventory();
        } else if (slot == 20) { // Trust nearby
            p.performCommand("proshield trust-nearby");
            p.closeInventory();
        } else if (slot == 22) { // Manage Roles
            p.performCommand("proshield roles");
            p.closeInventory();
        } else if (slot == 24) { // Preview Borders
            p.performCommand("proshield preview 12");
            p.closeInventory();
        }
    }

    private void handleAdminClick(Player p, int slot) {
        // Mirror the layout in openAdmin()
        if (slot == 10) { // fire
            toggleConfig("protection.fire.enabled");
            reopened(p, true);
        } else if (slot == 11) { // explosions
            toggleConfig("protection.explosions.enabled");
            reopened(p, true);
        } else if (slot == 12) { // entity grief
            toggleConfig("protection.entity-grief.enabled");
            reopened(p, true);
        } else if (slot == 13) { // interactions
            toggleConfig("protection.interactions.enabled");
            reopened(p, true);
        } else if (slot == 14) { // PvP block (inverse of pvp-in-claims)
            boolean pvp = plugin.getConfig().getBoolean("protection.pvp-in-claims", false);
            plugin.getConfig().set("protection.pvp-in-claims", !pvp); // invert
            plugin.saveConfig();
            reopened(p, true);
        } else if (slot == 20) { // keep items in claims
            toggleConfig("claims.keep-items.enabled");
            reopened(p, true);
        } else if (slot == 21) { // debug
            plugin.setDebug(!plugin.isDebug());
            reopened(p, true);
        } else if (slot == 29) { // claim utilities
            p.performCommand("proshield admin claims");
            p.closeInventory();
        } else if (slot == 30) { // teleport tools
            p.performCommand("proshield admin tp");
            p.closeInventory();
        } else if (slot == 31) { // stats
            p.performCommand("proshield stats");
            p.closeInventory();
        } else if (slot == plugin.getConfig().getInt("gui.slots.admin.help", 22)) {
            p.sendMessage(prefix() + ChatColor.YELLOW + "Tip: Left-click toggles, right-click shows details.");
        } else if (slot == plugin.getConfig().getInt("gui.slots.admin.back", 31)) {
            openMain(p);
        }
    }

    private void handleHelpClick(Player p, int slot) {
        int backSlot = plugin.getConfig().getInt("gui.slots.main.back", 48);
        if (slot == backSlot) {
            openMain(p);
        }
    }

    private void reopened(Player p, boolean admin) {
        p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.6f, 1.4f);
        if (admin) openAdmin(p); else openMain(p);
    }

    private void toggleConfig(String path) {
        boolean cur = plugin.getConfig().getBoolean(path, false);
        plugin.getConfig().set(path, !cur);
        plugin.saveConfig();
    }

    /* =========================================================
     * Item builders
     * ========================================================= */

    private ItemStack button(Material mat, String name, String... lore) {
        ItemStack it = new ItemStack(mat);
        ItemMeta meta = it.getItemMeta();
        meta.setDisplayName(color(name));
        if (lore != null && lore.length > 0) {
            List<String> lines = new ArrayList<>();
            for (String s : lore) lines.add(color("&7" + s));
            meta.setLore(lines);
        }
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        it.setItemMeta(meta);
        return it;
    }

    private ItemStack disabledButton(String name, String... lore) {
        ItemStack it = new ItemStack(Material.GRAY_DYE);
        ItemMeta meta = it.getItemMeta();
        meta.setDisplayName(color(name));
        List<String> lines = new ArrayList<>();
        for (String s : lore) lines.add(color("&8" + s));
        meta.setLore(lines);
        it.setItemMeta(meta);
        return it;
    }

    private ItemStack toggleButton(boolean on, String name, String... more) {
        ItemStack it = new ItemStack(on ? Material.LIME_DYE : Material.RED_DYE);
        ItemMeta meta = it.getItemMeta();
        meta.setDisplayName(color((on ? "&a" : "&c") + name));
        List<String> lore = new ArrayList<>();
        if (more != null) for (String s : more) lore.add(color("&7" + s));
        meta.setLore(lore);
        it.setItemMeta(meta);
        return it;
    }

    private ItemStack backButton() {
        return button(Material.ARROW, "&fBack", "Return to previous menu");
    }

    private ItemStack glass(Material type, String title) {
        ItemStack it = new ItemStack(type);
        ItemMeta m = it.getItemMeta();
        m.setDisplayName(title);
        it.setItemMeta(m);
        return it;
    }

    private ItemStack loreBook(String title, List<String> lines) {
        ItemStack it = new ItemStack(Material.KNOWLEDGE_BOOK);
        ItemMeta m = it.getItemMeta();
        m.setDisplayName(color(title));
        List<String> lore = new ArrayList<>();
        for (String s : lines) lore.add(color(s));
        m.setLore(lore);
        it.setItemMeta(m);
        return it;
    }

    /* =========================================================
     * Compass helpers
     * ========================================================= */

    public ItemStack createAdminCompass() {
        ItemStack it = new ItemStack(Material.COMPASS);
        ItemMeta meta = it.getItemMeta();
        meta.setDisplayName(COMPASS_NAME);
        List<String> lore = new ArrayList<>();
        lore.add(color("&7Right-click to open ProShield."));
        meta.setLore(lore);
        it.setItemMeta(meta);
        return it;
    }

    /** Optional in case you call it from elsewhere; actual registration is in ProShield. */
    public void registerCompassRecipe() {
        try {
            ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(plugin, "proshield_compass"), createAdminCompass());
            recipe.shape("IRI", "RCR", "IRI");
            recipe.setIngredient('I', Material.IRON_INGOT);
            recipe.setIngredient('R', Material.REDSTONE);
            recipe.setIngredient('C', Material.COMPASS);
            Bukkit.removeRecipe(recipe.getKey());
            Bukkit.addRecipe(recipe);
        } catch (Throwable ignored) { }
    }

    /* =========================================================
     * Utils
     * ========================================================= */

    private String prefix() {
        String pf = plugin.getConfig().getString("messages.prefix", "&3[ProShield]&r ");
        return ChatColor.translateAlternateColorCodes('&', pf);
    }

    private String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    private String onOff(boolean b) {
        return b ? ChatColor.GREEN + "ON" + ChatColor.GRAY : ChatColor.RED + "OFF" + ChatColor.GRAY;
    }
}
