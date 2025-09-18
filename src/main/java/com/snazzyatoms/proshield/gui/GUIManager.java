package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.expansions.ExpansionRequest;
import com.snazzyatoms.proshield.expansions.ExpansionRequestManager;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.roles.ClaimRole;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * GUIManager (ProShield v1.2.6 polished & synced)
 *
 * Menus:
 *  - Main
 *  - Claim Info
 *  - Trusted Players / Assign Role
 *  - Claim Flags
 *  - Player Expansion Request
 *  - Admin Tools
 *  - Expansion Review / Deny Reasons
 *  - Expansion History
 *  - World Controls
 *
 * Sync (messages.yml):
 *  - Back/Exit buttons
 *  - Admin Tools buttons
 *  - Expansion admin lore
 *  - World Controls toggles
 *  - Notifications (reload/debug/bypass/etc.)
 */
public class GUIManager {

    private static final String TAG_UUID   = "#UUID:";
    private static final String TAG_TS     = "#TS:";
    private static final String TAG_CFLAG  = "#CFLAG:";
    private static final String TAG_WCTRL  = "#WCTRL:";

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final ClaimRoleManager roleManager;
    private final ExpansionRequestManager expansionManager;
    private final MessagesUtil messages;

    private final Map<UUID, UUID> pendingRoleAssignments = new HashMap<>();
    private final Map<UUID, Integer> historyPages = new HashMap<>();
    private final Map<UUID, List<ExpansionRequest>> filteredHistory = new HashMap<>();
    private final Map<UUID, UUID> pendingDenyTarget = new HashMap<>();
    private final Map<UUID, Integer> pendingPlayerExpansionAmount = new HashMap<>();

    private static final int HISTORY_PER_PAGE = 18;

    public GUIManager(ProShield plugin) {
        this.plugin = plugin;
        this.plotManager = plugin.getPlotManager();
        this.roleManager = plugin.getRoleManager();
        this.expansionManager = plugin.getExpansionRequestManager();
        this.messages = plugin.getMessagesUtil();
    }

    /* ---------- Utilities ---------- */

    private ItemStack backButton() {
        return simpleItem(Material.ARROW,
                messages.getOrDefault("messages.gui.back-button", "&eBack"),
                messages.getListOrDefault("messages.gui.back-lore", List.of("&7Return to previous menu")));
    }

    private ItemStack exitButton() {
        return simpleItem(Material.BARRIER,
                messages.getOrDefault("messages.gui.exit-button", "&cExit"),
                messages.getListOrDefault("messages.gui.exit-lore", List.of("&7Close this menu safely")));
    }

    private void placeNavButtons(Inventory inv) {
        int size = inv.getSize();
        inv.setItem(size - 9, backButton());
        inv.setItem(size - 1, exitButton());
    }

    private ItemStack simpleItem(Material mat, String name, String... lore) {
        return simpleItem(mat, name, Arrays.asList(lore));
    }

    private ItemStack simpleItem(Material mat, String name, List<String> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(messages.color(name));
            if (lore != null && !lore.isEmpty()) {
                List<String> colored = new ArrayList<>();
                for (String l : lore) colored.add(messages.color(l));
                meta.setLore(colored);
            }
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        return item;
    }

    private boolean isNamed(ItemStack item, String needle) {
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return false;
        return ChatColor.stripColor(item.getItemMeta().getDisplayName()).equalsIgnoreCase(needle);
    }

    public boolean isBack(ItemStack item) {
        return isNamed(item, ChatColor.stripColor(messages.getOrDefault("messages.gui.back-button", "Back")));
    }
    public boolean isExit(ItemStack item) {
        return isNamed(item, ChatColor.stripColor(messages.getOrDefault("messages.gui.exit-button", "Exit")));
    }

    private UUID extractHiddenUuid(ItemStack item) {
        List<String> lore = (item != null && item.hasItemMeta()) ? item.getItemMeta().getLore() : null;
        if (lore == null) return null;
        for (String line : lore) {
            String raw = ChatColor.stripColor(line);
            if (raw != null && raw.startsWith(TAG_UUID)) {
                try { return UUID.fromString(raw.substring(TAG_UUID.length()).trim()); }
                catch (Exception ignored) {}
            }
        }
        return null;
    }

    private Instant extractHiddenTimestamp(ItemStack item) {
        List<String> lore = (item != null && item.hasItemMeta()) ? item.getItemMeta().getLore() : null;
        if (lore == null) return null;
        for (String line : lore) {
            String raw = ChatColor.stripColor(line);
            if (raw != null && raw.startsWith(TAG_TS)) {
                try { return Instant.parse(raw.substring(TAG_TS.length()).trim()); }
                catch (Exception ignored) {}
            }
        }
        return null;
    }

    private String extractHidden(ItemStack item, String tag) {
        List<String> lore = (item != null && item.hasItemMeta()) ? item.getItemMeta().getLore() : null;
        if (lore == null) return null;
        for (String line : lore) {
            String raw = ChatColor.stripColor(line);
            if (raw != null && raw.startsWith(tag)) {
                return raw.substring(tag.length()).trim();
            }
        }
        return null;
    }

    private void clickTone(Player p) {
        try { p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.6f, 1.4f); }
        catch (Throwable ignored) {}
    }

    /* ============================
     * MAIN MENU
     * ============================ */
    public void openMain(Player player) {
        String title = plugin.getConfig().getString("gui.menus.main.title", "&6ProShield Menu");
        int size = plugin.getConfig().getInt("gui.menus.main.size", 45);
        Inventory inv = Bukkit.createInventory(player, size, messages.color(title));

        inv.setItem(10, simpleItem(Material.GRASS_BLOCK, "&aClaim Land", "&7Claim the chunk you are in."));
        inv.setItem(12, buildClaimInfoItem(player));
        inv.setItem(14, simpleItem(Material.BARRIER, "&cUnclaim Land", "&7Remove your current claim."));
        inv.setItem(16, simpleItem(Material.PLAYER_HEAD, "&bTrusted Players", "&7Manage trusted players & roles."));
        inv.setItem(28, simpleItem(Material.REDSTONE_TORCH, "&eClaim Flags", "&7Toggle protection flags."));

        if (plugin.getConfig().getBoolean("claims.expansion.enabled", true)) {
            inv.setItem(30, simpleItem(Material.EMERALD, "&aRequest Expansion", "&7Request to expand your claim."));
        }

        if (player.hasPermission("proshield.admin")
                || player.hasPermission("proshield.admin.expansions")
                || player.hasPermission("proshield.admin.worldcontrols")) {
            inv.setItem(32, simpleItem(Material.COMMAND_BLOCK, "&cAdmin Tools", "&7Admin-only controls."));
        }

        placeNavButtons(inv);
        player.openInventory(inv);
    }

    public void handleMainClick(Player player, InventoryClickEvent event) {
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta() || !clicked.getItemMeta().hasDisplayName()) return;

        String name = ChatColor.stripColor(clicked.getItemMeta().getDisplayName()).toLowerCase(Locale.ROOT);

        if (name.contains("claim land")) {
            if (plotManager.getPlotByOwner(player.getUniqueId()) != null) {
                messages.send(player, "&cYou already own a claim.");
                return;
            }
            plotManager.createPlot(player.getUniqueId(), player.getLocation());
            messages.send(player, "&aClaim created for your current chunk.");
            clickTone(player);
            player.closeInventory();

        } else if (name.contains("unclaim")) {
            Plot plot = plotManager.getPlotAt(player.getLocation());
            if (plot != null && (plot.getOwner().equals(player.getUniqueId()) || player.hasPermission("proshield.admin"))) {
                plotManager.deletePlot(plot.getId());
                messages.send(player, "&cYour claim has been unclaimed.");
            } else {
                messages.send(player, "&cYou are not the owner of this claim.");
            }
            clickTone(player);
            player.closeInventory();

        } else if (name.contains("trusted players")) {
            clickTone(player);
            openTrusted(player);

        } else if (name.contains("claim flags")) {
            clickTone(player);
            openFlags(player);

        } else if (name.contains("request expansion")) {
            clickTone(player);
            openPlayerExpansionRequest(player);

        } else if (name.contains("admin tools")) {
            clickTone(player);
            openAdminTools(player);

        } else if (isBack(clicked)) {
            clickTone(player);
            openMain(player);

        } else if (isExit(clicked)) {
            clickTone(player);
            player.closeInventory();
        }
    }

    /** Claim Info */
    private ItemStack buildClaimInfoItem(Player player) {
        Plot plot = plotManager.getPlotAt(player.getLocation());
        List<String> lore = new ArrayList<>();
        if (plot == null) {
            lore.add("&7No claim here.");
        } else {
            OfflinePlayer owner = plugin.getServer().getOfflinePlayer(plot.getOwner());
            String ownerName = (owner != null && owner.getName() != null) ? owner.getName() : plot.getOwner().toString();

            long enabledFlags = 0;
            ConfigurationSection avail = plugin.getConfig().getConfigurationSection("flags.available");
            if (avail != null) {
                for (String key : avail.getKeys(false)) {
                    boolean eff = getEffectiveClaimFlag(plot, key);
                    if (eff) enabledFlags++;
                }
            }

            lore.add("&7World: &f" + plot.getWorld());
            lore.add("&7Center: &f" + plot.getX() + ", " + plot.getZ());
            lore.add("&7Owner: &f" + ownerName);
            lore.add("&7Radius: &f" + plot.getRadius() + " blocks");
            lore.add("&7Flags Enabled: &f" + enabledFlags);
        }
        return simpleItem(Material.PAPER, "&eClaim Info", lore);
    }

    private boolean getEffectiveClaimFlag(Plot plot, String key) {
        Boolean v = plot.getFlags().get(key);
        if (v != null) return v;
        return plugin.getConfig().getBoolean("flags.available." + key + ".default", false);
    }

    /* ============================
     * PLAYER EXPANSION REQUEST
     * ============================ */
    public void openPlayerExpansionRequest(Player player) {
        String title = plugin.getConfig().getString("gui.menus.expansion-request.title", "&aRequest Expansion");
        int size = plugin.getConfig().getInt("gui.menus.expansion-request.size", 27);
        Inventory inv = Bukkit.createInventory(player, size, messages.color(title));

        int current = pendingPlayerExpansionAmount.getOrDefault(player.getUniqueId(), 16);
        inv.setItem(10, simpleItem(Material.LIME_DYE, "&f+16", "&7Increase by 16 blocks"));
        inv.setItem(11, simpleItem(Material.LIME_DYE, "&f+32", "&7Increase by 32 blocks"));
        inv.setItem(12, simpleItem(Material.LIME_DYE, "&f+64", "&7Increase by 64 blocks"));
        inv.setItem(13, simpleItem(Material.EMERALD_BLOCK, "&aSubmit Request",
                "&7Current amount: &f" + current,
                "&7Click to submit"));
        inv.setItem(14, simpleItem(Material.RED_DYE, "&f-16", "&7Decrease by 16 blocks"));
        inv.setItem(15, simpleItem(Material.RED_DYE, "&f-32", "&7Decrease by 32 blocks"));
        inv.setItem(16, simpleItem(Material.RED_DYE, "&f-64", "&7Decrease by 64 blocks"));

        placeNavButtons(inv);
        player.openInventory(inv);
    }

    public void handlePlayerExpansionRequestClick(Player player, InventoryClickEvent event) {
        ItemStack clicked = event.getCurrentItem();
        if (isBack(clicked)) { clickTone(player); openMain(player); return; }
        if (isExit(clicked)) { clickTone(player); player.closeInventory(); return; }
        if (clicked == null || !clicked.hasItemMeta() || !clicked.getItemMeta().hasDisplayName()) return;

        String dn = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
        int current = pendingPlayerExpansionAmount.getOrDefault(player.getUniqueId(), 16);

        switch (dn) {
            case "+16" -> current += 16;
            case "+32" -> current += 32;
            case "+64" -> current += 64;
            case "-16" -> current = Math.max(1, current - 16);
            case "-32" -> current = Math.max(1, current - 32);
            case "-64" -> current = Math.max(1, current - 64);
            case "Submit Request" -> {
                int amount = Math.max(1, current);
                expansionManager.createRequest(player.getUniqueId(), amount);
                messages.send(player, messages.getOrDefault("messages.expansion.request-sent",
                        "&eYour expansion request for +{blocks} blocks has been sent to admins.")
                        .replace("{blocks}", String.valueOf(amount)));
                pendingPlayerExpansionAmount.remove(player.getUniqueId());
                clickTone(player);
                openMain(player);
                return;
            }
            default -> { /* ignore */ }
        }
        pendingPlayerExpansionAmount.put(player.getUniqueId(), current);
        clickTone(player);
        openPlayerExpansionRequest(player);
    }

    /* ============================
     * TRUSTED PLAYERS + ROLES
     * ============================ */
    public void openTrusted(Player player) {
        String title = plugin.getConfig().getString("gui.menus.roles.title", "&bTrusted Players");
        int size = plugin.getConfig().getInt("gui.menus.roles.size", 45);
        Inventory inv = Bukkit.createInventory(player, size, messages.color(title));

        Plot plot = plotManager.getPlotAt(player.getLocation());
        if (plot == null) {
            inv.setItem(13, simpleItem(Material.BARRIER, "&cNo claim here", "&7Stand inside your claim to manage roles."));
            placeNavButtons(inv);
            player.openInventory(inv);
            return;
        }

        int slot = 0;
        for (UUID uuid : plot.getTrusted().keySet()) {
            if (uuid.equals(plot.getOwner()) || uuid.equals(player.getUniqueId())) continue;

            OfflinePlayer trusted = plugin.getServer().getOfflinePlayer(uuid);
            String display = (trusted != null && trusted.getName() != null) ? trusted.getName() : uuid.toString().substring(0, 8);

            ClaimRole role = roleManager.getRole(uuid, plot);
            List<String> lore = new ArrayList<>();
            lore.add(messages.color("&7Role: &b" + role.getDisplayName()));
            lore.add(messages.color("&aLeft-click: Assign new role"));
            lore.add(messages.color("&cRight-click: Untrust"));
            lore.add(TAG_UUID + uuid);

            ItemStack head = simpleItem(Material.PLAYER_HEAD, "&f" + display, lore);
            inv.setItem(slot++, head);
            if (slot >= size - 9) break;
        }

        placeNavButtons(inv);
        player.openInventory(inv);
    }

    public void handleTrustedClick(Player player, InventoryClickEvent event) {
        ItemStack clicked = event.getCurrentItem();
        if (isBack(clicked)) { clickTone(player); openMain(player); return; }
        if (isExit(clicked)) { clickTone(player); player.closeInventory(); return; }
        if (clicked == null || !clicked.hasItemMeta()) return;

        Plot plot = plotManager.getPlotAt(player.getLocation());
        if (plot == null) return;

        UUID targetUuid = extractHiddenUuid(clicked);
        if (targetUuid == null) return;

        if (event.isLeftClick()) {
            clickTone(player);
            openAssignRole(player, targetUuid);
        } else if (event.isRightClick()) {
            roleManager.setRole(plot, targetUuid, ClaimRole.NONE);
            String name = Optional.ofNullable(Bukkit.getOfflinePlayer(targetUuid).getName())
                    .orElse(targetUuid.toString().substring(0, 8));
            messages.send(player, "&cUntrusted &f" + name);
            clickTone(player);
            openTrusted(player);
        }
    }

    public void openAssignRole(Player actor, UUID targetUuid) {
        String title = plugin.getConfig().getString("gui.menus.assign-role.title", "&bAssign Role");
        int size = plugin.getConfig().getInt("gui.menus.assign-role.size", 45);
        Inventory inv = Bukkit.createInventory(actor, size, messages.color(title));

        int slot = 0;
        for (ClaimRole role : ClaimRole.values()) {
            if (role == ClaimRole.NONE || role == ClaimRole.OWNER) continue;
            ItemStack item = simpleItem(Material.BOOK, role.getDisplayName(), "&7Click to assign this role");
            inv.setItem(slot++, item);
            if (slot >= size - 9) break;
        }

        placeNavButtons(inv);
        actor.openInventory(inv);
        pendingRoleAssignments.put(actor.getUniqueId(), targetUuid);
    }

    public void handleAssignRoleClick(Player player, InventoryClickEvent event) {
        ItemStack clicked = event.getCurrentItem();
        if (isBack(clicked)) { clearPendingRoleAssignment(player.getUniqueId()); clickTone(player); openTrusted(player); return; }
        if (isExit(clicked)) { clearPendingRoleAssignment(player.getUniqueId()); clickTone(player); player.closeInventory(); return; }
        if (clicked == null || !clicked.hasItemMeta()) return;

        UUID targetUuid = pendingRoleAssignments.remove(player.getUniqueId());
        if (targetUuid == null) return;

        String roleName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
        ClaimRole role = ClaimRole.fromName(roleName);

        if (role == ClaimRole.NONE || role == ClaimRole.OWNER) {
            messages.send(player, "&cInvalid role selected.");
            clickTone(player);
            openTrusted(player);
            return;
        }

        Plot plot = plotManager.getPlotAt(player.getLocation());
        if (plot != null) {
            roleManager.setRole(plot, targetUuid, role);
            messages.send(player, "&aAssigned role &f" + role.getDisplayName() + " &ato target.");
        }
        clickTone(player);
        openTrusted(player);
    }

    public void clearPendingRoleAssignment(UUID actor) {
        pendingRoleAssignments.remove(actor);
    }

    /* ============================
     * CLAIM FLAGS
     * ============================ */
    public void openFlags(Player player) {
        String title = plugin.getConfig().getString("gui.menus.flags.title", "&eClaim Flags");
        int size = plugin.getConfig().getInt("gui.menus.flags.size", 45);
        Inventory inv = Bukkit.createInventory(player, size, messages.color(title));

        Plot plot = plotManager.getPlotAt(player.getLocation());
        if (plot == null) {
            inv.setItem(13, simpleItem(Material.BARRIER, "&cNo claim here", "&7Stand inside your claim to manage flags."));
            placeNavButtons(inv);
            player.openInventory(inv);
            return;
        }

        ConfigurationSection avail = plugin.getConfig().getConfigurationSection("flags.available");
        if (avail != null) {
            int slot = 0;
            for (String key : avail.getKeys(false)) {
                String path = "flags.available." + key;
                String displayName = plugin.getConfig().getString(path + ".name", key);
                boolean current = getEffectiveClaimFlag(plot, key);

                ItemStack item = new ItemStack(current ? Material.LIME_DYE : Material.GRAY_DYE);
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(messages.color(displayName));
                    meta.setLore(Arrays.asList(
                            messages.color("&7Click to toggle"),
                            messages.color("&fCurrent: " + (current ? "&aEnabled" : "&cDisabled")),
                            TAG_CFLAG + key
                    ));
                    item.setItemMeta(meta);
                }
                inv.setItem(slot++, item);
                if (slot >= size - 9) break;
            }
        }

        placeNavButtons(inv);
        player.openInventory(inv);
    }

    public void handleFlagsClick(Player player, InventoryClickEvent event) {
        ItemStack clicked = event.getCurrentItem();
        if (isBack(clicked)) { clickTone(player); openMain(player); return; }
        if (isExit(clicked)) { clickTone(player); player.closeInventory(); return; }

        Plot plot = plotManager.getPlotAt(player.getLocation());
        if (plot == null) return;
        if (clicked == null || !clicked.hasItemMeta()) return;

        String flagKey = extractHidden(clicked, TAG_CFLAG);
        if (flagKey == null || flagKey.isEmpty()) return;

        boolean current = getEffectiveClaimFlag(plot, flagKey);
        boolean newValue = !current;
        plot.getFlags().put(flagKey, newValue);
        messages.send(player, "&eFlag &f" + flagKey + " &eis now " + (newValue ? "&aEnabled" : "&cDisabled"));
        plotManager.saveAll();
        clickTone(player);
        openFlags(player);
    }

    /* ============================
     * ADMIN TOOLS
     * ============================ */
    public void openAdminTools(Player player) {
        String title = plugin.getConfig().getString("gui.menus.admin-tools.title", "&cAdmin Tools");
        int size = plugin.getConfig().getInt("gui.menus.admin-tools.size", 45);
        Inventory inv = Bukkit.createInventory(player, size, messages.color(title));

        int slot = 10;
        if (player.hasPermission("proshield.admin")) {
            inv.setItem(slot++, simpleItem(Material.REPEATER,
                    messages.getOrDefault("messages.admin.reload", "&eReload Configs"),
                    List.of("&7Reload ProShield configs.")));
            inv.setItem(slot++, simpleItem(Material.ENDER_EYE,
                    messages.getOrDefault("messages.admin.debug-toggle", "&aToggle Debug"),
                    List.of("&7Enable/disable debug logging.")));
            inv.setItem(slot++, simpleItem(Material.BARRIER,
                    messages.getOrDefault("messages.admin.bypass-toggle", "&cToggle Bypass"),
                    List.of("&7Admin bypass for claims.")));
        }
        if (player.hasPermission("proshield.admin.expansions")) {
            inv.setItem(slot++, simpleItem(Material.EMERALD,
                    messages.getOrDefault("messages.admin.expansion-requests", "&eExpansion Requests"),
                    List.of("&7Review pending player requests.")));
            inv.setItem(slot++, simpleItem(Material.CLOCK,
                    messages.getOrDefault("messages.admin.expansion-history", "&eExpansion History"),
                    List.of("&7View past requests.")));
        }
        if (player.hasPermission("proshield.admin.worldcontrols")) {
            inv.setItem(slot++, simpleItem(Material.BEDROCK,
                    messages.getOrDefault("messages.admin.world-controls", "&cWorld Controls"),
                    List.of("&7Toggle world-level protections.")));
        }

        placeNavButtons(inv);
        player.openInventory(inv);
    }

    public void handleAdminClick(Player player, InventoryClickEvent event) {
        ItemStack clicked = event.getCurrentItem();
        if (isBack(clicked)) { clickTone(player); openMain(player); return; }
        if (isExit(clicked)) { clickTone(player); player.closeInventory(); return; }
        if (clicked == null || !clicked.hasItemMeta()) return;

        String name = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
        if (name == null) return;

        if (name.equalsIgnoreCase("Reload Configs") && player.hasPermission("proshield.admin")) {
            plugin.reloadConfig();
            plugin.loadMessagesConfig();
            messages.reload();
            messages.send(player, messages.getOrDefault("messages.reloaded", "&aProShield configuration reloaded."));
            clickTone(player);

        } else if (name.equalsIgnoreCase("Toggle Debug") && player.hasPermission("proshield.admin")) {
            plugin.toggleDebug();
            messages.send(player, plugin.isDebugEnabled()
                    ? messages.getOrDefault("messages.admin.debug-on", "&eDebug mode: &aENABLED")
                    : messages.getOrDefault("messages.admin.debug-off", "&eDebug mode: &cDISABLED"));
            clickTone(player);

        } else if (name.equalsIgnoreCase("Toggle Bypass") && player.hasPermission("proshield.admin")) {
            UUID uuid = player.getUniqueId();
            if (plugin.isBypassing(uuid)) {
                plugin.getBypassing().remove(uuid);
                messages.send(player, messages.getOrDefault("messages.admin.bypass-off", "&cBypass disabled."));
            } else {
                plugin.getBypassing().add(uuid);
                messages.send(player, messages.getOrDefault("messages.admin.bypass-on", "&aBypass enabled."));
            }
            clickTone(player);

        } else if (name.equalsIgnoreCase("Expansion Requests") && player.hasPermission("proshield.admin.expansions")) {
            clickTone(player);
            openExpansionReview(player);

        } else if (name.equalsIgnoreCase("Expansion History") && player.hasPermission("proshield.admin.expansions")) {
            clickTone(player);
            openFilteredHistory(player, new ArrayList<>(expansionManager.getAllRequests()));

        } else if (name.equalsIgnoreCase("World Controls") && player.hasPermission("proshield.admin.worldcontrols")) {
            clickTone(player);
            openWorldControls(player);
        }
    }

    /* ============================
     * WORLD CONTROLS
     * ============================ */
    public void openWorldControls(Player admin) {
        String title = plugin.getConfig().getString("gui.menus.world-controls.title", "&cWorld Controls");
        int size = plugin.getConfig().getInt("gui.menus.world-controls.size", 45);
        Inventory inv = Bukkit.createInventory(admin, size, messages.color(title));

        ConfigurationSection sec = plugin.getConfig().getConfigurationSection("protection.world-controls.defaults");
        if (sec != null) {
            int slot = 0;
            for (String key : sec.getKeys(false)) {
                boolean enabled = sec.getBoolean(key, false);
                Material mat = enabled ? Material.LIME_DYE : Material.GRAY_DYE;
                inv.setItem(slot++, simpleItem(mat, "&f" + key,
                        "&7Click to toggle",
                        "&fCurrent: " + (enabled
                                ? messages.getOrDefault("messages.world-controls.enabled", "&aENABLED")
                                : messages.getOrDefault("messages.world-controls.disabled", "&cDISABLED")),
                        TAG_WCTRL + key));
                if (slot >= size - 9) break;
            }
        } else {
            inv.setItem(13, simpleItem(Material.BARRIER, "&7No world controls configured",
                    "&7Add protection.world-controls.defaults.* in config.yml"));
        }

        placeNavButtons(inv);
        admin.openInventory(inv);
    }

    public void handleWorldControlsClick(Player admin, InventoryClickEvent event) {
        ItemStack clicked = event.getCurrentItem();
        if (isBack(clicked)) { clickTone(admin); openAdminTools(admin); return; }
        if (isExit(clicked)) { clickTone(admin); admin.closeInventory(); return; }
        if (clicked == null || !clicked.hasItemMeta()) return;

        String key = extractHidden(clicked, TAG_WCTRL);
        if (key == null || key.isEmpty()) return;

        ConfigurationSection sec = plugin.getConfig().getConfigurationSection("protection.world-controls.defaults");
        if (sec == null) return;

        boolean current = sec.getBoolean(key, false);
        boolean newValue = !current;
        sec.set(key, newValue);
        plugin.saveConfig();

        messages.send(admin, messages.getOrDefault("messages.world-controls.toggle", "&eWorld control &f{flag} &eis now {state}.")
                .replace("{flag}", key)
                .replace("{state}", newValue
                        ? messages.getOrDefault("messages.world-controls.enabled", "&aENABLED")
                        : messages.getOrDefault("messages.world-controls.disabled", "&cDISABLED")));

        clickTone(admin);
        openWorldControls(admin);
    }

    /* ============================
     * EXPANSION REVIEW + DENY REASONS
     * ============================ */
    public void openExpansionReview(Player admin) {
        String title = plugin.getConfig().getString("gui.menus.expansion-requests.title", "&eExpansion Requests");
        int size = plugin.getConfig().getInt("gui.menus.expansion-requests.size", 45);
        Inventory inv = Bukkit.createInventory(admin, size, messages.color(title));

        List<ExpansionRequest> pending = new ArrayList<>(expansionManager.getPendingRequests());
        if (pending.isEmpty()) {
            inv.setItem(22, simpleItem(Material.BARRIER, "&7No Pending Requests", "&7There are no requests to review."));
        } else {
            int slot = 0;
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());
            for (ExpansionRequest req : pending) {
                UUID requester = req.getRequester();
                OfflinePlayer p = Bukkit.getOfflinePlayer(requester);
                String name = (p != null && p.getName() != null) ? p.getName() : requester.toString();

                String approveInfo = messages.getOrDefault("messages.expansion-admin.lore.approve", "&aLeft-click: Approve this expansion");
                String denyInfo    = messages.getOrDefault("messages.expansion-admin.lore.deny", "&cRight-click: Deny and choose a reason");

                List<String> lore = new ArrayList<>();
                lore.add("&7Blocks: &f" + req.getAmount());
                lore.add("&7When: &f" + fmt.format(req.getTimestamp()));
                lore.add(approveInfo);
                lore.add(denyInfo);
                lore.add(TAG_UUID + requester);
                lore.add(TAG_TS + req.getTimestamp());

                inv.setItem(slot++, simpleItem(Material.LIME_WOOL, "&aApprove: " + name, lore));
                if (slot < size - 9) {
                    inv.setItem(slot++, simpleItem(Material.RED_WOOL, "&cDeny: " + name, lore));
                }
                if (slot >= size - 9) break;
            }
        }

        placeNavButtons(inv);
        admin.openInventory(inv);
    }

    public void handleExpansionReviewClick(Player admin, InventoryClickEvent event) {
        ItemStack clicked = event.getCurrentItem();
        if (isBack(clicked)) { clickTone(admin); openAdminTools(admin); return; }
        if (isExit(clicked)) { clickTone(admin); admin.closeInventory(); return; }
        if (clicked == null || !clicked.hasItemMeta() || !clicked.getItemMeta().hasDisplayName()) return;

        String dn = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
        if (dn == null || dn.equalsIgnoreCase("No Pending Requests")) return;

        UUID target = extractHiddenUuid(clicked);
        Instant ts = extractHiddenTimestamp(clicked);
        if (target == null || ts == null) return;

        if (dn.startsWith("Approve: ")) {
            boolean ok = approveByKey(target, ts, admin.getUniqueId());
            String who = Optional.ofNullable(Bukkit.getOfflinePlayer(target).getName())
                    .orElse(target.toString().substring(0, 8));
            if (ok) {
                messages.send(admin, "&aApproved expansion for &f" + who);
            } else {
                messages.send(admin, "&cUnable to locate pending request for &f" + who);
            }
            clickTone(admin);
            openExpansionReview(admin);

        } else if (dn.startsWith("Deny: ")) {
            clickTone(admin);
            openDenyReasons(admin, target, ts);
        }
    }

    private boolean approveByKey(UUID requester, Instant ts, UUID reviewer) {
        ExpansionRequest req = findRequest(requester, ts);
        if (req == null) return false;
        expansionManager.approveRequest(req, reviewer);
        return true;
    }

    private boolean denyByKey(UUID requester, Instant ts, UUID reviewer, String reason) {
        ExpansionRequest req = findRequest(requester, ts);
        if (req == null) return false;
        expansionManager.denyRequest(req, reviewer, reason);
        return true;
    }

    private ExpansionRequest findRequest(UUID requester, Instant ts) {
        List<ExpansionRequest> list = expansionManager.getPendingRequestsFor(requester);
        ExpansionRequest match = list.stream().filter(r -> r.getTimestamp().equals(ts)).findFirst().orElse(null);
        if (match != null) return match;
        return expansionManager.getRequests(requester).stream().filter(r -> r.getTimestamp().equals(ts)).findFirst().orElse(null);
    }

    public void openDenyReasons(Player admin, UUID target, Instant ts) {
        String title = plugin.getConfig().getString("gui.menus.deny-reasons.title", "&cDeny Reasons");
        int size = plugin.getConfig().getInt("gui.menus.deny-reasons.size", 27);
        Inventory inv = Bukkit.createInventory(admin, size, messages.color(title));

        Set<String> keys = messages.getKeys("messages.deny-reasons");
        if (keys != null && !keys.isEmpty()) {
            int slot = 0;
            for (String key : keys) {
                String reason = messages.getOrDefault("messages.deny-reasons." + key, key);
                inv.setItem(slot++, simpleItem(Material.PAPER,
                        "&fReason: " + key,
                        "&7" + ChatColor.stripColor(messages.color(reason)),
                        TAG_UUID + target,
                        TAG_TS + ts));
                if (slot >= size - 9) break;
            }
        } else {
            inv.setItem(13, simpleItem(Material.BARRIER, "&7No reasons configured",
                    "&7Add messages.deny-reasons.* in messages.yml"));
        }

        pendingDenyTarget.put(admin.getUniqueId(), target);
        placeNavButtons(inv);
        admin.openInventory(inv);
    }

    public void handleDenyReasonClick(Player admin, InventoryClickEvent event) {
        ItemStack clicked = event.getCurrentItem();
        if (isBack(clicked)) { clickTone(admin); openExpansionReview(admin); return; }
        if (isExit(clicked)) { clickTone(admin); admin.closeInventory(); return; }
        if (clicked == null || !clicked.hasItemMeta() || !clicked.getItemMeta().hasDisplayName()) return;

        String dn = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
        if (dn == null || !dn.startsWith("Reason: ")) return;

        String key = dn.substring("Reason: ".length()).trim();
        if (key.isEmpty()) return;

        UUID target = extractHiddenUuid(clicked);
        Instant ts = extractHiddenTimestamp(clicked);
        if (target == null || ts == null) return;

        String reasonMsg = messages.getOrDefault("messages.deny-reasons." + key, key);

        boolean ok = denyByKey(target, ts, admin.getUniqueId(), reasonMsg);
        String who = Optional.ofNullable(Bukkit.getOfflinePlayer(target).getName())
                .orElse(target.toString().substring(0, 8));
        if (ok) {
            messages.send(admin, "&cDenied expansion for &f" + who + " &7(" + key + ")");
        } else {
            messages.send(admin, "&cUnable to locate pending request for &f" + who);
        }
        clickTone(admin);
        openExpansionReview(admin);
    }

    public void clearPendingDenyTarget(UUID admin) {
        pendingDenyTarget.remove(admin);
    }

    /* ============================
     * EXPANSION HISTORY
     * ============================ */
    public void openFilteredHistory(Player admin, List<ExpansionRequest> list) {
        filteredHistory.put(admin.getUniqueId(), list);
        historyPages.put(admin.getUniqueId(), 0);
        openFilteredHistoryPage(admin);
    }

    private void openFilteredHistoryPage(Player admin) {
        List<ExpansionRequest> history = filteredHistory.getOrDefault(admin.getUniqueId(), List.of());
        int page = historyPages.getOrDefault(admin.getUniqueId(), 0);
        int size = 54;
        String title = messages.color("&7Expansion History (Page " + (page + 1) + ")");
        Inventory inv = Bukkit.createInventory(admin, size, title);

        int start = page * HISTORY_PER_PAGE;
        int end = Math.min(start + HISTORY_PER_PAGE, history.size());
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());

        for (int i = start; i < end; i++) {
            ExpansionRequest req = history.get(i);
            OfflinePlayer p = Bukkit.getOfflinePlayer(req.getRequester());
            String name = (p != null && p.getName() != null) ? p.getName() : req.getRequester().toString();

            String status;
            Material icon;

            switch (req.getStatus()) {
                case APPROVED -> { status = "APPROVED"; icon = Material.LIME_DYE; }
                case DENIED -> {
                    String reason = req.getDenialReason();
                    status = (reason != null && !reason.isEmpty()) ? "DENIED (" + reason + ")" : "DENIED";
                    icon = Material.RED_DYE;
                }
                case EXPIRED -> { status = "EXPIRED"; icon = Material.YELLOW_DYE; }
                default -> { status = "PENDING"; icon = Material.ORANGE_DYE; }
            }

            List<String> lore = new ArrayList<>();
            lore.add("&7Blocks: &f" + req.getAmount());
            lore.add("&7When: &f" + fmt.format(req.getTimestamp()));
            lore.add("&7Status: &f" + status);

            inv.setItem(i - start, simpleItem(icon, "&f" + name, lore));
        }

        if (page > 0) inv.setItem(size - 6, simpleItem(Material.ARROW, "&aPrevious Page"));
        if ((page + 1) * HISTORY_PER_PAGE < history.size())
            inv.setItem(size - 4, simpleItem(Material.ARROW, "&aNext Page"));

        placeNavButtons(inv);
        admin.openInventory(inv);
    }

    public void handleHistoryClick(Player player, InventoryClickEvent event) {
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;
        String name = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
        UUID uuid = player.getUniqueId();

        if ("Previous Page".equalsIgnoreCase(name)) {
            historyPages.put(uuid, Math.max(0, historyPages.getOrDefault(uuid, 0) - 1));
            clickTone(player);
            openFilteredHistoryPage(player);
        } else if ("Next Page".equalsIgnoreCase(name)) {
            historyPages.put(uuid, historyPages.getOrDefault(uuid, 0) + 1);
            clickTone(player);
            openFilteredHistoryPage(player);
        } else if (isBack(clicked)) {
            clickTone(player);
            openAdminTools(player);
        } else if (isExit(clicked)) {
            clickTone(player);
            player.closeInventory();
        }
    }
}
