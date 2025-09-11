package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.cache.GUICache;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.plots.PlotSettings; // ✅ missing import
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class GUIManager implements Listener {

    private static final String TITLE_MAIN        = ChatColor.DARK_AQUA + "ProShield";
    private static final String TITLE_FLAGS       = ChatColor.BLUE + "Flags";
    private static final String TITLE_ROLES       = ChatColor.BLUE + "Roles";
    private static final String TITLE_TRUST       = ChatColor.BLUE + "Trust Player";
    private static final String TITLE_UNTRUST     = ChatColor.BLUE + "Untrust Player";
    private static final String TITLE_TRANSFER    = ChatColor.BLUE + "Transfer Ownership";
    private static final String TITLE_ADMIN       = ChatColor.DARK_RED + "Admin";
    private static final String TITLE_ADMIN_WILD  = ChatColor.DARK_RED + "Admin • Wilderness";
    private static final String TITLE_PICK_PLAYER = ChatColor.GOLD + "Pick Player • ";

    private final ProShield plugin;
    private final GUICache cache;
    private final PlotManager plots;
    private final ClaimRoleManager roles;
    private final MessagesUtil msg;

    private final Map<UUID, java.util.function.Consumer<UUID>> pendingPickers = new ConcurrentHashMap<>();
    private final Map<UUID, Runnable> backActions = new ConcurrentHashMap<>();

    public GUIManager(ProShield plugin, GUICache cache) {
        this.plugin = plugin;
        this.cache = cache;
        this.plots = plugin.getPlotManager();
        this.roles = plugin.getRoleManager();
        this.msg   = plugin.getMessagesUtil();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /* ========================================================
     * COMPASS
     * ======================================================== */
    // ... (unchanged from your version) ...

    /* ========================================================
     * PUBLIC API
     * ======================================================== */
    public void openMain(Player p)        { openMainInternal(p); }
    public void openAdmin(Player p)       { openAdminInternal(p); }
    public void openFlagsMenu(Player p)   { openFlagsInternal(p); }
    public void openRolesMenu(Player p)   { openRolesInternal(p); }
    public void openTrustMenu(Player p)   { openTrustInternal(p); }
    public void openUntrustMenu(Player p) { openUntrustInternal(p); }
    public void openTransferMenu(Player p){ openTransferInternal(p); }

    public void openRolesGUI(Player p, Plot plot) { openRolesInternal(p, plot); }

    public GUICache getCache() { return cache; }
    public void clearCache()   { cache.clearCache(); }

    /* ========================================================
     * INTERNAL OPEN METHODS
     * ======================================================== */
    private void openMainInternal(Player p) { /* your code from before */ }
    private void openAdminInternal(Player p) { /* your code from before */ }
    private void openFlagsInternal(Player p) { /* your code from before */ }
    private void openRolesInternal(Player p) { /* your code from before */ }
    private void openRolesInternal(Player p, Plot plot) { openRolesInternal(p); }
    private void openTrustInternal(Player p) { /* your code from before */ }
    private void openUntrustInternal(Player p) { /* your code from before */ }
    private void openTransferInternal(Player p) { /* your code from before */ }

    /* ========================================================
     * CLICK HANDLERS
     * ======================================================== */
    private void handleMainClick(Player p, ItemStack it, ClickType click) { /* your code */ }
    private void handleRolesClick(Player p, ItemStack it, ClickType click) { /* your code */ }
    private void handleTrustClick(Player p, ItemStack it, ClickType click) { /* your code */ }
    private void handleUntrustClick(Player p, ItemStack it, ClickType click) { /* your code */ }
    private void handleTransferClick(Player p, ItemStack it, ClickType click) { /* your code */ }
    private void handleAdminClick(Player p, ItemStack it, ClickType click) { /* your code */ }
    private void handleAdminWildernessClick(Player p, ItemStack it, ClickType click) { /* your code */ }
    private void handlePickPlayerClick(Player p, ItemStack it, ClickType click) { /* your code */ }

    /* ========================================================
     * HELPERS (re-added so errors go away)
     * ======================================================== */
    private boolean valid(ItemStack it) {
        return it != null && it.getType() != Material.AIR && it.hasItemMeta() && it.getItemMeta().hasDisplayName();
    }

    private void set(Inventory inv, int slot, ItemStack it) {
        if (slot >= 0 && slot < inv.getSize()) inv.setItem(slot, it);
    }

    private ItemStack toggleItem(Material type, String name, boolean enabled) {
        ItemStack it = new ItemStack(type);
        ItemMeta m = it.getItemMeta();
        if (m != null) {
            m.setDisplayName(ChatColor.WHITE + name);
            m.setLore(List.of(
                    ChatColor.GRAY + "State: " + (enabled ? ChatColor.GREEN + "ON" : ChatColor.RED + "OFF"),
                    ChatColor.YELLOW + "Click to toggle."
            ));
            if (enabled) m.addEnchant(Enchantment.UNBREAKING, 1, true);
            m.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
            it.setItemMeta(m);
        }
        return it;
    }

    private boolean canEdit(Player p, Plot plot) {
        UUID id = p.getUniqueId();
        return (plot.getOwner() != null && plot.getOwner().equals(id)) || p.hasPermission("proshield.admin");
    }

    private InventoryHolder dummyHolder() {
        return () -> null;
    }
}
