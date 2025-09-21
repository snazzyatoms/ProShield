
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
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * GUIManager (ProShield v1.2.6+ Multilingual)
 *
 * Menus:
 *  - Main
 *  - Claim Info
 *  - Trusted Players / Assign Role
 *  - Claim Flags
 *  - Player Expansion Request (Submit + Status)
 *  - Admin Tools
 *  - Expansion Review (Approve / Deny → Reason)
 *  - Deny Reasons
 *  - Expansion History (Paginated)
 *  - World Controls
 *
 * Multilingual:
 *  - Uses messages.yml keys for all titles, buttons, and lores.
 *  - GUIListener routes clicks by localized titles using messages.gui.titles.*
 *
 * Persistence:
 *  - Expansion requests are persisted by ExpansionRequestManager (expansions.yml)
 *  - Review actions (approve/deny + reason + reviewer + timestamps) are saved
 */
public class GUIManager {

    /* ---------- Hidden tags embedded in item lore for routing ---------- */
    private static final String TAG_UUID  = "#UUID:";   // generic player UUID
    private static final String TAG_TS    = "#TS:";     // timestamp ISO
    private static final String TAG_CFLAG = "#CFLAG:";  // claim flag key
    private static final String TAG_WCTRL = "#WCTRL:";  // world control key
    private static final String TAG_RID   = "#RID:";    // expansion request id
    private static final String TAG_ACT   = "#ACT:";    // action hint (APPROVE / DENY / PAGE_*)

    private static final String ACT_APPROVE   = "APPROVE";
    private static final String ACT_DENY      = "DENY";
    private static final String ACT_PAGE_NEXT = "PAGE_NEXT";
    private static final String ACT_PAGE_PREV = "PAGE_PREV";

    private static final int HISTORY_PER_PAGE = 18;

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final ClaimRoleManager roleManager;
    private final ExpansionRequestManager expansionManager;
    private final MessagesUtil messages;

    /* ---------- ephemeral GUI state ---------- */
    private final Map<UUID, UUID> pendingRoleAssignments = new HashMap<>();             // actor → target
    private final Map<UUID, UUID> pendingDenyTarget      = new HashMap<>();            // admin → requestId
    private final Map<UUID, Integer> pendingPlayerExpansionAmount = new HashMap<>();   // player → amount
    private final Map<UUID, Integer> historyPages        = new HashMap<>();            // player → page index

    public GUIManager(ProShield plugin) {
        this.plugin = plugin;
        this.plotManager = plugin.getPlotManager();
        this.roleManager = plugin.getRoleManager();
        this.expansionManager = plugin.getExpansionRequestManager();
        this.messages = plugin.getMessagesUtil();
    }

    /* ===================================================================
     * Utilities
     * =================================================================== */

    private static String strip(String s) {
        return s == null ? "" : ChatColor.stripColor(s);
    }

    private void clickTone(Player p) {
        try { p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.6f, 1.4f); }
        catch (Throwable ignored) {}
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
                List<String> colored = new ArrayList<>(lore.size());
                for (String line : lore) colored.add(messages.color(line));
                meta.setLore(colored);
            }
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack backButton() {
        List<String> bl = messages.getList("messages.gui.back-lore");
        if (bl.isEmpty()) bl = List.of("&7Return to the previous menu");
        return simpleItem(Material.ARROW,
                messages.getOrDefault("messages.gui.back-button", "&eBack"),
                bl);
    }

    private ItemStack exitButton() {
        List<String> el = messages.getList("messages.gui.exit-lore");
        if (el.isEmpty()) el = List.of("&7Close this menu safely");
        return simpleItem(Material.BARRIER,
                messages.getOrDefault("messages.gui.exit-button", "&cExit"),
                el);
    }

    private void placeNavButtons(Inventory inv) {
        int size = inv.getSize();
        inv.setItem(size - 9, backButton());
        inv.setItem(size - 1, exitButton());
    }

    private boolean isNamed(ItemStack item, String needle) {
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return false;
        return strip(item.getItemMeta().getDisplayName()).equalsIgnoreCase(needle);
    }

    public boolean isBack(ItemStack item) {
        return isNamed(item, strip(messages.getOrDefault("messages.gui.back-button", "Back")));
    }
    public boolean isExit(ItemStack item) {
        return isNamed(item, strip(messages.getOrDefault("messages.gui.exit-button", "Exit")));
    }

    private UUID extractHiddenUuid(ItemStack item) {
        List<String> lore = (item != null && item.hasItemMeta()) ? item.getItemMeta().getLore() : null;
        if (lore == null) return null;
        for (String line : lore) {
            String raw = strip(line);
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
            String raw = strip(line);
            if (raw != null && raw.startsWith(TAG_TS)) {
                try { return Instant.parse(raw.substring(TAG_TS.length()).trim()); }
                catch (Exception ignored) {}
            }
        }
        return null;
    }

    private UUID extractHiddenRequestId(ItemStack item) {
        List<String> lore = (item != null && item.hasItemMeta()) ? item.getItemMeta().getLore() : null;
        if (lore == null) return null;
        for (String line : lore) {
            String raw = strip(line);
            if (raw != null && raw.startsWith(TAG_RID)) {
                try { return UUID.fromString(raw.substring(TAG_RID.length()).trim()); }
                catch (Exception ignored) {}
            }
        }
        return null;
    }

    private String extractHidden(ItemStack item, String tag) {
        List<String> lore = (item != null && item.hasItemMeta()) ? item.getItemMeta().getLore() : null;
        if (lore == null) return null;
        for (String line : lore) {
            String raw = strip(line);
            if (raw != null && raw.startsWith(tag)) {
                return raw.substring(tag.length()).trim();
            }
        }
        return null;
    }

    private boolean getEffectiveClaimFlag(Plot plot, String key) {
        Boolean v = plot.getFlags().get(key);
        if (v != null) return v;
        return plugin.getConfig().getBoolean("flags.available." + key + ".default", false);
    }

    private DateTimeFormatter dtFmt() {
        ZoneId zone = ZoneId.systemDefault();
        return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(zone);
    }

    private String safeName(UUID uuid) {
        OfflinePlayer op = Bukkit.getOfflinePlayer(uuid);
        return (op != null && op.getName() != null) ? op.getName() : uuid.toString().substring(0, 8);
    }

    /* ===================================================================
     * MAIN MENU
     * =================================================================== */

    public void openMain(Player player) {
        String title = messages.getOrDefault("messages.gui.titles.main", "&6ProShield Menu");
        int size = plugin.getConfig().getInt("gui.menus.main.size", 45);
        Inventory inv = Bukkit.createInventory(player, size, messages.color(title));

        inv.setItem(10, simpleItem(Material.GRASS_BLOCK,
                "&a" + messages.getOrDefault("messages.gui.button.claim-land", "Claim Land"),
                "&7" + messages.getOrDefault("messages.gui.button.claim-land-lore", "Claim the chunk you are in.")));

        inv.setItem(12, buildClaimInfoItem(player));

        inv.setItem(14, simpleItem(Material.BARRIER,
                "&c" + messages.getOrDefault("messages.gui.button.unclaim-land", "Unclaim Land"),
                "&7" + messages.getOrDefault("messages.gui.button.unclaim-land-lore", "Remove your current claim.")));

        inv.setItem(16, simpleItem(Material.PLAYER_HEAD,
                messages.getOrDefault("messages.gui.titles.trusted", "&bTrusted Players"),
                "&7" + messages.getOrDefault("messages.gui.button.trusted-lore", "Manage trusted players & roles.")));

        inv.setItem(28, simpleItem(Material.REDSTONE_TORCH,
                messages.getOrDefault("messages.gui.titles.flags", "&eClaim Flags"),
                "&7" + messages.getOrDefault("messages.gui.button.flags-lore", "Toggle protection flags.")));

        if (plugin.getConfig().getBoolean("claims.expansion.enabled", true)) {
            inv.setItem(30, simpleItem(Material.EMERALD,
                    messages.getOrDefault("messages.gui.titles.request-expansion", "&aRequest Expansion"),
                    "&7" + messages.getOrDefault("messages.gui.button.request-expansion-lore", "Request to expand your claim.")));
        }

        if (player.hasPermission("proshield.admin")
                || player.hasPermission("proshield.admin.expansions")
                || player.hasPermission("proshield.admin.worldcontrols")) {
            inv.setItem(32, simpleItem(Material.COMMAND_BLOCK,
                    messages.getOrDefault("messages.gui.titles.admin", "&cAdmin Tools"),
                    "&7" + messages.getOrDefault("messages.gui.button.admin-lore", "Admin-only controls.")));
        }

        placeNavButtons(inv);
        player.openInventory(inv);
    }

    public void handleMainClick(Player player, InventoryClickEvent event) {
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta() || !clicked.getItemMeta().hasDisplayName()) return;

        String name = strip(clicked.getItemMeta().getDisplayName()).toLowerCase(Locale.ROOT);

        if (name.contains("claim land")) {
            if (plotManager.getPlotByOwner(player.getUniqueId()) != null) {
                messages.send(player, messages.getOrDefault("messages.error.already-has-claim", "&cYou already own a claim."));
                return;
            }
            plotManager.createPlot(player.getUniqueId(), player.getLocation());
            messages.send(player, messages.getOrDefault("messages.info.claim-created", "&aClaim created for your current chunk."));
            clickTone(player);
            player.closeInventory();

        } else if (name.contains("unclaim")) {
            Plot plot = plotManager.getPlotAt(player.getLocation());
            if (plot != null && (plot.getOwner().equals(player.getUniqueId()) || player.hasPermission("proshield.admin"))) {
                plotManager.deletePlot(plot.getId());
                messages.send(player, messages.getOrDefault("messages.info.unclaimed", "&cYour claim has been unclaimed."));
            } else {
                messages.send(player, messages.getOrDefault("messages.error.not-owner", "&cYou are not the owner of this claim."));
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
        String name = messages.getOrDefault("messages.gui.button.claim-info", "&eClaim Info");

        List<String> lore = new ArrayList<>();
        if (plot == null) {
            lore.add(messages.color("&7" + messages.getOrDefault("messages.info.no-claim-here", "No claim here.")));
        } else {
            Map<String, String> info = plot.getInfo();
            for (Map.Entry<String, String> entry : info.entrySet()) {
                lore.add(messages.color("&7" + entry.getKey() + ": &f" + entry.getValue()));
            }
        }
        return simpleItem(Material.PAPER, name, lore);
    }

    /* ===================================================================
     * PLAYER EXPANSION REQUEST (Submit + Status)
     * =================================================================== */

    public void openPlayerExpansionRequest(Player player) {
        String title = messages.getOrDefault("messages.gui.titles.request-expansion", "&aRequest Expansion");
        int size = plugin.getConfig().getInt("gui.menus.player-expansion.size", 27);
        Inventory inv = Bukkit.createInventory(player, size, messages.color(title));

        int current = pendingPlayerExpansionAmount.getOrDefault(player.getUniqueId(), 16);

        // Controls
        inv.setItem(10, simpleItem(Material.LIME_DYE, "&f+16", "&7" + messages.getOrDefault("messages.gui.expansion.plus16", "Increase by 16 blocks")));
        inv.setItem(11, simpleItem(Material.LIME_DYE, "&f+32", "&7" + messages.getOrDefault("messages.gui.expansion.plus32", "Increase by 32 blocks")));
        inv.setItem(12, simpleItem(Material.LIME_DYE, "&f+64", "&7" + messages.getOrDefault("messages.gui.expansion.plus64", "Increase by 64 blocks")));

        inv.setItem(13, simpleItem(Material.EMERALD_BLOCK,
                messages.getOrDefault("messages.gui.expansion.submit", "&aSubmit Request"),
                "&7" + messages.getOrDefault("messages.gui.expansion.current-amount", "Current amount:") + " &f" + current,
                "&7" + messages.getOrDefault("messages.gui.expansion.submit-lore", "Click to submit")));

        inv.setItem(14, simpleItem(Material.RED_DYE, "&f-16", "&7" + messages.getOrDefault("messages.gui.expansion.minus16", "Decrease by 16 blocks")));
        inv.setItem(15, simpleItem(Material.RED_DYE, "&f-32", "&7" + messages.getOrDefault("messages.gui.expansion.minus32", "Decrease by 32 blocks")));
        inv.setItem(16, simpleItem(Material.RED_DYE, "&f-64", "&7" + messages.getOrDefault("messages.gui.expansion.minus64", "Decrease by 64 blocks")));

        // Status preview (latest request for this player)
        ExpansionRequest latest = expansionManager.getLatestFor(player.getUniqueId());
        if (latest != null) {
            List<String> statusLore = new ArrayList<>();
            statusLore.add("&7" + messages.getOrDefault("messages.expansion.amount", "Amount:") + " &f+" + latest.getAmount());
            statusLore.add("&7" + messages.getOrDefault("messages.expansion.created", "Created:") + " &f" + dtFmt().format(latest.getCreatedAt()));
            statusLore.add("&7" + messages.getOrDefault("messages.expansion.status", "Status:") + " &e" + latest.getStatus().name());
            if (latest.getReviewedAt() != null) {
                statusLore.add("&7" + messages.getOrDefault("messages.expansion.reviewed", "Reviewed:") + " &f" + dtFmt().format(latest.getReviewedAt()));
            }
            if (latest.getReviewedBy() != null) {
                statusLore.add("&7" + messages.getOrDefault("messages.expansion.reviewer", "Reviewer:") + " &f" + safeName(latest.getReviewedBy()));
            }
            if (latest.getDenialReason() != null) {
                statusLore.add("&7" + messages.getOrDefault("messages.expansion.reason", "Reason:") + " &f" + latest.getDenialReason());
            }
            inv.setItem(22, simpleItem(Material.PAPER,
                    messages.getOrDefault("messages.gui.expansion.latest", "&eYour latest request"),
                    statusLore));
        }

        placeNavButtons(inv);
        player.openInventory(inv);
    }

    public void handlePlayerExpansionRequestClick(Player player, InventoryClickEvent event) {
        ItemStack clicked = event.getCurrentItem();
        if (isBack(clicked)) { clickTone(player); openMain(player); return; }
        if (isExit(clicked)) { clickTone(player); player.closeInventory(); return; }
        if (clicked == null || !clicked.hasItemMeta() || !clicked.getItemMeta().hasDisplayName()) return;

        String dn = strip(clicked.getItemMeta().getDisplayName());
        int current = pendingPlayerExpansionAmount.getOrDefault(player.getUniqueId(), 16);

        switch (dn) {
            case "+16" -> current += 16;
            case "+32" -> current += 32;
            case "+64" -> current += 64;
            case "-16" -> current = Math.max(1, current - 16);
            case "-32" -> current = Math.max(1, current - 32);
            case "-64" -> current = Math.max(1, current - 64);
            default -> {
                if (dn.toLowerCase(Locale.ROOT).contains("submit")) {
                    int amount = Math.max(1, current);
                    ExpansionRequest req = expansionManager.createRequest(player.getUniqueId(), amount);
                    if (req != null) {
                        messages.send(player, messages.getOrDefault("messages.expansion.request-sent",
                                "&eYour expansion request for +{blocks} blocks has been sent to admins.")
                                .replace("{blocks}", String.valueOf(amount)));
                    } else {
                        messages.send(player, messages.getOrDefault("messages.error.expansion-submit-failed",
                                "&cCould not submit your request. Try again later."));
                    }
                    pendingPlayerExpansionAmount.remove(player.getUniqueId());
                    clickTone(player);
                    openMain(player);
                    return;
                }
            }
        }
        pendingPlayerExpansionAmount.put(player.getUniqueId(), current);
        clickTone(player);
        openPlayerExpansionRequest(player);
    }

    /* ===================================================================
     * TRUSTED PLAYERS + ROLES
     * =================================================================== */

    public void openTrusted(Player player) {
        String title = messages.getOrDefault("messages.gui.titles.trusted", "&bTrusted Players");
        int size = plugin.getConfig().getInt("gui.menus.roles.size", 45);
        Inventory inv = Bukkit.createInventory(player, size, messages.color(title));

        Plot plot = plotManager.getPlotAt(player.getLocation());
        if (plot == null) {
            inv.setItem(13, simpleItem(Material.BARRIER,
                    "&c" + messages.getOrDefault("messages.error.no-claim-here", "No claim here"),
                    "&7" + messages.getOrDefault("messages.info.stand-in-claim", "Stand inside your claim to manage roles.")));
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
            lore.add(messages.color("&7" + messages.getOrDefault("messages.roles.label", "Role:") + " &b" + role.getDisplayName()));
            lore.add(messages.color("&a" + messages.getOrDefault("messages.gui.hint.left-assign", "Left-click: Assign new role")));
            lore.add(messages.color("&c" + messages.getOrDefault("messages.gui.hint.right-untrust", "Right-click: Untrust")));
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
            messages.send(player, messages.getOrDefault("messages.roles.untrusted", "&cUntrusted &f{player}")
                    .replace("{player}", name));
            clickTone(player);
            openTrusted(player);
        }
    }

    public void openAssignRole(Player actor, UUID targetUuid) {
        String title = messages.getOrDefault("messages.gui.titles.assign-role", "&bAssign Role");
        int size = plugin.getConfig().getInt("gui.menus.assign-role.size", 45);
        Inventory inv = Bukkit.createInventory(actor, size, messages.color(title));

        int slot = 0;
        for (ClaimRole role : ClaimRole.values()) {
            if (role == ClaimRole.NONE || role == ClaimRole.OWNER) continue;

            String displayName = role.getDisplayName();

            List<String> lore = messages.getList("messages.roles.lore." + role.name().toLowerCase(Locale.ROOT));
            if (lore.isEmpty()) {
                lore = List.of("&7" + messages.getOrDefault("messages.gui.hint.click-assign", "Click to assign this role"));
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

        String roleName = strip(clicked.getItemMeta().getDisplayName());
        ClaimRole role = ClaimRole.fromName(roleName);

        if (role == ClaimRole.NONE || role == ClaimRole.OWNER) {
            messages.send(player, messages.getOrDefault("messages.error.invalid-role", "&cInvalid role selection."));
            return;
        }

        Plot plot = plotManager.getPlotAt(player.getLocation());
        if (plot == null) {
            messages.send(player, messages.getOrDefault("messages.error.no-claim-here", "&cNo claim found here."));
            return;
        }

        roleManager.setRole(plot, targetUuid, role);
        String targetName = Optional.ofNullable(Bukkit.getOfflinePlayer(targetUuid).getName())
                .orElse(targetUuid.toString().substring(0, 8));

        messages.send(player, messages.getOrDefault("messages.roles.assigned",
                "&aAssigned &f{player} &ato role &f{role}&a.")
                .replace("{player}", targetName)
                .replace("{role}", role.getDisplayName()));
        clickTone(player);
        openTrusted(player);
    }

    public void clearPendingRoleAssignment(UUID actorUuid) {
        pendingRoleAssignments.remove(actorUuid);
    }

    /* ===================================================================
     * FLAGS (Player Claim)
     * =================================================================== */

    public void openFlags(Player player) {
        String title = messages.getOrDefault("messages.gui.titles.flags", "&eClaim Flags");
        int size = plugin.getConfig().getInt("gui.menus.flags.size", 27);
        Inventory inv = Bukkit.createInventory(player, size, messages.color(title));

        Plot plot = plotManager.getPlotAt(player.getLocation());
        if (plot == null) {
            inv.setItem(13, simpleItem(Material.BARRIER,
                    "&c" + messages.getOrDefault("messages.error.no-claim-here", "No claim here"),
                    "&7" + messages.getOrDefault("messages.info.stand-in-claim", "Stand inside your claim to manage flags.")));
        } else {
            boolean pvp = getEffectiveClaimFlag(plot, "pvp");
            inv.setItem(11, simpleItem(Material.DIAMOND_SWORD,
                    "&fPvP: " + (pvp ? "&aON" : "&cOFF"),
                    "&7" + messages.getOrDefault("messages.flags.pvp-lore", "Toggle PvP inside this claim"),
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
                messages.send(player, messages.getOrDefault("messages.flags.toggled",
                        "&eToggled &f{flag} &eto {state}")
                        .replace("{flag}", flagKey)
                        .replace("{state}", !current ? "&aON" : "&cOFF"));
                clickTone(player);
                openFlags(player);
            }
        }
    }

    /* ===================================================================
     * ADMIN TOOLS
     * =================================================================== */

    public void openAdminTools(Player player) {
        String title = messages.getOrDefault("messages.gui.titles.admin", "&cAdmin Tools");
        int size = plugin.getConfig().getInt("gui.menus.admin-tools.size", 45);
        Inventory inv = Bukkit.createInventory(player, size, messages.color(title));

        inv.setItem(10, simpleItem(Material.BOOK,
                messages.getOrDefault("messages.gui.admin.reload", "&eReload Config"),
                "&7" + messages.getOrDefault("messages.gui.admin.reload-lore", "Reloads all configuration files.")));

        inv.setItem(12, simpleItem(Material.REDSTONE_TORCH,
                messages.getOrDefault("messages.gui.admin.debug", "&cToggle Debug"),
                "&7" + messages.getOrDefault("messages.gui.admin.debug-lore", "Enable/disable debug mode.")));

        inv.setItem(14, simpleItem(Material.FEATHER,
                messages.getOrDefault("messages.gui.admin.bypass", "&aToggle Bypass"),
                "&7" + messages.getOrDefault("messages.gui.admin.bypass-lore", "Enable/disable admin bypass.")));

        inv.setItem(16, simpleItem(Material.NETHER_STAR,
                messages.getOrDefault("messages.gui.titles.world-controls", "&dWorld Controls"),
                "&7" + messages.getOrDefault("messages.gui.admin.world-controls-lore", "Manage world-level flags.")));

        inv.setItem(28, simpleItem(Material.PAPER,
                messages.getOrDefault("messages.gui.titles.expansion-requests", "&eExpansion Requests"),
                "&7" + messages.getOrDefault("messages.gui.admin.expansion-requests-lore", "Review pending expansion requests.")));

        inv.setItem(30, simpleItem(Material.WRITABLE_BOOK,
                messages.getOrDefault("messages.gui.titles.expansion-history", "&7Expansion History"),
                "&7" + messages.getOrDefault("messages.gui.admin.expansion-history-lore", "Browse expansion request history.")));

        placeNavButtons(inv);
        player.openInventory(inv);
    }

    public void handleAdminClick(Player player, InventoryClickEvent event) {
        ItemStack clicked = event.getCurrentItem();
        if (isBack(clicked)) { clickTone(player); openMain(player); return; }
        if (isExit(clicked)) { clickTone(player); player.closeInventory(); return; }
        if (clicked == null || !clicked.hasItemMeta() || !clicked.getItemMeta().hasDisplayName()) return;

        String name = strip(clicked.getItemMeta().getDisplayName()).toLowerCase(Locale.ROOT);

        if (name.contains("reload")) {
            plugin.reloadConfig();
            plugin.getMessagesUtil().reload();
            messages.send(player, messages.getOrDefault("messages.reloaded", "&aProShield configuration reloaded."));
            clickTone(player);
        } else if (name.contains("debug")) {
            plugin.toggleDebug();
            messages.send(player, plugin.isDebugEnabled()
                    ? messages.getOrDefault("messages.admin.debug-on", "&eDebug mode: &aENABLED")
                    : messages.getOrDefault("messages.admin.debug-off", "&eDebug mode: &cDISABLED"));
            clickTone(player);
        } else if (name.contains("bypass")) {
            boolean newState = plugin.toggleBypass(player.getUniqueId());
            messages.send(player, newState
                    ? messages.getOrDefault("messages.admin.bypass-on", "&aBypass enabled.")
                    : messages.getOrDefault("messages.admin.bypass-off", "&cBypass disabled."));
            clickTone(player);
        } else if (name.contains("world controls")) {
            clickTone(player);
            openWorldControls(player);
        } else if (name.contains("expansion requests")) {
            clickTone(player);
            openExpansionReview(player);
        } else if (name.contains("expansion history")) {
            clickTone(player);
            openHistory(player, 0, null);
        }
    }

    /* ===================================================================
     * WORLD CONTROLS (Admin)
     * =================================================================== */

    public void openWorldControls(Player player) {
        String title = messages.getOrDefault("messages.gui.titles.world-controls", "&cWorld Controls");
        int size = plugin.getConfig().getInt("gui.menus.world-controls.size", 27);
        Inventory inv = Bukkit.createInventory(player, size, messages.color(title));

        boolean globalPvp = plugin.getConfig().getBoolean("world.flags.pvp", true);
        inv.setItem(11, simpleItem(Material.DIAMOND_SWORD,
                "&fGlobal PvP: " + (globalPvp ? "&aON" : "&cOFF"),
                "&7" + messages.getOrDefault("messages.world-controls.pvp-lore", "Toggle PvP across the entire world"),
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

            messages.send(player, messages.getOrDefault("messages.world-controls.toggle",
                    "&eWorld control &f{flag} &eis now {state}.")
                    .replace("{flag}", flagKey)
                    .replace("{state}", !current
                            ? messages.getOrDefault("messages.world-controls.enabled", "&aENABLED")
                            : messages.getOrDefault("messages.world-controls.disabled", "&cDISABLED")));
            clickTone(player);
            openWorldControls(player);
        }
    }

    /* ===================================================================
     * EXPANSION REVIEW (Admin)
     * =================================================================== */

    public void openExpansionReview(Player player) {
        String title = messages.getOrDefault("messages.gui.titles.expansion-requests", "&eExpansion Requests");
        int size = plugin.getConfig().getInt("gui.menus.expansion-requests.size", 54);
        Inventory inv = Bukkit.createInventory(player, size, messages.color(title));

        List<ExpansionRequest> pending = expansionManager.getPendingRequests();
        if (pending.isEmpty()) {
            inv.setItem(22, simpleItem(Material.GRAY_STAINED_GLASS_PANE,
                    messages.getOrDefault("messages.expansion.none-pending", "&7No pending requests"),
                    "&7" + messages.getOrDefault("messages.expansion.none-pending-lore", "Check back later.")));
            placeNavButtons(inv);
            player.openInventory(inv);
            return;
        }

        int slot = 0;
        DateTimeFormatter fmt = dtFmt();
        for (ExpansionRequest req : pending) {
            String name = safeName(req.getRequester());

            List<String> lore = new ArrayList<>();
            lore.add("&7" + messages.getOrDefault("messages.expansion.requester", "Requester:") + " &f" + name);
            lore.add("&7" + messages.getOrDefault("messages.expansion.amount", "Amount:") + " &f+" + req.getAmount());
            lore.add("&7" + messages.getOrDefault("messages.expansion.status", "Status:") + " &e" + req.getStatus().name());
            lore.add("&7" + messages.getOrDefault("messages.expansion.created", "Created:") + " &f" + fmt.format(req.getCreatedAt()));
            lore.add(messages.color("&a" + messages.getOrDefault("messages.expansion-admin.lore.approve", "Left-click: Approve this expansion")));
            lore.add(messages.color("&c" + messages.getOrDefault("messages.expansion-admin.lore.deny", "Right-click: Deny and choose a reason")));
            lore.add(messages.color("&7" + messages.getOrDefault("messages.expansion-admin.lore.info", "Use the deny reasons menu for detailed denial messages.")));
            lore.add(TAG_RID + req.getId());

            ItemStack item = simpleItem(Material.EMERALD, "&e+" + req.getAmount() + " &7(" + name + ")", lore);
            inv.setItem(slot++, item);
            if (slot >= size - 9) break;
        }

        placeNavButtons(inv);
        player.openInventory(inv);
    }

    public void handleExpansionReviewClick(Player player, InventoryClickEvent event) {
        ItemStack clicked = event.getCurrentItem();
        if (isBack(clicked)) { clickTone(player); openAdminTools(player); return; }
        if (isExit(clicked)) { clickTone(player); player.closeInventory(); return; }
        if (clicked == null || !clicked.hasItemMeta()) return;

        UUID rid = extractHiddenRequestId(clicked);
        if (rid == null) return;

        ExpansionRequest req = expansionManager.findById(rid);
        if (req == null || !req.isPending()) {
            messages.send(player, messages.getOrDefault("messages.expansion.not-found", "&cRequest not found or not pending."));
            clickTone(player);
            openExpansionReview(player);
            return;
        }

        if (event.isLeftClick()) {
            expansionManager.approveRequest(req, player.getUniqueId());
            messages.send(player, messages.getOrDefault("messages.expansion.approved-admin", "&aApproved expansion (+{blocks}) for {player}.")
                    .replace("{blocks}", String.valueOf(req.getAmount()))
                    .replace("{player}", safeName(req.getRequester())));
            clickTone(player);
            openExpansionReview(player);
        } else if (event.isRightClick()) {
            pendingDenyTarget.put(player.getUniqueId(), rid);
            clickTone(player);
            openDenyReasons(player);
        }
    }

    /* ===================================================================
     * DENY REASONS
     * =================================================================== */

    public void openDenyReasons(Player player) {
        String title = messages.getOrDefault("messages.gui.titles.deny-reasons", "&cDeny Reasons");
        int size = plugin.getConfig().getInt("gui.menus.deny-reasons.size", 27);
        Inventory inv = Bukkit.createInventory(player, size, messages.color(title));

        Set<String> keys = messages.getKeys("messages.deny-reasons");
        if (keys.isEmpty()) {
            inv.setItem(13, simpleItem(Material.BARRIER,
                    "&c" + messages.getOrDefault("messages.deny.none", "No reasons configured"),
                    "&7" + messages.getOrDefault("messages.deny.none-lore", "Add your reasons in messages.yml")));
        } else {
            int slot = 0;
            for (String k : keys) {
                String txt = messages.getOrDefault("messages.deny-reasons." + k, "&cReason");
                inv.setItem(slot++, simpleItem(Material.RED_DYE, txt, "&7Click to use this reason"));
                if (slot >= size - 9) break;
            }
        }

        placeNavButtons(inv);
        player.openInventory(inv);
    }

    public void handleDenyReasonClick(Player player, InventoryClickEvent event) {
        ItemStack clicked = event.getCurrentItem();
        if (isBack(clicked)) { clickTone(player); openExpansionReview(player); return; }
        if (isExit(clicked)) { clearPendingDenyTarget(player.getUniqueId()); clickTone(player); player.closeInventory(); return; }
        if (clicked == null || !clicked.hasItemMeta() || !clicked.getItemMeta().hasDisplayName()) return;

        UUID rid = pendingDenyTarget.remove(player.getUniqueId());
        if (rid == null) { clickTone(player); openExpansionReview(player); return; }

        ExpansionRequest req = expansionManager.findById(rid);
        if (req == null || !req.isPending()) {
            messages.send(player, messages.getOrDefault("messages.expansion.not-found", "&cRequest not found or not pending."));
            clickTone(player);
            openExpansionReview(player);
            return;
        }

        String reason = strip(clicked.getItemMeta().getDisplayName());
        if (reason.isBlank()) reason = messages.getOrDefault("messages.deny.default", "Denied.");

        expansionManager.denyRequest(req, player.getUniqueId(), reason);
        messages.send(player, messages.getOrDefault("messages.expansion.denied-admin", "&cDenied expansion (+{blocks}) for {player}: {reason}")
                .replace("{blocks}", String.valueOf(req.getAmount()))
                .replace("{player}", safeName(req.getRequester()))
                .replace("{reason}", reason));

        clickTone(player);
        openExpansionReview(player);
    }

    public void clearPendingDenyTarget(UUID admin) {
        pendingDenyTarget.remove(admin);
    }

    /* ===================================================================
     * EXPANSION HISTORY (Paginated)
     * =================================================================== */

    public void openHistory(Player player, int page, UUID filterByPlayer) {
        String title = messages.getOrDefault("messages.gui.titles.expansion-history", "&7Expansion History");
        int size = plugin.getConfig().getInt("gui.menus.expansion-history.size", 54);
        Inventory inv = Bukkit.createInventory(player, size, messages.color(title));

        List<ExpansionRequest> source;
        if (filterByPlayer != null) {
            source = expansionManager.getAllRequestsFor(filterByPlayer);
        } else {
            source = expansionManager.getAllRequests();
        }

        source.sort(Comparator.comparing(ExpansionRequest::getCreatedAt).reversed());
        historyPages.put(player.getUniqueId(), page);

        int start = page * HISTORY_PER_PAGE;
        int end = Math.min(start + HISTORY_PER_PAGE, source.size());

        if (start >= source.size()) {
            inv.setItem(22, simpleItem(Material.GRAY_STAINED_GLASS_PANE,
                    messages.getOrDefault("messages.history.empty", "&7No history to show"),
                    "&7" + messages.getOrDefault("messages.history.empty-lore", "There are no entries at this time.")));
            placeNavButtons(inv);
            player.openInventory(inv);
            return;
        }

        DateTimeFormatter fmt = dtFmt();
        int slot = 0;
        for (int i = start; i < end; i++) {
            ExpansionRequest req = source.get(i);
            String name = safeName(req.getRequester());

            List<String> lore = new ArrayList<>();
            lore.add("&7" + messages.getOrDefault("messages.expansion.requester", "Requester:") + " &f" + name);
            lore.add("&7" + messages.getOrDefault("messages.expansion.amount", "Amount:") + " &f+" + req.getAmount());
            lore.add("&7" + messages.getOrDefault("messages.expansion.status", "Status:") + " &e" + req.getStatus().name());
            lore.add("&7" + messages.getOrDefault("messages.expansion.created", "Created:") + " &f" + fmt.format(req.getCreatedAt()));

            if (req.getReviewedAt() != null) {
                lore.add("&7" + messages.getOrDefault("messages.expansion.reviewed", "Reviewed:") + " &f" + fmt.format(req.getReviewedAt()));
            }
            if (req.getReviewedBy() != null) {
                lore.add("&7" + messages.getOrDefault("messages.expansion.reviewer", "Reviewer:") + " &f" + safeName(req.getReviewedBy()));
            }
            if (req.getDenialReason() != null) {
                lore.add("&7" + messages.getOrDefault("messages.expansion.reason", "Reason:") + " &f" + req.getDenialReason());
            }
            lore.add(TAG_RID + req.getId());
            lore.add(TAG_TS + req.getCreatedAt().toString());

            Material icon = switch (req.getStatus()) {
                case APPROVED -> Material.LIME_DYE;
                case DENIED -> Material.RED_DYE;
                case EXPIRED -> Material.GRAY_DYE;
                default -> Material.PAPER;
            };

            inv.setItem(slot++, simpleItem(icon, "&f+" + req.getAmount() + " &7(" + name + ")", lore));
        }

        // Pagination controls
        if (end < source.size()) {
            inv.setItem(size - 5, simpleItem(Material.ARROW,
                    "&e" + messages.getOrDefault("messages.gui.page.next", "Next page"),
                    List.of(messages.color("&7" + messages.getOrDefault("messages.gui.page.next", "Next page")), TAG_ACT + ACT_PAGE_NEXT)));
        }
        if (page > 0) {
            inv.setItem(size - 6, simpleItem(Material.ARROW,
                    "&e" + messages.getOrDefault("messages.gui.page.prev", "Previous page"),
                    List.of(messages.color("&7" + messages.getOrDefault("messages.gui.page.prev", "Previous page")), TAG_ACT + ACT_PAGE_PREV)));
        }

        placeNavButtons(inv);
        player.openInventory(inv);
    }

    public void handleHistoryClick(Player player, InventoryClickEvent event) {
        ItemStack clicked = event.getCurrentItem();
        if (isBack(clicked)) { clickTone(player); openAdminTools(player); return; }
        if (isExit(clicked)) { clickTone(player); player.closeInventory(); return; }
        if (clicked == null || !clicked.hasItemMeta()) return;

        String act = extractHidden(clicked, TAG_ACT);
        if (ACT_PAGE_NEXT.equalsIgnoreCase(act)) {
            int cur = historyPages.getOrDefault(player.getUniqueId(), 0);
            clickTone(player);
            openHistory(player, cur + 1, null);
            return;
        } else if (ACT_PAGE_PREV.equalsIgnoreCase(act)) {
            int cur = historyPages.getOrDefault(player.getUniqueId(), 0);
            clickTone(player);
            openHistory(player, Math.max(0, cur - 1), null);
            return;
        }
    }
}
