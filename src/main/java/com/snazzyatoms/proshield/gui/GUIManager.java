package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.cache.GUICache;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.plots.PlotSettings;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.UUID;

/**
 * GUIManager
 *
 * Centralized GUI open/build helpers for Player & Admin menus.
 * - Player Main: Flags, Roles, Trust, Untrust, Transfer
 * - Admin Main:  Compass TP, Force Unclaim, Transfer, Reload, Debug Toggle,
 *                Wilderness Messages Toggle, Admin Flag-Chat Toggle
 *
 * Notes:
 * - Click handling is delegated to PlayerMenuListener/AdminMenuListener/FlagsListener.
 * - This class only builds + opens inventories and updates GUICache menu state.
 * - Titles are consistent with listeners; do not change them silently.
 */
public class GUIManager {

    /* =========================
     * Titles (DO NOT CHANGE)
     * ========================= */
    public static final String TITLE_MAIN        = ChatColor.DARK_AQUA + "ProShield";
    public static final String TITLE_FLAGS       = ChatColor.BLUE + "Flags";
    public static final String TITLE_ROLES       = ChatColor.BLUE + "Roles";
    public static final String TITLE_TRUST       = ChatColor.BLUE + "Trust Player";
    public static final String TITLE_UNTRUST     = ChatColor.BLUE + "Untrust Player";
    public static final String TITLE_TRANSFER    = ChatColor.BLUE + "Transfer Ownership";
    public static final String TITLE_ADMIN       = ChatColor.DARK_RED + "Admin";
    public static final String TITLE_ADMIN_WILD  = ChatColor.DARK_RED + "Admin • Wilderness";

    private final ProShield plugin;
    private final GUICache cache;
    private final PlotManager plots;
    private final ClaimRoleManager roles;
    private final MessagesUtil msg;

    public GUIManager(ProShield plugin, GUICache cache) {
        this.plugin = plugin;
        this.cache  = cache;
        this.plots  = plugin.getPlotManager();
        this.roles  = plugin.getRoleManager();
        this.msg    = plugin.getMessagesUtil();
        // Listeners are registered separately (PlayerMenuListener, AdminMenuListener, FlagsListener)
    }

    /* =========================
     * PUBLIC OPEN METHODS
     * ========================= */
    public void openMain(Player p) {
        Inventory inv = Bukkit.createInventory(dummyHolder(), 27, TITLE_MAIN);

        // Row layout (27 slots: 0..26)
        set(inv, 10, makeItem(Material.SHIELD, "Flags", List.of("Configure claim protections."), true));
        set(inv, 11, makeItem(Material.PLAYER_HEAD, "Roles", List.of("Manage claim roles."), false));
        set(inv, 12, makeItem(Material.EMERALD, "Trust", List.of("Trust a player (optionally choose role)."), false));
        set(inv, 13, makeItem(Material.REDSTONE, "Untrust", List.of("Remove a trusted player."), false));
        set(inv, 14, makeItem(Material.CHEST, "Transfer", List.of("Transfer claim ownership."), false));

        // Admin button (only visible to admins)
        if (p.hasPermission("proshield.admin")) {
            set(inv, 16, makeItem(Material.NETHER_STAR, "Admin", List.of("Open admin controls."), true));
        } else {
            set(inv, 16, makeItem(Material.GRAY_STAINED_GLASS_PANE, " ", null, false));
        }

        // Close
        set(inv, 22, makeItem(Material.BARRIER, "Close", List.of("Close the menu."), false));

        // Track open menu for listeners
        cache.setOpenMenu(p.getUniqueId(), "main");
        p.openInventory(inv);
        p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.1f);
    }

    public void openFlagsMenu(Player p) {
        // Build a snapshot from the claim the player is in (read-only UI; clicks go to FlagsListener)
        Plot plot = plots.getPlot(p.getLocation());
        Inventory inv = Bukkit.createInventory(dummyHolder(), 36, TITLE_FLAGS);

        if (plot == null) {
            set(inv, 13, makeItem(Material.BARRIER, "No Claim Here", List.of("Stand inside a claim to edit flags."), false));
        } else {
            PlotSettings s = plot.getSettings();

            // Left column
            set(inv, 10, toggleItem(Material.TNT,            "Explosions",      s.isExplosionsAllowed()));
            set(inv, 11, toggleItem(Material.BUCKET,         "Buckets",         s.isBucketAllowed()));
            set(inv, 12, toggleItem(Material.ITEM_FRAME,     "Item Frames",     s.isItemFramesAllowed()));
            set(inv, 13, toggleItem(Material.ARMOR_STAND,    "Armor Stands",    s.isArmorStandsAllowed()));
            set(inv, 14, toggleItem(Material.LEAD,           "Animals",         s.isAnimalAccessAllowed()));
            set(inv, 15, toggleItem(Material.BONE,           "Pets",            s.isPetAccessAllowed()));

            // Right column
            set(inv, 19, toggleItem(Material.CHEST,          "Containers",      s.isContainersAllowed()));
            set(inv, 20, toggleItem(Material.MINECART,       "Vehicles",        s.isVehiclesAllowed()));
            set(inv, 21, toggleItem(Material.FLINT_AND_STEEL,"Fire",            s.isFireAllowed()));
            set(inv, 22, toggleItem(Material.REPEATER,       "Redstone",        s.isRedstoneAllowed()));
            set(inv, 23, toggleItem(Material.CREEPER_HEAD,   "Entity Griefing", s.isEntityGriefingAllowed()));
            set(inv, 24, toggleItem(Material.IRON_SWORD,     "PvP",             s.isPvpEnabled()));

            // Bottom row
            set(inv, 28, toggleItem(Material.FEATHER,        "Mob Repel",       s.isMobRepelEnabled()));
            set(inv, 29, toggleItem(Material.ROTTEN_FLESH,   "Mob Despawn",     s.isMobDespawnInsideEnabled()));
            set(inv, 30, toggleItem(Material.TOTEM_OF_UNDYING,"Keep Items",     s.isKeepItemsEnabled()));
        }

        set(inv, 31, makeItem(Material.ARROW, "Back", List.of("Back to main menu."), false));
        set(inv, 35, makeItem(Material.BARRIER, "Close", List.of("Close the menu."), false));

        cache.setOpenMenu(p.getUniqueId(), "flags");
        p.openInventory(inv);
        p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.05f);
    }

    public void openRolesMenu(Player p) {
        Inventory inv = Bukkit.createInventory(dummyHolder(), 27, TITLE_ROLES);

        set(inv, 11, makeItem(Material.BOOK, "Viewer", List.of("View only."), false));
        set(inv, 12, makeItem(Material.IRON_PICKAXE, "Builder", List.of("Build/interact."), false));
        set(inv, 13, makeItem(Material.DIAMOND_SWORD, "Moderator", List.of("Moderation in claim."), false));
        set(inv, 14, makeItem(Material.NETHERITE_SWORD, "Co-Owner", List.of("All permissions except transfer."), true));

        set(inv, 22, makeItem(Material.ARROW, "Back", List.of("Back to main menu."), false));

        cache.setOpenMenu(p.getUniqueId(), "roles");
        p.openInventory(inv);
        p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.05f);
    }

    public void openTrustMenu(Player p) {
        Inventory inv = Bukkit.createInventory(dummyHolder(), 27, TITLE_TRUST);

        set(inv, 11, makeItem(Material.NAME_TAG, "Pick Player", List.of("Click a player head to trust."), false));
        set(inv, 12, makeItem(Material.IRON_PICKAXE, "Default Role: Builder", List.of("Use /trust <name> [role] for custom."), false));
        set(inv, 22, makeItem(Material.ARROW, "Back", List.of("Back to main menu."), false));

        cache.setOpenMenu(p.getUniqueId(), "trust");
        p.openInventory(inv);
        p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.05f);
    }

    public void openUntrustMenu(Player p) {
        Inventory inv = Bukkit.createInventory(dummyHolder(), 27, TITLE_UNTRUST);

        set(inv, 11, makeItem(Material.PLAYER_HEAD, "Pick Trusted Player", List.of("Click to untrust."), false));
        set(inv, 22, makeItem(Material.ARROW, "Back", List.of("Back to main menu."), false));

        cache.setOpenMenu(p.getUniqueId(), "untrust");
        p.openInventory(inv);
        p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.05f);
    }

    public void openTransferMenu(Player p) {
        Inventory inv = Bukkit.createInventory(dummyHolder(), 27, TITLE_TRANSFER);

        set(inv, 11, makeItem(Material.PLAYER_HEAD, "Pick New Owner", List.of("Transfers this claim to another player."), true));
        set(inv, 22, makeItem(Material.ARROW, "Back", List.of("Back to main menu."), false));

        cache.setOpenMenu(p.getUniqueId(), "transfer");
        p.openInventory(inv);
        p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.05f);
    }

    public void openAdmin(Player p) {
        Inventory inv = Bukkit.createInventory(dummyHolder(), 27, TITLE_ADMIN);

        // Read current toggles to display state
        boolean debugOn     = plugin.isDebugEnabled();
        boolean wildMsgOn   = plugin.getConfig().getBoolean("messages.wilderness.enabled", true);
        boolean flagChatOn  = plugin.getConfig().getBoolean("messages.admin-flag-chat", true);

        // Top row
        set(inv, 10, makeItem(Material.COMPASS, "Locate/TP", List.of("Teleport to claims / navigate."), true));
        set(inv, 11, makeItem(Material.BARRIER, "Force Unclaim", List.of("Force-remove a claim."), false));
        set(inv, 12, makeItem(Material.CHEST, "Transfer (Admin)", List.of("Admin transfer ownership."), false));

        // Middle row
        set(inv, 13, makeItem(Material.BOOK, "Reload", List.of("Reload config & messages."), false));
        set(inv, 14, toggleItem(Material.REDSTONE, "Debug", debugOn));
        set(inv, 15, toggleItem(Material.PAPER, "Wilderness Messages", wildMsgOn));

        // Bottom row
        set(inv, 16, toggleItem(Material.LEVER, "Admin Flag-Chat", flagChatOn));
        set(inv, 22, makeItem(Material.ARROW, "Back", List.of("Back to main menu."), false));

        cache.setOpenMenu(p.getUniqueId(), "admin");
        p.openInventory(inv);
        p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
    }

    /* =========================
     * Helpers
     * ========================= */
    private InventoryHolder dummyHolder() {
        return () -> null;
    }

    private void set(Inventory inv, int slot, ItemStack it) {
        if (slot >= 0 && slot < inv.getSize()) inv.setItem(slot, it);
    }

    private ItemStack makeItem(Material type, String name, List<String> lore, boolean glint) {
        ItemStack it = new ItemStack(type);
        ItemMeta m = it.getItemMeta();
        if (m != null) {
            m.setDisplayName(ChatColor.WHITE + name);
            if (lore != null && !lore.isEmpty()) {
                m.setLore(lore.stream().map(s -> ChatColor.GRAY + s).toList());
            }
            if (glint) m.addEnchant(Enchantment.UNBREAKING, 1, true);
            m.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
            it.setItemMeta(m);
        }
        return it;
    }

    private ItemStack toggleItem(Material type, String name, boolean on) {
        ItemStack it = new ItemStack(type);
        ItemMeta m = it.getItemMeta();
        if (m != null) {
            m.setDisplayName(ChatColor.WHITE + name);
            m.setLore(List.of(
                    ChatColor.GRAY + "State: " + (on ? ChatColor.GREEN + "ON" : ChatColor.RED + "OFF"),
                    ChatColor.YELLOW + "Click to toggle."
            ));
            if (on) m.addEnchant(Enchantment.UNBREAKING, 1, true);
            m.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
            it.setItemMeta(m);
        }
        return it;
    }

    /* =========================
     * Exposed for listeners
     * ========================= */
    public GUICache getCache()            { return cache; }
    public PlotManager getPlotManager()   { return plots; }
    public ClaimRoleManager getRoleMgr()  { return roles; }
    public MessagesUtil getMessages()     { return msg; }
    public ProShield getPlugin()          { return plugin; }

    // Convenience re-openers used by listeners for “Back” actions:
    public void backToMain(Player p)      { openMain(p); }
    public void backToFlags(Player p)     { openFlagsMenu(p); }
    public void backToRoles(Player p)     { openRolesMenu(p); }
    public void backToTrust(Player p)     { openTrustMenu(p); }
    public void backToUntrust(Player p)   { openUntrustMenu(p); }
    public void backToTransfer(Player p)  { openTransferMenu(p); }
    public void backToAdmin(Player p)     { openAdmin(p); }
}
