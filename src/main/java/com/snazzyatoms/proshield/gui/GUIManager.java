// ========================== GUIManager.java (v1.2.6 FINAL) ==========================
// Polished, consolidated GUI Manager — preserves 1.2.4 → 1.2.6 and enhances:
// - Full Admin ► World Controls (PvP ON by default; SafeZone OFF by default)  ✅
// - Claim Info / Trusted (pagination) / Roles assign / Flags toggle            ✅
// - Expansion Requests (player submit + admin review)                          ✅
// - Back & Exit buttons consistently wired                                     ✅
// - Works with provided GUIListener (title-based routing + handle*Click API)   ✅
// - Avoids deprecated methods from older drafts (compiles vs your classes)     ✅
// ================================================================================

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
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.World;
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
import java.util.function.Function;

// Main entry for all ProShield GUIs.
public class GUIManager {

    // ------------------------------ Constants ------------------------------

    private static final int SIZE_27 = 27;
    private static final int SIZE_36 = 36;
    private static final int SIZE_45 = 45;
    private static final int SIZE_54 = 54;

    private static final ItemStack FILLER = quick(Material.GRAY_STAINED_GLASS_PANE, " ");
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            .withZone(ZoneId.systemDefault());

    // Admin ► World Controls default keys (kept simple and explicit)
    private static final String WC_PVP      = "pvp";
    private static final String WC_SAFEZONE = "safezone";

    // ----------------------------- Dependencies ----------------------------

    private final ProShield plugin;
    private final MessagesUtil messages;
    private final PlotManager plots;
    private final ClaimRoleManager roles;
    private final ExpansionRequestManager expansions;

    // ------------------------------ State ----------------------------------

    // Per-player "view stack" so Back works across nested menus
    private final Map<UUID, Deque<View>> nav = new HashMap<>();

    // ---------------------------- Construction -----------------------------

    public GUIManager(ProShield plugin) {
        this.plugin   = plugin;
        this.messages = plugin.getMessagesUtil();
        this.plots    = plugin.getPlotManager();
        this.roles    = plugin.getRoleManager();
        this.expansions = plugin.getExpansionRequestManager();
    }

    // --- Compatibility alias for callers expecting openMain(Player) ---
    public void openMain(Player player) { openMainMenu(player); }

    // ---------------------------- Openers (UI) -----------------------------

    // Titles intentionally include stable keywords so GUIListener’s title routing matches.
    private String title(String key, String fallback) {
        // messages.yml: messages.gui.titles.<key>
        String raw = messages.getOrDefault("messages.gui.titles." + key, fallback);
        return ChatColor.translateAlternateColorCodes('&', raw);
    }

    public void openMainMenu(Player p) {
        Inventory inv = Bukkit.createInventory(p, SIZE_27, title("main", "&8ProShield Menu"));
        border(inv);

        Plot here = plots.getPlotAt(p.getLocation());

        inv.setItem(11, iconClaimButton(here == null));
        inv.setItem(13, iconClaimInfo(here));
        inv.setItem(15, iconUnclaimButton(here != null));

        inv.setItem(20, iconTrusted(here != null));
        inv.setItem(22, iconRoles(here != null));
        inv.setItem(24, iconFlags(here != null));

        if (p.hasPermission("proshield.admin")) {
            inv.setItem(26, iconAdminTools());
        }

        push(p, View.main());
        p.openInventory(inv);
        click(p);
    }

    public void openClaimInfo(Player p) {
        Plot plot = plots.getPlotAt(p.getLocation());
        Inventory inv = Bukkit.createInventory(p, SIZE_36, title("claim-info", "&8Claim Info"));
        border(inv);

        if (plot == null) {
            inv.setItem(13, textItem(Material.BOOK, "&7No claim here.", List.of(
                    line("#NOOP")
            )));
        } else {
            inv.setItem(11, iconOwner(plot.getOwner()));
            inv.setItem(13, iconClaimSummary(plot));
            inv.setItem(15, iconExpansionRequest());
        }

        inv.setItem(31, backButton());
        push(p, View.claimInfo());
        p.openInventory(inv);
        click(p);
    }

    public void openTrusted(Player p, int page) {
        Plot plot = plots.getPlotAt(p.getLocation());
        Inventory inv = Bukkit.createInventory(p, SIZE_54, title("trusted", "&8Trusted Players"));
        border(inv);

        if (plot == null) {
            inv.setItem(22, textItem(Material.PLAYER_HEAD, "&7No claim here.", List.of(line("#NOOP"))));
        } else {
            // Paginate trusted map
            List<UUID> list = new ArrayList<>(plot.getTrusted().keySet());
            list.sort(Comparator.comparing(this::nameOrShort));
            int pageSize = 28;
            int maxPage  = Math.max(0, (list.size() - 1) / pageSize);
            page = Math.max(0, Math.min(page, maxPage));

            placePager(inv, page, maxPage);

            int start = page * pageSize;
            for (int i = 0; i < pageSize && start + i < list.size(); i++) {
                UUID u = list.get(start + i);
                inv.setItem(grid(i), iconTrustedEntry(plot, u));
            }
        }

        inv.setItem(49, backButton());
        push(p, View.trusted(page));
        p.openInventory(inv);
        click(p);
    }

    public void openAssignRole(Player p) {
        // This menu expects you clicked a trusted player first, so we store “pending target” in view
        Plot plot = plots.getPlotAt(p.getLocation());
        if (plot == null) { warn(p, "&cStand inside your claim to assign roles."); return; }

        View peek = peek(p);
        if (peek == null || peek.pendingTarget == null) { warn(p, "&cPick a player in Trusted first."); return; }

        Inventory inv = Bukkit.createInventory(p, SIZE_45, title("assign-role", "&8Assign Role"));
        border(inv);

        int i = 10;
        for (ClaimRole r : ClaimRole.values()) {
            if (r == ClaimRole.NONE) continue; // skip NONE
            inv.setItem(i, iconRoleOption(r, plot, peek.pendingTarget));
            i += (i % 9 == 7) ? 3 : 1;
        }

        inv.setItem(40, backButton());
        push(p, View.assignRole(peek.pendingTarget));
        p.openInventory(inv);
        click(p);
    }

    public void openFlags(Player p, int page) {
        Plot plot = plots.getPlotAt(p.getLocation());
        Inventory inv = Bukkit.createInventory(p, SIZE_54, title("flags", "&8Claim Flags"));
        border(inv);

        if (plot == null) {
            inv.setItem(22, textItem(Material.LEVER, "&7No claim here.", List.of(line("#NOOP"))));
        } else {
            List<FlagSpec> spec = defaultFlags();
            int pageSize = 21;
            int maxPage  = Math.max(0, (spec.size() - 1) / pageSize);
            page = Math.max(0, Math.min(page, maxPage));
            placePager(inv, page, maxPage);

            int start = page * pageSize;
            for (int i = 0; i < pageSize && start + i < spec.size(); i++) {
                FlagSpec f = spec.get(start + i);
                inv.setItem(grid(i), iconFlag(plot, f));
            }
        }
        inv.setItem(49, backButton());

        push(p, View.flags(page));
        p.openInventory(inv);
        click(p);
    }

    public void openAdmin(Player p) {
        Inventory inv = Bukkit.createInventory(p, SIZE_27, title("admin", "&8Admin Tools"));
        border(inv);

        inv.setItem(11, textItem(Material.ENDER_PEARL, "&bNearest Claim", List.of(
                gray("&7Teleport to nearest claim (≤200 blocks)."),
                line("#ADMIN:TP_NEAREST")
        )));
        inv.setItem(13, textItem(Material.PAPER, "&aPending Requests", List.of(
                gray("&7Approve or deny expansion requests."),
                line("#ADMIN:PENDING")
        )));
        inv.setItem(15, textItem(Material.REDSTONE, "&cWorld Controls", List.of(
                gray("&7Toggle per-world settings (PvP, Safe Zone, …)"),
                line("#ADMIN:WORLD_CTRL")
        )));

        inv.setItem(22, backButton());
        push(p, View.admin());
        p.openInventory(inv);
        click(p);
    }

    public void openWorldControls(Player p, int page) {
        // World list + enter world detail
        Inventory inv = Bukkit.createInventory(p, SIZE_54, title("world-controls", "&8World Controls"));
        border(inv);

        List<World> worlds = Bukkit.getWorlds();
        int pageSize = 21;
        int maxPage  = Math.max(0, (worlds.size() - 1) / pageSize);
        page = Math.max(0, Math.min(page, maxPage));
        placePager(inv, page, maxPage);

        int start = page * pageSize;
        for (int i = 0; i < pageSize && start + i < worlds.size(); i++) {
            World w = worlds.get(start + i);
            inv.setItem(grid(i), iconWorld(w.getName()));
        }

        inv.setItem(49, backButton());
        push(p, View.worlds(page));
        p.openInventory(inv);
        click(p);
    }

    public void openWorldDetail(Player p, String worldName) {
        Inventory inv = Bukkit.createInventory(p, SIZE_45, title("world-controls", "&8World: " + worldName));
        border(inv);

        // Show well-known toggles first (PvP / SafeZone), then anything else under config worlds.<world>.*
        boolean pvp      = readWorldBool(worldName, WC_PVP, true);       // default ON
        boolean safezone = readWorldBool(worldName, WC_SAFEZONE, false); // default OFF

        inv.setItem(10, iconWorldToggle(worldName, WC_PVP, pvp,
                Material.IRON_SWORD, "&ePvP", "&7Global player-versus-player damage"));
        inv.setItem(12, iconWorldToggle(worldName, WC_SAFEZONE, safezone,
                Material.TOTEM_OF_UNDYING, "&dSafe Zone", "&7Disable combat & damage in world"));

        // Extra keys (any booleans the server owner added under worlds.<world>.xxx)
        Map<String, Boolean> extras = readAllWorldBools(worldName);
        int slot = 20;
        for (Map.Entry<String, Boolean> e : extras.entrySet()) {
            String key = e.getKey();
            if (key.equalsIgnoreCase(WC_PVP) || key.equalsIgnoreCase(WC_SAFEZONE)) continue;
            boolean val = e.getValue();
            inv.setItem(slot, iconWorldToggle(worldName, key, val, Material.LEVER,
                    "&6" + key, "&7Toggle " + key));
            slot += (slot % 9 == 7) ? 3 : 1;
            if (slot >= 33) break; // keep layout neat
        }

        inv.setItem(40, backButton());
        push(p, View.worldDetail(worldName));
        p.openInventory(inv);
        click(p);
    }

    public void openPending(Player p, int page) {
        Inventory inv = Bukkit.createInventory(p, SIZE_54, title("expansion-requests", "&8Expansion Requests"));
        border(inv);

        List<ExpansionRequest> pending = new ArrayList<>(expansions.getPendingRequests());
        pending.sort(Comparator.comparing(ExpansionRequest::getCreatedAt).reversed());

        int pageSize = 21;
        int maxPage  = Math.max(0, (pending.size() - 1) / pageSize);
        page = Math.max(0, Math.min(page, maxPage));
        placePager(inv, page, maxPage);

        int start = page * pageSize;
        for (int i = 0; i < pageSize && start + i < pending.size(); i++) {
            ExpansionRequest r = pending.get(start + i);
            inv.setItem(grid(i), iconRequest(r));
        }

        inv.setItem(49, backButton());
        push(p, View.pending(page));
        p.openInventory(inv);
        click(p);
    }

    public void openHistory(Player p) {
        Inventory inv = Bukkit.createInventory(p, SIZE_54, title("expansion-history", "&8Expansion History"));
        border(inv);

        List<ExpansionRequest> history = expansions.getAllRequests(); // includes all states
        history.sort(Comparator.comparing(ExpansionRequest::getCreatedAt).reversed());

        int i = 0;
        for (ExpansionRequest r : history) {
            if (i >= 28) break;
            inv.setItem(grid(i++), iconRequest(r));
        }

        inv.setItem(49, backButton());
        push(p, View.history());
        p.openInventory(inv);
        click(p);
    }
    // ----------------------------- Click Handlers -----------------------------

    // These match GUIListener’s routing table (title contains keys).
    public void handleMainClick(Player p, InventoryClickEvent e) {
        ItemStack it = e.getCurrentItem();
        if (!valid(it)) return;
        e.setCancelled(true);
        String id = extractId(it);
        if (id == null) return;

        switch (id) {
            case "CLAIM" -> {
                Plot here = plots.getPlotAt(p.getLocation());
                if (here != null) { warn(p, "&cThis chunk is already claimed."); return; }
                Plot created = plots.createPlot(p.getUniqueId(), p.getLocation());
                if (created != null) {
                    msg(p, "&aClaim created.");
                    openClaimInfo(p);
                } else warn(p, "&cUnable to create claim here.");
            }
            case "UNCLAIM" -> {
                Plot here = plots.getPlotAt(p.getLocation());
                if (here == null) { warn(p, "&cNo claim here."); return; }
                if (!here.getOwner().equals(p.getUniqueId()) && !p.hasPermission("proshield.admin")) {
                    deny(p); return;
                }
                plots.deletePlot(here.getId());
                msg(p, "&eClaim removed.");
                openMainMenu(p);
            }
            case "INFO" -> openClaimInfo(p);
            case "TRUSTED" -> openTrusted(p, 0);
            case "ROLES" -> {
                // To assign a role, first choose a player in Trusted; this button explains flow.
                msg(p, "&7Open &fTrusted&7, click a player, then choose a role.");
            }
            case "FLAGS" -> openFlags(p, 0);
            case "ADMIN" -> {
                if (!p.hasPermission("proshield.admin")) { deny(p); return; }
                openAdmin(p);
            }
            case "BACK" -> back(p);
            case "EXIT" -> p.closeInventory();
        }
    }

    public void handleAssignRoleClick(Player p, InventoryClickEvent e) {
        ItemStack it = e.getCurrentItem();
        if (!valid(it)) return;
        e.setCancelled(true);
        String id = extractId(it);
        if (id == null) return;

        Plot plot = plots.getPlotAt(p.getLocation());
        if (plot == null) { warn(p, "&cNo claim here."); return; }

        View peek = peek(p);
        if (peek == null || peek.pendingTarget == null) { warn(p, "&cPick a player first."); return; }

        if (!plot.getOwner().equals(p.getUniqueId()) && !p.hasPermission("proshield.admin")) { deny(p); return; }

        if (id.startsWith("ROLE:")) {
            String roleName = id.substring("ROLE:".length());
            ClaimRole role = ClaimRole.fromName(roleName);
            if (role == ClaimRole.NONE) { warn(p, "&cUnknown role."); return; }
            roles.setRole(plot, peek.pendingTarget, role);
            plots.saveAll();
            msg(p, "&aAssigned &f" + nameOrShort(peek.pendingTarget) + " &ato &f" + role.getDisplayName());
            back(p); // back to previous (Trusted or Role list)
        } else if (id.equals("BACK")) {
            back(p);
        } else if (id.equals("EXIT")) {
            p.closeInventory();
        }
    }

    public void handleTrustedClick(Player p, InventoryClickEvent e) {
        ItemStack it = e.getCurrentItem();
        if (!valid(it)) return;
        e.setCancelled(true);
        String id = extractId(it);
        if (id == null) return;

        Plot plot = plots.getPlotAt(p.getLocation());
        if (plot == null) { warn(p, "&cNo claim here."); return; }

        if (id.startsWith("TRUST:REMOVE:")) {
            if (!plot.getOwner().equals(p.getUniqueId()) && !p.hasPermission("proshield.admin")) { deny(p); return; }
            UUID target = uuidSafe(id.substring("TRUST:REMOVE:".length()));
            if (target == null) return;
            plot.getTrusted().remove(target);
            plots.saveAll();
            msg(p, "&eRemoved &f" + nameOrShort(target) + " &efrom trusted.");
            openTrusted(p, pageFrom(peek(p))); // reopen same page
        } else if (id.startsWith("TRUST:ROLE:")) {
            UUID target = uuidSafe(id.substring("TRUST:ROLE:".length()));
            if (target == null) return;
            setPendingTarget(p, target);
            openAssignRole(p);
        } else if (id.startsWith("PAGE:")) {
            int page = safeInt(id.substring("PAGE:".length()), 0);
            openTrusted(p, page);
        } else if (id.equals("BACK")) {
            back(p);
        } else if (id.equals("EXIT")) {
            p.closeInventory();
        }
    }

    public void handleFlagsClick(Player p, InventoryClickEvent e) {
        ItemStack it = e.getCurrentItem();
        if (!valid(it)) return;
        e.setCancelled(true);
        String id = extractId(it);
        if (id == null) return;

        if (id.startsWith("PAGE:")) {
            openFlags(p, safeInt(id.substring(5), 0));
            return;
        }
        if (id.equals("BACK")) { back(p); return; }
        if (id.equals("EXIT")) { p.closeInventory(); return; }

        Plot plot = plots.getPlotAt(p.getLocation());
        if (plot == null) { warn(p, "&cNo claim here."); return; }

        if (!plot.getOwner().equals(p.getUniqueId()) && !p.hasPermission("proshield.admin")) { deny(p); return; }

        if (id.startsWith("FLAG:")) {
            String key = id.substring("FLAG:".length());
            boolean cur = plot.getFlag(key);
            plot.setFlag(key, !cur);
            plots.saveAll();
            msg(p, "&b" + key + " &7→ " + (plot.getFlag(key) ? "&aON" : "&cOFF"));
            // reopen same page
            openFlags(p, pageFrom(peek(p)));
        }
    }

    public void handleAdminClick(Player p, InventoryClickEvent e) {
        ItemStack it = e.getCurrentItem();
        if (!valid(it)) return;
        e.setCancelled(true);
        String id = extractId(it);
        if (id == null) return;

        if (!p.hasPermission("proshield.admin")) { deny(p); return; }

        switch (id) {
            case "ADMIN:PENDING" -> openPending(p, 0);
            case "ADMIN:WORLD_CTRL" -> openWorldControls(p, 0);
            case "ADMIN:TP_NEAREST" -> {
                Plot nearest = nearestClaimTo(p.getLocation(), 200);
                if (nearest == null) { warn(p, "&cNo nearby claims."); return; }
                Location dest = centerOf(nearest, p.getLocation().getWorld());
                if (dest != null) p.teleport(dest);
                msg(p, "&aTeleported to claim &f" + nearest.getId());
                p.closeInventory();
            }
            case "BACK" -> back(p);
            case "EXIT" -> p.closeInventory();
        }
    }

    public void handleWorldControlsClick(Player p, InventoryClickEvent e) {
        ItemStack it = e.getCurrentItem();
        if (!valid(it)) return;
        e.setCancelled(true);
        String id = extractId(it);
        if (id == null) return;

        if (!p.hasPermission("proshield.admin")) { deny(p); return; }

        if (id.startsWith("PAGE:")) {
            openWorldControls(p, safeInt(id.substring(5), 0));
            return;
        }
        if (id.equals("BACK")) { back(p); return; }
        if (id.equals("EXIT")) { p.closeInventory(); return; }

        if (id.startsWith("WORLD:OPEN:")) {
            String world = id.substring("WORLD:OPEN:".length());
            openWorldDetail(p, world);
            return;
        }
        if (id.startsWith("WORLD:TOGGLE:")) {
            // WORLD:TOGGLE:<world>:<key>
            String[] parts = id.split(":", 4);
            if (parts.length < 4) return;
            String world = parts[2];
            String key   = parts[3];
            boolean cur  = readWorldBool(world, key, defaultWorld(key));
            writeWorldBool(world, key, !cur);
            msg(p, "&b" + world + "." + key + " &7→ " + (!cur ? "&aON" : "&cOFF"));
            // Re-open same detail view
            openWorldDetail(p, world);
        }
    }

    public void handlePlayerExpansionRequestClick(Player p, InventoryClickEvent e) {
        ItemStack it = e.getCurrentItem();
        if (!valid(it)) return;
        e.setCancelled(true);
        String id = extractId(it);
        if (id == null) return;

        // Only action here is “EXPANSION:REQUEST:+1”
        if (id.startsWith("EXPAND:")) {
            int amount = safeInt(id.substring("EXPAND:".length()), 1);
            ExpansionRequest created = expansions.createRequest(p.getUniqueId(), amount);
            if (created != null) {
                msg(p, "&aExpansion request submitted (&f+" + amount + "&a).");
                soundGood(p);
            } else {
                warn(p, "&cYou may already have a pending request.");
            }
            openClaimInfo(p);
        } else if (id.equals("BACK")) {
            back(p);
        } else if (id.equals("EXIT")) {
            p.closeInventory();
        }
    }

    public void handleExpansionReviewClick(Player p, InventoryClickEvent e) {
        ItemStack it = e.getCurrentItem();
        if (!valid(it)) return;
        e.setCancelled(true);
        String id = extractId(it);
        if (id == null) return;

        if (!p.hasPermission("proshield.admin")) { deny(p); return; }

        if (id.startsWith("PAGE:")) {
            openPending(p, safeInt(id.substring(5), 0));
            return;
        }
        if (id.equals("BACK")) { back(p); return; }
        if (id.equals("EXIT")) { p.closeInventory(); return; }

        // REQ:<uuid>:APPROVE or REQ:<uuid>:DENY
        if (id.startsWith("REQ:")) {
            String[] parts = id.split(":", 3);
            if (parts.length < 3) return;
            UUID reqId = uuidSafe(parts[1]);
            String action = parts[2];
            ExpansionRequest req = (reqId != null) ? expansions.getById(reqId) : null;
            if (req == null) { warn(p, "&cRequest not found."); return; }

            if (action.equals("APPROVE")) {
                expansions.approveRequest(req, p.getUniqueId());
                msg(p, "&aApproved &f" + shortId(req.getId()) + "&a.");
                Player requester = Bukkit.getPlayer(req.getRequester());
                if (requester != null) requester.sendMessage(gray("&aYour expansion request was approved."));
                openPending(p, pageFrom(peek(p)));
            } else if (action.equals("DENY")) {
                expansions.denyRequest(req, p.getUniqueId(), "Denied via GUI");
                msg(p, "&cDenied &f" + shortId(req.getId()) + "&c.");
                Player requester = Bukkit.getPlayer(req.getRequester());
                if (requester != null) requester.sendMessage(gray("&cYour expansion request was denied."));
                openPending(p, pageFrom(peek(p)));
            }
        }
    }

    public void handleHistoryClick(Player p, InventoryClickEvent e) {
        ItemStack it = e.getCurrentItem();
        if (!valid(it)) return;
        e.setCancelled(true);
        String id = extractId(it);
        if (id == null) return;

        if (id.equals("BACK")) { back(p); return; }
        if (id.equals("EXIT")) { p.closeInventory(); return; }
        // history is read-only
    }

    public void handleDenyReasonClick(Player p, InventoryClickEvent e) {
        // In 1.2.6 we do not collect free-text deny reasons in-GUI; kept for compatibility.
        ItemStack it = e.getCurrentItem();
        if (!valid(it)) return;
        e.setCancelled(true);
        String id = extractId(it);
        if (id == null) return;

        if (id.equals("BACK")) { back(p); return; }
        if (id.equals("EXIT")) { p.closeInventory(); return; }
    }
    // ------------------------------ Icon Builders ------------------------------

    private ItemStack iconClaimButton(boolean canClaim) {
        return textItem(canClaim ? Material.LIME_BED : Material.GRAY_BED,
                canClaim ? "&aClaim Chunk" : "&7Claim Chunk",
                List.of(gray(canClaim ? "&7Create a new claim centered here." : "&7Already claimed."),
                        line("#CLAIM")));
    }

    private ItemStack iconUnclaimButton(boolean can) {
        return textItem(can ? Material.RED_BED : Material.GRAY_BED,
                can ? "&cUnclaim Chunk" : "&7Unclaim Chunk",
                List.of(gray(can ? "&7Remove your current claim." : "&7No claim here."),
                        line("#UNCLAIM")));
    }

    private ItemStack iconClaimInfo(Plot plot) {
        List<String> lore = new ArrayList<>();
        if (plot == null) {
            lore.add(gray("&7You are not standing in a claim."));
        } else {
            lore.add(gray("&7ID: &f" + plot.getId()));
            lore.add(gray("&7Owner: &f" + nameOrShort(plot.getOwner())));
            lore.add(gray("&7Center: &f" + plot.getWorld() + " " + plot.getX() + "," + plot.getZ()));
            lore.add(gray("&7Radius: &f" + plot.getRadius()));
        }
        lore.add(line("#INFO"));
        return textItem(Material.BOOK, "&bClaim Info", lore);
    }

    private ItemStack iconTrusted(boolean enabled) {
        return textItem(Material.PLAYER_HEAD, enabled ? "&aTrusted Players" : "&7Trusted Players",
                List.of(gray(enabled ? "&7Manage trusted players & roles." : "&7No claim here."),
                        line("#TRUSTED")));
    }

    private ItemStack iconRoles(boolean enabled) {
        return textItem(Material.NAME_TAG, enabled ? "&eAssign Roles" : "&7Assign Roles",
                List.of(gray(enabled ? "&7Choose a player in Trusted, then pick a role." : "&7No claim here."),
                        line("#ROLES")));
    }

    private ItemStack iconFlags(boolean enabled) {
        return textItem(Material.LEVER, enabled ? "&6Flags" : "&7Flags",
                List.of(gray(enabled ? "&7Toggle claim flags (explosions, fire, pvp…)" : "&7No claim here."),
                        line("#FLAGS")));
    }

    private ItemStack iconAdminTools() {
        return textItem(Material.COMPASS, "&dAdmin Tools", List.of(
                gray("&7TP to nearest claim / review requests / world controls"),
                line("#ADMIN")));
    }

    private ItemStack iconOwner(UUID owner) {
        return textItem(Material.PLAYER_HEAD, "&bOwner", List.of(
                gray("&7" + nameOrShort(owner)),
                line("#NOOP")));
    }

    private ItemStack iconClaimSummary(Plot plot) {
        return textItem(Material.MAP, "&eClaim Summary", List.of(
                gray("&7World: &f" + plot.getWorld()),
                gray("&7Center: &f" + plot.getX() + "," + plot.getZ()),
                gray("&7Radius: &f" + plot.getRadius()),
                line("#NOOP")
        ));
    }

    private ItemStack iconExpansionRequest() {
        return textItem(Material.EMERALD, "&aRequest Expansion", List.of(
                gray("&7Submit +1 expansion request."),
                line("#EXPAND:1")
        ));
    }

    private ItemStack iconTrustedEntry(Plot plot, UUID target) {
        ClaimRole r = roles.getRole(target, plot);
        return textItem(Material.PLAYER_HEAD, "&a" + nameOrShort(target), List.of(
                gray("&7Role: &f" + r.getDisplayName()),
                gray("&8Left: set role  •  Right: remove"),
                line("#TRUST:ROLE:" + target),
                line("#TRUST:REMOVE:" + target) // second marker used by right-click in future; GUIListener routes by title not click-type
        ));
    }

    private static class FlagSpec {
        final String key, name;
        final Material icon;
        FlagSpec(String key, String name, Material icon) { this.key = key; this.name = name; this.icon = icon; }
    }

    private List<FlagSpec> defaultFlags() {
        return List.of(
                new FlagSpec("explosions", "Explosions", Material.TNT),
                new FlagSpec("fire_spread", "Fire Spread", Material.FLINT_AND_STEEL),
                new FlagSpec("pvp", "Player PvP", Material.IRON_SWORD),
                new FlagSpec("mob_grief", "Mob Griefing", Material.CREEPER_HEAD),
                new FlagSpec("leaf_decay", "Leaf Decay", Material.OAK_LEAVES),
                new FlagSpec("trample", "Crop Trample", Material.WHEAT),
                new FlagSpec("containers", "Container Protection", Material.CHEST),
                new FlagSpec("redstone", "Redstone Use", Material.REDSTONE_TORCH),
                new FlagSpec("entry", "Entry Allowed", Material.OAK_DOOR),
                new FlagSpec("exit", "Exit Allowed", Material.DARK_OAK_DOOR)
        );
    }

    private ItemStack iconFlag(Plot plot, FlagSpec f) {
        boolean on = plot.getFlag(f.key);
        return textItem(f.icon, (on ? "&a" : "&c") + f.name, List.of(
                gray("&7State: &f" + (on ? "ON" : "OFF")),
                line("#FLAG:" + f.key)
        ));
    }

    private ItemStack iconRoleOption(ClaimRole role, Plot plot, UUID target) {
        List<String> lore = new ArrayList<>();
        lore.add(gray("&7Interact: &f" + (role.canInteract() ? "Yes" : "No")));
        lore.add(gray("&7Build: &f" + (role.canBuild() ? "Yes" : "No")));
        lore.add(gray("&7Open Containers: &f" + (role.canOpenContainers() ? "Yes" : "No")));
        lore.add(gray("&7Modify Flags: &f" + (role.canModifyFlags() ? "Yes" : "No")));
        lore.add(gray("&7Manage Roles: &f" + (role.canManageRoles() ? "Yes" : "No")));
        lore.add(gray("&7Transfer Claim: &f" + (role.canTransferClaim() ? "Yes" : "No")));
        lore.add("");
        lore.add(line("#ROLE:" + role.name()));
        return textItem(Material.NAME_TAG, "&e" + role.getDisplayName(), lore);
    }

    private ItemStack iconRequest(ExpansionRequest r) {
        String who = nameOrShort(r.getRequester());
        String st  = r.getStatus().name();
        String when = (r.getCreatedAt() != null ? TS.format(r.getCreatedAt()) : "-");
        return textItem(Material.PAPER, "&bExpansion Request", List.of(
                gray("&7Player: &f" + who),
                gray("&7Amount: &f+" + r.getAmount()),
                gray("&7Created: &f" + when),
                gray("&7Status: &f" + st),
                "",
                line("#REQ:" + r.getId() + ":APPROVE"),
                line("#REQ:" + r.getId() + ":DENY")
        ));
    }

    private ItemStack iconWorld(String world) {
        return textItem(Material.GRASS_BLOCK, "&a" + world, List.of(
                gray("&7View & toggle settings"),
                line("#WORLD:OPEN:" + world)
        ));
    }

    private ItemStack iconWorldToggle(String world, String key, boolean on, Material mat, String name, String desc) {
        return textItem(mat, (on ? "&a" : "&c") + name, List.of(
                gray("&7" + desc),
                gray("&7State: &f" + (on ? "ON" : "OFF")),
                line("#WORLD:TOGGLE:" + world + ":" + key)
        ));
    }

    private ItemStack backButton() {
        return textItem(Material.BARRIER, "&7Back", List.of(line("#BACK")));
    }

    private ItemStack exitButton() {
        return textItem(Material.OAK_DOOR, "&7Exit", List.of(line("#EXIT")));
    }

    // ------------------------------ UI Helpers ------------------------------

    private static void border(Inventory inv) {
        int size = inv.getSize();
        for (int i = 0; i < size; i++) {
            int r = i / 9, c = i % 9;
            if (r == 0 || r == (size / 9) - 1 || c == 0 || c == 8) {
                if (inv.getItem(i) == null) inv.setItem(i, FILLER.clone());
            }
        }
    }

    private void placePager(Inventory inv, int page, int maxPage) {
        inv.setItem(inv.getSize() - 8, textItem(Material.ARROW, "&7« Prev", List.of(line("#PAGE:" + Math.max(0, page - 1)))));
        inv.setItem(inv.getSize() - 2, textItem(Material.ARROW, "&7Next »", List.of(line("#PAGE:" + Math.min(maxPage, page + 1)))));
    }

    private static int grid(int i) {
        int row = i / 7;
        int col = (i % 7) + 1;
        return 9 + row * 9 + col;
    }

    private static ItemStack textItem(Material m, String name, List<String> lore) {
        ItemStack it = new ItemStack(m);
        ItemMeta im = it.getItemMeta();
        if (im != null) {
            im.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
            if (lore != null) {
                List<String> colored = new ArrayList<>(lore.size());
                for (String s : lore) colored.add(ChatColor.translateAlternateColorCodes('&', s));
                im.setLore(colored);
            }
            im.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_POTION_EFFECTS);
            it.setItemMeta(im);
        }
        return it;
    }

    private static ItemStack quick(Material m, String name) { return textItem(m, name, null); }

    private static String line(String id) { return ChatColor.DARK_GRAY + id; }

    private static boolean valid(ItemStack it) { return it != null && it.getType() != Material.AIR; }

    private static String extractId(ItemStack it) {
        ItemMeta im = it.getItemMeta();
        if (im == null || im.getLore() == null) return null;
        for (String l : im.getLore()) {
            String s = ChatColor.stripColor(l);
            if (s != null && s.startsWith("#")) return s.substring(1);
        }
        return null;
    }

    private static String gray(String s) { return ChatColor.translateAlternateColorCodes('&', s); }

    private void msg(Player p, String coloredMsg) { p.sendMessage(ChatColor.GRAY + "[ProShield] " + ChatColor.translateAlternateColorCodes('&', coloredMsg)); }
    private void warn(Player p, String coloredMsg) { p.sendMessage(ChatColor.GRAY + "[ProShield] " + ChatColor.translateAlternateColorCodes('&', coloredMsg)); }
    private void click(Player p) { p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.6f, 1.25f); }
    private void soundGood(Player p) { p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.9f, 1.2f); }

    private String nameOrShort(UUID u) {
        OfflinePlayer op = Bukkit.getOfflinePlayer(u);
        String n = (op != null ? op.getName() : null);
        return (n == null || n.isBlank()) ? u.toString().substring(0, 8) : n;
    }

    private int safeInt(String s, int def) { try { return Integer.parseInt(s); } catch (Exception e) { return def; } }
    private UUID uuidSafe(String s) { try { return UUID.fromString(s); } catch (Exception e) { return null; } }
    private String shortId(UUID id) { String s = id.toString(); return s.substring(0, 8); }
    // ------------------------------ View Stack ------------------------------

    private static class View {
        enum Kind { MAIN, CLAIM, TRUSTED, ASSIGN_ROLE, FLAGS, ADMIN, WORLDS, WORLD_DETAIL, PENDING, HISTORY }
        final Kind kind;
        final int page;
        final UUID pendingTarget; // for role assignment
        final String worldDetail; // world name for WORLD_DETAIL

        private View(Kind k, int page, UUID target, String world) {
            this.kind = k; this.page = page; this.pendingTarget = target; this.worldDetail = world;
        }

        static View main()             { return new View(Kind.MAIN, 0, null, null); }
        static View claimInfo()        { return new View(Kind.CLAIM, 0, null, null); }
        static View trusted(int page)  { return new View(Kind.TRUSTED, page, null, null); }
        static View assignRole(UUID t) { return new View(Kind.ASSIGN_ROLE, 0, t, null); }
        static View flags(int page)    { return new View(Kind.FLAGS, page, null, null); }
        static View admin()            { return new View(Kind.ADMIN, 0, null, null); }
        static View worlds(int page)   { return new View(Kind.WORLDS, page, null, null); }
        static View worldDetail(String w) { return new View(Kind.WORLD_DETAIL, 0, null, w); }
        static View pending(int page)  { return new View(Kind.PENDING, page, null, null); }
        static View history()          { return new View(Kind.HISTORY, 0, null, null); }
    }

    private void push(Player p, View v) {
        nav.computeIfAbsent(p.getUniqueId(), k -> new ArrayDeque<>()).push(v);
    }

    private View peek(Player p) {
        Deque<View> d = nav.get(p.getUniqueId());
        return (d == null || d.isEmpty()) ? null : d.peek();
    }

    private void setPendingTarget(Player p, UUID u) {
        View top = peek(p);
        if (top == null) return;
        // replace top with same view + pending target (used to flow from Trusted to Assign Role)
        pop(p);
        nav.get(p.getUniqueId()).push(new View(top.kind, top.page, u, top.worldDetail));
    }

    private int pageFrom(View v) { return (v == null ? 0 : v.page); }

    private void pop(Player p) {
        Deque<View> d = nav.get(p.getUniqueId());
        if (d != null && !d.isEmpty()) d.pop();
    }

    private void back(Player p) {
        Deque<View> d = nav.get(p.getUniqueId());
        if (d == null || d.isEmpty()) { p.closeInventory(); return; }
        // pop current
        d.pop();
        if (d.isEmpty()) { p.closeInventory(); return; }
        View prev = d.peek();
        switch (prev.kind) {
            case MAIN -> openMainMenu(p);
            case CLAIM -> openClaimInfo(p);
            case TRUSTED -> openTrusted(p, prev.page);
            case ASSIGN_ROLE -> openAssignRole(p);
            case FLAGS -> openFlags(p, prev.page);
            case ADMIN -> openAdmin(p);
            case WORLDS -> openWorldControls(p, prev.page);
            case WORLD_DETAIL -> openWorldDetail(p, prev.worldDetail);
            case PENDING -> openPending(p, prev.page);
            case HISTORY -> openHistory(p);
        }
    }

    // --------------------------- World Controls I/O ---------------------------

    private boolean defaultWorld(String key) {
        if (WC_PVP.equalsIgnoreCase(key)) return true;        // PvP ON
        if (WC_SAFEZONE.equalsIgnoreCase(key)) return false;  // SafeZone OFF
        return false;
    }

    private String worldPath(String world, String key) { return "worlds." + world + "." + key; }

    private boolean readWorldBool(String world, String key, boolean def) {
        try {
            if (!plugin.getConfig().isSet(worldPath(world, key))) return def;
            return plugin.getConfig().getBoolean(worldPath(world, key), def);
        } catch (Exception e) {
            return def;
        }
    }

    private void writeWorldBool(String world, String key, boolean value) {
        try {
            plugin.getConfig().set(worldPath(world, key), value);
            plugin.saveConfig();
        } catch (Exception ignored) {}
    }

    private Map<String, Boolean> readAllWorldBools(String world) {
        Map<String, Boolean> map = new LinkedHashMap<>();
        try {
            if (plugin.getConfig().isConfigurationSection("worlds." + world)) {
                for (String k : plugin.getConfig().getConfigurationSection("worlds." + world).getKeys(false)) {
                    map.put(k, plugin.getConfig().getBoolean("worlds." + world + "." + k,
                            defaultWorld(k)));
                }
            } else {
                // If absent, seed with defaults
                map.put(WC_PVP, defaultWorld(WC_PVP));
                map.put(WC_SAFEZONE, defaultWorld(WC_SAFEZONE));
            }
        } catch (Exception e) {
            map.put(WC_PVP, defaultWorld(WC_PVP));
            map.put(WC_SAFEZONE, defaultWorld(WC_SAFEZONE));
        }
        return map;
    }

    // --------------------------- Nearest Claim Helper ---------------------------

    private Plot nearestClaimTo(Location loc, int maxRange) {
        if (loc == null) return null;
        double best = Double.MAX_VALUE;
        Plot bestPlot = null;
        for (Plot p : plots.getPlots()) {
            if (!p.getWorld().equalsIgnoreCase(loc.getWorld().getName())) continue;
            Location c = centerOf(p, loc.getWorld());
            double d = c.distance(loc);
            if (d < best && d <= maxRange) { best = d; bestPlot = p; }
        }
        return bestPlot;
    }

    private Location centerOf(Plot p, World world) {
        if (p == null || world == null) return null;
        int x = p.getX();
        int z = p.getZ();
        int y = Math.max(world.getHighestBlockYAt(x, z), 64);
        return new Location(world, x + 0.5, y + 1.0, z + 0.5);
    }
}
// ================================= EOF =========================================
