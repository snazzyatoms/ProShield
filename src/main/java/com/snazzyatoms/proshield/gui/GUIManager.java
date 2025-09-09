package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.PlotManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public class GUIManager {

    public static final String TITLE_MAIN  = ChatColor.DARK_AQUA + "ProShield";
    public static final String TITLE_ADMIN = ChatColor.DARK_RED + "ProShield • Admin";
    public static final String TITLE_HELP  = ChatColor.DARK_GREEN + "ProShield • Help";

    private final ProShield plugin;
    private final PlotManager plots;
    private final NamespacedKey compKey;

    public GUIManager(ProShield plugin, PlotManager plots) {
        this.plugin = plugin;
        this.plots = plots;
        this.compKey = new NamespacedKey(plugin, "proshield_compass");
    }

    // ---------- Compass ----------
    public ItemStack createPlayerCompass() {
        ItemStack it = new ItemStack(Material.COMPASS);
        ItemMeta m = it.getItemMeta();
        m.setDisplayName(ChatColor.AQUA + "ProShield Compass");
        m.setLore(Arrays.asList(ChatColor.GRAY + "Right-click to open ProShield."));
        it.setItemMeta(m);
        return it;
    }

    public ItemStack createAdminCompass() {
        ItemStack it = new ItemStack(Material.RECOVERY_COMPASS);
        ItemMeta m = it.getItemMeta();
        m.setDisplayName(ChatColor.GOLD + "ProShield Admin Compass");
        m.setLore(Arrays.asList(ChatColor.YELLOW + "Right-click for Admin tools."));
        it.setItemMeta(m);
        return it;
    }

    public boolean isProShieldCompass(ItemStack it) {
        if (it == null || it.getType() == Material.AIR || !it.hasItemMeta()) return false;
        String name = it.getItemMeta().getDisplayName();
        return name != null && (name.contains("ProShield Compass") || name.contains("Admin Compass"));
    }

    public void giveCompass(Player p, boolean admin) {
        ItemStack compass = admin ? createAdminCompass() : createPlayerCompass();
        if (p.getInventory().firstEmpty() == -1) {
            if (plugin.getConfig().getBoolean("compass.drop-if-full", true)) {
                p.getWorld().dropItemNaturally(p.getLocation(), compass);
            }
        } else {
            p.getInventory().addItem(compass);
        }
    }

    public void registerCompassRecipe() {
        // optional: you can add a shaped recipe here (kept simple/omitted to avoid dupes)
    }

    // ---------- Inventories ----------
    public Inventory openMain(Player p, boolean isAdmin) {
        Inventory inv = Bukkit.createInventory(p, 54, TITLE_MAIN);
        fill(inv, new ItemStack(Material.GRAY_STAINED_GLASS_PANE));

        set(inv, plugin.getConfig().getInt("gui.slots.main.create", 11),
                icon(Material.GREEN_WOOL, "&aClaim Chunk", "&7Protect this chunk."));
        set(inv, plugin.getConfig().getInt("gui.slots.main.info", 13),
                icon(Material.PAPER, "&bClaim Info", "&7Owner, Trusted, Roles..."));
        set(inv, plugin.getConfig().getInt("gui.slots.main.remove", 15),
                icon(Material.RED_WOOL, "&cUnclaim", "&7Remove your claim here."));

        set(inv, plugin.getConfig().getInt("gui.slots.main.help", 31),
                icon(Material.BOOK, "&eHelp", "&7Shows only commands you can use."));

        if (isAdmin && p.hasPermission("proshield.admin.gui")) {
            set(inv, plugin.getConfig().getInt("gui.slots.main.admin", 33),
                    icon(Material.NETHER_STAR, "&6Admin Tools", "&7Open Admin controls"));
        }

        set(inv, plugin.getConfig().getInt("gui.slots.main.back", 48),
                icon(Material.ARROW, "&7Back", "&7Return to previous menu"));
        p.openInventory(inv);
        return inv;
    }

    public Inventory openAdmin(Player p) {
        Inventory inv = Bukkit.createInventory(p, 54, TITLE_ADMIN);
        fill(inv, new ItemStack(Material.BLACK_STAINED_GLASS_PANE));

        set(inv, slot("admin.fire", 10), icon(Material.FIRE_CHARGE, "&cFire", state("protection.fire.enabled")));
        set(inv, slot("admin.explosions", 11), icon(Material.TNT, "&cExplosions", state("protection.explosions.enabled")));
        set(inv, slot("admin.entity-grief", 12), icon(Material.ENDERMAN_SPAWN_EGG, "&cEntity Grief", state("protection.entity-grief.enabled")));
        set(inv, slot("admin.interactions", 13), icon(Material.LEVER, "&cInteractions", state("protection.interactions.enabled")));
        set(inv, slot("admin.pvp", 14), icon(Material.IRON_SWORD, "&cPvP in Claims", pvpState()));

        set(inv, slot("admin.keep-items", 20), icon(Material.CHEST, "&aKeep Items", state("claims.keep-items.enabled") + time("claims.keep-items.despawn-seconds")));
        set(inv, slot("admin.purge-expired", 21), icon(Material.BONE, "&6Purge Expired", "&7Runs expiry cleanup"));
        set(inv, slot("admin.reload", 22), icon(Material.REPEATER, "&aReload Config", "&7Reload ProShield configs"));
        set(inv, slot("admin.debug", 23), icon(Material.REDSTONE, "&dDebug", bool(plugin.getConfig().getBoolean("proshield.debug"))));
        set(inv, slot("admin.compass-drop-if-full", 24), icon(Material.COMPASS, "&bCompass drop-if-full", bool(plugin.getConfig().getBoolean("compass.drop-if-full", true))));
        set(inv, slot("admin.spawn-guard", 25), icon(Material.BEACON, "&bSpawn Guard",
                (plugin.getConfig().getBoolean("spawn.block-claiming", true) ? "&aON " : "&cOFF ")
                        + "&7radius: " + plugin.getConfig().getInt("spawn.radius", 32)));

        set(inv, slot("admin.tp-tools", 30), icon(Material.ENDER_PEARL, "&9TP Tools", "&7Teleport to claims"));
        set(inv, slot("admin.help", 22), icon(Material.BOOK, "&eAdmin Help", "&7Toggle and manage server-wide rules."));

        set(inv, slot("admin.back", 31), icon(Material.ARROW, "&7Back", "&7Return to main"));
        p.openInventory(inv);
        return inv;
    }

    public Inventory openHelp(Player p) {
        Inventory inv = Bukkit.createInventory(p, 54, TITLE_HELP);
        fill(inv, new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE));

        set(inv, 20, icon(Material.MAP, "&b/proshield claim", "&7Claim your current chunk"));
        set(inv, 21, icon(Material.PAPER, "&b/proshield info", "&7Info for your current chunk"));
        set(inv, 22, icon(Material.BARRIER, "&b/proshield unclaim", "&7Remove your claim"));
        set(inv, 23, icon(Material.NAME_TAG, "&b/proshield trust <player> [role]", "&7Trust with role"));
        set(inv, 24, icon(Material.NAME_TAG, "&b/proshield untrust <player>", "&7Revoke access"));
        set(inv, 25, icon(Material.BOOK, "&b/proshield trusted", "&7List trusted players"));
        set(inv, 49, icon(Material.ARROW, "&7Back", "&7Return"));
        p.openInventory(inv);
        return inv;
    }

    // ---------- Helpers ----------
    private ItemStack icon(Material mat, String name, String... lore) {
        ItemStack it = new ItemStack(mat);
        ItemMeta m = it.getItemMeta();
        m.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        m.setLore(Arrays.stream(lore).map(s -> ChatColor.translateAlternateColorCodes('&', s)).toList());
        it.setItemMeta(m);
        return it;
    }

    private void set(Inventory inv, int slot, ItemStack item) {
        if (slot >= 0 && slot < inv.getSize()) inv.setItem(slot, item);
    }

    private void fill(Inventory inv, ItemStack filler) {
        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) == null || inv.getItem(i).getType() == Material.AIR) inv.setItem(i, filler);
        }
    }

    private String state(String path) { return plugin.getConfig().getBoolean(path, true) ? "&aON" : "&cOFF"; }
    private String bool(boolean b) { return b ? "&aON" : "&cOFF"; }
    private String time(String path) { return " &7(" + plugin.getConfig().getInt(path, 900) + "s)"; }
    private String pvpState() { return plugin.getConfig().getBoolean("protection.pvp-in-claims", false) ? "&cON" : "&aOFF"; }
    private int slot(String path, int def) { return plugin.getConfig().getInt("gui.slots." + path, def); }

    public boolean isOurInventory(InventoryView view) {
        if (view == null) return false;
        String t = view.getTitle();
        return t.equals(TITLE_MAIN) || t.equals(TITLE_ADMIN) || t.equals(TITLE_HELP);
    }

    public void handleInventoryClick(Player p, int rawSlot, ItemStack clicked, InventoryView view) {
        String title = view.getTitle();
        if (TITLE_MAIN.equals(title)) {
            if (rawSlot == slot("main.create", 11)) p.performCommand("proshield claim");
            else if (rawSlot == slot("main.info", 13)) p.performCommand("proshield info");
            else if (rawSlot == slot("main.remove", 15)) p.performCommand("proshield unclaim");
            else if (rawSlot == slot("main.help", 31)) openHelp(p);
            else if (rawSlot == slot("main.admin", 33) && p.hasPermission("proshield.admin.gui")) openAdmin(p);
            else if (rawSlot == slot("main.back", 48)) p.closeInventory();
        }
        else if (TITLE_ADMIN.equals(title)) {
            if (rawSlot == slot("admin.fire", 10)) toggle("protection.fire.enabled");
            else if (rawSlot == slot("admin.explosions", 11)) toggle("protection.explosions.enabled");
            else if (rawSlot == slot("admin.entity-grief", 12)) toggle("protection.entity-grief.enabled");
            else if (rawSlot == slot("admin.interactions", 13)) toggle("protection.interactions.enabled");
            else if (rawSlot == slot("admin.pvp", 14)) toggle("protection.pvp-in-claims");
            else if (rawSlot == slot("admin.keep-items", 20)) toggle("claims.keep-items.enabled");
            else if (rawSlot == slot("admin.purge-expired", 21)) p.performCommand("proshield purgeexpired " + plugin.getConfig().getInt("expiry.days",30) + " dryrun");
            else if (rawSlot == slot("admin.reload", 22)) plugin.reloadAllConfigs();
            else if (rawSlot == slot("admin.debug", 23)) {
                boolean d = plugin.getConfig().getBoolean("proshield.debug", false);
                plugin.getConfig().set("proshield.debug", !d);
                plugin.saveConfig();
            } else if (rawSlot == slot("admin.compass-drop-if-full", 24)) toggle("compass.drop-if-full");
            else if (rawSlot == slot("admin.spawn-guard", 25)) toggle("spawn.block-claiming");
            else if (rawSlot == slot("admin.tp-tools", 30)) p.performCommand("proshield info"); // simple hook
            else if (rawSlot == slot("admin.back", 31)) openMain(p, p.hasPermission("proshield.admin.gui"));
            openAdmin(p); // refresh states
        }
        else if (TITLE_HELP.equals(title)) {
            if (rawSlot == 49) openMain(p, p.hasPermission("proshield.admin.gui"));
        }
    }

    private void toggle(String path) {
        boolean cur = plugin.getConfig().getBoolean(path, false);
        plugin.getConfig().set(path, !cur);
        plugin.saveConfig();
    }

    public void onConfigReload() {
        // nothing complex needed; GUIs read live config
    }
}
