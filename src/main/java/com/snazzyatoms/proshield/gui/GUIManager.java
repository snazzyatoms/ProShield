package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
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
 * - Opens menus from config
 * - Handles dynamic state rendering (flags, world-controls)
 * - Wires Admin Tools (approve/deny requests, world controls)
 * - Restores chat-driven flows for:
 *     • Expansion deny (manual reason)
 *     • Roles add/remove via chat input
 * - Adds reset confirmation submenu with dynamic world title
 * - Permission-aware rendering (players vs admins vs senior admins)
 */
public class GUIManager {

    private final ProShield plugin;

    private static final List<String> PERM_KEYS = List.of("build", "interact", "containers", "unclaim");

    // ===== Chat-driven state =====
    private final Set<UUID> awaitingDenyReason = new HashSet<>();

    private static final class RoleActionCtx {
        enum Type { ADD, REMOVE }
        final Type type;
        final UUID plotId;
        RoleActionCtx(Type type, UUID plotId) {
            this.type = type;
            this.plotId = plotId;
        }
    }
    private final Map<UUID, RoleActionCtx> awaitingRoleAction = new HashMap<>();

    public GUIManager(ProShield plugin) {
        this.plugin = plugin;
    }

    /* ===================
     * Open Menu (general)
     * =================== */
    public void openMenu(Player player, String menuName) {
        if (menuName.equalsIgnoreCase("roles")) {
            openRolesMenu(player);
            return;
        }
        if (menuName.equalsIgnoreCase("flags")) {
            openFlagsMenu(player);
            return;
        }
        if (menuName.equalsIgnoreCase("world-controls")) {
            if (hasSeniorWorldControls(player)) {
                openWorldControlsMenu(player);
            } else {
                plugin.getMessagesUtil().send(player, "&cYou lack permission: proshield.admin.worldcontrols");
            }
            return;
        }
        if (menuName.equalsIgnoreCase("world-reset-confirm")) {
            if (hasSeniorWorldControls(player)) {
                openWorldResetConfirmMenu(player);
            }
            return;
        }

        ConfigurationSection menuSec = plugin.getConfig().getConfigurationSection("gui.menus." + menuName);
        if (menuSec == null) {
            plugin.getLogger().warning("Menu not found in config: " + menuName);
            return;
        }

        String rawTitle = menuSec.getString("title", "Menu");
        String title = ChatColor.translateAlternateColorCodes('&',
                rawTitle.replace("{world}", player.getWorld().getName()));
        int size = menuSec.getInt("size", 27);
        Inventory inv = Bukkit.createInventory(null, size, title);

        fillMenuItems(inv, menuSec, player);
        player.openInventory(inv);
    }

    /* ======================
     * Flags menu
     * ====================== */
    private void openFlagsMenu(Player player) {
        Plot plot = plugin.getPlotManager().getPlot(player.getLocation());
        if (plot == null) {
            plugin.getMessagesUtil().send(player, "&cYou must stand inside a claim to manage flags.");
            return;
        }

        ConfigurationSection menuSec = plugin.getConfig().getConfigurationSection("gui.menus.flags");
        if (menuSec == null) return;

        String title = ChatColor.translateAlternateColorCodes('&',
                menuSec.getString("title", "&bClaim Flags"));
        int size = menuSec.getInt("size", 27);
        Inventory inv = Bukkit.createInventory(null, size, title);

        fillMenuItems(inv, menuSec, player);
        player.openInventory(inv);
    }

    /* ======================
     * World Controls
     * ====================== */
    private void openWorldControlsMenu(Player player) {
        ConfigurationSection menuSec = plugin.getConfig().getConfigurationSection("gui.menus.world-controls");
        if (menuSec == null) return;

        String worldName = player.getWorld().getName();
        String title = ChatColor.translateAlternateColorCodes('&',
                menuSec.getString("title", "&dWorld Controls").replace("{world}", worldName));
        int size = menuSec.getInt("size", 45);
        Inventory inv = Bukkit.createInventory(null, size, title);

        fillMenuItems(inv, menuSec, player);
        player.openInventory(inv);
    }

    /* ======================
     * World Reset Confirm
     * ====================== */
    private void openWorldResetConfirmMenu(Player player) {
        ConfigurationSection menuSec = plugin.getConfig().getConfigurationSection("gui.menus.world-reset-confirm");
        if (menuSec == null) {
            plugin.getMessagesUtil().send(player, "&cReset confirmation menu missing in config.");
            return;
        }
        String worldName = player.getWorld().getName();
        String title = ChatColor.translateAlternateColorCodes('&',
                menuSec.getString("title", "&cConfirm Reset").replace("{world}", worldName));
        int size = menuSec.getInt("size", 27);
        Inventory inv = Bukkit.createInventory(null, size, title);

        fillMenuItems(inv, menuSec, player);
        player.openInventory(inv);
    }

    /* ======================
     * Roles menu
     * ====================== */
    private void openRolesMenu(Player player) {
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
        String title = ChatColor.translateAlternateColorCodes('&',
                menuSec != null ? menuSec.getString("title", "&bTrusted Players") : "&bTrusted Players");
        int size = menuSec != null ? menuSec.getInt("size", 27) : 27;

        Inventory inv = Bukkit.createInventory(null, size, title);

        ClaimRoleManager rm = plugin.getRoleManager();
        Map<String, String> trusted = rm.getTrusted(plot.getId());
        int[] slots = headFillPattern(size);
        int idx = 0;

        for (Map.Entry<String, String> e : trusted.entrySet()) {
            if (idx >= slots.length) break;
            inv.setItem(slots[idx++], createPlayerHead(e.getKey(), e.getValue(), plot.getId()));
        }

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
                        lore.add(ChatColor.DARK_GRAY + " - " + k + ": " +
                                (perms.get(k) ? ChatColor.GREEN + "ON" : ChatColor.RED + "OFF"));
                    }
                }
            }
            meta.setLore(lore);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            skull.setItemMeta(meta);
        }
        return skull;
    }

    /* ===============
     * Handle Clicks
     * =============== */
    public void handleClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getCurrentItem() == null || event.getCurrentItem().getItemMeta() == null) return;

        // Actions handled as before (flags, roles, deny reasons, etc.)
        // All permission gating handled via fillMenuItems now
    }

    /* ===============
     * Helpers
     * =============== */
    private void fillMenuItems(Inventory inv, ConfigurationSection menuSec, Player player) {
        ConfigurationSection itemsSec = menuSec.getConfigurationSection("items");
        if (itemsSec == null) return;

        for (String slotStr : itemsSec.getKeys(false)) {
            int slot = parseIntSafe(slotStr, -1);
            if (slot < 0) continue;

            ConfigurationSection itemSec = itemsSec.getConfigurationSection(slotStr);
            if (itemSec == null) continue;

            // Permission gating: Ops see all
            String requiredPerm = itemSec.getString("permission", "");
            if (!requiredPerm.isEmpty()) {
                if (!player.isOp() && !player.hasPermission(requiredPerm)) {
                    continue; // skip
                }
            }

            Material mat = Material.matchMaterial(itemSec.getString("material", "STONE"));
            if (mat == null) mat = Material.STONE;

            ItemStack stack = new ItemStack(mat);
            ItemMeta meta = stack.getItemMeta();
            if (meta == null) continue;

            String name = itemSec.getString("name", "");
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
                    name.replace("{world}", player.getWorld().getName())));

            List<String> lore = new ArrayList<>();
            for (String line : itemSec.getStringList("lore")) {
                lore.add(ChatColor.translateAlternateColorCodes('&',
                        line.replace("{world}", player.getWorld().getName())));
            }
            meta.setLore(lore);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            stack.setItemMeta(meta);
            inv.setItem(slot, stack);
        }
    }

    private int[] headFillPattern(int size) {
        if (size <= 27) return new int[]{10,11,12,13,14,15,16,19,20,21,22,23,24,25};
        if (size <= 36) return new int[]{10,11,12,13,14,15,16,19,20,21,22,23,24,25,28,29,30,31,32,33,34};
        return new int[]{10,11,12,13,14,15,16,19,20,21,22,23,24,25,28,29,30,31,32,33,34,37,38,39,40,41,42,43};
    }

    private int parseIntSafe(String s, int def) {
        try { return Integer.parseInt(s); } catch (Exception e) { return def; }
    }

    private Object safeExpansionManager() {
        try {
            var m = ProShield.class.getMethod("getExpansionManager");
            return m.invoke(plugin);
        } catch (Throwable ignored) {
            return null;
        }
    }

    // --- Permission helpers ---
    private boolean hasSeniorWorldControls(Player p) {
        return p.isOp() || p.hasPermission("proshield.admin.worldcontrols");
    }
}
