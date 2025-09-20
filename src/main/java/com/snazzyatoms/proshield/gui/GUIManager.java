// src/main/java/com/snazzyatoms/proshield/gui/GUIManager.java
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
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.time.Instant;
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
            lore.add(messages.color("&7No claim here."));
        } else {
            Map<String, String> info = plot.getInfo();
            for (Map.Entry<String, String> entry : info.entrySet()) {
                lore.add(messages.color("&7" + entry.getKey() + ": &f" + entry.getValue()));
            }
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
        String title = plugin.getConfig().getString("gui.menus.player-expansion.title", "&aRequest Expansion");
        int size = plugin.getConfig().getInt("gui.menus.player-expansion.size", 27);
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

            // Display name from messages.yml
            String displayName = role.getDisplayName();

            // Lore from messages.yml
            List<String> lore = messages.getListOrNull("messages.roles.lore." + role.name().toLowerCase(Locale.ROOT));
            if (lore == null || lore.isEmpty()) {
                lore = new ArrayList<>();
                lore.add("&7Click to assign this role");
            }

            ItemStack item = simpleItem(Material.BOOK, displayName, lore);
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
            messages.send(player, "&cInvalid role selection.");
            return;
        }

        Plot plot = plotManager.getPlotAt(player.getLocation());
        if (plot == null) {
            messages.send(player, "&cNo claim found here.");
            return;
        }

        roleManager.setRole(plot, targetUuid, role);

        String targetName = Optional.ofNullable(Bukkit.getOfflinePlayer(targetUuid).getName())
                .orElse(targetUuid.toString().substring(0, 8));

        messages.send(player, "&aAssigned &f" + targetName + " &ato role &f" + role.getDisplayName() + "&a.");
        clickTone(player);
        openTrusted(player);
    }

    private void clearPendingRoleAssignment(UUID actorUuid) {
        pendingRoleAssignments.remove(actorUuid);
    }

    /* ============================
     * FLAGS (Player Claim)
     * ============================ */
    public void openFlags(Player player) {
        // Placeholder - implement claim flag menu
        String title = plugin.getConfig().getString("gui.menus.flags.title", "&eClaim Flags");
        int size = plugin.getConfig().getInt("gui.menus.flags.size", 27);
        Inventory inv = Bukkit.createInventory(player, size, messages.color(title));

        Plot plot = plotManager.getPlotAt(player.getLocation());
        if (plot == null) {
            inv.setItem(13, simpleItem(Material.BARRIER, "&cNo claim here", "&7Stand inside your claim to manage flags."));
        } else {
            // Example: PvP flag toggle
            boolean pvp = getEffectiveClaimFlag(plot, "pvp");
            inv.setItem(11, simpleItem(Material.DIAMOND_SWORD, "&fPvP: " + (pvp ? "&aON" : "&cOFF"),
                    "&7Toggle PvP inside this claim",
                    TAG_CFLAG + "pvp"));
        }

        placeNavButtons(inv);
        player.openInventory(inv);
    }

    public void handleFlagsClick(Player player, InventoryClickEvent event) {
        ItemStack clicked = event.getCurrentItem();
        if (isBack(clicked)) { clickTone(player); openMain(player); return; }
        if (isExit(clicked)) { clickTone(player); player.closeInventory(); return; }
        if (clicked == null || !clicked.hasItemMeta()) return;

        String flagKey = extractHidden(clicked, TAG_CFLAG);
        if (flagKey != null) {
            Plot plot = plotManager.getPlotAt(player.getLocation());
            if (plot != null) {
                boolean current = getEffectiveClaimFlag(plot, flagKey);
                plot.setFlag(flagKey, !current);
                plotManager.saveAll();
                messages.send(player, "&eToggled &f" + flagKey + " &eto " + (!current ? "&aON" : "&cOFF"));
                clickTone(player);
                openFlags(player);
            }
        }
    }

    /* ============================
     * ADMIN TOOLS
     * ============================ */
    public void openAdminTools(Player player) {
        String title = plugin.getConfig().getString("gui.menus.admin.title", "&cAdmin Tools");
        int size = plugin.getConfig().getInt("gui.menus.admin.size", 27);
        Inventory inv = Bukkit.createInventory(player, size, messages.color(title));

        inv.setItem(10, simpleItem(Material.BOOK, "&eReload Config", "&7Reloads all configuration files."));
        inv.setItem(12, simpleItem(Material.REDSTONE_TORCH, "&cToggle Debug", "&7Enable/disable debug mode."));
        inv.setItem(14, simpleItem(Material.FEATHER, "&aToggle Bypass", "&7Enable/disable admin bypass."));
        inv.setItem(16, simpleItem(Material.NETHER_STAR, "&dWorld Controls", "&7Manage world-level flags."));

        placeNavButtons(inv);
        player.openInventory(inv);
    }

    public void handleAdminToolsClick(Player player, InventoryClickEvent event) {
        ItemStack clicked = event.getCurrentItem();
        if (isBack(clicked)) { clickTone(player); openMain(player); return; }
        if (isExit(clicked)) { clickTone(player); player.closeInventory(); return; }
        if (clicked == null || !clicked.hasItemMeta() || !clicked.getItemMeta().hasDisplayName()) return;

        String name = ChatColor.stripColor(clicked.getItemMeta().getDisplayName()).toLowerCase(Locale.ROOT);

        if (name.contains("reload")) {
            plugin.reloadConfig();
            messages.send(player, "&aProShield configuration reloaded.");
            clickTone(player);
        } else if (name.contains("debug")) {
            plugin.toggleDebug();
            messages.send(player, plugin.isDebugEnabled() ? "&eDebug mode: &aENABLED" : "&eDebug mode: &cDISABLED");
            clickTone(player);
        } else if (name.contains("bypass")) {
            boolean newState = plugin.toggleBypass(player.getUniqueId());
            messages.send(player, newState ? "&aBypass enabled." : "&cBypass disabled.");
            clickTone(player);
        } else if (name.contains("world controls")) {
            clickTone(player);
            openWorldControls(player);
        }
    }

    /* ============================
     * WORLD CONTROLS (Admin)
     * ============================ */
    public void openWorldControls(Player player) {
        String title = plugin.getConfig().getString("gui.menus.world-controls.title", "&cWorld Controls");
        int size = plugin.getConfig().getInt("gui.menus.world-controls.size", 27);
        Inventory inv = Bukkit.createInventory(player, size, messages.color(title));

        // Example: toggle PvP globally
        boolean globalPvp = plugin.getConfig().getBoolean("world.flags.pvp", true);
        inv.setItem(11, simpleItem(Material.DIAMOND_SWORD, "&fGlobal PvP: " + (globalPvp ? "&aON" : "&cOFF"),
                "&7Toggle PvP across the entire world",
                TAG_WCTRL + "pvp"));

        placeNavButtons(inv);
        player.openInventory(inv);
    }

    public void handleWorldControlsClick(Player player, InventoryClickEvent event) {
        ItemStack clicked = event.getCurrentItem();
        if (isBack(clicked)) { clickTone(player); openAdminTools(player); return; }
        if (isExit(clicked)) { clickTone(player); player.closeInventory(); return; }
        if (clicked == null || !clicked.hasItemMeta()) return;

        String flagKey = extractHidden(clicked, TAG_WCTRL);
        if (flagKey != null) {
            boolean current = plugin.getConfig().getBoolean("world.flags." + flagKey, true);
            plugin.getConfig().set("world.flags." + flagKey, !current);
            plugin.saveConfig();
            messages.send(player, "&eWorld control &f" + flagKey + " &eis now " + (!current ? "&aENABLED" : "&cDISABLED"));
            clickTone(player);
            openWorldControls(player);
        }
    }
}
