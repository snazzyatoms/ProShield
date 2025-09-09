package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.PlotManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class GUIManager {

    private final ProShield plugin;
    private final PlotManager plots;

    // Titles
    private static final String TITLE_MAIN  = ChatColor.DARK_AQUA + "ProShield";
    private static final String TITLE_ADMIN = ChatColor.DARK_RED + "ProShield • Admin";

    // Slots (pulled from config with sane defaults)
    private int MAIN_CREATE = 11, MAIN_INFO = 13, MAIN_REMOVE = 15, MAIN_HELP = 31, MAIN_ADMIN = 33, MAIN_BACK = 48;
    private int ADM_FIRE = 10, ADM_EXPLOSIONS = 11, ADM_ENTITY_GRIEF = 12, ADM_INTERACTIONS = 13, ADM_PVP = 14;
    private int ADM_KEEPITEMS = 20, ADM_PURGE = 21, ADM_DEBUG = 23, ADM_COMPASS_DROP = 24, ADM_RELOAD = 25, ADM_TP = 30, ADM_BACK = 31, ADM_HELP = 22, ADM_SPAWN_GUARD = 26;

    public GUIManager(ProShield plugin, PlotManager plots) {
        this.plugin = plugin;
        this.plots = plots;
        readSlotsFromConfig();
    }

    public void onConfigReload() {
        readSlotsFromConfig();
    }

    private void readSlotsFromConfig() {
        var c = plugin.getConfig();
        MAIN_CREATE = c.getInt("gui.slots.main.create", MAIN_CREATE);
        MAIN_INFO   = c.getInt("gui.slots.main.info", MAIN_INFO);
        MAIN_REMOVE = c.getInt("gui.slots.main.remove", MAIN_REMOVE);
        MAIN_HELP   = c.getInt("gui.slots.main.help", MAIN_HELP);
        MAIN_ADMIN  = c.getInt("gui.slots.main.admin", MAIN_ADMIN);
        MAIN_BACK   = c.getInt("gui.slots.main.back", MAIN_BACK);

        ADM_FIRE         = c.getInt("gui.slots.admin.fire", ADM_FIRE);
        ADM_EXPLOSIONS   = c.getInt("gui.slots.admin.explosions", ADM_EXPLOSIONS);
        ADM_ENTITY_GRIEF = c.getInt("gui.slots.admin.entity-grief", ADM_ENTITY_GRIEF);
        ADM_INTERACTIONS = c.getInt("gui.slots.admin.interactions", ADM_INTERACTIONS);
        ADM_PVP          = c.getInt("gui.slots.admin.pvp", ADM_PVP);
        ADM_KEEPITEMS    = c.getInt("gui.slots.admin.keep-items", ADM_KEEPITEMS);
        ADM_PURGE        = c.getInt("gui.slots.admin.purge-expired", ADM_PURGE);
        ADM_DEBUG        = c.getInt("gui.slots.admin.debug", ADM_DEBUG);
        ADM_COMPASS_DROP = c.getInt("gui.slots.admin.compass-drop-if-full", ADM_COMPASS_DROP);
        ADM_RELOAD       = c.getInt("gui.slots.admin.reload", ADM_RELOAD);
        ADM_TP           = c.getInt("gui.slots.admin.tp-tools", ADM_TP);
        ADM_BACK         = c.getInt("gui.slots.admin.back", ADM_BACK);
        ADM_HELP         = c.getInt("gui.slots.admin.help", ADM_HELP);
        ADM_SPAWN_GUARD  = c.getInt("gui.slots.admin.spawn-guard", ADM_SPAWN_GUARD);
    }

    // ---------- Openers ----------
    public void openMain(Player p) {
        Inventory inv = Bukkit.createInventory(p, 54, TITLE_MAIN);
        fill(inv, glassPane((short) 3, " "));
        inv.setItem(MAIN_CREATE, icon(Material.OAK_SIGN, "&aClaim this chunk", List.of("&7Protect your current chunk.")));
        inv.setItem(MAIN_INFO,   icon(Material.BOOK,     "&bClaim info",      List.of("&7Owner, trusted, roles.")));
        inv.setItem(MAIN_REMOVE, icon(Material.BARRIER,  "&cUnclaim",         List.of("&7Release protection.")));
        inv.setItem(MAIN_HELP,   icon(Material.MAP,      "&eHelp",            List.of("&7Commands & tips.")));

        if (p.hasPermission("proshield.admin.gui")) {
            inv.setItem(MAIN_ADMIN, icon(Material.NETHER_STAR, "&cAdmin Tools", List.of("&7Open the Admin menu.")));
        }

        inv.setItem(MAIN_BACK, icon(Material.ARROW, "&7Back", List.of("&7Close and return.")));
        p.openInventory(inv);
    }

    public void openAdmin(Player p) {
        Inventory inv = Bukkit.createInventory(p, 54, TITLE_ADMIN);
        fill(inv, glassPane((short) 14, " "));

        inv.setItem(ADM_FIRE,         toggle("Fire",         Material.BLAZE_POWDER, "protection.fire.enabled"));
        inv.setItem(ADM_EXPLOSIONS,   toggle("Explosions",   Material.TNT,          "protection.explosions.enabled"));
        inv.setItem(ADM_ENTITY_GRIEF, toggle("Entity Grief", Material.ENDERMITE_SPAWN_EGG, "protection.entity-grief.enabled"));
        inv.setItem(ADM_INTERACTIONS, toggle("Interactions", Material.LEVER,        "protection.interactions.enabled"));
        inv.setItem(ADM_PVP,          booleanIcon("PvP in Claims", Material.IRON_SWORD, "protection.pvp-in-claims"));

        inv.setItem(ADM_KEEPITEMS,    booleanIcon("Keep Items in Claims", Material.CHEST, "claims.keep-items.enabled"));
        inv.setItem(ADM_PURGE,        icon(Material.HOPPER, "&6Purge Expired Claims", List.of("&7Run /proshield purgeexpired")));
        inv.setItem(ADM_DEBUG,        booleanIcon("Debug Logging", Material.COMPARATOR, "proshield.debug"));
        inv.setItem(ADM_COMPASS_DROP, booleanIcon("Compass: Drop if Full", Material.COMPASS, "compass.drop-if-full"));
        inv.setItem(ADM_SPAWN_GUARD,  booleanIcon("Spawn Guard", Material.BEACON, "spawn.block-claiming"));

        inv.setItem(ADM_RELOAD, icon(Material.LIME_DYE, "&aReload Config",
                List.of("&7Apply config changes immediately.",
                        "&8Click to reload.")));

        inv.setItem(ADM_TP, icon(Material.ENDER_PEARL, "&bTeleport Tools",
                List.of("&7(Admin) Jump to claim centers quickly.",
                        "&8(Coming in 1.2.5)")));

        inv.setItem(ADM_HELP, icon(Material.PAPER, "&eAdmin Help",
                List.of("&7Left-click toggles, Right-click opens details.",
                       "&7Back goes to player menu.",
                       "&8More in 2.0…")));

        inv.setItem(ADM_BACK, icon(Material.ARROW, "&7Back", List.of("&7Return to main menu.")));
        p.openInventory(inv);
    }

    // ---------- Click Handling ----------
    public boolean isOurInventory(Inventory inv) {
        if (inv == null) return false;
        String t = inv.getTitle();
        return TITLE_MAIN.equals(t) || TITLE_ADMIN.equals(t);
    }

    public void handleInventoryClick(Player p, int slot, ItemStack clicked, String title) {
        boolean isAdmin = TITLE_ADMIN.equals(title);

        if (!isAdmin) {
            if (slot == MAIN_CREATE) {
                p.closeInventory();
                p.performCommand("proshield claim");
                p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.2f);
                return;
            }
            if (slot == MAIN_INFO) {
                p.closeInventory();
                p.performCommand("proshield info");
                return;
            }
            if (slot == MAIN_REMOVE) {
                p.closeInventory();
                p.performCommand("proshield unclaim");
                return;
            }
            if (slot == MAIN_HELP) {
                p.closeInventory();
                p.performCommand("proshield");
                return;
            }
            if (slot == MAIN_ADMIN && p.hasPermission("proshield.admin.gui")) {
                openAdmin(p);
                return;
            }
            if (slot == MAIN_BACK) {
                p.closeInventory();
                return;
            }
            return;
        }

        // ADMIN
        if (slot == ADM_FIRE)         { toggleConfig("protection.fire.enabled"); refreshAdmin(p); return; }
        if (slot == ADM_EXPLOSIONS)   { toggleConfig("protection.explosions.enabled"); refreshAdmin(p); return; }
        if (slot == ADM_ENTITY_GRIEF) { toggleConfig("protection.entity-grief.enabled"); refreshAdmin(p); return; }
        if (slot == ADM_INTERACTIONS) { toggleConfig("protection.interactions.enabled"); refreshAdmin(p); return; }
        if (slot == ADM_PVP)          { toggleConfig("protection.pvp-in-claims"); refreshAdmin(p); return; }
        if (slot == ADM_KEEPITEMS)    { toggleConfig("claims.keep-items.enabled"); refreshAdmin(p); return; }
        if (slot == ADM_DEBUG)        { toggleConfig("proshield.debug"); refreshAdmin(p); return; }
        if (slot == ADM_COMPASS_DROP) { toggleConfig("compass.drop-if-full"); refreshAdmin(p); return; }
        if (slot == ADM_SPAWN_GUARD)  { toggleConfig("spawn.block-claiming"); refreshAdmin(p); return; }

        if (slot == ADM_RELOAD) {
            // FIX: call plugin.reloadAllConfigs() (not plugin.onConfigReload)
            plugin.reloadAllConfigs();
            // Re-open admin after reload so player sees refreshed toggles
            Bukkit.getScheduler().runTask(plugin, () -> openAdmin(p));
            return;
        }

        if (slot == ADM_PURGE) {
            p.closeInventory();
            p.performCommand("proshield purgeexpired 30 dryrun");
            return;
        }

        if (slot == ADM_BACK) {
            openMain(p); // Back goes to main player menu
            return;
        }
    }

    private void refreshAdmin(Player p) {
        plugin.saveConfig();
        openAdmin(p);
        p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.3f);
    }

    private void toggleConfig(String path) {
        boolean v = plugin.getConfig().getBoolean(path, false);
        plugin.getConfig().set(path, !v);
        plugin.saveConfig();
    }

    // ---------- Compass helpers (unchanged) ----------
    public ItemStack createPlayerCompass() {
        ItemStack it = new ItemStack(Material.COMPASS);
        ItemMeta m = it.getItemMeta();
        m.setDisplayName(color("&bProShield Compass"));
        m.setLore(List.of(color("&7Right-click to open the menu.")));
        m.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        it.setItemMeta(m);
        return it;
    }

    public ItemStack createAdminCompass() {
        ItemStack it = new ItemStack(Material.RECOVERY_COMPASS);
        ItemMeta m = it.getItemMeta();
        m.setDisplayName(color("&cAdmin ProShield"));
        m.setLore(List.of(color("&7Right-click: Admin tools"),
                          color("&8Requires permission: proshield.admin.gui")));
        m.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        it.setItemMeta(m);
        return it;
    }

    public boolean isProShieldCompass(ItemStack stack) {
        if (stack == null || !stack.hasItemMeta() || !stack.getItemMeta().hasDisplayName()) return false;
        String dn = ChatColor.stripColor(stack.getItemMeta().getDisplayName());
        return "ProShield Compass".equalsIgnoreCase(dn) || "Admin ProShield".equalsIgnoreCase(dn);
    }

    // ---------- UI helpers ----------
    private void fill(Inventory inv, ItemStack item) {
        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) == null) inv.setItem(i, item);
        }
    }

    private ItemStack glassPane(short color, String name) {
        ItemStack it = new ItemStack(Material.STAINED_GLASS_PANE, 1, color);
        ItemMeta m = it.getItemMeta();
        m.setDisplayName(name);
        it.setItemMeta(m);
        return it;
    }

    private ItemStack icon(Material mat, String name, List<String> lore) {
        ItemStack it = new ItemStack(mat);
        ItemMeta m = it.getItemMeta();
        m.setDisplayName(color(name));
        if (lore != null) {
            List<String> ll = new ArrayList<>();
            for (String s : lore) ll.add(color(s));
            m.setLore(ll);
        }
        it.setItemMeta(m);
        return it;
    }

    private ItemStack toggle(String label, Material mat, String path) {
        boolean v = plugin.getConfig().getBoolean(path, true);
        return icon(mat, (v ? "&a" : "&c") + label, List.of("&7" + path + ": " + v));
    }

    private ItemStack booleanIcon(String label, Material mat, String path) {
        boolean v = plugin.getConfig().getBoolean(path, false);
        return icon(mat, (v ? "&a" : "&c") + label, List.of("&7" + path + ": " + v));
    }

    private String color(String s) { return ChatColor.translateAlternateColorCodes('&', s); }
}
