package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.PlotManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * ProShield GUI Manager
 * - Builds Player Main, Admin, and Help GUIs
 * - Tracks per-player "back" history
 * - Exposes helpers for GUIListener & PlayerJoinListener
 */
public class GUIManager {

    // Titles must be compile-time constants for String switch/case:
    private static final String TITLE_MAIN  = "§3ProShield";
    private static final String TITLE_ADMIN = "§4ProShield Admin";
    private static final String TITLE_HELP  = "§6ProShield Help";

    private final ProShield plugin;
    private final PlotManager plots;

    // Back navigation: store a stack of titles per player
    private final Map<UUID, Deque<String>> backStackTitle = new HashMap<>();

    // Slot mapping (read from config; reload-safe)
    private int MAIN_CREATE, MAIN_INFO, MAIN_REMOVE, MAIN_HELP, MAIN_ADMIN, MAIN_BACK;
    private int ADM_FIRE, ADM_EXPLOSIONS, ADM_ENTITY_GRIEF, ADM_INTERACTIONS, ADM_PVP;
    private int ADM_KEEP_ITEMS, ADM_PURGE_EXPIRED, ADM_DEBUG, ADM_COMPASS_DROP, ADM_SPAWN_GUARD, ADM_TP_TOOLS, ADM_BACK, ADM_HELP, ADM_RELOAD;

    public GUIManager(ProShield plugin, PlotManager plots) {
        this.plugin = plugin;
        this.plots = plots;
        readSlotsFromConfig();
    }

    /** Called by /proshield reload and Admin Reload button. */
    public void onConfigReload() {
        readSlotsFromConfig();
    }

    // =========================
    // ==== Public helpers  ====
    // =========================

    /** Open Player Main GUI (push to history). */
    public void openMain(Player p) {
        Inventory inv = buildMain(p);
        pushHistory(p, TITLE_MAIN);
        p.openInventory(inv);
        clickSfx(p);
    }

    /** Back-compat overload (some older call sites used a boolean). */
    public void openMain(Player p, boolean ignored) {
        openMain(p);
    }

    /** Open Admin GUI (push to history). */
    public void openAdmin(Player p) {
        Inventory inv = buildAdmin(p);
        pushHistory(p, TITLE_ADMIN);
        p.openInventory(inv);
        clickSfx(p);
    }

    /** Open Help GUI (push to history). */
    public void openHelp(Player p) {
        Inventory inv = buildHelp(p);
        pushHistory(p, TITLE_HELP);
        p.openInventory(inv);
        clickSfx(p);
    }

    /** Back navigation; returns true if a previous page was opened. */
    public boolean goBack(Player p) {
        // Pop current
        popHistory(p);
        String prev = peekHistory(p);
        if (prev == null) {
            p.closeInventory();
            return false;
        }
        switch (prev) {
            case TITLE_ADMIN -> p.openInventory(buildAdmin(p));
            case TITLE_HELP  -> p.openInventory(buildHelp(p));
            default          -> p.openInventory(buildMain(p));
        }
        clickSfx(p);
        return true;
    }

    /** Give proper compass (admin vs player) with fallback drop when full. */
    public void giveCompass(Player p, boolean dropIfFull) {
        ItemStack compass = (p.hasPermission("proshield.admin") || p.hasPermission("proshield.admin.gui"))
                ? createAdminCompass() : createPlayerCompass();
        Map<Integer, ItemStack> leftover = p.getInventory().addItem(compass);
        if (!leftover.isEmpty() && dropIfFull) {
            p.getWorld().dropItemNaturally(p.getLocation(), compass);
        }
    }

    /** Identify a ProShield compass. */
    public boolean isProShieldCompass(ItemStack item) {
        if (item == null || item.getType() != Material.COMPASS) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return false;
        String dn = ChatColor.stripColor(meta.getDisplayName()).toLowerCase(Locale.ROOT);
        return dn.contains("proshield");
    }

    /** Minimal “is ours?” check for GUIListener. */
    public boolean isOurInventory(InventoryView view) {
        if (view == null) return false;
        String t = view.getTitle();
        return TITLE_MAIN.equals(t) || TITLE_ADMIN.equals(t) || TITLE_HELP.equals(t);
    }

    /** Central click router for GUIListener. */
    public void handleInventoryClick(Player p, int slot, ItemStack clicked, InventoryView view) {
        String title = view.getTitle();

        // MAIN
        if (TITLE_MAIN.equals(title)) {
            if (slot == MAIN_CREATE) {
                p.performCommand("proshield claim");
                p.closeInventory();
            } else if (slot == MAIN_INFO) {
                p.performCommand("proshield info");
                p.closeInventory();
            } else if (slot == MAIN_REMOVE) {
                p.performCommand("proshield unclaim");
                p.closeInventory();
            } else if (slot == MAIN_HELP) {
                openHelp(p);
            } else if (slot == MAIN_ADMIN) {
                if (p.hasPermission("proshield.admin") || p.hasPermission("proshield.admin.gui")) {
                    openAdmin(p);
                } else {
                    p.sendMessage(prefix() + ChatColor.RED + "You don't have permission to open the Admin menu.");
                }
            } else if (slot == MAIN_BACK) {
                goBack(p);
            }
            return;
        }

        // ADMIN
        if (TITLE_ADMIN.equals(title)) {
            if (!p.hasPermission("proshield.admin") && !p.hasPermission("proshield.admin.gui")) {
                p.sendMessage(prefix() + ChatColor.RED + "Admin-only menu.");
                p.closeInventory();
                return;
            }

            boolean changed = false;
            FileConfiguration cfg = plugin.getConfig();

            if (slot == ADM_FIRE) {
                toggle(cfg, "protection.fire.enabled"); changed = true;
            } else if (slot == ADM_EXPLOSIONS) {
                toggle(cfg, "protection.explosions.enabled"); changed = true;
            } else if (slot == ADM_ENTITY_GRIEF) {
                toggle(cfg, "protection.entity-grief.enabled"); changed = true;
            } else if (slot == ADM_INTERACTIONS) {
                toggle(cfg, "protection.interactions.enabled"); changed = true;
            } else if (slot == ADM_PVP) {
                // true = PvP allowed in claims; false = PvP blocked (safer default)
                toggle(cfg, "protection.pvp-in-claims"); changed = true;
            } else if (slot == ADM_KEEP_ITEMS) {
                toggle(cfg, "claims.keep-items.enabled"); changed = true;
            } else if (slot == ADM_DEBUG) {
                boolean newVal = !plugin.isDebug();
                plugin.setDebug(newVal);
                cfg.set("proshield.debug", newVal);
                changed = true;
            } else if (slot == ADM_COMPASS_DROP) {
                toggle(cfg, "compass.drop-if-full"); changed = true;
            } else if (slot == ADM_SPAWN_GUARD) {
                toggle(cfg, "spawn.block-claiming"); changed = true;
            } else if (slot == ADM_RELOAD) {
                plugin.saveConfig();
                plugin.onConfigReload();
                p.sendMessage(prefix() + ChatColor.YELLOW + "Config reloaded.");
                openAdmin(p); // reflect fresh state
                return;
            } else if (slot == ADM_TP_TOOLS) {
                p.sendMessage(prefix() + ChatColor.GRAY + "Teleport tools coming soon.");
                p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.0f);
                return;
            } else if (slot == ADM_HELP) {
                openHelp(p);
                return;
            } else if (slot == ADM_BACK) {
                goBack(p);
                return;
            }

            if (changed) {
                plugin.saveConfig();
                openAdmin(p);
            }
            return;
        }

        // HELP
        if (TITLE_HELP.equals(title)) {
            if (slot == MAIN_BACK || slot == ADM_BACK) {
                goBack(p);
            }
        }
    }

    // ==============================
    // ==== Compass builders     ====
    // ==============================

    public ItemStack createPlayerCompass() {
        ItemStack it = new ItemStack(Material.COMPASS);
        ItemMeta meta = it.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + "ProShield Compass");
        meta.setLore(List.of(
                ChatColor.GRAY + "Open the ProShield menu",
                ChatColor.DARK_GRAY + "(Right-click)"
        ));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        it.setItemMeta(meta);
        return it;
    }

    public ItemStack createAdminCompass() {
        ItemStack it = new ItemStack(Material.COMPASS);
        ItemMeta meta = it.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "ProShield Admin Compass");
        meta.setLore(List.of(
                ChatColor.GRAY + "Open the Admin menu",
                ChatColor.DARK_GRAY + "(Right-click)"
        ));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        it.setItemMeta(meta);
        return it;
    }

    // =================================================
    // ==== Inventory builders (Main / Admin / Help) ===
    // =================================================

    private Inventory buildMain(Player p) {
        Inventory inv = Bukkit.createInventory(p, 54, TITLE_MAIN);
        fill(inv, pane("§8ProShield"));

        inv.setItem(MAIN_CREATE, icon(Material.GRASS_BLOCK, ChatColor.GREEN + "Claim Chunk",
                List.of(ChatColor.GRAY + "Claim the chunk you are standing in")));
        inv.setItem(MAIN_INFO, icon(Material.BOOK, ChatColor.AQUA + "Claim Info",
                List.of(ChatColor.GRAY + "See owner/trusted players")));
        inv.setItem(MAIN_REMOVE, icon(Material.BARRIER, ChatColor.RED + "Unclaim Chunk",
                List.of(ChatColor.GRAY + "Release your claim")));
        inv.setItem(MAIN_HELP, icon(Material.PAPER, ChatColor.GOLD + "Help",
                List.of(ChatColor.GRAY + "Commands & tips")));

        if (p.hasPermission("proshield.admin") || p.hasPermission("proshield.admin.gui")) {
            inv.setItem(MAIN_ADMIN, icon(Material.REDSTONE, ChatColor.DARK_RED + "Admin Menu",
                    List.of(ChatColor.GRAY + "Admin tools & toggles")));
        }

        inv.setItem(MAIN_BACK, backButton());
        return inv;
    }

    private Inventory buildAdmin(Player p) {
        Inventory inv = Bukkit.createInventory(p, 54, TITLE_ADMIN);
        fill(inv, pane("§8Admin Controls"));

        FileConfiguration cfg = plugin.getConfig();

        inv.setItem(ADM_FIRE,         toggleIcon(Material.FLINT_AND_STEEL, "protection.fire.enabled", ChatColor.GOLD + "Fire Protection", cfg));
        inv.setItem(ADM_EXPLOSIONS,   toggleIcon(Material.TNT, "protection.explosions.enabled", ChatColor.RED + "Explosion Protection", cfg));
        inv.setItem(ADM_ENTITY_GRIEF, toggleIcon(Material.ENDERMAN_SPAWN_EGG, "protection.entity-grief.enabled", ChatColor.LIGHT_PURPLE + "Entity Griefing", cfg));
        inv.setItem(ADM_INTERACTIONS, toggleIcon(Material.LEVER, "protection.interactions.enabled", ChatColor.YELLOW + "Interaction Guard", cfg));
        inv.setItem(ADM_PVP,          toggleIcon(Material.IRON_SWORD, "protection.pvp-in-claims", ChatColor.DARK_RED + "PvP Allowed in Claims", cfg));

        inv.setItem(ADM_KEEP_ITEMS,   toggleIcon(Material.CHEST, "claims.keep-items.enabled", ChatColor.AQUA + "Keep Items in Claims", cfg));
        inv.setItem(ADM_DEBUG,        toggleIcon(Material.REPEATER, "proshield.debug", ChatColor.GRAY + "Debug Logging", cfg));
        inv.setItem(ADM_COMPASS_DROP, toggleIcon(Material.DROPPER, "compass.drop-if-full", ChatColor.BLUE + "Compass: Drop if Full", cfg));
        inv.setItem(ADM_SPAWN_GUARD,  toggleIcon(Material.SPAWNER, "spawn.block-claiming", ChatColor.DARK_GREEN + "Spawn Guard", cfg));

        inv.setItem(ADM_TP_TOOLS, icon(Material.ENDER_PEARL, ChatColor.DARK_AQUA + "Teleport Tools",
                List.of(ChatColor.GRAY + "Coming soon")));

        inv.setItem(ADM_HELP, icon(Material.PAPER, ChatColor.GOLD + "Admin Help",
                List.of(ChatColor.GRAY + "Quick tips & references")));

        inv.setItem(ADM_RELOAD, icon(Material.COMMAND_BLOCK, ChatColor.GREEN + "Reload Config",
                List.of(ChatColor.GRAY + "Apply latest config changes")));

        inv.setItem(ADM_BACK, backButton());
        return inv;
    }

    private Inventory buildHelp(Player p) {
        Inventory inv = Bukkit.createInventory(p, 54, TITLE_HELP);
        fill(inv, pane("§8Help"));

        inv.setItem(22, icon(Material.KNOWLEDGE_BOOK, ChatColor.GOLD + "Quick Reference", List.of(
                ChatColor.GRAY + "/proshield claim",
                ChatColor.GRAY + "/proshield unclaim",
                ChatColor.GRAY + "/proshield trust <player> [role]",
                ChatColor.GRAY + "/proshield info",
                ChatColor.DARK_GRAY + "Use the compass to open this menu"
        )));

        inv.setItem(MAIN_BACK, backButton());
        return inv;
    }

    // ============================
    // ==== Private utilities   ====
    // ============================

    private void readSlotsFromConfig() {
        FileConfiguration cfg = plugin.getConfig();

        // Main
        MAIN_CREATE = cfg.getInt("gui.slots.main.create", 11);
        MAIN_INFO   = cfg.getInt("gui.slots.main.info", 13);
        MAIN_REMOVE = cfg.getInt("gui.slots.main.remove", 15);
        MAIN_HELP   = cfg.getInt("gui.slots.main.help", 31);
        MAIN_ADMIN  = cfg.getInt("gui.slots.main.admin", 33);
        MAIN_BACK   = cfg.getInt("gui.slots.main.back", 48);

        // Admin
        ADM_FIRE          = cfg.getInt("gui.slots.admin.fire", 10);
        ADM_EXPLOSIONS    = cfg.getInt("gui.slots.admin.explosions", 11);
        ADM_ENTITY_GRIEF  = cfg.getInt("gui.slots.admin.entity-grief", 12);
        ADM_INTERACTIONS  = cfg.getInt("gui.slots.admin.interactions", 13);
        ADM_PVP           = cfg.getInt("gui.slots.admin.pvp", 14);

        ADM_KEEP_ITEMS    = cfg.getInt("gui.slots.admin.keep-items", 20);
        ADM_PURGE_EXPIRED = cfg.getInt("gui.slots.admin.purge-expired", 21);
        ADM_DEBUG         = cfg.getInt("gui.slots.admin.debug", 23);
        ADM_COMPASS_DROP  = cfg.getInt("gui.slots.admin.compass-drop-if-full", 24);
        ADM_RELOAD        = cfg.getInt("gui.slots.admin.reload", 25);
        ADM_SPAWN_GUARD   = cfg.getInt("gui.slots.admin.spawn-guard", 26);
        ADM_TP_TOOLS      = cfg.getInt("gui.slots.admin.tp-tools", 30);
        ADM_BACK          = cfg.getInt("gui.slots.admin.back", 31);
        ADM_HELP          = cfg.getInt("gui.slots.admin.help", 22);
    }

    private void toggle(FileConfiguration cfg, String path) {
        boolean nv = !cfg.getBoolean(path, false);
        cfg.set(path, nv);
    }

    private void pushHistory(Player p, String title) {
        backStackTitle.computeIfAbsent(p.getUniqueId(), k -> new ArrayDeque<>());
        Deque<String> dq = backStackTitle.get(p.getUniqueId());
        if (dq.isEmpty() || !dq.peekLast().equals(title)) {
            dq.addLast(title);
        }
    }

    private void popHistory(Player p) {
        Deque<String> dq = backStackTitle.get(p.getUniqueId());
        if (dq == null || dq.isEmpty()) return;
        dq.removeLast();
    }

    private String peekHistory(Player p) {
        Deque<String> dq = backStackTitle.get(p.getUniqueId());
        if (dq == null || dq.isEmpty()) return null;
        return dq.peekLast();
    }

    private void clickSfx(Player p) {
        try {
            p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.2f);
        } catch (Throwable ignored) {}
    }

    private ItemStack backButton() {
        return icon(Material.ARROW, ChatColor.YELLOW + "Back",
                List.of(ChatColor.GRAY + "Return to previous page"));
    }

    private ItemStack pane(String name) {
        ItemStack it = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta im = it.getItemMeta();
        im.setDisplayName(name);
        it.setItemMeta(im);
        return it;
    }

    private ItemStack icon(Material mat, String name, List<String> lore) {
        ItemStack it = new ItemStack(mat);
        ItemMeta im = it.getItemMeta();
        im.setDisplayName(name);
        im.setLore(lore);
        im.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        it.setItemMeta(im);
        return it;
    }

    private ItemStack toggleIcon(Material mat, String path, String label, FileConfiguration cfg) {
        boolean on = cfg.getBoolean(path, false);
        String state = on ? ChatColor.GREEN + "ON" : ChatColor.RED + "OFF";
        return icon(mat, label, List.of(
                ChatColor.GRAY + "Toggle: " + state,
                ChatColor.DARK_GRAY + "(" + path + ")"
        ));
    }

    /** Fill empty slots with a decorative pane (don’t overwrite placed icons). */
    private void fill(Inventory inv, ItemStack filler) {
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack cur = inv.getItem(i);
            if (cur == null || cur.getType() == Material.AIR) {
                inv.setItem(i, filler);
            }
        }
    }

    private String prefix() {
        return plugin.getConfig().getString("messages.prefix", ChatColor.DARK_AQUA + "[ProShield] ") + ChatColor.RESET;
    }
}
