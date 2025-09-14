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
 * - Handles menus: Main, Flags, Roles, Admin/Expansion, Deny Reasons
 * - Trusted players menu shows heads with roles + overrides
 * - Supports per-player permissions and role cycling
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

    /* ========================
     * Awaiting helper methods
     * ======================== */
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

    public static void handleRoleChatInput(Player actor, String message, ProShield plugin) {
        String action = awaitingRoleAction.remove(actor.getUniqueId());
        UUID plotId = awaitingRolePlot.remove(actor.getUniqueId());
        if (action == null || plotId == null) {
            plugin.getMessagesUtil().send(actor, "&7No role action pending.");
            return;
        }

        Plot plot = plugin.getPlotManager().getPlotById(plotId);
        if (plot == null) {
            plugin.getMessagesUtil().send(actor, "&cThat claim no longer exists.");
            return;
        }

        if (action.equalsIgnoreCase("add")) {
            String targetName = message.trim();
            if (targetName.isEmpty()) {
                plugin.getMessagesUtil().send(actor, "&cNo player name supplied.");
                return;
            }

            ClaimRoleManager roles = plugin.getRoleManager();
            OfflinePlayer off = Bukkit.getOfflinePlayer(targetName);

            if (off != null && plot.isOwner(off.getUniqueId())) {
                plugin.getMessagesUtil().send(actor, "&cThat player already owns this claim.");
                return;
            }

            boolean ok = roles.trustPlayer(plot, targetName, "trusted");
            if (ok) {
                plugin.getMessagesUtil().send(actor, "&aTrusted &e" + targetName + " &aas &6trusted&a.");
            } else {
                plugin.getMessagesUtil().send(actor, "&e" + targetName + " &7was already trusted.");
            }
        } else {
            plugin.getMessagesUtil().send(actor, "&7Unhandled role action: " + action);
        }
    }

    /* ===================
     * Open Menu (general)
     * =================== */
    public void openMenu(Player player, String menuName) {
        if (menuName.equalsIgnoreCase("roles")) {
            openRolesMenu(player);
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
                if (lore != null && !lore.isEmpty()) {
                    lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s));
                    meta.setLore(lore);
                }

                if (meta != null) {
                    meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                    stack.setItemMeta(meta);
                }

                inv.setItem(slot, stack);
            }
        }

        player.openInventory(inv);
    }

    /* ======================
     * Roles / Trusted players
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
        String title = ChatColor.translateAlternateColorCodes('&', menuSec != null ? menuSec.getString("title", "&bTrusted Players") : "&bTrusted Players");
        int size = menuSec != null ? menuSec.getInt("size", 27) : 27;

        Inventory inv = Bukkit.createInventory(null, size, title);

        if (menuSec != null) {
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
                    if (lore != null && !lore.isEmpty()) {
                        lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s));
                        meta.setLore(lore);
                    }

                    stack.setItemMeta(meta);
                    inv.setItem(slot, stack);
                }
            }
        }

        ClaimRoleManager rm = plugin.getRoleManager();
        Map<String, String> trusted = rm.getTrusted(plot.getId());
        int[] slots = headFillPattern(size);
        int idx = 0;

        for (Map.Entry<String, String> e : trusted.entrySet()) {
            if (idx >= slots.length) break;
            inv.setItem(slots[idx++], createPlayerHead(e.getKey(), e.getValue(), plot.getId()));
        }

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

            lore.add("");
            lore.add(ChatColor.YELLOW + "Click: Manage");
            lore.add(ChatColor.RED + "Shift-Click: Untrust");

            meta.setLore(lore);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            skull.setItemMeta(meta);
        }
        return skull;
    }

    private int[] headFillPattern(int size) {
        if (size <= 27) return new int[]{10,11,12,13,14,15,16, 19,20,21,22,23,24,25};
        if (size <= 36) return new int[]{10,11,12,13,14,15,16, 19,20,21,22,23,24,25, 28,29,30,31,32,33,34};
        return new int[]{10,11,12,13,14,15,16, 19,20,21,22,23,24,25, 28,29,30,31,32,33,34, 37,38,39,40,41,42,43};
    }

    /* ===============
     * Handle Clicks
     * =============== */
    public void handleClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getCurrentItem() == null || event.getCurrentItem().getItemMeta() == null) return;

        String title = event.getView().getTitle();
        String name = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());

        // MAIN MENU
        if (title.contains("ProShield Menu")) {
            switch (name.toLowerCase(Locale.ROOT)) {
                case "claim land" -> player.performCommand("proshield claim");
                case "claim info" -> player.performCommand("proshield info");
                case "unclaim land" -> player.performCommand("proshield unclaim");
                case "trusted players" -> openMenu(player, "roles");
                case "claim flags" -> openMenu(player, "flags");
                case "admin tools" -> openMenu(player, "admin-expansions");
            }
            return;
        }

        // FLAGS MENU
        if (title.contains("Claim Flags")) {
            Plot plot = plugin.getPlotManager().getPlot(player.getLocation());
            if (plot == null) {
                plugin.getMessagesUtil().send(player, "&cYou must stand inside a claim.");
                return;
            }
            switch (name.toLowerCase(Locale.ROOT)) {
                case "explosions" -> toggleFlag(plot, "explosions", player);
                case "buckets" -> toggleFlag(plot, "buckets", player);
                case "item frames" -> toggleFlag(plot, "item-frames", player);
                case "armor stands" -> toggleFlag(plot, "armor-stands", player);
                case "containers" -> toggleFlag(plot, "containers", player);
                case "pets" -> toggleFlag(plot, "pets", player);
                case "pvp" -> toggleFlag(plot, "pvp", player);
                case "safe zone" -> toggleFlag(plot, "safezone", player);
                case "back" -> openMenu(player, "main");
            }
            if (!name.equalsIgnoreCase("back")) openMenu(player, "flags");
            return;
        }

        // EXPANSIONS
        if (title.contains("Expansion Requests")) {
            switch (name.toLowerCase(Locale.ROOT)) {
                case "pending requests" -> showPendingRequests(player);
                case "approve selected" -> handleExpansionApproval(player);
                case "deny selected" -> openMenu(player, "deny-reasons");
                case "back" -> openMenu(player, "main");
            }
            return;
        }

        // DENY REASONS
        if (title.contains("Deny Reasons")) {
            List<ExpansionRequest> pending = ExpansionQueue.getPendingRequests();
            if (pending.isEmpty()) {
                plugin.getMessagesUtil().send(player, "&7No requests to deny.");
                openMenu(player, "admin-expansions");
                return;
            }
            ExpansionRequest req = pending.get(0);
            if (name.equalsIgnoreCase("back")) { openMenu(player, "admin-expansions"); return; }
            if (name.equalsIgnoreCase("other")) {
                awaitingReason.put(player.getUniqueId(), req);
                plugin.getMessagesUtil().send(player, "&eType your denial reason in chat...");
                player.closeInventory();
                return;
            }
            ExpansionQueue.denyRequest(req, name);
            plugin.getMessagesUtil().send(player, "&cRequest denied (" + name + ").");
            openMenu(player, "admin-expansions");
            return;
        }
    }

    /* ===============
     * Helpers
     * =============== */
    private void toggleFlag(Plot plot, String flag, Player player) {
        boolean current = plot.getFlag(flag, false);
        plot.setFlag(flag, !current);
        MessagesUtil messages = plugin.getMessagesUtil();
        messages.send(player, current ? "&c" + flag + " disabled." : "&a" + flag + " enabled.");
    }

    private void showPendingRequests(Player player) {
        List<ExpansionRequest> pending = ExpansionQueue.getPendingRequests();
        if (pending.isEmpty()) {
            plugin.getMessagesUtil().send(player, "&7No pending requests.");
            return;
        }
        for (ExpansionRequest req : pending) {
            String pName = Bukkit.getOfflinePlayer(req.getPlayerId()).getName();
            plugin.getMessagesUtil().send(player,
                    "&eRequest: " + pName + " +" + req.getExtraRadius() + " blocks (" +
                            (System.currentTimeMillis() - req.getRequestTime()) / 1000 + "s ago)");
        }
    }

    private void handleExpansionApproval(Player player) {
        List<ExpansionRequest> pending = ExpansionQueue.getPendingRequests();
        if (pending.isEmpty()) {
            plugin.getMessagesUtil().send(player, "&7No requests to approve.");
            return;
        }
        ExpansionRequest req = pending.get(0);
        ExpansionQueue.approveRequest(req);
        Player target = Bukkit.getPlayer(req.getPlayerId());
        if (target != null) target.sendMessage(ChatColor.GREEN + "Your expansion request was approved!");
        plugin.getMessagesUtil().send(player, "&aRequest approved.");
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
