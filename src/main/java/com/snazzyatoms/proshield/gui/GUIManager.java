// ========================== GUIManager.java (v1.2.6 FINAL, all features intact) ==========================
// ProShield GUI Manager — fully consolidated and feature-complete
// Covers everything from v1.2.4 → v1.2.6 with no losses.
// Includes:
// - Main Menu, Claim Info, Trusted (pagination), Roles assign
// - Claim Flags, Expansion Requests (submit/review/history)
// - Admin Tools (World Controls, Pending, Teleport Nearest)
// - Back/Exit navigation stack
// - deny(Player p) helper for permissions
// - Fully GUI-driven (no text-only commands required except admin fallbacks)
// ====================================================================================

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

public class GUIManager {

    // ------------------------------ Constants ------------------------------

    private static final int SIZE_27 = 27;
    private static final int SIZE_36 = 36;
    private static final int SIZE_45 = 45;
    private static final int SIZE_54 = 54;

    private static final ItemStack FILLER = quick(Material.GRAY_STAINED_GLASS_PANE, " ");
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            .withZone(ZoneId.systemDefault());

    private static final String WC_PVP      = "pvp";
    private static final String WC_SAFEZONE = "safezone";

    // ----------------------------- Dependencies ----------------------------

    private final ProShield plugin;
    private final MessagesUtil messages;
    private final PlotManager plots;
    private final ClaimRoleManager roles;
    private final ExpansionRequestManager expansions;

    // ------------------------------ State ----------------------------------

    private final Map<UUID, Deque<View>> nav = new HashMap<>();

    // For deny-reason flows
    private final Map<UUID, UUID> denyTargets = new HashMap<>();

    // ---------------------------- Construction -----------------------------

    public GUIManager(ProShield plugin) {
        this.plugin   = plugin;
        this.messages = plugin.getMessagesUtil();
        this.plots    = plugin.getPlotManager();
        this.roles    = plugin.getRoleManager();
        this.expansions = plugin.getExpansionRequestManager();
    }

    public void openMain(Player player) { openMainMenu(player); }

    // ---------------------------- Openers (UI) -----------------------------

    private String title(String key, String fallback) {
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
            inv.setItem(13, textItem(Material.BOOK, "&7No claim here.", List.of(line("#NOOP"))));
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
        Plot plot = plots.getPlotAt(p.getLocation());
        if (plot == null) { deny(p); return; }

        View peek = peek(p);
        if (peek == null || peek.pendingTarget == null) { deny(p); return; }

        Inventory inv = Bukkit.createInventory(p, SIZE_45, title("assign-role", "&8Assign Role"));
        border(inv);

        int i = 10;
        for (ClaimRole r : ClaimRole.values()) {
            if (r == ClaimRole.NONE) continue;
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
    // --------------------------- Admin & World Controls ---------------------------

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

        boolean pvp      = readWorldBool(worldName, WC_PVP, true);       // default ON
        boolean safezone = readWorldBool(worldName, WC_SAFEZONE, false); // default OFF

        inv.setItem(10, iconWorldToggle(worldName, WC_PVP, pvp,
                Material.IRON_SWORD, "&ePvP", "&7Global player-versus-player damage"));
        inv.setItem(12, iconWorldToggle(worldName, WC_SAFEZONE, safezone,
                Material.TOTEM_OF_UNDYING, "&dSafe Zone", "&7Disable combat & damage in world"));

        Map<String, Boolean> extras = readAllWorldBools(worldName);
        int slot = 20;
        for (Map.Entry<String, Boolean> e : extras.entrySet()) {
            String key = e.getKey();
            if (key.equalsIgnoreCase(WC_PVP) || key.equalsIgnoreCase(WC_SAFEZONE)) continue;
            boolean val = e.getValue();
            inv.setItem(slot, iconWorldToggle(worldName, key, val, Material.LEVER,
                    "&6" + key, "&7Toggle " + key));
            slot += (slot % 9 == 7) ? 3 : 1;
            if (slot >= 33) break;
        }

        inv.setItem(40, backButton());
        push(p, View.worldDetail(worldName));
        p.openInventory(inv);
        click(p);
    }

    // --------------------------- Expansion Requests ---------------------------

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

    // ----------------------------- Central Click Router -----------------------------

    /** Routes clicks by inventory title keywords into specific handlers. */
    public void handleClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        String title = e.getView().getTitle();
        if (title == null) return;

        String low = ChatColor.stripColor(title).toLowerCase(Locale.ROOT);

        // Always cancel default behavior inside our GUIs
        e.setCancelled(true);

        try {
            if (low.contains("proshield menu") || low.contains("main")) {
                handleMainClick(p, e);
            } else if (low.contains("claim info")) {
                handleMainClaimInfoClickOrPlayerRequest(p, e);
            } else if (low.contains("trusted")) {
                handleTrustedClick(p, e);
            } else if (low.contains("assign role")) {
                handleAssignRoleClick(p, e);
            } else if (low.contains("claim flags") || low.contains("flags")) {
                handleFlagsClick(p, e);
            } else if (low.contains("admin tools") || low.equals("admin")) {
                handleAdminClick(p, e);
            } else if (low.contains("world controls")) {
                handleWorldControlsClick(p, e);
            } else if (low.contains("expansion requests")) {
                handleExpansionReviewClick(p, e);
            } else if (low.contains("expansion history")) {
                handleHistoryClick(p, e);
            } else if (low.contains("deny reason")) {
                handleDenyReasonClick(p, e);
            }
        } catch (Throwable ex) {
            plugin.getLogger().warning("[GUIManager] Click routing error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // Claim Info is mostly read-only except the player expansion request button.
    private void handleMainClaimInfoClickOrPlayerRequest(Player p, InventoryClickEvent e) {
        ItemStack it = e.getCurrentItem();
        if (!valid(it)) return;
        String id = extractId(it);
        if (id == null) return;
        if (id.startsWith("EXPAND:")) {
            handlePlayerExpansionRequestClick(p, e);
        } else if (id.equals("BACK")) {
            back(p);
        } else if (id.equals("EXIT")) {
            p.closeInventory();
        }
    }

    // ----------------------------- Click Handlers -----------------------------

    public void handleMainClick(Player p, InventoryClickEvent e) {
        ItemStack it = e.getCurrentItem();
        if (!valid(it)) return;
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
            case "ROLES" -> msg(p, "&7Open &fTrusted&7, click a player, then choose a role.");
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
            back(p);
        } else if (id.equals("BACK")) {
            back(p);
        } else if (id.equals("EXIT")) {
            p.closeInventory();
        }
    }

    public void handleTrustedClick(Player p, InventoryClickEvent e) {
        ItemStack it = e.getCurrentItem();
        if (!valid(it)) return;
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
            openTrusted(p, pageFrom(peek(p)));
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
            openFlags(p, pageFrom(peek(p)));
        }
    }

    public void handleAdminClick(Player p, InventoryClickEvent e) {
        ItemStack it = e.getCurrentItem();
        if (!valid(it)) return;
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
            String[] parts = id.split(":", 4);
            if (parts.length < 4) return;
            String world = parts[2];
            String key   = parts[3];
            boolean cur  = readWorldBool(world, key, defaultWorld(key));
            writeWorldBool(world, key, !cur);
            msg(p, "&b" + world + "." + key + " &7→ " + (!cur ? "&aON" : "&cOFF"));
            openWorldDetail(p, world);
        }
    }

    public void handlePlayerExpansionRequestClick(Player p, InventoryClickEvent e) {
        ItemStack it = e.getCurrentItem();
        if (!valid(it)) return;
        String id = extractId(it);
        if (id == null) return;

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
        String id = extractId(it);
        if (id == null) return;

        if (!p.hasPermission("proshield.admin")) { deny(p); return; }

        if (id.startsWith("PAGE:")) {
            openPending(p, safeInt(id.substring(5), 0));
            return;
        }
        if (id.equals("BACK")) { back(p); return; }
        if (id.equals("EXIT")) { p.closeInventory(); return; }

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
        String id = extractId(it);
        if (id == null) return;

        if (id.equals("BACK")) { back(p); return; }
        if (id.equals("EXIT")) { p.closeInventory(); return; }
        // read-only
    }

    public void handleDenyReasonClick(Player p, InventoryClickEvent e) {
        ItemStack it = e.getCurrentItem();
        if (!valid(it)) return;
        String id = extractId(it);
        if (id == null) return;

        if (id.equals("BACK")) { back(p); return; }
        if (id.equals("EXIT")) { p.closeInventory(); return; }
    }
    // ------------------------------ Icon Builders ------------------------------

    private ItemStack iconClaimButton(boolean canClaim) {
        return textItem(Material.GRASS_BLOCK,
                canClaim ? "&aClaim Land" : "&cAlready Claimed",
                List.of(line(canClaim ? "#CLAIM" : "#NOOP")));
    }

    private ItemStack iconUnclaimButton(boolean canUnclaim) {
        return textItem(Material.BARRIER,
                canUnclaim ? "&cUnclaim Land" : "&7No Claim Here",
                List.of(line(canUnclaim ? "#UNCLAIM" : "#NOOP")));
    }

    private ItemStack iconClaimInfo(Plot plot) {
        return textItem(Material.BOOK,
                "&eClaim Info",
                List.of(line("#INFO"),
                        gray("&7Owner: &f" + (plot != null ? nameOrShort(plot.getOwner()) : "N/A"))));
    }

    private ItemStack iconTrusted(boolean enabled) {
        return textItem(Material.PLAYER_HEAD,
                enabled ? "&bTrusted Players" : "&7No Claim",
                List.of(line(enabled ? "#TRUSTED" : "#NOOP")));
    }

    private ItemStack iconRoles(boolean enabled) {
        return textItem(Material.NAME_TAG,
                enabled ? "&dRoles" : "&7No Claim",
                List.of(line(enabled ? "#ROLES" : "#NOOP")));
    }

    private ItemStack iconFlags(boolean enabled) {
        return textItem(Material.LEVER,
                enabled ? "&6Claim Flags" : "&7No Claim",
                List.of(line(enabled ? "#FLAGS" : "#NOOP")));
    }

    private ItemStack iconAdminTools() {
        return textItem(Material.COMPASS,
                "&cAdmin Tools",
                List.of(line("#ADMIN")));
    }

    private ItemStack iconOwner(UUID owner) {
        return textItem(Material.PLAYER_HEAD,
                "&fOwner: &e" + nameOrShort(owner),
                List.of(line("#NOOP")));
    }

    private ItemStack iconClaimSummary(Plot plot) {
        return textItem(Material.MAP,
                "&fClaim ID: &a" + plot.getId(),
                List.of(gray("&7Created: &f" + TS.format(plot.getCreatedAt())),
                        gray("&7Trusted: &f" + plot.getTrusted().size()),
                        line("#NOOP")));
    }

    private ItemStack iconExpansionRequest() {
        return textItem(Material.EMERALD,
                "&aRequest Expansion",
                List.of(gray("&7Submit a claim expansion request"),
                        line("#EXPAND:1")));
    }

    private ItemStack iconTrustedEntry(Plot plot, UUID target) {
        ClaimRole role = roles.getRole(plot, target);
        return textItem(Material.PLAYER_HEAD,
                "&f" + nameOrShort(target) + " &7[" + role.getDisplayName() + "]",
                List.of(line("TRUST:ROLE:" + target),
                        line("TRUST:REMOVE:" + target)));
    }

    private ItemStack iconRoleOption(ClaimRole r, Plot plot, UUID target) {
        boolean has = roles.getRole(plot, target) == r;
        return textItem(Material.PAPER,
                (has ? "&a" : "&7") + r.getDisplayName(),
                List.of(line("ROLE:" + r.name())));
    }

    private static class FlagSpec {
        final String key;
        final String label;
        final Material icon;

        FlagSpec(String key, String label, Material icon) {
            this.key = key; this.label = label; this.icon = icon;
        }
    }

    private List<FlagSpec> defaultFlags() {
        return List.of(
                new FlagSpec("tnt", "&cTNT Explosions", Material.TNT),
                new FlagSpec("fire", "&6Fire Spread", Material.FLINT_AND_STEEL),
                new FlagSpec("pvp", "&ePvP", Material.IRON_SWORD),
                new FlagSpec("mobgrief", "&aMob Griefing", Material.CREEPER_HEAD)
        );
    }

    private ItemStack iconFlag(Plot plot, FlagSpec f) {
        boolean enabled = plot.getFlag(f.key);
        return textItem(f.icon,
                f.label + " " + (enabled ? "&aON" : "&cOFF"),
                List.of(line("FLAG:" + f.key)));
    }

    private ItemStack iconWorld(String world) {
        return textItem(Material.GRASS_BLOCK,
                "&fWorld: &e" + world,
                List.of(line("WORLD:OPEN:" + world)));
    }

    private ItemStack iconWorldToggle(String world, String key, boolean enabled, Material icon, String label, String desc) {
        return textItem(icon,
                label + " " + (enabled ? "&aON" : "&cOFF"),
                List.of(gray(desc),
                        line("WORLD:TOGGLE:" + world + ":" + key)));
    }

    private ItemStack iconRequest(ExpansionRequest r) {
        String status = r.getStatus().name();
        return textItem(Material.PAPER,
                "&fReq " + shortId(r.getId()) + " &7[" + status + "]",
                List.of(gray("&7Player: &f" + nameOrShort(r.getRequester())),
                        gray("&7Created: &f" + TS.format(r.getCreatedAt())),
                        line("REQ:" + r.getId() + ":APPROVE"),
                        line("REQ:" + r.getId() + ":DENY")));
    }

    private ItemStack backButton() {
        return textItem(Material.ARROW, "&7Back", List.of(line("#BACK")));
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

    // ✅ FIX: deny(Player) helper
    private void deny(Player p) {
        warn(p, "&cYou don’t have permission for this action.");
        click(p);
    }

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
        final String type;
        final int page;
        final String world;
        final UUID pendingTarget;

        private View(String type, int page, String world, UUID target) {
            this.type = type; this.page = page; this.world = world; this.pendingTarget = target;
        }

        static View main()               { return new View("MAIN", 0, null, null); }
        static View claimInfo()          { return new View("CLAIMINFO", 0, null, null); }
        static View trusted(int page)    { return new View("TRUSTED", page, null, null); }
        static View assignRole(UUID t)   { return new View("ASSIGNROLE", 0, null, t); }
        static View flags(int page)      { return new View("FLAGS", page, null, null); }
        static View admin()              { return new View("ADMIN", 0, null, null); }
        static View worlds(int page)     { return new View("WORLDS", page, null, null); }
        static View worldDetail(String w){ return new View("WORLDDETAIL", 0, w, null); }
        static View pending(int page)    { return new View("PENDING", page, null, null); }
        static View history()            { return new View("HISTORY", 0, null, null); }
    }

    private void push(Player p, View v) { nav.computeIfAbsent(p.getUniqueId(), k -> new ArrayDeque<>()).push(v); }
    private View peek(Player p) { Deque<View> st = nav.get(p.getUniqueId()); return (st == null || st.isEmpty()) ? null : st.peek(); }
    private void back(Player p) {
        Deque<View> st = nav.get(p.getUniqueId());
        if (st == null || st.size() <= 1) { p.closeInventory(); return; }
        st.pop(); View prev = st.peek();
        if (prev == null) { p.closeInventory(); return; }
        switch (prev.type) {
            case "MAIN"       -> openMainMenu(p);
            case "CLAIMINFO"  -> openClaimInfo(p);
            case "TRUSTED"    -> openTrusted(p, prev.page);
            case "ASSIGNROLE" -> openAssignRole(p);
            case "FLAGS"      -> openFlags(p, prev.page);
            case "ADMIN"      -> openAdmin(p);
            case "WORLDS"     -> openWorldControls(p, prev.page);
            case "WORLDDETAIL"-> openWorldDetail(p, prev.world);
            case "PENDING"    -> openPending(p, prev.page);
            case "HISTORY"    -> openHistory(p);
            default           -> p.closeInventory();
        }
    }

    private int pageFrom(View v) { return (v != null ? v.page : 0); }
    private void setPendingTarget(Player p, UUID t) { View v = peek(p); if (v != null) nav.get(p.getUniqueId()).push(View.assignRole(t)); }

    // --------------------------- World Controls I/O ---------------------------

    private boolean readWorldBool(String world, String key, boolean def) {
        return plugin.getConfig().getBoolean("worlds." + world + "." + key, def);
    }

    private void writeWorldBool(String world, String key, boolean val) {
        plugin.getConfig().set("worlds." + world + "." + key, val);
        plugin.saveConfig();
    }

    private Map<String, Boolean> readAllWorldBools(String world) {
        Map<String, Boolean> map = new HashMap<>();
        if (plugin.getConfig().isConfigurationSection("worlds." + world)) {
            for (String k : plugin.getConfig().getConfigurationSection("worlds." + world).getKeys(false)) {
                map.put(k, plugin.getConfig().getBoolean("worlds." + world + "." + k));
            }
        }
        return map;
    }

    private boolean defaultWorld(String key) {
        return switch (key.toLowerCase(Locale.ROOT)) {
            case "pvp" -> true;
            case "safezone" -> false;
            default -> false;
        };
    }
    // --------------------------- Nearest Claim Helper ---------------------------

    private Plot nearestClaimTo(Location loc, int radius) {
        Plot nearest = null;
        double bestDist = Double.MAX_VALUE;

        for (Plot p : plots.getAllPlots()) {
            Location c = centerOf(p, loc.getWorld());
            if (c == null) continue;
            double dist = c.distance(loc);
            if (dist < bestDist && dist <= radius) {
                bestDist = dist;
                nearest = p;
            }
        }
        return nearest;
    }

    private Location centerOf(Plot plot, World world) {
        if (plot == null || world == null) return null;
        int cx = plot.getX() << 4; // chunk to block
        int cz = plot.getZ() << 4;
        int midX = cx + 8;
        int midZ = cz + 8;
        int y = world.getHighestBlockYAt(midX, midZ);
        return new Location(world, midX + 0.5, y + 1, midZ + 0.5);
    }
}
// ================================= EOF =========================================
