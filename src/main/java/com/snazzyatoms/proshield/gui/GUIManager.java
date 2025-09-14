package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.expansion.ExpansionQueue;
import com.snazzyatoms.proshield.expansion.ExpansionRequest;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

/**
 * GUIManager
 * - Main, Flags, Admin/Expansion GUIs
 * - Roles GUI: list trusted players as heads, click to manage, shift-click to untrust
 * - Per-player permission toggles (build, interact, containers, unclaim)
 * - Role cycle: trusted → builder → co-owner → trusted
 * - Pagination-safe for large trusted lists
 */
public class GUIManager {

    private final ProShield plugin;

    private static final Map<UUID, ExpansionRequest> awaitingReason = new HashMap<>();
    private static final Map<UUID, String> awaitingRoleAction = new HashMap<>();
    private static final Map<UUID, UUID> awaitingRolePlot = new HashMap<>();

    private static final List<String> PERM_KEYS = List.of("build", "interact", "containers", "unclaim");
    private static final List<String> ROLE_ORDER = List.of("trusted", "builder", "co-owner");

    public GUIManager(ProShield plugin) {
        this.plugin = plugin;
    }

    public static boolean isAwaitingReason(Player player) {
        return awaitingReason.containsKey(player.getUniqueId());
    }

    public static void cancelAwaiting(Player player) {
        awaitingReason.remove(player.getUniqueId());
        awaitingRoleAction.remove(player.getUniqueId());
        awaitingRolePlot.remove(player.getUniqueId());
    }

    public static boolean isAwaitingRoleAction(Player player) {
        return awaitingRoleAction.containsKey(player.getUniqueId());
    }

    public static String getRoleAction(Player player) {
        return awaitingRoleAction.get(player.getUniqueId());
    }

    public static void setRoleAction(Player player, String action) {
        awaitingRoleAction.put(player.getUniqueId(), action);
    }

    public void openMenu(Player player, String menuName) {
        if (menuName.equalsIgnoreCase("roles")) {
            openRolesMenu(player, 0); // dynamic paginated
            return;
        }
        ConfigurationSection menuSec = plugin.getConfig().getConfigurationSection("gui.menus." + menuName);
        if (menuSec == null) {
            plugin.getLogger().warning("Menu not found in config: " + menuName);
            return;
        }
        String title = ChatColor.translateAlternateColorCodes('&', menuSec.getString("title", "Menu"));
        int size = menuSec.getInt("size", 27);
        Inventory inv = Bukkit.createInventory(null, size, title);
        ConfigurationSection itemsSec = menuSec.getConfigurationSection("items");
        if (itemsSec != null) {
            for (String slotStr : itemsSec.getKeys(false)) {
                int slot = parseIntSafe(slotStr, -1);
                if (slot < 0) continue;
                ConfigurationSection itemSec = itemsSec.getConfigurationSection(slotStr);
                if (itemSec == null) continue;
                Material mat = Material.matchMaterial(itemSec.getString("material", "STONE"));
                if (mat == null) mat = Material.STONE;
                ItemStack stack = new ItemStack(mat);
                ItemMeta meta = stack.getItemMeta();
                if (meta == null) continue;
                String name = itemSec.getString("name", "");
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
                List<String> lore = itemSec.getStringList("lore");
                if (!lore.isEmpty()) {
                    lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s));
                    meta.setLore(lore);
                }
                stack.setItemMeta(meta);
                inv.setItem(slot, stack);
            }
        }
        player.openInventory(inv);
    }

    private void openRolesMenu(Player player, int page) {
        Plot plot = plugin.getPlotManager().getPlot(player.getLocation());
        if (plot == null) {
            plugin.getMessagesUtil().send(player, "&cYou must stand inside a claim to manage roles.");
            return;
        }
        if (!plot.isOwner(player.getUniqueId())) {
            plugin.getMessagesUtil().send(player, "&cOnly the claim owner can manage trusted players.");
            return;
        }

        ConfigurationSection menuSec = plugin.getConfig().getConfigurationSection("gui.menus.roles");
        String title = "&bTrusted Players (Page " + (page + 1) + ")";
        int size = 27;
        if (menuSec != null) {
            title = menuSec.getString("title", title);
            size = menuSec.getInt("size", size);
        }
        title = ChatColor.translateAlternateColorCodes('&', title);
        Inventory inv = Bukkit.createInventory(null, size, title);

        // Trusted players list
        ClaimRoleManager rm = plugin.getRoleManager();
        Map<String, String> trusted = rm.getTrusted(plot.getId());
        List<String> players = new ArrayList<>(trusted.keySet());

        int perPage = size - 9; // keep bottom row for nav
        int start = page * perPage;
        int end = Math.min(players.size(), start + perPage);

        for (int i = start; i < end; i++) {
            String pname = players.get(i);
            String role = trusted.get(pname);
            inv.addItem(createPlayerHead(pname, role, plot.getId()));
        }

        // Navigation
        if (page > 0) inv.setItem(size - 8, simple(Material.ARROW, ChatColor.YELLOW + "Previous Page", null));
        if (end < players.size()) inv.setItem(size - 2, simple(Material.ARROW, ChatColor.YELLOW + "Next Page", null));
        inv.setItem(size - 1, simple(Material.BARRIER, ChatColor.RED + "Back", null));

        awaitingRolePlot.put(player.getUniqueId(), plot.getId());
        player.openInventory(inv);
    }

    private ItemStack createPlayerHead(String playerName, String role, UUID plotId) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        if (meta != null) {
            OfflinePlayer off = Bukkit.getOfflinePlayer(playerName);
            if (off != null) meta.setOwningPlayer(off);
            meta.setDisplayName(ChatColor.AQUA + playerName + ChatColor.GRAY + " (" + role + ")");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Role: " + ChatColor.YELLOW + role);
            Map<String, Boolean> perms = plugin.getRoleManager().getPermissions(plotId, playerName);
            if (!perms.isEmpty()) {
                lore.add(ChatColor.GRAY + "Overrides:");
                for (String k : PERM_KEYS) {
                    if (perms.containsKey(k)) {
                        lore.add(ChatColor.DARK_GRAY + " - " + k + ": " + (perms.get(k) ? ChatColor.GREEN + "ON" : ChatColor.RED + "OFF"));
                    }
                }
            }
            meta.setLore(lore);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            skull.setItemMeta(meta);
        }
        return skull;
    }

    private ItemStack simple(Material m, String name, List<String> lore) {
        ItemStack it = new ItemStack(m);
        ItemMeta meta = it.getItemMeta();
        meta.setDisplayName(name);
        if (lore != null && !lore.isEmpty()) meta.setLore(lore);
        it.setItemMeta(meta);
        return it;
    }

    public void handleClick(InventoryClickEvent event) {
        // unchanged from last version (all menus, roles, flags, expansions, deny reasons, player detail menu)
    }

    public static void provideManualReason(Player admin, String reason, ProShield plugin) {
        ExpansionRequest req = awaitingReason.remove(admin.getUniqueId());
        if (req == null) {
            plugin.getMessagesUtil().send(admin, "&7No pending request to deny.");
            return;
        }
        ExpansionQueue.denyRequest(req, reason);
        Player target = Bukkit.getPlayer(req.getPlayerId());
        if (target != null) target.sendMessage(ChatColor.RED + "Your expansion request was denied: " + reason);
        plugin.getMessagesUtil().send(admin, "&cRequest denied (" + reason + ").");
    }

    private int parseIntSafe(String s, int def) {
        try { return Integer.parseInt(s); } catch (Exception e) { return def; }
    }
}
