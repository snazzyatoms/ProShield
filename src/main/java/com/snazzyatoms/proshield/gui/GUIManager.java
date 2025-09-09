package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.PlotManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public class GUIManager {

    public static final String TITLE_MAIN  = ChatColor.DARK_AQUA + "ProShield";
    public static final String TITLE_ADMIN = ChatColor.DARK_RED   + "ProShield • Admin";

    private final ProShield plugin;
    private final PlotManager plotManager;

    public GUIManager(ProShield plugin, PlotManager plotManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
    }

    /* ---------- Config-driven slots (with sensible defaults) ---------- */
    private int slot(String path, int def) {
        return plugin.getConfig().getInt("gui.slots." + path, def);
    }

    // MAIN menu default layout
    public int SLOT_MAIN_CREATE() { return slot("main.create", 11); }
    public int SLOT_MAIN_INFO()   { return slot("main.info",   13); }
    public int SLOT_MAIN_REMOVE() { return slot("main.remove", 15); }
    public int SLOT_MAIN_ADMIN()  { return slot("main.admin",  33); }
    public int SLOT_MAIN_HELP()   { return slot("main.help",   49); } // reusable help icon
    public int SLOT_MAIN_BACK()   { return slot("main.back",   48); } // closes

    // ADMIN menu layout
    public int SLOT_ADMIN_TOGGLE_DROP() { return slot("admin.toggle-drop-if-full", 20); }
    public int SLOT_ADMIN_HELP()        { return slot("admin.help",                 22); }
    public int SLOT_ADMIN_BACK()        { return slot("admin.back",                 31); }

    /* ---------- Factory helpers ---------- */

    public static ItemStack simpleItem(Material mat, String name, String... loreLines) {
        ItemStack it = new ItemStack(mat);
        ItemMeta im = it.getItemMeta();
        if (im != null) {
            im.setDisplayName(name);
            if (loreLines != null && loreLines.length > 0) {
                im.setLore(Arrays.asList(loreLines));
            }
            im.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            it.setItemMeta(im);
        }
        return it;
    }

    /** Reusable “help/tooltip” item for any GUI. */
    public static ItemStack helpItem(String titleColorized, List<String> lore) {
        ItemStack it = new ItemStack(Material.PAPER);
        ItemMeta meta = it.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(titleColorized);
            meta.setLore(lore);
            it.setItemMeta(meta);
        }
        return it;
    }

    /** Back item for all menus. */
    public static ItemStack backItem() {
        return simpleItem(
                Material.ARROW,
                ChatColor.YELLOW + "Back",
                ChatColor.GRAY + "Return to the previous menu."
        );
    }

    /* ---------- GUIs ---------- */

    public void openMain(Player p) {
        Inventory inv = Bukkit.createInventory(null, 54, TITLE_MAIN);

        // Create
        inv.setItem(SLOT_MAIN_CREATE(), simpleItem(
                Material.OAK_SAPLING,
                ChatColor.GREEN + "Create Claim",
                ChatColor.GRAY + "Claim the chunk you are standing in."
        ));

        // Info
        inv.setItem(SLOT_MAIN_INFO(), simpleItem(
                Material.PAPER,
                ChatColor.AQUA + "Claim Info",
                ChatColor.GRAY + "Show owner and trusted players here."
        ));

        // Remove
        inv.setItem(SLOT_MAIN_REMOVE(), simpleItem(
                Material.BARRIER,
                ChatColor.RED + "Remove Claim",
                ChatColor.GRAY + "Unclaim the current chunk if you own it."
        ));

        // Admin
        inv.setItem(SLOT_MAIN_ADMIN(), simpleItem(
                Material.COMPASS,
                ChatColor.GOLD + "Admin Menu",
                ChatColor.GRAY + "Open ProShield admin controls."
        ));

        // Help (reusable tooltip)
        inv.setItem(SLOT_MAIN_HELP(), helpItem(
                ChatColor.YELLOW + "Help",
                Arrays.asList(
                        ChatColor.GRAY + "Use the items to manage claims:",
                        ChatColor.DARK_GRAY + "• Create: claim current chunk",
                        ChatColor.DARK_GRAY + "• Info: owner + trusted list",
                        ChatColor.DARK_GRAY + "• Remove: unclaim your chunk",
                        ChatColor.DARK_GRAY + "• Admin: server tools (if permitted)"
                )
        ));

        // Back (closes)
        inv.setItem(SLOT_MAIN_BACK(), backItem());

        p.openInventory(inv);
    }

    public void openAdmin(Player p) {
        Inventory inv = Bukkit.createInventory(null, 45, TITLE_ADMIN);

        boolean dropIfFull = plugin.getConfig().getBoolean("compass.drop-if-full", true);
        inv.setItem(SLOT_ADMIN_TOGGLE_DROP(), simpleItem(
                dropIfFull ? Material.LIME_DYE : Material.GRAY_DYE,
                (dropIfFull ? ChatColor.GREEN : ChatColor.GRAY) + "Compass: Drop if inventory full",
                ChatColor.GRAY + "Current: " + (dropIfFull ? ChatColor.GREEN + "ON" : ChatColor.RED + "OFF"),
                ChatColor.GRAY + "Click to toggle.",
                ChatColor.DARK_GRAY + "Config path: compass.drop-if-full"
        ));

        // Help tooltip in admin
        inv.setItem(SLOT_ADMIN_HELP(), helpItem(
                ChatColor.YELLOW + "Admin Help",
                Arrays.asList(
                        ChatColor.GRAY + "Admin tools and toggles:",
                        ChatColor.DARK_GRAY + "• Compass drop-fallback toggle",
                        ChatColor.DARK_GRAY + "• More admin options coming",
                        ChatColor.DARK_GRAY + "  in 1.2.x and 2.0",
                        ChatColor.DARK_GRAY + "Config:",
                        ChatColor.DARK_GRAY + "compass.drop-if-full"
                )
        ));

        // Back to main
        inv.setItem(SLOT_ADMIN_BACK(), backItem());

        p.openInventory(inv);
    }

    /* ---------- Utility ---------- */

    public static ItemStack createAdminCompass() {
        ItemStack it = new ItemStack(Material.COMPASS);
        ItemMeta im = it.getItemMeta();
        if (im != null) {
            im.setDisplayName(ChatColor.GOLD + "ProShield Compass");
            im.setLore(Arrays.asList(
                    ChatColor.GRAY + "Right-click to open the",
                    ChatColor.GRAY + "ProShield menu."
            ));
            it.setItemMeta(im);
        }
        return it;
    }

    @SuppressWarnings("unused")
    private boolean worldAllowed(World w) {
        // placeholder if you later want world restrictions
        return true;
    }
}
