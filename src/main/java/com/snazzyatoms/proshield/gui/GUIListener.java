package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.PlotManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class GUIManager {

    public static final String TITLE = ChatColor.DARK_AQUA + "ProShield";

    private final ProShield plugin;
    private final PlotManager plots;

    // GUI slots pulled from config (with sensible fallbacks)
    private int MAIN_SLOT_CREATE = 11;
    private int MAIN_SLOT_INFO   = 13;
    private int MAIN_SLOT_REMOVE = 15;
    private int MAIN_SLOT_BACK   = 48;
    private int MAIN_SLOT_HELP   = 49;
    private int MAIN_SLOT_ADMIN  = 33;

    private int ADMIN_SLOT_TOGGLE_DROP_IF_FULL = 20;
    private int ADMIN_SLOT_HELP                = 22;
    private int ADMIN_SLOT_BACK                = 31;

    public GUIManager(ProShield plugin, PlotManager plots) {
        this.plugin = plugin;
        this.plots  = plots;
        reloadGuiSlots(); // load from config
    }

    /* =========================
       Public GUI open helpers
       ========================= */

    public void openMain(Player p) {
        Inventory inv = Bukkit.createInventory(p, 54, TITLE);

        // Row of glass as background (optional subtle polish)
        fillBorders(inv, Material.CYAN_STAINED_GLASS_PANE);

        inv.setItem(MAIN_SLOT_CREATE, createNamedItem(Material.LIME_WOOL,
                "&aClaim Chunk",
                "&7Claim the chunk you are standing in.",
                "&8Click to claim"));

        inv.setItem(MAIN_SLOT_INFO, createNamedItem(Material.BOOK,
                "&bClaim Info",
                "&7View owner and trusted players.",
                "&8Click to view"));

        inv.setItem(MAIN_SLOT_REMOVE, createNamedItem(Material.RED_WOOL,
                "&cUnclaim Chunk",
                "&7Remove your claim from this chunk.",
                "&8Click to unclaim"));

        // Back (from submenus)
        inv.setItem(MAIN_SLOT_BACK, createNamedItem(Material.ARROW,
                "&fBack",
                "&7Go back to previous menu"));

        // Help
        inv.setItem(MAIN_SLOT_HELP, createNamedItem(Material.MAP,
                "&eHelp",
                "&7Shows commands relevant to your permissions.",
                "&8Click to open"));

        // Admin
        inv.setItem(MAIN_SLOT_ADMIN, createNamedItem(Material.COMPASS,
                "&6Admin",
                "&7Open the admin panel (requires permission).",
                "&8Click to open"));

        p.openInventory(inv);
    }

    public void openHelp(Player p) {
        Inventory inv = Bukkit.createInventory(p, 54, TITLE);

        fillBorders(inv, Material.LIGHT_BLUE_STAINED_GLASS_PANE);

        // Build a small permission-aware help list
        List<String> lines = new ArrayList<>();
        lines.add("&fAvailable Commands");
        lines.add("&7");
        lines.add("&b/proshield claim &7- Claim current chunk");
        lines.add("&b/proshield unclaim &7- Unclaim current chunk");
        lines.add("&b/proshield info &7- Info about this chunk");
        lines.add("&b/proshield trust <player> [role] &7- Trust a player");
        lines.add("&b/proshield untrust <player> &7- Remove trust");
        lines.add("&b/proshield trusted &7- List trusted players");
        if (p.hasPermission("proshield.compass")) {
            lines.add("&b/proshield compass &7- Get the ProShield compass");
        }
        if (p.hasPermission("proshield.admin")) {
            lines.add("&7");
            lines.add("&6Admin Tools");
            lines.add("&e/proshield reload &7- Reload configuration");
            lines.add("&e/proshield purgeexpired <days> [dryrun] &7- Expiry cleanup");
            lines.add("&e/proshield debug <on|off|toggle> &7- Toggle debug");
        }

        // Put lines into lore on a written book
        inv.setItem(22, createNamedItem(Material.WRITTEN_BOOK, "&eHelp &7(Your Permissions)", lines));

        // Back
        inv.setItem(MAIN_SLOT_BACK, createNamedItem(Material.ARROW, "&fBack", "&7Return to main menu"));
        // Admin quick link (if permitted)
        inv.setItem(MAIN_SLOT_ADMIN, createNamedItem(Material.COMPASS, "&6Admin", "&7Open admin panel"));

        p.openInventory(inv);
    }

    public void openAdmin(Player p) {
        Inventory inv = Bukkit.createInventory(p, 54, TITLE);

        fillBorders(inv, Material.YELLOW_STAINED_GLASS_PANE);

        // Toggle: compass drop-if-full
        boolean dropIfFull = plugin.getConfig().getBoolean("compass.drop-if-full", true);
        inv.setItem(ADMIN_SLOT_TOGGLE_DROP_IF_FULL,
                createNamedItem(dropIfFull ? Material.CLOCK : Material.BARRIER,
                "&6Compass: Drop If Full &7(" + (dropIfFull ? "&aON" : "&cOFF") + "&7)",
                "&7If inventory is full when giving a compass,",
                "&7drop at the player's feet (ON) or skip (OFF).",
                "&8Click to toggle"));

        // Admin help tooltip
        inv.setItem(ADMIN_SLOT_HELP, createNamedItem(Material.PAPER,
                "&eAdmin Help",
                "&7Admin-only utilities live here.",
                "&7Spawn no-claim radius is configured in &fconfig.yml&7:",
                "&7- &eno-claim.spawn.enabled: &a" + plugin.getConfig().getBoolean("no-claim.spawn.enabled", true),
                "&7- &eradius-blocks: &a" + plugin.getConfig().getInt("no-claim.spawn.radius-blocks", 96)));

        // Back to main
        inv.setItem(ADMIN_SLOT_BACK, createNamedItem(Material.ARROW, "&fBack", "&7Return to main menu"));

        p.openInventory(inv);
    }

    /* =========================
       Compass helpers
       ========================= */

    public static ItemStack createAdminCompass() {
        ItemStack it = new ItemStack(Material.COMPASS);
        ItemMeta meta = it.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.AQUA + "ProShield Compass");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Right-click to open the ProShield menu");
            meta.setLore(lore);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            it.setItemMeta(meta);
        }
        return it;
    }

    public void giveCompass(Player p, boolean respectInventory) {
        ItemStack compass = createAdminCompass();
        boolean dropIfFull = plugin.getConfig().getBoolean("compass.drop-if-full", true);

        if (!respectInventory) {
            p.getInventory().addItem(compass);
            return;
        }

        if (p.getInventory().firstEmpty() == -1) {
            if (dropIfFull) {
                p.getWorld().dropItemNaturally(p.getLocation(), compass);
                p.sendMessage(prefix() + ChatColor.YELLOW + "Inventory full — dropped a compass at your feet.");
            } else {
                p.sendMessage(prefix() + ChatColor.GRAY + "Inventory full — use /proshield compass when you have space.");
            }
        } else {
            p.getInventory().addItem(compass);
        }
    }

    public void registerCompassRecipe() {
        ItemStack compass = createAdminCompass();
        NamespacedKey key = new NamespacedKey(plugin, "proshield_admin_compass");
        ShapedRecipe recipe = new ShapedRecipe(key, compass);
        recipe.shape("IRI", "RCR", "IRI");
        recipe.setIngredient('I', Material.IRON_INGOT);
        recipe.setIngredient('R', Material.REDSTONE);
        recipe.setIngredient('C', Material.COMPASS);
        Bukkit.removeRecipe(key); // avoid dupes on reload
        Bukkit.addRecipe(recipe);
    }

    /* =========================
       Internal helpers
       ========================= */

    private void reloadGuiSlots() {
        // main
        MAIN_SLOT_CREATE = plugin.getConfig().getInt("gui.slots.main.create", MAIN_SLOT_CREATE);
        MAIN_SLOT_INFO   = plugin.getConfig().getInt("gui.slots.main.info", MAIN_SLOT_INFO);
        MAIN_SLOT_REMOVE = plugin.getConfig().getInt("gui.slots.main.remove", MAIN_SLOT_REMOVE);
        MAIN_SLOT_BACK   = plugin.getConfig().getInt("gui.slots.main.back", MAIN_SLOT_BACK);
        MAIN_SLOT_HELP   = plugin.getConfig().getInt("gui.slots.main.help", MAIN_SLOT_HELP);
        MAIN_SLOT_ADMIN  = plugin.getConfig().getInt("gui.slots.main.admin", MAIN_SLOT_ADMIN);

        // admin
        ADMIN_SLOT_TOGGLE_DROP_IF_FULL = plugin.getConfig().getInt("gui.slots.admin.toggle-drop-if-full", ADMIN_SLOT_TOGGLE_DROP_IF_FULL);
        ADMIN_SLOT_HELP                = plugin.getConfig().getInt("gui.slots.admin.help", ADMIN_SLOT_HELP);
        ADMIN_SLOT_BACK                = plugin.getConfig().getInt("gui.slots.admin.back", ADMIN_SLOT_BACK);
    }

    private void fillBorders(Inventory inv, Material mat) {
        ItemStack pane = createNamedItem(mat, " ");
        // Top & bottom rows
        for (int i = 0; i < 9; i++) inv.setItem(i, pane);
        for (int i = 45; i < 54; i++) inv.setItem(i, pane);
        // Left & right columns
        for (int r = 1; r < 5; r++) {
            inv.setItem(r * 9, pane);
            inv.setItem(r * 9 + 8, pane);
        }
    }

    private ItemStack createNamedItem(Material mat, String name, String... loreLines) {
        ItemStack it = new ItemStack(mat);
        ItemMeta meta = it.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
            if (loreLines != null && loreLines.length > 0) {
                List<String> lore = new ArrayList<>();
                for (String s : loreLines) {
                    lore.add(ChatColor.translateAlternateColorCodes('&', s));
                }
                meta.setLore(lore);
            }
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            it.setItemMeta(meta);
        }
        return it;
    }

    private ItemStack createNamedItem(Material mat, String name, List<String> loreColored) {
        ItemStack it = new ItemStack(mat);
        ItemMeta meta = it.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
            if (loreColored != null && !loreColored.isEmpty()) {
                List<String> lore = new ArrayList<>();
                for (String s : loreColored) {
                    lore.add(ChatColor.translateAlternateColorCodes('&', s));
                }
                meta.setLore(lore);
            }
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            it.setItemMeta(meta);
        }
        return it;
    }

    private String prefix() {
        return ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("messages.prefix", "&3[ProShield]&r "));
    }

    /* Exposed for ProShield to refresh slot cache on /proshield reload */
    public void onConfigReload() {
        reloadGuiSlots();
    }
}
