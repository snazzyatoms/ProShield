// path: src/main/java/com/snazzyatoms/proshield/gui/GUIManager.java
package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.PlotManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

/**
 * Central GUI builder/handler. Includes:
 * - Main (player) menu
 * - Admin menu (with Reload)
 * - Help pages
 * - Working Back button (GUI history per-player)
 * - Compass helpers (admin/player), isOurInventory(), click handling
 */
public class GUIManager {

    private final ProShield plugin;
    private final PlotManager plots;

    // Titles
    public static final String TITLE_MAIN  = ChatColor.DARK_AQUA + "ProShield";
    public static final String TITLE_ADMIN = ChatColor.DARK_RED + "ProShield Admin";
    public static final String TITLE_HELP  = ChatColor.GOLD + "ProShield Help";

    // PDC keys for identifying our inventories and compass items
    private final NamespacedKey keyGui;
    private final NamespacedKey keyCompassKind; // "admin" or "player"

    // GUI slots (loaded from config; defaults if missing)
    private int sMainCreate=11, sMainInfo=13, sMainRemove=15, sMainAdmin=33, sMainHelp=49, sMainBack=48;
    private int sAdminDropIfFull=20, sAdminHelp=22, sAdminBack=31, sAdminReload=25;

    // Simple GUI history: previous inventories per player (LIFO)
    private final Map<UUID, Deque<Inventory>> history = new HashMap<>();

    public GUIManager(ProShield plugin, PlotManager plots) {
        this.plugin = plugin;
        this.plots = plots;
        this.keyGui = new NamespacedKey(plugin, "ps-gui");
        this.keyCompassKind = new NamespacedKey(plugin, "ps-compass-kind");
        readGuiSlots();
    }

    /* -----------------------------
       Public entry points (openers)
       ----------------------------- */

    public void openMain(Player p) {
        pushAndOpen(p, buildMain(p));
    }

    public void openAdmin(Player p) {
        // Guard: permission
        if (!p.hasPermission("proshield.admin.gui") && !p.hasPermission("proshield.admin")) {
            p.sendMessage(prefix() + ChatColor.RED + "You don't have permission to open the admin menu.");
            return;
        }
        pushAndOpen(p, buildAdmin(p));
    }

    public void openHelp(Player p) {
        pushAndOpen(p, buildHelp(p));
    }

    /* -----------------------------
       Back / History
       ----------------------------- */

    /** Call this when opening a GUI to enable Back. */
    private void pushAndOpen(Player p, Inventory next) {
        // push current top if our GUI
        InventoryView view = p.getOpenInventory();
        if (view != null && isOurInventory(view)) {
            history.computeIfAbsent(p.getUniqueId(), u -> new ArrayDeque<>()).push(view.getTopInventory());
        }
        p.openInventory(next);
    }

    /** Handle a Back click. */
    private void goBack(Player p) {
        Deque<Inventory> stack = history.get(p.getUniqueId());
        if (stack == null || stack.isEmpty()) {
            // Fallback: go to main
            p.openInventory(buildMain(p));
            return;
        }
        Inventory prev = stack.pop();
        p.openInventory(prev);
    }

    /** Clear history when player leaves the GUI world (called optionally on quit/close). */
    public void clearHistory(UUID playerId) {
        history.remove(playerId);
    }

    /* -----------------------------
       Inventory builders
       ----------------------------- */

    private Inventory buildMain(Player p) {
        Inventory inv = Bukkit.createInventory(new Holder(Holder.Type.MAIN), 54, TITLE_MAIN);
        ItemStack filler = glass(ChatColor.DARK_GRAY + " ");
        fill(inv, filler);

        inv.setItem(sMainCreate, button(Material.OAK_SAPLING, ChatColor.GREEN + "Claim Chunk",
                list("Claim your current chunk.")));
        inv.setItem(sMainInfo, button(Material.PAPER, ChatColor.AQUA + "Claim Info",
                list("View ownership & trusted players.")));
        inv.setItem(sMainRemove, button(Material.BARRIER, ChatColor.RED + "Unclaim",
                list("Remove your current claim.")));

        if (p.hasPermission("proshield.admin") || p.hasPermission("proshield.admin.gui")) {
            inv.setItem(sMainAdmin, button(Material.REDSTONE, ChatColor.RED + "Admin Menu",
                    list("Manage server-wide settings.")));
        } else {
            inv.setItem(sMainAdmin, button(Material.GRAY_DYE, ChatColor.DARK_GRAY + "Admin Menu",
                    list("No permission")));
        }

        inv.setItem(sMainHelp, button(Material.BOOK, ChatColor.GOLD + "Help",
                list("Only shows commands you can use.")));

        inv.setItem(sMainBack, backButton());
        return tag(inv);
    }

    private Inventory buildAdmin(Player p) {
        Inventory inv = Bukkit.createInventory(new Holder(Holder.Type.ADMIN), 54, TITLE_ADMIN);
        ItemStack filler = glass(ChatColor.DARK_GRAY + " ");
        fill(inv, filler);

        // Toggle: "Drop compass if full" (autogive fallback)
        boolean dropIfFull = plugin.getConfig().getBoolean("compass.drop-if-full", true);
        inv.setItem(sAdminDropIfFull, toggle(
                dropIfFull ? Material.LIME_DYE : Material.RED_DYE,
                ChatColor.YELLOW + "Drop Compass If Inventory Full",
                dropIfFull
                        ? list("Currently: " + ChatColor.GREEN + "ON", "If inventory is full, drop at feet.")
                        : list("Currently: " + ChatColor.RED + "OFF", "If inventory is full, do NOT drop at feet.")
        ));

        // Reload action
        inv.setItem(sAdminReload, button(Material.REPEATER, ChatColor.AQUA + "Reload",
                list("Reload config & caches", "Requires permission: proshield.admin.reload")));

        // Small admin help tooltip
        inv.setItem(sAdminHelp, button(Material.WRITABLE_BOOK, ChatColor.GOLD + "Admin Help",
                list("Tips:",
                     "- Left-click toggles/settings",
                     "- Reload re-reads config",
                     "- Back returns to previous screen",
                     ChatColor.DARK_GRAY + "More tools in 2.0!")));

        inv.setItem(sAdminBack, backButton());
        return tag(inv);
    }

    private Inventory buildHelp(Player p) {
        Inventory inv = Bukkit.createInventory(new Holder(Holder.Type.HELP), 54, TITLE_HELP);
        ItemStack filler = glass(ChatColor.DARK_GRAY + " ");
        fill(inv, filler);

        // Show only commands the player can use (lightweight)
        List<String> lines = new ArrayList<>();
        addIfPerm(p, lines, "proshield.use", "/proshield", "/proshield claim", "/proshield info", "/proshield unclaim");
        addIfPerm(p, lines, "proshield.use", "/proshield trust <player> [role]", "/proshield untrust <player>", "/proshield trusted");
        addIfPerm(p, lines, "proshield.compass", "/proshield compass");
        addIfPerm(p, lines, "proshield.admin", "/proshield bypass <on|off|toggle>", "/proshield purgeexpired <days> [dryrun]");
        addIfPerm(p, lines, "proshield.admin.reload", "/proshield reload");

        inv.setItem(22, scroll(Material.MAP, ChatColor.GOLD + "Commands You Can Use", lines));
        inv.setItem(49, backButton());
        return tag(inv);
    }

    /* -----------------------------
       Click Handling
       ----------------------------- */

    public boolean isOurInventory(InventoryView view) {
        if (view == null) return false;
        Inventory top = view.getTopInventory();
        if (!(top.getHolder() instanceof Holder)) return false;
        // also check PDC tag for extra safety
        return top.getItem(0) != null || ((Holder) top.getHolder()).type != null; // holder check enough
    }

    public void handleInventoryClick(Player p, int slot, ItemStack clicked, InventoryView view) {
        if (view == null || !(view.getTopInventory().getHolder() instanceof Holder)) return;
        Holder holder = (Holder) view.getTopInventory().getHolder();

        // Back button universal handling
        if (isBackButton(clicked)) {
            p.closeInventory(); // close first to avoid flicker on some servers
            goBack(p);
            return;
        }

        // Route by menu type
        switch (holder.type) {
            case MAIN -> handleMainClick(p, slot, clicked);
            case ADMIN -> handleAdminClick(p, slot, clicked);
            case HELP -> handleHelpClick(p, slot, clicked);
        }
    }

    private void handleMainClick(Player p, int slot, ItemStack item) {
        if (slot == sMainCreate) {
            p.closeInventory();
            p.performCommand("proshield claim");
            return;
        }
        if (slot == sMainInfo) {
            p.closeInventory();
            p.performCommand("proshield info");
            return;
        }
        if (slot == sMainRemove) {
            p.closeInventory();
            p.performCommand("proshield unclaim");
            return;
        }
        if (slot == sMainAdmin) {
            if (p.hasPermission("proshield.admin") || p.hasPermission("proshield.admin.gui")) {
                openAdmin(p);
            } else {
                p.sendMessage(prefix() + ChatColor.RED + "No permission.");
            }
            return;
        }
        if (slot == sMainHelp) {
            openHelp(p);
        }
    }

    private void handleAdminClick(Player p, int slot, ItemStack item) {
        if (!p.hasPermission("proshield.admin") && !p.hasPermission("proshield.admin.gui")) {
            p.sendMessage(prefix() + ChatColor.RED + "No permission.");
            return;
        }

        if (slot == sAdminDropIfFull) {
            boolean current = plugin.getConfig().getBoolean("compass.drop-if-full", true);
            plugin.getConfig().set("compass.drop-if-full", !current);
            plugin.saveConfig();
            p.sendMessage(prefix() + "Set compass.drop-if-full: " + ChatColor.YELLOW + (!current));
            // refresh screen
            p.openInventory(buildAdmin(p));
            return;
        }

        if (slot == sAdminReload) {
            if (!p.hasPermission("proshield.admin.reload")) {
                p.sendMessage(prefix() + ChatColor.RED + "You need proshield.admin.reload to do this.");
                return;
            }
            // Reload configs & caches via plugin
            plugin.reloadAllConfigs();
            p.sendMessage(prefix() + ChatColor.GREEN + "Config reloaded.");
            // Reopen admin to reflect new settings
            p.openInventory(buildAdmin(p));
            return;
        }

        if (slot == sAdminHelp) {
            openHelp(p);
            return;
        }

        if (slot == sAdminBack) {
            p.closeInventory();
            goBack(p);
        }
    }

    private void handleHelpClick(Player p, int slot, ItemStack item) {
        if (slot == 49) {
            p.closeInventory();
            goBack(p);
        }
    }

    /* -----------------------------
       Compass helpers
       ----------------------------- */

    public void registerCompassRecipe() {
        // (Idempotent) Admin compass recipe
        try {
            ShapedRecipe admin = new ShapedRecipe(new NamespacedKey(plugin, "proshield_admin_compass"),
                    createAdminCompass());
            admin.shape(" R ", "RCR", " R ");
            admin.setIngredient('R', Material.REDSTONE);
            admin.setIngredient('C', Material.COMPASS);
            Bukkit.addRecipe(admin);
        } catch (IllegalStateException ignored) {
            // recipe may already be registered
        }

        // (Optional) Player compass recipe (commented out by default)
        // ShapedRecipe player = new ShapedRecipe(new NamespacedKey(plugin, "proshield_player_compass"),
        //         createPlayerCompass());
        // player.shape(" I ", "ICI", " I ");
        // player.setIngredient('I', Material.IRON_INGOT);
        // player.setIngredient('C', Material.COMPASS);
        // Bukkit.addRecipe(player);
    }

    public ItemStack createAdminCompass() {
        ItemStack it = new ItemStack(Material.COMPASS);
        ItemMeta meta = it.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "ProShield Admin Compass");
        meta.setLore(list(ChatColor.GRAY + "Right-click to open Admin GUI"));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.getPersistentDataContainer().set(keyCompassKind, PersistentDataType.STRING, "admin");
        it.setItemMeta(meta);
        return it;
    }

    public ItemStack createPlayerCompass() {
        ItemStack it = new ItemStack(Material.COMPASS);
        ItemMeta meta = it.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + "ProShield Compass");
        meta.setLore(list(ChatColor.GRAY + "Right-click to open ProShield"));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.getPersistentDataContainer().set(keyCompassKind, PersistentDataType.STRING, "player");
        it.setItemMeta(meta);
        return it;
    }

    public boolean isProShieldCompass(ItemStack item) {
        if (item == null || item.getType() != Material.COMPASS || !item.hasItemMeta()) return false;
        String kind = item.getItemMeta().getPersistentDataContainer().get(keyCompassKind, PersistentDataType.STRING);
        return "admin".equals(kind) || "player".equals(kind);
    }

    /** Give either player or admin compass; adminPreferred gives admin compass to players with admin perms. */
    public void giveCompass(Player p, boolean adminPreferred) {
        ItemStack compass;
        if (adminPreferred && (p.hasPermission("proshield.admin") || p.isOp())) {
            compass = createAdminCompass();
        } else {
            compass = createPlayerCompass();
        }

        HashMap<Integer, ItemStack> excess = p.getInventory().addItem(compass);
        if (!excess.isEmpty()) {
            boolean dropIfFull = plugin.getConfig().getBoolean("compass.drop-if-full", true);
            if (dropIfFull) {
                p.getWorld().dropItemNaturally(p.getLocation(), compass);
                p.sendMessage(prefix() + ChatColor.YELLOW + "Inventory full; dropped a ProShield compass at your feet.");
            } else {
                p.sendMessage(prefix() + ChatColor.RED + "Inventory full; could not give compass.");
            }
        }
    }

    /** Called by plugin after a reload to re-read slot mapping etc. */
    public void onConfigReload() {
        readGuiSlots();
    }

    /* -----------------------------
       Internals / utilities
       ----------------------------- */

    private String prefix() {
        return ChatColor.DARK_AQUA + "[ProShield] " + ChatColor.RESET;
    }

    private void readGuiSlots() {
        // defaults already set in fields
        ConfigurationSection base = plugin.getConfig().getConfigurationSection("gui.slots");
        if (base != null) {
            ConfigurationSection main = base.getConfigurationSection("main");
            if (main != null) {
                sMainCreate = main.getInt("create", sMainCreate);
                sMainInfo = main.getInt("info", sMainInfo);
                sMainRemove = main.getInt("remove", sMainRemove);
                sMainAdmin = main.getInt("admin", sMainAdmin);
                sMainHelp = main.getInt("help", sMainHelp);
                sMainBack = main.getInt("back", sMainBack);
            }
            ConfigurationSection adm = base.getConfigurationSection("admin");
            if (adm != null) {
                sAdminDropIfFull = adm.getInt("toggle-drop-if-full", sAdminDropIfFull);
                sAdminHelp = adm.getInt("help", sAdminHelp);
                sAdminBack = adm.getInt("back", sAdminBack);
                sAdminReload = adm.getInt("reload", sAdminReload);
            }
        }
    }

    private Inventory tag(Inventory inv) {
        // Lightweight: holder already marks it as ours
        return inv;
    }

    private ItemStack glass(String name) {
        ItemStack it = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta m = it.getItemMeta();
        m.setDisplayName(name);
        it.setItemMeta(m);
        return it;
    }

    private ItemStack button(Material mat, String name, List<String> lore) {
        ItemStack it = new ItemStack(mat);
        ItemMeta m = it.getItemMeta();
        m.setDisplayName(name);
        m.setLore(lore);
        it.setItemMeta(m);
        return it;
    }

    private ItemStack toggle(Material mat, String name, List<String> lore) {
        return button(mat, name, lore);
    }

    private ItemStack backButton() {
        return button(Material.ARROW, ChatColor.YELLOW + "Back", list("Return to previous page"));
    }

    private ItemStack scroll(Material mat, String name, List<String> lore) {
        return button(mat, name, lore);
    }

    private boolean isBackButton(ItemStack it) {
        if (it == null || !it.hasItemMeta()) return false;
        String dn = it.getItemMeta().getDisplayName();
        return dn != null && ChatColor.stripColor(dn).equalsIgnoreCase("Back") && it.getType() == Material.ARROW;
    }

    private List<String> list(String... arr) {
        return Arrays.asList(arr);
    }

    private void addIfPerm(Player p, List<String> out, String perm, String... commands) {
        if (p.hasPermission(perm)) {
            out.add(ChatColor.YELLOW + "• " + ChatColor.WHITE + String.join(", ", commands));
        }
    }

    private void fill(Inventory inv, ItemStack filler) {
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack curr = inv.getItem(i);
            if (curr == null || curr.getType() == Material.AIR) inv.setItem(i, filler);
        }
    }

    /* -----------------------------
       Holder marker
       ----------------------------- */
    private static final class Holder implements InventoryHolder {
        enum Type { MAIN, ADMIN, HELP }
        final Type type;
        Holder(Type type) { this.type = type; }
        @Override public Inventory getInventory() { return Bukkit.createInventory(null, InventoryType.CHEST); }
    }
}
