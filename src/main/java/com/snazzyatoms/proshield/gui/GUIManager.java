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
 * - Role cycle: trusted -> builder -> co-owner -> trusted
 */
public class GUIManager {

    private final ProShield plugin;

    // Expansion deny "other" reason capture
    private static final Map<UUID, ExpansionRequest> awaitingReason = new HashMap<>();

    // Role chat action ("add")
    private static final Map<UUID, String> awaitingRoleAction = new HashMap<>();
    private static final Map<UUID, UUID> awaitingRolePlot = new HashMap<>(); // which plot player is targeting for role ops

    // Supported per-player permission keys
    private static final List<String> PERM_KEYS = List.of("build", "interact", "containers", "unclaim");
    // Roles cycle order (owner excluded from GUI assignment)
    private static final List<String> ROLE_ORDER = List.of("trusted", "builder", "co-owner");

    public GUIManager(ProShield plugin) {
        this.plugin = plugin;
    }

    /* =========================
     * Awaiting helper queries
     * ========================= */
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

    /* =====================================================
     * Optional hook for ChatListener (safe to ignore for now)
     * ===================================================== */
    public static void handleRoleChatInput(Player adminOrOwner, String message, ProShield plugin) {
        String action = awaitingRoleAction.remove(adminOrOwner.getUniqueId());
        UUID plotId = awaitingRolePlot.remove(adminOrOwner.getUniqueId());
        if (action == null || plotId == null) {
            plugin.getMessagesUtil().send(adminOrOwner, "&7No role action pending.");
            return;
        }

        Plot plot = plugin.getPlotManager().getPlot(plotId);
        if (plot == null) {
            plugin.getMessagesUtil().send(adminOrOwner, "&cThat claim no longer exists.");
            return;
        }

        // For now we support "add" via chat (trust player as 'trusted')
        if (action.equalsIgnoreCase("add")) {
            String targetName = message.trim();
            if (targetName.isEmpty()) {
                plugin.getMessagesUtil().send(adminOrOwner, "&cNo player name supplied.");
                return;
            }
            ClaimRoleManager roles = plugin.getRoleManager();

            // ensure not owner
            OfflinePlayer off = Bukkit.getOfflinePlayer(targetName);
            if (off != null && plot.isOwner(off.getUniqueId())) {
                plugin.getMessagesUtil().send(adminOrOwner, "&cThat player already owns this claim.");
                return;
            }

            boolean ok = roles.trustPlayer(plot, targetName, "trusted");
            if (ok) {
                plugin.getMessagesUtil().send(adminOrOwner, "&aTrusted &e" + targetName + " &aas &6trusted&a.");
            } else {
                plugin.getMessagesUtil().send(adminOrOwner, "&e" + targetName + " &7was already trusted.");
            }
        } else {
            plugin.getMessagesUtil().send(adminOrOwner, "&7Unhandled role action: " + action);
        }
    }

    /* ================
     * Open config menu
     * ================ */
    public void openMenu(Player player, String menuName) {
        if (menuName.equalsIgnoreCase("roles")) {
            openRolesMenu(player); // dynamic menu
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
            for (Map.Entry<String, Object> entry : itemsSec.getValues(false).entrySet()) {
                String slotStr = entry.getKey();
                ConfigurationSection itemSec = itemsSec.getConfigurationSection(slotStr);
                if (itemSec == null) continue;

                int slot = parseIntSafe(slotStr, -1);
                if (slot < 0) continue;

                Material mat = Material.matchMaterial(itemSec.getString("material", "STONE"));
                if (mat == null) mat = Material.STONE;

                ItemStack stack = new ItemStack(mat);
                ItemMeta meta = stack.getItemMeta();
                if (meta == null) continue;

                String name = itemSec.getString("name", "");
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));

                List<String> lore = itemSec.getStringList("lore");
                if (lore != null && !lore.isEmpty()) {
                    for (int i = 0; i < lore.size(); i++) {
                        lore.set(i, ChatColor.translateAlternateColorCodes('&', lore.get(i)));
                    }
                    meta.setLore(lore);
                }

                stack.setItemMeta(meta);
                inv.setItem(slot, stack);
            }
        }

        player.openInventory(inv);
    }

    /* ==========================
     * Roles menu (dynamic heads)
     * ========================== */
    private void openRolesMenu(Player player) {
        Plot plot = plugin.getPlotManager().getPlot(player.getLocation());
        if (plot == null) {
            plugin.getMessagesUtil().send(player, "&cYou must stand inside a claim to manage roles.");
            return;
        }
        // Only owner/co-owner may manage trusted list
        if (!plot.isOwner(player.getUniqueId())) {
            plugin.getMessagesUtil().send(player, "&cOnly the claim owner can manage trusted players.");
            return;
        }

        // Title and size from config if present
        ConfigurationSection menuSec = plugin.getConfig().getConfigurationSection("gui.menus.roles");
        String title = "&bTrusted Players";
        int size = 27;
        if (menuSec != null) {
            title = menuSec.getString("title", title);
            size = menuSec.getInt("size", size);
        }
        title = ChatColor.translateAlternateColorCodes('&', title);
        Inventory inv = Bukkit.createInventory(null, size, title);

        // Place the static buttons from config (Add/Remove/Back), so users’ config works
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

        // Fill with trusted player heads
        ClaimRoleManager rm = plugin.getRoleManager();
        Map<String, String> trusted = rm.getTrusted(plot.getId()); // name -> role
        int[] fillSlots = headFillPattern(size); // pattern for 27/36/45 etc
        int idx = 0;

        for (Map.Entry<String, String> e : trusted.entrySet()) {
            if (idx >= fillSlots.length) break;
            int slot = fillSlots[idx++];
            ItemStack head = createPlayerHead(e.getKey(), e.getValue(), plot.getId());
            inv.setItem(slot, head);
        }

        // Remember which plot this player is managing (so chat "add" knows target claim)
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

            // Build lore with UUID + permissions state
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.DARK_GRAY + "UUID: " + (off != null ? off.getUniqueId() : "unknown"));
            lore.add(ChatColor.GRAY + "Role: " + ChatColor.YELLOW + role);

            Map<String, Boolean> perms = plugin.getRoleManager().getPermissions(plotId, playerName);
            if (!perms.isEmpty()) {
                lore.add(ChatColor.GRAY + "Overrides:");
                for (String k : PERM_KEYS) {
                    if (perms.containsKey(k)) {
                        lore.add(ChatColor.DARK_GRAY + " - " + k + ": " + coloredBool(perms.get(k)));
                    }
                }
            } else {
                lore.add(ChatColor.GRAY + "Overrides: " + ChatColor.DARK_GRAY + "(none)");
            }

            lore.add("");
            lore.add(ChatColor.YELLOW + "Click: " + ChatColor.WHITE + "Manage");
            lore.add(ChatColor.RED + "Shift-Click: " + ChatColor.WHITE + "Untrust");

            meta.setLore(lore);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            skull.setItemMeta(meta);
        }
        return skull;
    }

    private String coloredBool(boolean v) {
        return v ? ChatColor.GREEN + "ON" : ChatColor.RED + "OFF";
    }

    private int[] headFillPattern(int size) {
        // Nice centered rows; supports 27/36/45
        if (size <= 27) return new int[]{10,11,12,13,14,15,16, 19,20,21,22,23,24,25};
        if (size <= 36) return new int[]{10,11,12,13,14,15,16, 19,20,21,22,23,24,25, 28,29,30,31,32,33,34};
        return new int[]{10,11,12,13,14,15,16, 19,20,21,22,23,24,25, 28,29,30,31,32,33,34, 37,38,39,40,41,42,43};
    }

    /* ==================
     * Player detail menu
     * ================== */
    private void openPlayerDetailMenu(Player viewer, UUID plotId, String targetName) {
        String title = ChatColor.translateAlternateColorCodes('&', "&9Player Settings: &b" + targetName);
        Inventory inv = Bukkit.createInventory(null, 27, title);

        ClaimRoleManager rm = plugin.getRoleManager();
        String role = rm.getRole(plotId, targetName);
        if (role == null) role = "trusted";

        // Role cycler
        ItemStack roleItem = new ItemStack(Material.NAME_TAG);
        {
            ItemMeta meta = roleItem.getItemMeta();
            meta.setDisplayName(ChatColor.GOLD + "Role: " + ChatColor.YELLOW + role);
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Cycle Role: " + ChatColor.WHITE + "trusted → builder → co-owner");
            lore.add(ChatColor.DARK_GRAY + "Click to cycle");
            meta.setLore(lore);
            roleItem.setItemMeta(meta);
        }
        inv.setItem(10, roleItem);

        // Permission toggles
        Map<String, Boolean> overrides = rm.getPermissions(plotId, targetName);
        int[] permSlots = new int[]{12, 13, 14, 15};
        for (int i = 0; i < PERM_KEYS.size() && i < permSlots.length; i++) {
            String key = PERM_KEYS.get(i);
            boolean value = overrides.getOrDefault(key, false);
            inv.setItem(permSlots[i], toggleItem(key, value));
        }

        // Back button
        inv.setItem(26, simple(Material.BARRIER, ChatColor.RED + "Back", List.of(ChatColor.GRAY + "Return to roles")));

        viewer.openInventory(inv);
    }

    private ItemStack toggleItem(String key, boolean value) {
        Material mat = value ? Material.LIME_DYE : Material.GRAY_DYE;
        ItemStack it = new ItemStack(mat);
        ItemMeta meta = it.getItemMeta();
        meta.setDisplayName((value ? ChatColor.GREEN : ChatColor.RED) + key.toUpperCase(Locale.ROOT));
        meta.setLore(List.of(ChatColor.GRAY + "Click to toggle"));
        it.setItemMeta(meta);
        return it;
    }

    private ItemStack simple(Material m, String name, List<String> lore) {
        ItemStack it = new ItemStack(m);
        ItemMeta meta = it.getItemMeta();
        meta.setDisplayName(name);
        if (lore != null && !lore.isEmpty()) meta.setLore(lore);
        it.setItemMeta(meta);
        return it;
    }

    /* ==================
     * Handle inventory UI
     * ================== */
    public void handleClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getCurrentItem() == null || event.getCurrentItem().getItemMeta() == null) return;

        String title = event.getView().getTitle();
        String name = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());

        // --- MAIN MENU ---
        if (title.contains("ProShield Menu")) {
            switch (name.toLowerCase(Locale.ROOT)) {
                case "claim land" -> player.performCommand("claim");
                case "claim info" -> player.performCommand("proshield info");
                case "unclaim land" -> player.performCommand("unclaim");
                case "trusted players" -> openMenu(player, "roles");
                case "claim flags" -> openMenu(player, "flags");
                case "admin tools" -> openMenu(player, "admin-expansions");
            }
            return;
        }

        // --- FLAGS MENU ---
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

        // --- ADMIN EXPANSIONS MENU ---
        if (title.contains("Expansion Requests")) {
            switch (name.toLowerCase(Locale.ROOT)) {
                case "pending requests" -> showPendingRequests(player);
                case "approve selected" -> handleExpansionApproval(player);
                case "deny selected" -> openMenu(player, "deny-reasons");
                case "back" -> openMenu(player, "main");
            }
            return;
        }

        // --- DENY REASONS MENU ---
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

        // --- ROLES MENU (list) ---
        if (title.contains("Trusted Players")) {
            Plot plot = plugin.getPlotManager().getPlot(player.getLocation());
            if (plot == null) { plugin.getMessagesUtil().send(player, "&cStand inside your claim."); return; }
            // control buttons
            switch (name.toLowerCase(Locale.ROOT)) {
                case "add player" -> {
                    awaitingRoleAction.put(player.getUniqueId(), "add");
                    awaitingRolePlot.put(player.getUniqueId(), plot.getId());
                    plugin.getMessagesUtil().send(player, "&eType the name of the player to trust...");
                    player.closeInventory();
                    return;
                }
                case "remove player" -> {
                    plugin.getMessagesUtil().send(player, "&7Shift-click a head to untrust. Click a head to manage.");
                    return;
                }
                case "back" -> { openMenu(player, "main"); return; }
            }
            // If clicked a head -> either untrust (shift) or manage
            if (event.getCurrentItem().getType() == Material.PLAYER_HEAD) {
                String display = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());
                String targetName = display;
                // display looks like "<name> (role)"; strip role if present
                int paren = targetName.indexOf(" (");
                if (paren > 0) targetName = targetName.substring(0, paren);

                if (event.isShiftClick()) {
                    // Untrust
                    boolean removed = plugin.getRoleManager().untrustPlayer(plot, targetName);
                    if (removed) {
                        plugin.getMessagesUtil().send(player, "&cUntrusted &e" + targetName + "&c.");
                    } else {
                        plugin.getMessagesUtil().send(player, "&7" + targetName + " wasn't trusted.");
                    }
                    openRolesMenu(player);
                } else {
                    // Manage this player
                    openPlayerDetailMenu(player, plot.getId(), targetName);
                }
            }
            return;
        }

        // --- PLAYER DETAIL MENU ---
        if (title.startsWith(ChatColor.translateAlternateColorCodes('&', "&9Player Settings:"))) {
            // Parse targetName from title
            String raw = ChatColor.stripColor(title);
            String prefix = "Player Settings: ";
            int idx = raw.indexOf(prefix);
            if (idx < 0) return;
            String targetName = raw.substring(idx + prefix.length()).trim();

            Plot plot = plugin.getPlotManager().getPlot(player.getLocation());
            if (plot == null) { plugin.getMessagesUtil().send(player, "&cStand inside your claim."); return; }

            ClaimRoleManager rm = plugin.getRoleManager();

            // Back
            if (name.equalsIgnoreCase("back")) {
                openRolesMenu(player);
                return;
            }

            // Role cycle
            if (name.startsWith("Role:")) {
                String currentRole = rm.getRole(plot.getId(), targetName);
                if (currentRole == null) currentRole = "trusted";
                String nextRole = nextRole(currentRole);
                ClaimRoleManager.PlayerRoleData data = new ClaimRoleManager.PlayerRoleData(nextRole);
                // preserve existing explicit overrides
                Map<String, Boolean> existing = rm.getPermissions(plot.getId(), targetName);
                for (Map.Entry<String, Boolean> e : existing.entrySet()) {
                    data.setPermission(e.getKey(), e.getValue());
                }
                // simply set role by trusting again with same name (if not present) or updating:
                // there's no direct setter exposed, so emulate: untrust + trust to change role
                rm.untrustPlayer(plot, targetName);
                rm.trustPlayer(plot, targetName, nextRole);
                // restore overrides
                for (Map.Entry<String, Boolean> e : existing.entrySet()) {
                    rm.setPermission(plot.getId(), targetName, e.getKey(), e.getValue());
                }
                plugin.getMessagesUtil().send(player, "&e" + targetName + "&7 is now role &6" + nextRole + "&7.");
                openPlayerDetailMenu(player, plot.getId(), targetName);
                return;
            }

            // Permission toggles
            String lower = name.toLowerCase(Locale.ROOT);
            for (String k : PERM_KEYS) {
                if (lower.startsWith(k)) {
                    Map<String, Boolean> perms = rm.getPermissions(plot.getId(), targetName);
                    boolean current = perms.getOrDefault(k, false);
                    rm.setPermission(plot.getId(), targetName, k, !current);
                    plugin.getMessagesUtil().send(player, "&7Set &e" + k + " &7for &e" + targetName + " &7to " + (current ? "&cOFF" : "&aON"));
                    openPlayerDetailMenu(player, plot.getId(), targetName);
                    return;
                }
            }
        }
    }

    private String nextRole(String current) {
        String c = current.toLowerCase(Locale.ROOT);
        int i = ROLE_ORDER.indexOf(c);
        if (i < 0) return ROLE_ORDER.get(0);
        return ROLE_ORDER.get((i + 1) % ROLE_ORDER.size());
    }

    /* ==========
     * Helpers
     * ========== */
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
