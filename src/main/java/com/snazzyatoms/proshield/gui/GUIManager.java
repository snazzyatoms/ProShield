package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.compass.CompassManager;
import com.snazzyatoms.proshield.gui.cache.GUICache;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
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
 * Restores the public API used across the plugin (listeners, commands).
 * Menus are constructed here; click behavior is handled by PlayerMenuListener/AdminMenuListener.
 */
public class GUIManager {

    // ===== Titles (stable, referenced by listeners) =====
    public static final String TITLE_MAIN        = ChatColor.DARK_AQUA + "ProShield";
    public static final String TITLE_FLAGS       = ChatColor.LIGHT_PURPLE + "Claim Flags";
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
    private final CompassManager compass;

    public GUIManager(ProShield plugin, GUICache cache) {
        this.plugin = plugin;
        this.cache = cache;
        this.plots = plugin.getPlotManager();
        this.roles = plugin.getRoleManager();
        this.msg   = plugin.getMessagesUtil();
        this.compass = new CompassManager(plugin); // delegates giveCompass()
        // No event registrations here; listeners do that themselves.
    }

    // =========================
    // Public API (expected)
    // =========================
    public void openMain(Player p) {
        Inventory inv = Bukkit.createInventory(dummyHolder(), 27, TITLE_MAIN);
        // Row center: core actions that PlayerMenuListener will interpret
        set(inv, 11, button(Material.MAP, ChatColor.AQUA + "Claim Info", List.of(gray("View claim details."))));
        set(inv, 12, button(Material.BEACON, ChatColor.GREEN + "Flags", List.of(gray("Open claim flags."))));
        set(inv, 13, button(Material.PLAYER_HEAD, ChatColor.YELLOW + "Trust", List.of(gray("Trust a player."))));
        set(inv, 14, button(Material.BARRIER, ChatColor.GOLD + "Untrust", List.of(gray("Remove trust from a player."))));
        set(inv, 15, button(Material.WRITABLE_BOOK, ChatColor.BLUE + "Roles", List.of(gray("Manage roles."))));
        // Navigation
        set(inv, 22, button(Material.ENDER_PEARL, ChatColor.LIGHT_PURPLE + "Transfer Ownership", List.of(gray("Give claim to another player."))));

        // Optional: if admin, hint admin menu access
        if (p.hasPermission("proshield.admin")) {
            set(inv, 26, button(Material.NETHER_STAR, ChatColor.DARK_RED + "Admin Menu", List.of(gray("Open admin tools."))));
        }

        cache.markPlayerMenu(p.getUniqueId(), inv);
        p.openInventory(inv);
        p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.1f);
    }

    public void openAdmin(Player p) {
        Inventory inv = Bukkit.createInventory(dummyHolder(), 27, TITLE_ADMIN);
        set(inv, 10, button(Material.COMPASS, ChatColor.AQUA + "Teleport / Inspect", List.of(gray("Admin navigation and inspection."))));
        set(inv, 12, button(Material.BEACON, ChatColor.RED + "Admin Flags", List.of(gray("Edit flags for any claim."))));
        set(inv, 14, button(Material.BOOK, ChatColor.GOLD + "Trusted / Roles (Admin)", List.of(gray("Edit trusted and roles for any claim."))));
        set(inv, 16, button(Material.CHEST, ChatColor.YELLOW + "Wilderness Tools", List.of(gray("Toggle wilderness messages / tools."))));
        set(inv, 22, button(Material.ARROW, ChatColor.GRAY + "Back to Main", List.of(gray("Return to player menu."))));

        cache.markAdminMenu(p.getUniqueId(), inv);
        p.openInventory(inv);
        p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 0.9f);
    }

    public void openFlagsMenu(Player p) {
        Inventory inv = Bukkit.createInventory(dummyHolder(), 27, TITLE_FLAGS);
        // These item names are matched by PlayerMenuListener/AdminMenuListener
        set(inv, 10, toggle(Material.TNT, "Explosions"));
        set(inv, 11, toggle(Material.BUCKET, "Buckets")); // single toggle for empty/fill
        set(inv, 12, toggle(Material.ITEM_FRAME, "Item Frames"));
        set(inv, 13, toggle(Material.ARMOR_STAND, "Armor Stands"));
        set(inv, 14, toggle(Material.LEAD, "Animals"));
        set(inv, 15, toggle(Material.BONE, "Pets"));
        set(inv, 16, toggle(Material.CHEST, "Containers"));

        set(inv, 19, toggle(Material.MINECART, "Vehicles"));
        set(inv, 20, toggle(Material.FLINT_AND_STEEL, "Fire"));
        set(inv, 21, toggle(Material.REDSTONE, "Redstone"));
        set(inv, 22, toggle(Material.ZOMBIE_HEAD, "Entity Griefing"));
        set(inv, 23, toggle(Material.IRON_SWORD, "PvP"));
        set(inv, 24, toggle(Material.SHIELD, "Mob Repel"));
        set(inv, 25, toggle(Material.SKELETON_SKULL, "Mob Despawn"));

        cache.markPlayerMenu(p.getUniqueId(), inv);
        p.openInventory(inv);
    }

    public void openRolesMenu(Player p) {
        // Fallback convenience method if someone still calls it with no plot.
        openRolesGUI(p, plots.getPlot(p.getLocation()));
    }

    public void openRolesGUI(Player p, Plot plot) {
        Inventory inv = Bukkit.createInventory(dummyHolder(), 27, TITLE_ROLES);
        set(inv, 11, button(Material.PLAYER_HEAD, ChatColor.YELLOW + "Trusted", List.of(gray("View or edit trusted players."))));
        set(inv, 13, button(Material.NAME_TAG, ChatColor.AQUA + "Assign Role", List.of(gray("Assign a role to a trusted player."))));
        set(inv, 15, button(Material.BOOK, ChatColor.GREEN + "Role Info", List.of(gray("View permissions for each role."))));
        cache.markPlayerMenu(p.getUniqueId(), inv);
        p.openInventory(inv);
    }

    public void openTrustMenu(Player p) {
        Inventory inv = Bukkit.createInventory(dummyHolder(), 27, TITLE_TRUST);
        // List is filled by PlayerMenuListener from online players or search UI
        set(inv, 22, button(Material.ARROW, ChatColor.GRAY + "Back", List.of(gray("Back to main."))));
        cache.markPlayerMenu(p.getUniqueId(), inv);
        p.openInventory(inv);
    }

    public void openUntrustMenu(Player p) {
        Inventory inv = Bukkit.createInventory(dummyHolder(), 27, TITLE_UNTRUST);
        // Filled by listener from claim trusted list
        set(inv, 22, button(Material.ARROW, ChatColor.GRAY + "Back", List.of(gray("Back to main."))));
        cache.markPlayerMenu(p.getUniqueId(), inv);
        p.openInventory(inv);
    }

    public void openTransferMenu(Player p) {
        Inventory inv = Bukkit.createInventory(dummyHolder(), 27, TITLE_TRANSFER);
        set(inv, 13, button(Material.PLAYER_HEAD, ChatColor.LIGHT_PURPLE + "Pick New Owner", List.of(gray("Choose a player to transfer ownership."))));
        set(inv, 22, button(Material.ARROW, ChatColor.GRAY + "Back", List.of(gray("Back to main."))));
        cache.markPlayerMenu(p.getUniqueId(), inv);
        p.openInventory(inv);
    }

    // =========================
    // Extra API used elsewhere
    // =========================
    public void clearCache() {
        cache.clearCache();
    }

    /** Delegates to CompassManager so existing command uses gui.giveCompass(player, isAdmin) */
    public void giveCompass(Player player, boolean adminStyled) {
        compass.giveCompass(player, adminStyled);
    }

    // =========================
    // Helpers
    // =========================

    private InventoryHolder dummyHolder() {
        // Neutral holder so listeners can distinguish our inventories if needed
        return () -> null;
    }

    private void set(Inventory inv, int slot, ItemStack it) {
        if (slot >= 0 && slot < inv.getSize()) inv.setItem(slot, it);
    }

    private ItemStack button(Material type, String name, List<String> lore) {
        ItemStack it = new ItemStack(type);
        ItemMeta meta = it.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (lore != null) meta.setLore(lore);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
            it.setItemMeta(meta);
        }
        return it;
    }

    private ItemStack toggle(Material type, String label) {
        // Visual only; actual enabled/disabled state + lore will be set by listeners when opening/refreshing
        return button(type, ChatColor.WHITE + label, List.of(gray("Click to toggle.")));
    }

    private String gray(String s) {
        return ChatColor.GRAY + s;
    }

    // =========================
    // Accessors for listeners
    // =========================
    public GUICache getCache() {
        return cache;
    }

    public PlotManager getPlotManager() {
        return plots;
    }

    public ClaimRoleManager getRoleManager() {
        return roles;
    }

    public MessagesUtil getMessages() {
        return msg;
    }

    public ProShield getPlugin() {
        return plugin;
    }

    // Optional convenience
    public boolean canEdit(Player p, Plot plot) {
        if (plot == null) return false;
        UUID id = p.getUniqueId();
        return (plot.getOwner() != null && plot.getOwner().equals(id)) || p.hasPermission("proshield.admin");
    }
}
