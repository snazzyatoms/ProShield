// src/main/java/com/snazzyatoms/proshield/gui/GUIManager.java
package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.cache.GUICache;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.plots.PlotSettings;
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
     * INVENTORY CLICK ROUTER
     * ======================================================== */
    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        String title = e.getView().getTitle();

        try {
            if (TITLE_MAIN.equals(title)) {
                e.setCancelled(true);
                handleMainClick(p, e.getCurrentItem(), e.getClick());
            } else if (TITLE_FLAGS.equals(title)) {
                e.setCancelled(true);
                handleFlagsClick(p, e.getCurrentItem(), e.getClick());
            } else if (TITLE_ROLES.equals(title)) {
                e.setCancelled(true);
                handleRolesClick(p, e.getCurrentItem(), e.getClick());
            } else if (TITLE_TRUST.equals(title)) {
                e.setCancelled(true);
                handleTrustClick(p, e.getCurrentItem(), e.getClick());
            } else if (TITLE_UNTRUST.equals(title)) {
                e.setCancelled(true);
                handleUntrustClick(p, e.getCurrentItem(), e.getClick());
            } else if (TITLE_TRANSFER.equals(title)) {
                e.setCancelled(true);
                handleTransferClick(p, e.getCurrentItem(), e.getClick());
            } else if (TITLE_ADMIN.equals(title)) {
                e.setCancelled(true);
                handleAdminClick(p, e.getCurrentItem(), e.getClick());
            } else if (TITLE_ADMIN_WILD.equals(title)) {
                e.setCancelled(true);
                handleAdminWildernessClick(p, e.getCurrentItem(), e.getClick());
            } else if (title.startsWith(TITLE_PICK_PLAYER)) {
                e.setCancelled(true);
                handlePickPlayerClick(p, e.getCurrentItem(), e.getClick());
            }
        } catch (Throwable t) {
            plugin.getLogger().warning("GUI click error: " + t.getMessage());
        }
    }

    /* ========================================================
     * INTERNAL OPEN METHODS
     * ======================================================== */
    private void openMainInternal(Player p) {
        Inventory inv = Bukkit.createInventory(dummyHolder(), 27, TITLE_MAIN);
        set(inv, 11, make(Material.PAPER, "Flags", List.of("Manage claim flags")));
        set(inv, 13, make(Material.BOOK, "Roles", List.of("Manage roles & trust")));
        set(inv, 15, make(Material.CHEST, "Transfer", List.of("Transfer ownership")));
        p.openInventory(inv);
    }

    private void openAdminInternal(Player p) {
        Inventory inv = Bukkit.createInventory(dummyHolder(), 27, TITLE_ADMIN);
        set(inv, 11, make(Material.MAP, "Teleport", List.of("Admin teleport to claims")));
        set(inv, 13, make(Material.BARRIER, "Force Unclaim", List.of("Remove any claim")));
        set(inv, 15, make(Material.OAK_SIGN, "Wilderness Tools", List.of("Manage wilderness options")));
        p.openInventory(inv);
    }

    private void openFlagsInternal(Player p) {
        Inventory inv = Bukkit.createInventory(dummyHolder(), 27, TITLE_FLAGS);
        set(inv, 22, make(Material.OAK_DOOR, "Back", List.of("Return to main menu")));
        p.openInventory(inv);
    }

    private void openRolesInternal(Player p) {
        Inventory inv = Bukkit.createInventory(dummyHolder(), 27, TITLE_ROLES);
        set(inv, 11, make(Material.PLAYER_HEAD, "Trust", List.of("Trust a player")));
        set(inv, 13, make(Material.BARRIER, "Untrust", List.of("Remove trust")));
        set(inv, 15, make(Material.BOOK, "Assign Roles", List.of("Change roles of trusted players")));
        set(inv, 22, make(Material.OAK_DOOR, "Back", List.of("Return to main menu")));
        p.openInventory(inv);
    }

    private void openRolesInternal(Player p, Plot plot) { openRolesInternal(p); }
    private void openTrustInternal(Player p) {
        Inventory inv = Bukkit.createInventory(dummyHolder(), 27, TITLE_TRUST);
        set(inv, 22, make(Material.OAK_DOOR, "Back", List.of("Return to Roles")));
        p.openInventory(inv);
    }
    private void openUntrustInternal(Player p) {
        Inventory inv = Bukkit.createInventory(dummyHolder(), 27, TITLE_UNTRUST);
        set(inv, 22, make(Material.OAK_DOOR, "Back", List.of("Return to Roles")));
        p.openInventory(inv);
    }
    private void openTransferInternal(Player p) {
        Inventory inv = Bukkit.createInventory(dummyHolder(), 27, TITLE_TRANSFER);
        set(inv, 22, make(Material.OAK_DOOR, "Back", List.of("Return to main menu")));
        p.openInventory(inv);
    }

    /* ========================================================
     * CLICK HANDLERS
     * ======================================================== */
    private void handleMainClick(Player p, ItemStack it, ClickType click) {
        if (!valid(it)) return;
        String name = ChatColor.stripColor(it.getItemMeta().getDisplayName()).toLowerCase();
        switch (name) {
            case "flags" -> openFlagsInternal(p);
            case "roles" -> openRolesInternal(p);
            case "transfer" -> openTransferInternal(p);
        }
    }

    private void handleFlagsClick(Player p, ItemStack it, ClickType click) {
        if (!valid(it)) return;
        if ("back".equalsIgnoreCase(ChatColor.stripColor(it.getItemMeta().getDisplayName()))) {
            openMainInternal(p);
        }
    }

    private void handleRolesClick(Player p, ItemStack it, ClickType click) {
        if (!valid(it)) return;
        String name = ChatColor.stripColor(it.getItemMeta().getDisplayName()).toLowerCase();
        switch (name) {
            case "trust" -> openTrustInternal(p);
            case "untrust" -> openUntrustInternal(p);
            case "assign roles" -> openRolesInternal(p);
            case "back" -> openMainInternal(p);
        }
    }

    private void handleTrustClick(Player p, ItemStack it, ClickType click) {
        if (!valid(it)) return;
        if ("back".equalsIgnoreCase(ChatColor.stripColor(it.getItemMeta().getDisplayName()))) {
            openRolesInternal(p);
        }
    }

    private void handleUntrustClick(Player p, ItemStack it, ClickType click) {
        if (!valid(it)) return;
        if ("back".equalsIgnoreCase(ChatColor.stripColor(it.getItemMeta().getDisplayName()))) {
            openRolesInternal(p);
        }
    }

    private void handleTransferClick(Player p, ItemStack it, ClickType click) {
        if (!valid(it)) return;
        if ("back".equalsIgnoreCase(ChatColor.stripColor(it.getItemMeta().getDisplayName()))) {
            openMainInternal(p);
        }
    }

    private void handleAdminClick(Player p, ItemStack it, ClickType click) {
        if (!valid(it)) return;
        if ("wilderness tools".equalsIgnoreCase(ChatColor.stripColor(it.getItemMeta().getDisplayName()))) {
            openAdminWilderness(p);
        } else if ("back".equalsIgnoreCase(ChatColor.stripColor(it.getItemMeta().getDisplayName()))) {
            openMainInternal(p);
        }
    }

    private void handleAdminWildernessClick(Player p, ItemStack it, ClickType click) {
        if (!valid(it)) return;
        String name = ChatColor.stripColor(it.getItemMeta().getDisplayName()).toLowerCase();

        switch (name) {
            case "wilderness messages" -> {
                boolean current = plugin.getConfig().getBoolean("messages.show-wilderness", false);
                plugin.getConfig().set("messages.show-wilderness", !current);
                plugin.saveConfig();
                p.sendMessage(ChatColor.YELLOW + "Wilderness messages: " +
                        (!current ? ChatColor.GREEN + "ENABLED" : ChatColor.RED + "DISABLED"));
                openAdminWilderness(p);
            }
            case "flag chat debug" -> {
                boolean current = plugin.getConfig().getBoolean("messages.admin-flag-chat", true);
                plugin.getConfig().set("messages.admin-flag-chat", !current);
                plugin.saveConfig();
                p.sendMessage(ChatColor.YELLOW + "Admin flag chat debug: " +
                        (!current ? ChatColor.GREEN + "ENABLED" : ChatColor.RED + "DISABLED"));
                openAdminWilderness(p);
            }
            case "back" -> openAdminInternal(p);
        }
    }

    private void handlePickPlayerClick(Player p, ItemStack it, ClickType click) {
        if (!valid(it)) return;
        UUID skullOwner = skullOwner(it);
        if (skullOwner != null) {
            java.util.function.Consumer<UUID> action = pendingPickers.remove(p.getUniqueId());
            if (action != null) action.accept(skullOwner);
        }
    }

    /* ========================================================
     * ADMIN WILDERNESS
     * ======================================================== */
    private void openAdminWilderness(Player p) {
        if (!p.hasPermission("proshield.admin")) {
            p.sendMessage(ChatColor.RED + "No permission.");
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
        boolean wildMsgEnabled = plugin.getConfig().getBoolean("messages.show-wilderness", false);
        set(inv, 15, toggleItem(Material.OAK_SIGN, "Wilderness Messages", wildMsgEnabled));

        // 🔹 NEW: Admin flag-chat debug toggle
        boolean flagChatEnabled = plugin.getConfig().getBoolean("messages.admin-flag-chat", true);
        set(inv, 16, toggleItem(Material.PAPER, "Flag Chat Debug", flagChatEnabled));

        // Back
        set(inv, 22, make(Material.OAK_DOOR, "Back", List.of("Return to Admin")));

        p.openInventory(inv);
    }

    /* ========================================================
     * HELPERS
     * ======================================================== */
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
            if (enabled) m.addEnchant(Enchantment.UNBREAKING, 1, true);
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
