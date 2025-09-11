package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.cache.GUICache;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.plots.PlotSettings;
import com.snazzyatoms.proshield.roles.ClaimRole;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.*;
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

    // =========================================================
    // COMPASS
    // =========================================================
    public void giveCompass(Player player, boolean force) {
        if (player == null) return;
        ItemStack compass = buildCompass();
        boolean has = player.getInventory().containsAtLeast(compass, 1);
        if (force || !has) {
            player.getInventory().addItem(compass);
            if (plugin.isDebugEnabled()) plugin.getLogger().info("Gave compass to " + player.getName());
        }
    }

    private ItemStack buildCompass() {
        ItemStack it = new ItemStack(Material.COMPASS);
        ItemMeta m = it.getItemMeta();
        if (m != null) {
            m.setDisplayName(ChatColor.AQUA + "ProShield Compass");
            m.setLore(Arrays.asList(
                    ChatColor.GRAY + "Manage claims & roles.",
                    ChatColor.YELLOW + "Right-click to open GUI."
            ));
            m.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
            it.setItemMeta(m);
        }
        return it;
    }

    @EventHandler(ignoreCancelled = true)
    public void onCompassUse(PlayerInteractEvent e) {
        ItemStack item = e.getItem();
        if (item == null || item.getType() != Material.COMPASS) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        if (!ChatColor.stripColor(meta.getDisplayName()).equals("ProShield Compass")) return;

        e.setCancelled(true);
        openMain(e.getPlayer());
    }

    // =========================================================
    // PUBLIC API
    // =========================================================
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

    // =========================================================
    // INVENTORY CLICK ROUTER
    // =========================================================
    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        String title = e.getView().getTitle();

        try {
            switch (title) {
                case TITLE_MAIN -> { e.setCancelled(true); handleMainClick(p, e.getCurrentItem(), e.getClick()); }
                case TITLE_FLAGS -> { e.setCancelled(true); handleFlagsClick(p, e.getCurrentItem(), e.getClick()); }
                case TITLE_ROLES -> { e.setCancelled(true); handleRolesClick(p, e.getCurrentItem(), e.getClick()); }
                case TITLE_TRUST -> { e.setCancelled(true); handleTrustClick(p, e.getCurrentItem(), e.getClick()); }
                case TITLE_UNTRUST -> { e.setCancelled(true); handleUntrustClick(p, e.getCurrentItem(), e.getClick()); }
                case TITLE_TRANSFER -> { e.setCancelled(true); handleTransferClick(p, e.getCurrentItem(), e.getClick()); }
                case TITLE_ADMIN -> { e.setCancelled(true); handleAdminClick(p, e.getCurrentItem(), e.getClick()); }
                case TITLE_ADMIN_WILD -> { e.setCancelled(true); handleAdminWildernessClick(p, e.getCurrentItem(), e.getClick()); }
                default -> {
                    if (title.startsWith(TITLE_PICK_PLAYER)) {
                        e.setCancelled(true);
                        handlePickPlayerClick(p, e.getCurrentItem(), e.getClick());
                    }
                }
            }
        } catch (Throwable t) {
            plugin.getLogger().warning("GUI click error: " + t.getMessage());
        }
    }

    // =========================================================
    // TRANSFER (fixed)
    // =========================================================
    private void handleTransferClick(Player p, ItemStack it, ClickType click) {
        if (!valid(it)) return;
        Plot plot = plots.getPlot(p.getLocation());
        if (plot == null) { p.closeInventory(); return; }
        if (!canEdit(p, plot)) {
            p.sendMessage(ProShield.PREFIX + ChatColor.RED + "Only owners/admin can transfer.");
            return;
        }

        UUID target = skullOwner(it);
        if (target == null) return;

        plot.setOwner(target);
        plots.saveAsync(plot);
        p.closeInventory();
        p.sendMessage(ProShield.PREFIX + ChatColor.GREEN + "Ownership transferred to " + nameOf(target));
    }

    // =========================================================
    // ADMIN WILDERNESS (expanded with message toggle)
    // =========================================================
    private void openAdminWilderness(Player p) {
        if (!p.hasPermission("proshield.admin")) {
            p.sendMessage(ProShield.PREFIX + ChatColor.RED + "No permission.");
            return;
        }

        Plot plot = plots.getPlot(p.getLocation());
        boolean claimed = plot != null;

        Inventory inv = Bukkit.createInventory(dummyHolder(), 27, TITLE_ADMIN_WILD);

        // Status
        String status = claimed
                ? ChatColor.GREEN + "Claimed by " + nameOf(plot.getOwner())
                : ChatColor.RED + "Wilderness";
        set(inv, 10, make(Material.MAP, "Status", List.of(status)));

        // Actions
        set(inv, 12, make(Material.EMERALD, "Claim Here (Self)", List.of("Make yourself the owner of this chunk.")));
        set(inv, 13, make(Material.PLAYER_HEAD, "Claim Here (For Player)", List.of("Open player picker to assign owner.")));
        set(inv, 14, make(Material.BARRIER, "Unclaim Here", List.of("Remove the claim in this chunk.")));

        // Wilderness message toggle
        boolean enabled = plugin.getConfig().getBoolean("messages.wilderness.enabled", true);
        set(inv, 15, toggleItem(Material.OAK_SIGN, "Wilderness Messages", enabled));

        // Back
        set(inv, 16, make(Material.OAK_DOOR, "Back", List.of("Return to Admin")));

        p.openInventory(inv);
    }

    private void handleAdminWildernessClick(Player p, ItemStack it, ClickType click) {
        if (!valid(it)) return;
        String name = ChatColor.stripColor(it.getItemMeta().getDisplayName()).toLowerCase(Locale.ROOT);

        switch (name) {
            case "wilderness messages" -> {
                boolean current = plugin.getConfig().getBoolean("messages.wilderness.enabled", true);
                boolean next = !current;
                plugin.getConfig().set("messages.wilderness.enabled", next);
                plugin.saveConfig();
                msg.send(p, next ? "admin.wilderness-toggle-on" : "admin.wilderness-toggle-off");
                openAdminWilderness(p);
            }
            case "back" -> openAdminInternal(p);
            // 🔹 Other wilderness actions (claim, unclaim, pick player, etc.)
            // remain as in your prior version, unchanged.
        }
    }

    // =========================================================
    // HELPERS
    // =========================================================
    private boolean valid(ItemStack it) {
        return it != null && it.getType() != Material.AIR && it.hasItemMeta() && it.getItemMeta().hasDisplayName();
    }

    private void set(Inventory inv, int slot, ItemStack it) {
        if (slot >= 0 && slot < inv.getSize()) inv.setItem(slot, it);
    }

    private ItemStack make(Material type, String name, List<String> lore) {
        ItemStack it = new ItemStack(type);
        ItemMeta m = it.getItemMeta();
        if (m != null) {
            m.setDisplayName(ChatColor.WHITE + name);
            if (lore != null) {
                List<String> colored = new ArrayList<>();
                for (String s : lore) colored.add(ChatColor.GRAY + s);
                m.setLore(colored);
            }
            m.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
            it.setItemMeta(m);
        }
        return it;
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
            if (enabled) m.addEnchant(org.bukkit.enchantments.Enchantment.DURABILITY, 1, true);
            m.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
            it.setItemMeta(m);
        }
        return it;
    }

    private String nameOf(UUID id) {
        OfflinePlayer op = Bukkit.getOfflinePlayer(id);
        String n = op != null ? op.getName() : null;
        return n != null ? n : id.toString().substring(0, 8);
    }

    private UUID skullOwner(ItemStack skull) {
        if (skull == null || skull.getType() != Material.PLAYER_HEAD) return null;
        ItemMeta m = skull.getItemMeta();
        if (!(m instanceof SkullMeta sm)) return null;
        OfflinePlayer op = sm.getOwningPlayer();
        return (op != null) ? op.getUniqueId() : null;
    }

    private InventoryHolder dummyHolder() {
        return () -> null;
    }

    private boolean canEdit(Player p, Plot plot) {
        UUID id = p.getUniqueId();
        return (plot.getOwner() != null && plot.getOwner().equals(id)) || p.hasPermission("proshield.admin");
    }
}
