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
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Player + Admin GUIs (v1.2.5)
 * - Uses GUICache for snappy render
 * - Preserves Admin GUI from 1.2.4 (with Reload + Back fixed)
 * - Adds Player submenus: Trust/Untrust, Roles, Flags, Transfer, Settings, Help
 */
public class GUIManager {

    private final ProShield plugin;
    private final PlotManager plots;
    private final GUICache cache;

    // Persistent tag used on our items/inventories
    private final NamespacedKey keyIsProShieldItem;
    private final NamespacedKey keyMenuId;

    // Menu identifiers (used as cache keys + holder ids)
    public enum MenuId {
        MAIN,
        PLAYER_ACTIONS,
        TRUST,
        UNTRUST,
        ROLES,
        FLAGS,
        TRANSFER,
        SETTINGS,
        HELP,
        ADMIN
    }

    // Titles (kept as constants; not using InventoryView#getTitle in listeners)
    private static final String TITLE_MAIN        = ChatColor.AQUA + "ProShield";
    private static final String TITLE_ADMIN       = ChatColor.DARK_AQUA + "ProShield • Admin";
    private static final String TITLE_TRUST       = ChatColor.AQUA + "ProShield • Trust";
    private static final String TITLE_UNTRUST     = ChatColor.AQUA + "ProShield • Untrust";
    private static final String TITLE_ROLES       = ChatColor.AQUA + "ProShield • Roles";
    private static final String TITLE_FLAGS       = ChatColor.AQUA + "ProShield • Flags";
    private static final String TITLE_TRANSFER    = ChatColor.AQUA + "ProShield • Transfer";
    private static final String TITLE_SETTINGS    = ChatColor.AQUA + "ProShield • Settings";
    private static final String TITLE_HELP        = ChatColor.AQUA + "ProShield • Help";

    // Slots (match your config’s GUI layout)
    private static final int SLOT_MAIN_CLAIM      = 11;
    private static final int SLOT_MAIN_INFO       = 13;
    private static final int SLOT_MAIN_UNCLAIM    = 15;
    private static final int SLOT_MAIN_HELP       = 31;
    private static final int SLOT_MAIN_ADMIN      = 33;
    private static final int SLOT_BACK            = 48;

    // Simple custom holder so we can identify inventories safely
    private static final class ProShieldHolder implements InventoryHolder {
        private final MenuId id;
        ProShieldHolder(MenuId id){ this.id = id; }
        @Override public Inventory getInventory() { return null; }
        public MenuId id(){ return id; }
    }

    public GUIManager(ProShield plugin, PlotManager plots) {
        this.plugin = plugin;
        this.plots = plots;
        this.cache = GUICache.get();
        this.keyIsProShieldItem = new NamespacedKey(plugin, "ps_item");
        this.keyMenuId = new NamespacedKey(plugin, "ps_menu");
    }

    /* =========================
     *  Public API (used elsewhere)
     * ========================= */

    public void onConfigReload() {
        // Any dynamic text/icons that rely on config should be cleared here
        cache.invalidateAll();
    }

    public void openMain(Player p) { openCached(p, MenuId.MAIN, TITLE_MAIN, this::buildMain); }

    public void openAdmin(Player p) {
        if (!p.hasPermission("proshield.admin.gui") && !p.hasPermission("proshield.admin")) {
            p.sendMessage(plugin.prefixed("&cYou don't have permission to open the Admin menu."));
            return;
        }
        openCached(p, MenuId.ADMIN, TITLE_ADMIN, this::buildAdmin);
    }

    public void openTrust(Player p)    { openCached(p, MenuId.TRUST,    TITLE_TRUST,    this::buildTrust); }
    public void openUntrust(Player p)  { openCached(p, MenuId.UNTRUST,  TITLE_UNTRUST,  this::buildUntrust); }
    public void openRoles(Player p)    { openCached(p, MenuId.ROLES,    TITLE_ROLES,    this::buildRoles); }
    public void openFlags(Player p)    { openCached(p, MenuId.FLAGS,    TITLE_FLAGS,    this::buildFlags); }
    public void openTransfer(Player p) { openCached(p, MenuId.TRANSFER, TITLE_TRANSFER, this::buildTransfer); }
    public void openSettings(Player p) { openCached(p, MenuId.SETTINGS, TITLE_SETTINGS, this::buildSettings); }
    public void openHelp(Player p)     { openCached(p, MenuId.HELP,     TITLE_HELP,     this::buildHelp); }

    /**
     * Identifies if an Inventory belongs to ProShield by checking its holder.
     */
    public boolean isOurInventory(Inventory inv) {
        if (inv == null) return false;
        InventoryHolder holder = inv.getHolder();
        return holder instanceof ProShieldHolder;
    }

    /**
     * Generic click handler used by GUIListener to route actions.
     */
    public void handleInventoryClick(Player p, Inventory inv, int slot, ItemStack current) {
        if (!isOurInventory(inv)) return;
        MenuId menu = ((ProShieldHolder) inv.getHolder()).id();

        // Back button common in most menus
        if (slot == SLOT_BACK) {
            // In all submenus, going back returns to main menu
            openMain(p);
            p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.2f);
            return;
        }

        switch (menu) {
            case MAIN:
                onMainClick(p, slot, current);
                break;
            case ADMIN:
                onAdminClick(p, slot, current);
                break;
            case TRUST:
                onTrustClick(p, slot, current);
                break;
            case UNTRUST:
                onUntrustClick(p, slot, current);
                break;
            case ROLES:
                onRolesClick(p, slot, current);
                break;
            case FLAGS:
                onFlagsClick(p, slot, current);
                break;
            case TRANSFER:
                onTransferClick(p, slot, current);
                break;
            case SETTINGS:
                onSettingsClick(p, slot, current);
                break;
            case HELP:
                // no special slot actions except back
                break;
        }
    }

    /**
     * Is this the special ProShield Compass?
     */
    public boolean isProShieldCompass(ItemStack stack) {
        if (stack == null || stack.getType() != Material.COMPASS) return false;
        if (!stack.hasItemMeta()) return false;
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return false;
        Byte tag = meta.getPersistentDataContainer().get(keyIsProShieldItem, PersistentDataType.BYTE);
        return tag != null && tag == (byte)1;
    }

    /**
     * Give a ProShield compass: Admin if perms/OP, otherwise Player compass.
     */
    public void giveCompass(Player p, boolean preferAdmin) {
        ItemStack item = (preferAdmin && isAdmin(p)) ? createAdminCompass() : createPlayerCompass();
        p.getInventory().addItem(item);
    }

    public ItemStack createAdminCompass() {
        return makeCompass("&bProShield &7• &6Admin", List.of("&7Right-click to open Admin menu"), true);
    }

    public ItemStack createPlayerCompass() {
        return makeCompass("&bProShield &7• &aMenu", List.of("&7Right-click to open ProShield"), false);
    }

    /* =========================
     *      Builders & Clicks
     * ========================= */

    private void openCached(Player p, MenuId id, String title, Consumer<Inventory> builder) {
        Inventory cached = cache.get(p, id.name());
        if (cached != null) {
            p.openInventory(cached);
            return;
        }
        Inventory inv = Bukkit.createInventory(new ProShieldHolder(id), 54, title);
        tagInventory(inv, id);
        fill(inv, glassPane());
        builder.accept(inv);
        cache.put(p, id.name(), inv);
        p.openInventory(inv);
    }

    private void buildMain(Inventory inv) {
        inv.setItem(SLOT_MAIN_CLAIM,    icon(Material.OAK_SAPLING, "&aClaim Chunk", lore("&7Protect this chunk")));
        inv.setItem(SLOT_MAIN_INFO,     icon(Material.PAPER, "&bClaim Info", lore("&7Owner & trusted players")));
        inv.setItem(SLOT_MAIN_UNCLAIM,  icon(Material.BARRIER, "&cUnclaim", lore("&7Release this chunk")));
        inv.setItem(SLOT_MAIN_HELP,     icon(Material.BOOK, "&eHelp", lore("&7Commands & tips")));
        inv.setItem(SLOT_BACK,          icon(Material.ARROW, "&7Back", lore("&7Close or go back")));

        // Player actions hub (new)
        inv.setItem(20, icon(Material.PLAYER_HEAD, "&aTrust / Untrust", lore("&7Manage trusted players")));
        inv.setItem(21, icon(Material.NAME_TAG, "&aRoles", lore("&7Assign roles to trusted players")));
        inv.setItem(22, icon(Material.REDSTONE_TORCH, "&aFlags", lore("&7Per-claim toggles (PvP, fire, etc.)")));
        inv.setItem(23, icon(Material.ENDER_EYE, "&aTransfer Ownership", lore("&7Give claim to another player")));
        inv.setItem(24, icon(Material.COMPARATOR, "&aSettings", lore("&7GUI options & preferences")));

        // Admin entry (visible only if allowed)
        inv.setItem(SLOT_MAIN_ADMIN, icon(
            isAdminIconMaterial(),
            isAdmin(Bukkit.getPlayerExact(ChatColor.stripColor(inv.getViewers().isEmpty() ? "" : inv.getViewers().get(0).getName()))) ? "&6Admin Menu" : "&8Admin Menu",
            isAdminTitleLore()
        ));
    }

    private void onMainClick(Player p, int slot, ItemStack current) {
        if (slot == SLOT_MAIN_HELP) { openHelp(p); return; }
        if (slot == SLOT_MAIN_CLAIM) { p.performCommand("proshield claim"); p.closeInventory(); return; }
        if (slot == SLOT_MAIN_INFO) { p.performCommand("proshield info"); p.closeInventory(); return; }
        if (slot == SLOT_MAIN_UNCLAIM) { p.performCommand("proshield unclaim"); p.closeInventory(); return; }

        if (slot == 20) { openTrust(p); return; }
        if (slot == 21) { openRoles(p); return; }
        if (slot == 22) { openFlags(p); return; }
        if (slot == 23) { openTransfer(p); return; }
        if (slot == 24) { openSettings(p); return; }

        if (slot == SLOT_MAIN_ADMIN) {
            if (isAdmin(p)) openAdmin(p);
            else p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1.2f);
        }
    }

    private void buildAdmin(Inventory inv) {
        // Keep 1.2.4 admin layout + reload/back
        inv.setItem(10, icon(Material.FLINT_AND_STEEL, "&cFire Protection", lore("&7Toggle fire spread/ignite")));
        inv.setItem(11, icon(Material.TNT, "&cExplosions", lore("&7Creeper/TNT/Wither/etc.")));
        inv.setItem(12, icon(Material.ENDERMAN_SPAWN_EGG, "&cEntity Grief", lore("&7Enderman/Ravager/Silverfish")));
        inv.setItem(13, icon(Material.LEVER, "&cInteractions", lore("&7Doors/buttons/levers/etc.")));
        inv.setItem(14, icon(Material.IRON_SWORD, "&cPvP in Claims", lore("&7Enable/disable PvP in claims")));
        inv.setItem(20, icon(Material.HOPPER, "&6Keep Items", lore("&7Prevent despawn in claims")));
        inv.setItem(21, icon(Material.BONE, "&6Purge Expired", lore("&7Run expiry purge task")));
        inv.setItem(22, icon(Material.BOOK, "&eHelp", lore("&7Summary of admin tools")));
        inv.setItem(23, icon(Material.REDSTONE, "&eDebug Logs", lore("&7Toggle debug logging")));
        inv.setItem(24, icon(Material.CHEST_MINECART, "&eCompass Drop If Full", lore("&7Toggle drop if inventory full")));
        inv.setItem(25, icon(Material.REPEATER, "&aReload", lore("&7Reload config & refresh cache")));
        inv.setItem(30, icon(Material.COMPASS, "&6TP / Tools", lore("&7Teleport & utilities")));
        inv.setItem(31, icon(Material.ARROW, "&7Back", lore("&7Return to player menu")));
    }

    private void onAdminClick(Player p, int slot, ItemStack current) {
        switch (slot) {
            case 10 -> p.performCommand("proshield settings fire toggle");
            case 11 -> p.performCommand("proshield settings explosions toggle");
            case 12 -> p.performCommand("proshield settings entitygrief toggle");
            case 13 -> p.performCommand("proshield settings interactions toggle");
            case 14 -> p.performCommand("proshield settings pvpinclaims toggle");
            case 20 -> p.performCommand("proshield settings keepitems toggle");
            case 21 -> p.performCommand("proshield purgeexpired 30 dryrun");
            case 22 -> openHelp(p);
            case 23 -> p.performCommand("proshield debug toggle");
            case 24 -> p.performCommand("proshield settings compassdrop toggle");
            case 25 -> {
                p.performCommand("proshield reload");
                cache.invalidate(p); // ensure fresh menus post-reload
                openAdmin(p);
            }
            case 30 -> p.performCommand("proshield admin tools");
            case 31 -> openMain(p);
            default -> { /* noop */ }
        }
    }

    private void buildTrust(Inventory inv) {
        inv.setItem(20, icon(Material.PLAYER_HEAD, "&aTrust Player", lore("&7Click to trust someone")));
        inv.setItem(22, icon(Material.WRITABLE_BOOK, "&aRadius Trust", lore("&7Trust nearby players")));
        inv.setItem(24, icon(Material.BOOK, "&eHelp", lore("&7Trust/Untrust guide")));
        inv.setItem(SLOT_BACK, icon(Material.ARROW, "&7Back", lore("&7Return to main")));
    }

    private void onTrustClick(Player p, int slot, ItemStack current) {
        switch (slot) {
            case 20 -> { p.closeInventory(); p.performCommand("proshield trust"); }
            case 22 -> { p.closeInventory(); p.performCommand("proshield trust radius"); }
            case 24 -> openHelp(p);
        }
    }

    private void buildUntrust(Inventory inv) {
        inv.setItem(22, icon(Material.BARRIER, "&cUntrust Player", lore("&7Click to revoke access")));
        inv.setItem(SLOT_BACK, icon(Material.ARROW, "&7Back", lore("&7Return to main")));
    }

    private void onUntrustClick(Player p, int slot, ItemStack current) {
        if (slot == 22) { p.closeInventory(); p.performCommand("proshield untrust"); }
    }

    private void buildRoles(Inventory inv) {
        inv.setItem(19, icon(Material.GRAY_DYE, "&7Visitor", lore("&7Walk only")));
        inv.setItem(21, icon(Material.LIGHT_BLUE_DYE, "&bMember", lore("&7Basic interactions")));
        inv.setItem(23, icon(Material.YELLOW_DYE, "&eContainer", lore("&7Use containers")));
        inv.setItem(25, icon(Material.LIME_DYE, "&aBuilder", lore("&7Build & break")));
        inv.setItem(31, icon(Material.ORANGE_DYE, "&6Co-Owner", lore("&7Near full access")));
        inv.setItem(SLOT_BACK, icon(Material.ARROW, "&7Back", lore("&7Return to main")));
    }

    private void onRolesClick(Player p, int slot, ItemStack current) {
        switch (slot) {
            case 19 -> cmd(p, "proshield role set Visitor");
            case 21 -> cmd(p, "proshield role set Member");
            case 23 -> cmd(p, "proshield role set Container");
            case 25 -> cmd(p, "proshield role set Builder");
            case 31 -> cmd(p, "proshield role set Co-Owner");
        }
    }

    private void buildFlags(Inventory inv) {
        inv.setItem(10, icon(Material.IRON_SWORD, "&cPvP", lore("&7Toggle PvP in this claim")));
        inv.setItem(12, icon(Material.FLINT_AND_STEEL, "&cFire", lore("&7Spread/burn/ignite")));
        inv.setItem(14, icon(Material.TNT, "&cExplosions", lore("&7Creeper/TNT/Wither")));
        inv.setItem(28, icon(Material.ZOMBIE_SPAWN_EGG, "&cMobs", lore("&7Spawn / repel at border")));
        inv.setItem(30, icon(Material.ENDER_PEARL, "&cEnderman Teleport", lore("&7Block inside claims")));
        inv.setItem(SLOT_BACK, icon(Material.ARROW, "&7Back", lore("&7Return to main")));
    }

    private void onFlagsClick(Player p, int slot, ItemStack current) {
        switch (slot) {
            case 10 -> cmd(p, "proshield flag pvp toggle");
            case 12 -> cmd(p, "proshield flag fire toggle");
            case 14 -> cmd(p, "proshield flag explosions toggle");
            case 28 -> cmd(p, "proshield flag mobs toggle");
            case 30 -> cmd(p, "proshield flag enderman-teleport toggle");
        }
    }

    private void buildTransfer(Inventory inv) {
        inv.setItem(22, icon(Material.ENDER_EYE, "&aTransfer Ownership", lore("&7Give claim to another player")));
        inv.setItem(SLOT_BACK, icon(Material.ARROW, "&7Back", lore("&7Return to main")));
    }

    private void onTransferClick(Player p, int slot, ItemStack current) {
        if (slot == 22) { p.closeInventory(); p.performCommand("proshield transfer"); }
    }

    private void buildSettings(Inventory inv) {
        inv.setItem(20, icon(Material.REDSTONE_TORCH, "&aToggle Messages", lore("&7Entry/Exit claim messages")));
        inv.setItem(22, icon(Material.COMPARATOR, "&aGUI Preferences", lore("&7Compact / Detailed mode")));
        inv.setItem(24, icon(Material.MAP, "&aCompass Behavior", lore("&7Auto-open / sounds")));
        inv.setItem(SLOT_BACK, icon(Material.ARROW, "&7Back", lore("&7Return to main")));
    }

    private void onSettingsClick(Player p, int slot, ItemStack current) {
        switch (slot) {
            case 20 -> cmd(p, "proshield settings messages toggle");
            case 22 -> cmd(p, "proshield settings gui compact toggle");
            case 24 -> cmd(p, "proshield settings compass autoopen toggle");
        }
    }

    private void buildHelp(Inventory inv) {
        List<String> lines = new ArrayList<>();
        lines.add(ChatColor.GRAY + "• " + ChatColor.WHITE + "/proshield claim" + ChatColor.GRAY + " – claim your current chunk");
        lines.add(ChatColor.GRAY + "• " + ChatColor.WHITE + "/proshield trust <player> [role]");
        lines.add(ChatColor.GRAY + "• " + ChatColor.WHITE + "/proshield unclaim, /proshield info");
        inv.setItem(22, icon(Material.BOOK, "&eQuick Help", lines));
        inv.setItem(SLOT_BACK, icon(Material.ARROW, "&7Back", lore("&7Return to main")));
    }

    private void onUntrustClick(Player p) { /* handled in onUntrustClick(Player,int,ItemStack) */ }

    /* =========================
     *    Internal utilities
     * ========================= */

    private void tagInventory(Inventory inv, MenuId id) {
        // Extra holder type already tags it, but we also tag items if needed later
        // Here we can insert a tiny marker item in an unused slot if you want future-proofing.
    }

    private boolean isAdmin(Player p) {
        return p != null && (p.isOp() || p.hasPermission("proshield.admin") || p.hasPermission("proshield.admin.gui"));
    }

    private Material isAdminIconMaterial() {
        return Material.NETHER_STAR;
    }

    private List<String> isAdminTitleLore() {
        List<String> l = new ArrayList<>();
        l.add(ChatColor.GRAY + "Open the Admin menu");
        return l;
    }

    private ItemStack glassPane() {
        ItemStack s = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta m = s.getItemMeta();
        if (m != null) {
            m.setDisplayName(" ");
            m.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            s.setItemMeta(m);
        }
        return s;
    }

    private void fill(Inventory inv, ItemStack filler) {
        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) == null || inv.getItem(i).getType() == Material.AIR) {
                inv.setItem(i, filler);
            }
        }
    }

    private ItemStack icon(Material mat, String name, List<String> lore) {
        ItemStack it = new ItemStack(mat);
        ItemMeta meta = it.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
            if (lore != null && !lore.isEmpty()) {
                List<String> cl = new ArrayList<>(lore.size());
                for (String s : lore) cl.add(ChatColor.translateAlternateColorCodes('&', s));
                meta.setLore(cl);
            }
            meta.getPersistentDataContainer().set(keyIsProShieldItem, PersistentDataType.BYTE, (byte)1);
            it.setItemMeta(meta);
        }
        return it;
    }

    private ItemStack makeCompass(String displayName, List<String> lore, boolean admin) {
        ItemStack c = new ItemStack(Material.COMPASS);
        ItemMeta m = c.getItemMeta();
        if (m != null) {
            m.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));
            if (lore != null) {
                List<String> l = new ArrayList<>(lore.size());
                for (String s : lore) l.add(ChatColor.translateAlternateColorCodes('&', s));
                m.setLore(l);
            }
            m.getPersistentDataContainer().set(keyIsProShieldItem, PersistentDataType.BYTE, (byte)1);
            m.getPersistentDataContainer().set(keyMenuId, PersistentDataType.STRING, admin ? "ADMIN" : "MAIN");
            c.setItemMeta(m);
        }
        return c;
    }

    private List<String> lore(String... lines) {
        List<String> l = new ArrayList<>();
        for (String s : lines) l.add(s);
        return l;
    }

    private void cmd(Player p, String command) {
        p.closeInventory();
        Bukkit.getScheduler().runTask(plugin, () -> p.performCommand(command));
    }
}
