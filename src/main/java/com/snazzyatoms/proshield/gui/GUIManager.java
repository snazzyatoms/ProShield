// ========================== GUIManager.java (v1.2.6 POLISHED, feature-complete) ==========================
// ProShield GUI Manager — consolidated, upgraded & complete (v1.2.4 → v1.2.6)
// - Main Menu, Claim Info, Trusted (pagination removed), Assign Role (lore from ClaimRole)
// - Claim Flags, Expansion Requests (submit/review/history)
// - Admin Tools (World Controls, Pending, Teleport Nearest)
// - Back/Exit navigation stack & title-based routing (works with GUIListener)
// - Permissions helper deny(Player) + externalized error messages
// - Uses Plot/PlotManager/ClaimRoleManager signatures (synchronized with your fixes)
// ======================================================================================================

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

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

/** Main entry for all ProShield GUIs. */
public class GUIManager {

    // ------------------------------ Constants ------------------------------

    private static final int SIZE_27 = 27;
    private static final int SIZE_36 = 36;
    private static final int SIZE_45 = 45;
    private static final int SIZE_54 = 54;

    private static final ItemStack FILLER = quick(Material.GRAY_STAINED_GLASS_PANE, " ");
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            .withZone(ZoneId.systemDefault());

    // Admin ► World Controls default keys
    private static final String WC_PVP      = "pvp";
    private static final String WC_SAFEZONE = "safezone";

    // ----------------------------- Dependencies ----------------------------

    private final ProShield plugin;
    private final MessagesUtil messages;
    private final PlotManager plots;
    private final ClaimRoleManager roles;
    private final ExpansionRequestManager expansions;

    // ------------------------------ State ----------------------------------

    private final Map<UUID, Deque<View>> nav = new HashMap<>(); // per-player "view stack"

    // ---------------------------- Construction -----------------------------

    public GUIManager(ProShield plugin) {
        this.plugin     = plugin;
        this.messages   = plugin.getMessagesUtil();
        this.plots      = plugin.getPlotManager();
        this.roles      = plugin.getRoleManager();
        this.expansions = plugin.getExpansionRequestManager();
    }

    // Compatibility alias for older callers
    public void openMain(Player player) { openMainMenu(player); }

    // ---------------------------- Title helper -----------------------------

    private String title(String key, String fallback) {
        String raw = messages.getOrDefault("messages.gui.titles." + key, fallback);
        return ChatColor.translateAlternateColorCodes('&', raw);
    }

    // ---------------------------- Openers (UI) -----------------------------

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

        // Exit (single)
        inv.setItem(26 - 1, exitButton()); // slot 25

        push(p, View.main());
        p.openInventory(inv);
        click(p);
    }

    public void openClaimInfo(Player p) {
        Plot plot = plots.getPlotAt(p.getLocation());
        Inventory inv = Bukkit.createInventory(p, SIZE_36, title("claim-info", "&8Claim Info"));
        border(inv);

        if (plot == null) {
            inv.setItem(13, textItem(Material.BOOK,
                    messages.getOrDefault("messages.gui.no-claim", "&7No claim here."),
                    List.of(line("#NOOP"))));
        } else {
            inv.setItem(11, iconOwner(plot.getOwner()));
            inv.setItem(13, iconClaimSummary(plot));
            inv.setItem(15, iconExpansionRequest());
        }

        // Back & Exit (single)
        inv.setItem(31, backButton());
        inv.setItem(32, exitButton());

        push(p, View.claimInfo());
        p.openInventory(inv);
        click(p);
    }

    public void openTrusted(Player p, int pageIgnored) {
        Plot plot = plots.getPlotAt(p.getLocation());
        Inventory inv = Bukkit.createInventory(p, SIZE_54, title("trusted", "&8Trusted Players"));
        border(inv);

        if (plot == null) {
            inv.setItem(22, textItem(Material.PLAYER_HEAD,
                    messages.getOrDefault("messages.gui.no-claim", "&7No claim here."),
                    List.of(line("#NOOP"))));
        } else {
            List<UUID> list = new ArrayList<>(plot.getTrusted().keySet());
            list.sort(Comparator.comparing(this::nameOrShort));

            // show up to 28 entries (4 rows × 7)
            int max = Math.min(28, list.size());
            for (int i = 0; i < max; i++) {
                UUID u = list.get(i);
                inv.setItem(grid(i), iconTrustedEntry(plot, u));
            }
        }

        inv.setItem(49, backButton());
        inv.setItem(50, exitButton());
        push(p, View.trusted(0));
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

            List<String> lore = new ArrayList<>(r.getLore());
            lore.add(line("ROLE:" + r.name())); // hidden click action

            inv.setItem(i, textItem(Material.PAPER,
                    (roles.getRole(plot, peek.pendingTarget) == r ? "&a" : "&7") + r.getDisplayName(),
                    lore));

            i += (i % 9 == 7) ? 3 : 1;
        }

        inv.setItem(40, backButton());
        inv.setItem(41, exitButton());
        push(p, View.assignRole(peek.pendingTarget));
        p.openInventory(inv);
        click(p);
    }
    public void openFlags(Player p, int pageIgnored) {
        Plot plot = plots.getPlotAt(p.getLocation());
        Inventory inv = Bukkit.createInventory(p, SIZE_54, title("flags", "&8Claim Flags"));
        border(inv);

        if (plot == null) {
            inv.setItem(22, textItem(Material.LEVER,
                    messages.getOrDefault("messages.gui.no-claim", "&7No claim here."),
                    List.of(line("#NOOP"))));
        } else {
            List<FlagSpec> spec = defaultFlags();
            int max = Math.min(21, spec.size());
            for (int i = 0; i < max; i++) {
                FlagSpec f = spec.get(i);
                inv.setItem(grid(i), iconFlag(plot, f));
            }
        }

        inv.setItem(49, backButton());
        inv.setItem(50, exitButton());
        push(p, View.flags(0));
        p.openInventory(inv);
        click(p);
    }

        // --------------------------- Admin & World Controls ---------------------------

    public void openAdmin(Player p) {
        Inventory inv = Bukkit.createInventory(p, SIZE_27, title("admin", "&8Admin Tools"));
        border(inv);

        inv.setItem(10, textItem(Material.ENDER_PEARL, "&bNearest Claim", List.of(
                gray("&7Teleport to nearest claim (≤200 blocks)."),
                line("#ADMIN:TP_NEAREST")
        )));
        inv.setItem(12, textItem(Material.PAPER, "&aPending Requests", List.of(
                gray("&7Approve or deny expansion requests."),
                line("#ADMIN:PENDING")
        )));
        inv.setItem(14, textItem(Material.REDSTONE, "&cWorld Controls", List.of(
                gray("&7Toggle per-world settings (PvP, Safe Zone, …)"),
                line("#ADMIN:WORLD_CTRL")
        )));
        inv.setItem(16, textItem(Material.REPEATER, "&eReload Config", List.of(
                gray("&7Reload ProShield configuration files."),
                line("#ADMIN:RELOAD")
        )));

        inv.setItem(22, backButton());
        inv.setItem(23, exitButton());

        push(p, View.admin());
        p.openInventory(inv);
        click(p);
    }

    public void openWorldControls(Player p, int pageIgnored) {
        Inventory inv = Bukkit.createInventory(p, SIZE_54, title("world-controls", "&8World Controls"));
        border(inv);

        List<World> worlds = Bukkit.getWorlds();
        int slot = 10;
        for (World w : worlds) {
            String name = w.getName();
            if (name.equalsIgnoreCase("world")) {
                inv.setItem(slot++, iconWorld(name)); // interactive
            } else {
                inv.setItem(slot++, textItem(Material.BARRIER,
                        "&c" + name,
                        List.of(gray("Coming in a future update"))));
            }
            if (slot >= 16) break;
        }

        inv.setItem(49, backButton());
        inv.setItem(50, exitButton());
        push(p, View.worlds(0));
        p.openInventory(inv);
        click(p);
    }

    public void openWorldDetail(Player p, String worldName) {
        Inventory inv = Bukkit.createInventory(p, SIZE_45, title("world-controls", "&8World: " + worldName));
        border(inv);

        boolean pvp      = readWorldBool(worldName, WC_PVP, true);       // default ON
        boolean safezone = readWorldBool(worldName, WC_SAFEZONE, false); // default OFF

        inv.setItem(11, iconWorldToggle(worldName, WC_PVP, pvp,
                Material.IRON_SWORD, "&ePvP", "&7Global player-versus-player damage"));
        inv.setItem(13, iconWorldToggle(worldName, WC_SAFEZONE, safezone,
                Material.TOTEM_OF_UNDYING, "&dSafe Zone", "&7Disable combat & damage in world"));

        // Placeholders for future toggles
        inv.setItem(15, textItem(Material.PAPER, "&7Explosions (soon)", List.of(gray("Coming in future update"))));
        inv.setItem(22, textItem(Material.PAPER, "&7Fire Spread (soon)", List.of(gray("Coming in future update"))));

        inv.setItem(40, backButton());
        inv.setItem(41, exitButton());
        push(p, View.worldDetail(worldName));
        p.openInventory(inv);
        click(p);
    }

    // --------------------------- Expansion Requests ---------------------------

    public void openPending(Player p, int pageIgnored) {
        Inventory inv = Bukkit.createInventory(p, SIZE_54, title("expansion-requests", "&8Expansion Requests"));
        border(inv);

        List<ExpansionRequest> pending = new ArrayList<>(expansions.getPendingRequests());
        pending.sort(Comparator.comparing(ExpansionRequest::getCreatedAt).reversed());

        int max = Math.min(21, pending.size());
        for (int i = 0; i < max; i++) {
            ExpansionRequest r = pending.get(i);
            inv.setItem(grid(i), iconRequest(r));
        }

        inv.setItem(49, backButton());
        inv.setItem(50, exitButton());
        push(p, View.pending(0));
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
        inv.setItem(50, exitButton());
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
            } else if (low.contains("world:")) {
                handleWorldControlsClick(p, e);
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

    // ----------------------------- Click Handlers -----------------------------

    public void handleMainClick(Player p, InventoryClickEvent e) {
        ItemStack it = e.getCurrentItem();
        if (!valid(it)) return;
        String id = extractId(it);
        if (id == null) return;

        switch (id) {
            case "CLAIM" -> {
                Plot here = plots.getPlotAt(p.getLocation());
                if (here != null) {
                    warn(p, messages.getOrDefault("messages.error.already-claimed", "&cThis chunk is already claimed."));
                    return;
                }
                Plot created = plots.createPlot(p.getUniqueId(), p.getLocation());
                if (created != null) {
                    msg(p, messages.getOrDefault("messages.success.claim-created", "&aClaim created."));
                    openClaimInfo(p);
                } else {
                    warn(p, messages.getOrDefault("messages.error.claim-failed", "&cUnable to create claim here."));
                }
            }
            case "UNCLAIM" -> {
                Plot here = plots.getPlotAt(p.getLocation());
                if (here == null) {
                    warn(p, messages.getOrDefault("messages.error.no-claim", "&cNo claim here."));
                    return;
                }
                if (!here.getOwner().equals(p.getUniqueId()) && !p.hasPermission("proshield.admin")) {
                    deny(p); return;
                }
                plots.deletePlot(here.getId());
                msg(p, messages.getOrDefault("messages.success.claim-removed", "&eClaim removed."));
                openMainMenu(p);
            }
            case "INFO" -> openClaimInfo(p);
            case "TRUSTED" -> openTrusted(p, 0);
            case "ROLES" -> msg(p, messages.getOrDefault("messages.info.roles-howto", "&7Open &fTrusted&7, click a player, then choose a role."));
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
        if (plot == null) { warn(p, messages.getOrDefault("messages.error.no-claim", "&cNo claim here.")); return; }

        View peek = peek(p);
        if (peek == null || peek.pendingTarget == null) { warn(p, messages.getOrDefault("messages.error.pick-player-first", "&cPick a player first.")); return; }

        if (!plot.getOwner().equals(p.getUniqueId()) && !p.hasPermission("proshield.admin")) { deny(p); return; }

        if (id.startsWith("ROLE:")) {
            String roleName = id.substring("ROLE:".length());
            ClaimRole role = ClaimRole.fromName(roleName);
            if (role == ClaimRole.NONE) { warn(p, messages.getOrDefault("messages.error.unknown-role", "&cUnknown role.")); return; }
            roles.setRole(plot, peek.pendingTarget, role);
            plots.save(plot);
            msg(p, messages.getOrDefault("messages.success.role-assigned", "&aAssigned &f%player% &ato &f%role%")
                    .replace("%player%", nameOrShort(peek.pendingTarget))
                    .replace("%role%", role.getDisplayName()));
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
        if (plot == null) { warn(p, messages.getOrDefault("messages.error.no-claim", "&cNo claim here.")); return; }

        if (id.startsWith("TRUST:REMOVE:")) {
            if (!plot.getOwner().equals(p.getUniqueId()) && !p.hasPermission("proshield.admin")) { deny(p); return; }
            UUID target = uuidSafe(id.substring("TRUST:REMOVE:".length()));
            if (target == null) return;
            plot.getTrusted().remove(target);
            plots.save(plot);
            msg(p, messages.getOrDefault("messages.success.trust-removed", "&eRemoved &f%player% &efrom trusted.")
                    .replace("%player%", nameOrShort(target)));
            openTrusted(p, 0);
        } else if (id.startsWith("TRUST:ROLE:")) {
            UUID target = uuidSafe(id.substring("TRUST:ROLE:".length()));
            if (target == null) return;
            setPendingTarget(p, target);
            openAssignRole(p);
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

        if (id.equals("BACK")) { back(p); return; }
        if (id.equals("EXIT")) { p.closeInventory(); return; }

        Plot plot = plots.getPlotAt(p.getLocation());
        if (plot == null) { warn(p, messages.getOrDefault("messages.error.no-claim", "&cNo claim here.")); return; }

        if (!plot.getOwner().equals(p.getUniqueId()) && !p.hasPermission("proshield.admin")) { deny(p); return; }

        if (id.startsWith("FLAG:")) {
            String key = id.substring("FLAG:".length());
            boolean cur = plot.getFlag(key);
            plot.setFlag(key, !cur);
            plots.save(plot);
            String on = messages.getOrDefault("messages.state.on", "&aON");
            String off = messages.getOrDefault("messages.state.off", "&cOFF");
            msg(p, messages.getOrDefault("messages.success.flag-toggled", "&b%flag% &7→ %state%")
                    .replace("%flag%", key)
                    .replace("%state%", plot.getFlag(key) ? on : off));
            openFlags(p, 0);
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
                Plot nearest = plots.findNearestClaim(p.getLocation(), 200);
                if (nearest == null) {
                    warn(p, messages.getOrDefault("messages.error.no-nearby-claims", "&cNo nearby claims."));
                    return;
                }
                Location dest = nearest.getCenter();
                if (dest != null) p.teleport(dest);
                msg(p, messages.getOrDefault("messages.success.tp-nearest", "&aTeleported to claim &f%id%")
                        .replace("%id%", nearest.getId().toString()));
                p.closeInventory();
            }
            case "ADMIN:RELOAD" -> {
                plugin.reloadConfig();
                plugin.loadMessagesConfig();
                msg(p, messages.getOrDefault("messages.success.reload", "&aProShield config reloaded."));
                soundGood(p);
                openAdmin(p);
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

        if (id.equals("BACK")) { back(p); return; }
        if (id.equals("EXIT")) { p.closeInventory(); return; }

        if (id.startsWith("WORLD:OPEN:")) {
            String world = id.substring("WORLD:OPEN:".length());
            if (!world.equalsIgnoreCase("world")) {
                warn(p, "&cThis world’s controls are coming in a future update.");
                return;
            }
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
            String on = messages.getOrDefault("messages.state.on", "&aON");
            String off = messages.getOrDefault("messages.state.off", "&cOFF");
            msg(p, messages.getOrDefault("messages.success.world-flag-toggled", "&b%world%.%key% &7→ %state%")
                    .replace("%world%", world)
                    .replace("%key%", key)
                    .replace("%state%", !cur ? on : off));
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
                msg(p, messages.getOrDefault("messages.success.expand-submitted", "&aExpansion request submitted (&f+%n%&a).")
                        .replace("%n%", String.valueOf(amount)));
                soundGood(p);
            } else {
                warn(p, messages.getOrDefault("messages.error.expand-already-pending", "&cYou may already have a pending request."));
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

        if (id.equals("BACK")) { back(p); return; }
        if (id.equals("EXIT")) { p.closeInventory(); return; }

        if (id.startsWith("REQ:")) {
            String[] parts = id.split(":", 3);
            if (parts.length < 3) return;
            UUID reqId = uuidSafe(parts[1]);
            String action = parts[2];
            ExpansionRequest req = (reqId != null) ? expansions.getById(reqId) : null;
            if (req == null) { warn(p, messages.getOrDefault("messages.error.request-not-found", "&cRequest not found.")); return; }

            if (action.equals("APPROVE")) {
                expansions.approveRequest(req, p.getUniqueId());
                msg(p, messages.getOrDefault("messages.success.request-approved", "&aApproved &f%id%&a.")
                        .replace("%id%", shortId(req.getId())));
                Player requester = Bukkit.getPlayer(req.getRequester());
                if (requester != null) requester.sendMessage(gray(messages.getOrDefault("messages.notify.expand-approved", "&aYour expansion request was approved.")));
                openPending(p, 0);
            } else if (action.equals("DENY")) {
                expansions.denyRequest(req, p.getUniqueId(), "Denied via GUI");
                msg(p, messages.getOrDefault("messages.success.request-denied", "&cDenied &f%id%&c.")
                        .replace("%id%", shortId(req.getId())));
                Player requester = Bukkit.getPlayer(req.getRequester());
                if (requester != null) requester.sendMessage(gray(messages.getOrDefault("messages.notify.expand-denied", "&cYour expansion request was denied.")));
                openPending(p, 0);
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
        return textItem(
                canClaim ? Material.LIME_BED : Material.GRAY_BED,
                canClaim ? "&aClaim Chunk" : "&7Claim Chunk",
                List.of(gray(canClaim
                        ? messages.getOrDefault("messages.lore.claim", "&7Create a new claim centered here.")
                        : messages.getOrDefault("messages.lore.already-claimed", "&7Already claimed.")),
                        line("#CLAIM"))
        );
    }

    private ItemStack iconUnclaimButton(boolean canUnclaim) {
        return textItem(
                canUnclaim ? Material.RED_BED : Material.GRAY_BED,
                canUnclaim ? "&cUnclaim Chunk" : "&7Unclaim Chunk",
                List.of(gray(canUnclaim
                        ? messages.getOrDefault("messages.lore.unclaim", "&7Remove your current claim.")
                        : messages.getOrDefault("messages.lore.no-claim", "&7No claim here.")),
                        line("#UNCLAIM"))
        );
    }

    private ItemStack iconClaimInfo(Plot plot) {
        List<String> lore = new ArrayList<>();
        if (plot == null) {
            lore.add(gray(messages.getOrDefault("messages.lore.no-claim", "&7You are not standing in a claim.")));
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
        return textItem(Material.PLAYER_HEAD,
                enabled ? "&aTrusted Players" : "&7Trusted Players",
                List.of(gray(enabled
                        ? messages.getOrDefault("messages.lore.trusted", "&7Manage trusted players & roles.")
                        : messages.getOrDefault("messages.lore.no-claim", "&7No claim here.")),
                        line("#TRUSTED")));
    }

    private ItemStack iconRoles(boolean enabled) {
        return textItem(Material.NAME_TAG,
                enabled ? "&eAssign Roles" : "&7Assign Roles",
                List.of(gray(enabled
                        ? messages.getOrDefault("messages.lore.roles", "&7Choose a player in Trusted, then pick a role.")
                        : messages.getOrDefault("messages.lore.no-claim", "&7No claim here.")),
                        line("#ROLES")));
    }

    private ItemStack iconFlags(boolean enabled) {
        return textItem(Material.LEVER,
                enabled ? "&6Flags" : "&7Flags",
                List.of(gray(enabled
                        ? messages.getOrDefault("messages.lore.flags", "&7Toggle claim flags (explosions, fire, pvp, safezone…)")
                        : messages.getOrDefault("messages.lore.no-claim", "&7No claim here.")),
                        line("#FLAGS")));
    }

    private ItemStack iconAdminTools() {
        return textItem(Material.COMPASS, "&dAdmin Tools", List.of(
                gray(messages.getOrDefault("messages.lore.admin", "&7TP to nearest claim / review requests / world controls")),
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
                gray(messages.getOrDefault("messages.lore.expand", "&7Submit +1 expansion request.")),
                line("#EXPAND:1")
        ));
    }

    private ItemStack iconTrustedEntry(Plot plot, UUID target) {
        ClaimRole r = roles.getRole(plot, target);
        return textItem(Material.PLAYER_HEAD,
                "&f" + nameOrShort(target) + " &7[" + r.getDisplayName() + "]",
                List.of(
                        gray(messages.getOrDefault("messages.lore.trusted-entry", "&8Left: set role  •  Right: remove")),
                        line("TRUST:ROLE:" + target),
                        line("TRUST:REMOVE:" + target)
                ));
    }

    private ItemStack iconRoleOption(ClaimRole r, Plot plot, UUID target) {
        boolean has = roles.getRole(plot, target) == r;
        return textItem(Material.PAPER,
                (has ? "&a" : "&7") + r.getDisplayName(),
                List.of(line("ROLE:" + r.name())));
    }

    private static class FlagSpec {
        final String key, name;
        final Material icon;
        FlagSpec(String key, String name, Material icon) { this.key = key; this.name = name; this.icon = icon; }
    }

    private List<FlagSpec> defaultFlags() {
        return List.of(
                new FlagSpec("safezone", "Safe Zone", Material.TOTEM_OF_UNDYING),
                new FlagSpec("pvp", "PvP Allowed", Material.IRON_SWORD),
                new FlagSpec("explosions", "Explosions", Material.TNT),
                new FlagSpec("fire-spread", "Fire Spread", Material.FLINT_AND_STEEL),
                new FlagSpec("block-break", "Block Break", Material.IRON_PICKAXE),
                new FlagSpec("block-place", "Block Place", Material.STONE),
                new FlagSpec("containers", "Container Protection", Material.CHEST),
                new FlagSpec("ignite-flint", "Ignite (Flint & Steel)", Material.FLINT_AND_STEEL),
                new FlagSpec("ignite-lava", "Ignite (Lava)", Material.LAVA_BUCKET),
                new FlagSpec("ignite-lightning", "Ignite (Lightning)", Material.LIGHTNING_ROD)
        );
    }

    private ItemStack iconFlag(Plot plot, FlagSpec f) {
        boolean on = plot.getFlag(f.key);
        return textItem(f.icon, (on ? "&a" : "&c") + f.name, List.of(
                gray("&7State: &f" + (on ? "ON" : "OFF")),
                line("#FLAG:" + f.key)
        ));
    }

    private ItemStack iconWorld(String world) {
        return textItem(Material.GRASS_BLOCK, "&a" + world, List.of(
                gray("&7View & toggle settings"),
                line("WORLD:OPEN:" + world)
        ));
    }

    private ItemStack iconWorldToggle(String world, String key, boolean on, Material mat, String name, String desc) {
        return textItem(mat, (on ? "&a" : "&c") + name, List.of(
                gray("&7" + desc),
                gray("&7State: &f" + (on ? "ON" : "OFF")),
                line("WORLD:TOGGLE:" + world + ":" + key)
        ));
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
                line("REQ:" + r.getId() + ":APPROVE"),
                line("REQ:" + r.getId() + ":DENY")
        ));
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
        // (No auto-exit here to avoid duplicates)
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

    private void deny(Player p) {
        warn(p, messages.getOrDefault("messages.error.no-permission", "&cYou don’t have permission for this action."));
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

    private void push(Player p, View v) {
        nav.computeIfAbsent(p.getUniqueId(), k -> new ArrayDeque<>()).push(v);
    }

    private View peek(Player p) {
        Deque<View> st = nav.get(p.getUniqueId());
        return (st == null || st.isEmpty()) ? null : st.peek();
    }

    /** Set the pending target for Assign Role flow and push a view so back() works. */
    public void setPendingTarget(Player p, UUID target) {
        push(p, View.assignRole(target));
    }

    public void clearNav(Player p) {
        Deque<View> st = nav.get(p.getUniqueId());
        if (st != null) st.clear();
    }

    private void back(Player p) {
        Deque<View> st = nav.get(p.getUniqueId());
        if (st == null || st.size() <= 1) { p.closeInventory(); clearNav(p); return; }
        st.pop();
        View prev = st.peek();
        if (prev == null) { p.closeInventory(); clearNav(p); return; }
        switch (prev.type) {
            case "MAIN"       -> openMainMenu(p);
            case "CLAIMINFO"  -> openClaimInfo(p);
            case "TRUSTED"    -> openTrusted(p, 0);
            case "ASSIGNROLE" -> openAssignRole(p);
            case "FLAGS"      -> openFlags(p, 0);
            case "ADMIN"      -> openAdmin(p);
            case "WORLDS"     -> openWorldControls(p, 0);
            case "WORLDDETAIL"-> openWorldDetail(p, prev.world);
            case "PENDING"    -> openPending(p, 0);
            case "HISTORY"    -> openHistory(p);
            default           -> { p.closeInventory(); clearNav(p); }
        }
    }

    // --------------------------- World Controls I/O ---------------------------

    private boolean readWorldBool(String world, String key, boolean def) {
        return plugin.getConfig().getBoolean("worlds." + world + "." + key, def);
    }

    private void writeWorldBool(String world, String key, boolean val) {
        plugin.getConfig().set("worlds." + world + "." + key, val);
        plugin.saveConfig();
    }

       private Map<String, Boolean> readAllWorldBools(String world) {
        Map<String, Boolean> map = new LinkedHashMap<>();
        if (plugin.getConfig().isConfigurationSection("worlds." + world)) {
            for (String k : plugin.getConfig().getConfigurationSection("worlds." + world).getKeys(false)) {
                map.put(k, plugin.getConfig().getBoolean("worlds." + world + "." + k, defaultWorld(k)));
            }
        } else {
            map.put(WC_PVP, defaultWorld(WC_PVP));
            map.put(WC_SAFEZONE, defaultWorld(WC_SAFEZONE));
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
}

// ================================= EOF =========================================
