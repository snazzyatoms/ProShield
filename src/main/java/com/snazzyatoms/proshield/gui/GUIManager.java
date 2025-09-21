// ========================== GUIManager.java (v1.2.6+) ==========================
// Polished, consolidated, single-file GUI manager — preserves 1.2.4 → 1.2.6,
// and extends with Flags menu, stronger pagination, admin pending review,
// click router, fallbacks, and UX refinements.
// ==============================================================================
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
import org.bukkit.Location;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ProShield GUIManager (v1.2.6+ polished)
 *
 * Single source of truth for all in-game menus:
 *  - Main Menu (Claim / Unclaim / Info / Trusted / Roles / Flags / Admin)
 *  - Claim Info
 *  - Trusted Players (pagination)
 *  - Roles (default role setter)
 *  - Flags (new in this polished build; toggle typical claim flags)
 *  - Admin Tools (TP to nearest claim, Pending Requests, World Controls stub)
 *  - Pending Requests (approve / deny)
 *
 * Public entrypoints (access from GUIListener/commands):
 *  - openMainMenu(Player)
 *  - openClaimInfo(Player, Plot)
 *  - openTrustedPlayers(Player, Plot, int page)
 *  - openRoles(Player, Plot)
 *  - openFlags(Player, Plot, int page)
 *  - openAdminTools(Player)
 *  - openPendingRequests(Player, int page)
 *  - handleClick(InventoryClickEvent)
 *
 * Notes:
 *  - Uses internal ViewState map to route clicks safely.
 *  - Fully null-safe; provides minimal fallbacks to compile on dev servers.
 *  - If your managers differ in method names, see adapters at bottom.
 */
public class GUIManager {

    // ------------------------------ Types ---------------------------------

    public enum Menu {
        MAIN,
        CLAIM_INFO,
        TRUSTED_PLAYERS,
        ROLES,
        FLAGS,
        ADMIN_TOOLS,
        PENDING_REQUESTS
    }

    // Common sizes
    private static final int SIZE_27 = 27;
    private static final int SIZE_36 = 36;
    private static final int SIZE_45 = 45;
    private static final int SIZE_54 = 54;

    // Timestamp display
    private static final DateTimeFormatter TS_FMT = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm")
            .withZone(ZoneId.systemDefault());

    // Decorative filler
    private static final ItemStack FILLER = quickItem(Material.GRAY_STAINED_GLASS_PANE, " ");

    // ------------------------- Dependencies & State -----------------------

    private final ProShield plugin;
    private final MessagesUtil messages;
    private final PlotManager plots;
    private final ClaimRoleManager roles;
    private final ExpansionRequestManager expansionMgr;

    // View routing (which menu the player is using)
    private final Map<UUID, ViewState> views = new HashMap<>();

    public GUIManager(ProShield plugin) {
        this.plugin = plugin;
        this.messages = safeMessages(plugin);
        this.plots = safePlotManager(plugin);
        this.roles = safeRoleManager(plugin);
        this.expansionMgr = safeExpansionManager(plugin);
    }

    // ------------------------------ Openers -------------------------------

    public void openMainMenu(Player p) {
        Plot plot = plots.getPlotAt(p.getLocation());

        Inventory inv = Bukkit.createInventory(p, SIZE_27, title(Menu.MAIN, "Main Menu"));
        border(inv);

        inv.setItem(11, iconClaim(plot));
        inv.setItem(13, iconClaimInfo(plot));
        inv.setItem(15, iconUnclaim(plot));

        inv.setItem(20, iconTrustedPlayers(plot));
        inv.setItem(22, iconRoles());
        inv.setItem(24, iconFlags());

        if (p.hasPermission("proshield.admin")) {
            inv.setItem(26, iconAdminTools());
        }

        p.openInventory(inv);
        views.put(p.getUniqueId(), ViewState.main(plot));
        ok(p);
    }

    public void openClaimInfo(Player p, Plot plot) {
        if (plot == null) {
            warn(p, "You are not in a claimed plot.");
            openMainMenu(p);
            return;
        }
        Inventory inv = Bukkit.createInventory(p, SIZE_36, title(Menu.CLAIM_INFO, "Claim Info"));
        border(inv);

        inv.setItem(11, iconClaimOwner(plot));
        inv.setItem(13, iconClaimChunks(plot));
        inv.setItem(15, iconExpansion(plot));

        inv.setItem(31, backButton("Back to Main"));

        p.openInventory(inv);
        views.put(p.getUniqueId(), ViewState.claimInfo(plot));
        ok(p);
    }

    public void openTrustedPlayers(Player p, Plot plot, int page) {
        if (plot == null) {
            warn(p, "No plot context available.");
            openMainMenu(p);
            return;
        }
        List<UUID> trusted = new ArrayList<>(plot.getTrusted());
        trusted.sort(Comparator.comparing(this::nameOf, String.CASE_INSENSITIVE_ORDER));

        int pageSize = 28; // 54 inv, minus borders
        int maxPage = Math.max(0, (trusted.size() - 1) / pageSize);
        page = Math.max(0, Math.min(page, maxPage));

        Inventory inv = Bukkit.createInventory(p, SIZE_54,
                title(Menu.TRUSTED_PLAYERS, "Trusted Players • Page " + (page + 1)));
        border(inv);
        addPaginator(inv, page, maxPage);

        int start = page * pageSize;
        for (int i = 0; i < pageSize && start + i < trusted.size(); i++) {
            UUID u = trusted.get(start + i);
            inv.setItem(slotGrid(i), iconTrusted(u, plot));
        }

        inv.setItem(49, backButton("Back"));

        p.openInventory(inv);
        views.put(p.getUniqueId(), ViewState.trusted(plot, page));
        ok(p);
    }

    public void openRoles(Player p, Plot plot) {
        if (plot == null) {
            warn(p, "No plot context available.");
            openMainMenu(p);
            return;
        }
        Inventory inv = Bukkit.createInventory(p, SIZE_45, title(Menu.ROLES, "Claim Roles"));
        border(inv);

        int i = 10;
        for (ClaimRole role : roles.getAllRoles()) {
            inv.setItem(i, iconRole(role, plot));
            i += (i % 9 == 7) ? 3 : 1;
        }

        inv.setItem(40, backButton("Back"));

        p.openInventory(inv);
        views.put(p.getUniqueId(), ViewState.roles(plot));
        ok(p);
    }

    public void openFlags(Player p, Plot plot, int page) {
        if (plot == null) {
            warn(p, "No plot context available.");
            openMainMenu(p);
            return;
        }
        // Typical flags: explosions, fire, pvp, mobGrief, leafDecay, trample, containerProtect, redstone, entry, exit
        List<FlagSpec> flags = defaultFlagsFor(plot);
        int pageSize = 21;
        int maxPage = Math.max(0, (flags.size() - 1) / pageSize);
        page = Math.max(0, Math.min(page, maxPage));

        Inventory inv = Bukkit.createInventory(p, SIZE_54, title(Menu.FLAGS, "Flags • Page " + (page + 1)));
        border(inv);
        addPaginator(inv, page, maxPage);

        int start = page * pageSize;
        for (int i = 0; i < pageSize && start + i < flags.size(); i++) {
            FlagSpec fs = flags.get(start + i);
            inv.setItem(slotGrid(i), iconFlag(plot, fs));
        }

        inv.setItem(49, backButton("Back"));

        p.openInventory(inv);
        views.put(p.getUniqueId(), ViewState.flags(plot, page));
        ok(p);
    }

    public void openAdminTools(Player p) {
        if (!p.hasPermission("proshield.admin")) {
            deny(p);
            return;
        }
        Inventory inv = Bukkit.createInventory(p, SIZE_27, title(Menu.ADMIN_TOOLS, "Admin Tools"));
        border(inv);

        inv.setItem(11, iconTeleportToClaim());
        inv.setItem(13, iconPendingRequests());
        inv.setItem(15, iconWorldControls());

        inv.setItem(22, backButton("Back"));

        p.openInventory(inv);
        views.put(p.getUniqueId(), ViewState.admin());
        ok(p);
    }

    public void openPendingRequests(Player p, int page) {
        if (!p.hasPermission("proshield.admin")) {
            deny(p);
            return;
        }

        List<ExpansionRequest> all = new ArrayList<>(expansionMgr.getPendingRequests());
        all.sort(Comparator.comparingLong(ExpansionRequest::getCreatedAt));

        int pageSize = 21;
        int maxPage = Math.max(0, (all.size() - 1) / pageSize);
        page = Math.max(0, Math.min(page, maxPage));

        Inventory inv = Bukkit.createInventory(p, SIZE_54, title(Menu.PENDING_REQUESTS,
                "Pending Requests • Page " + (page + 1)));
        border(inv);
        addPaginator(inv, page, maxPage);

        int start = page * pageSize;
        for (int i = 0; i < pageSize && start + i < all.size(); i++) {
            ExpansionRequest req = all.get(start + i);
            inv.setItem(slotGrid(i), iconRequest(req));
        }

        inv.setItem(49, backButton("Back"));

        p.openInventory(inv);
        views.put(p.getUniqueId(), ViewState.pending(page));
        ok(p);
    }
    // ---------------------------- Click Router ----------------------------

    public void handleClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        ItemStack item = e.getCurrentItem();
        if (item == null || item.getType() == Material.AIR) return;

        ViewState vs = views.get(p.getUniqueId());
        if (vs == null) return; // not our menu
        e.setCancelled(true);

        String id = idOf(item);
        if (id == null) return;

        switch (vs.menu) {
            case MAIN -> onClickMain(p, vs, id);
            case CLAIM_INFO -> onClickClaimInfo(p, vs, id);
            case TRUSTED_PLAYERS -> onClickTrusted(p, vs, id);
            case ROLES -> onClickRoles(p, vs, id);
            case FLAGS -> onClickFlags(p, vs, id);
            case ADMIN_TOOLS -> onClickAdmin(p, vs, id);
            case PENDING_REQUESTS -> onClickPending(p, vs, id);
        }
    }

    // ---------------------------- Click Handlers --------------------------

    private void onClickMain(Player p, ViewState vs, String id) {
        if (id.equals("BACK")) { p.closeInventory(); return; }
        switch (id) {
            case "CLAIM" -> {
                Plot current = plots.getPlotAt(p.getLocation());
                if (current != null) { warn(p, "Already claimed."); break; }
                Plot created = plots.claimChunk(p.getUniqueId(), p.getLocation());
                if (created != null) {
                    msg(p, ChatColor.GREEN + "Claimed current chunk.");
                    openClaimInfo(p, created);
                } else warn(p, "Unable to claim here.");
            }
            case "UNCLAIM" -> {
                Plot plot = plots.getPlotAt(p.getLocation());
                if (plot == null) { warn(p, "No claim here."); break; }
                if (!plot.isOwner(p.getUniqueId()) && !p.hasPermission("proshield.admin")) {
                    deny(p); break;
                }
                if (plots.unclaim(plot)) {
                    msg(p, ChatColor.YELLOW + "Unclaimed this chunk.");
                    openMainMenu(p);
                } else warn(p, "Unclaim failed.");
            }
            case "CLAIM_INFO" -> openClaimInfo(p, plots.getPlotAt(p.getLocation()));
            case "TRUSTED" -> openTrustedPlayers(p, plots.getPlotAt(p.getLocation()), 0);
            case "ROLES" -> openRoles(p, plots.getPlotAt(p.getLocation()));
            case "FLAGS" -> openFlags(p, plots.getPlotAt(p.getLocation()), 0);
            case "ADMIN" -> openAdminTools(p);
        }
    }

    private void onClickClaimInfo(Player p, ViewState vs, String id) {
        if (id.equals("BACK")) { openMainMenu(p); return; }
        switch (id) {
            case "EXPAND" -> {
                Plot plot = vs.plot;
                if (plot == null) { warn(p, "No plot context."); break; }
                boolean ok = expansionMgr.createRequest(
                        p.getUniqueId(),
                        plot.getId(),
                        1,
                        "Player requested a +1 expansion via GUI."
                );
                if (ok) {
                    msg(p, ChatColor.GREEN + "Expansion request submitted.");
                    p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1.2f);
                } else warn(p, "You already have a pending request or cannot expand.");
            }
        }
    }

    private void onClickTrusted(Player p, ViewState vs, String id) {
        if (id.equals("BACK")) { openClaimInfo(p, vs.plot); return; }
        if (id.startsWith("PAGE:")) {
            int target = parseIntSafe(id.substring(5), vs.page);
            openTrustedPlayers(p, vs.plot, target);
            return;
        }
        if (id.startsWith("TRUST:")) {
            String[] parts = id.split(":", 3);
            if (parts.length < 3) return;
            UUID target = uuidSafe(parts[1]);
            String action = parts[2];
            Plot plot = vs.plot;
            if (plot == null || target == null) return;

            if (!plot.isOwner(p.getUniqueId()) && !p.hasPermission("proshield.admin")) {
                deny(p); return;
            }
            if (action.equals("REMOVE")) {
                plot.getTrusted().remove(target);
                plots.save(plot);
                msg(p, ChatColor.YELLOW + "Removed " + nameOf(target) + " from trusted.");
                openTrustedPlayers(p, plot, vs.page);
            }
        }
    }

    private void onClickRoles(Player p, ViewState vs, String id) {
        if (id.equals("BACK")) { openClaimInfo(p, vs.plot); return; }
        if (!id.startsWith("ROLE:")) return;
        String roleId = id.substring(5);
        Plot plot = vs.plot;
        if (plot == null) return;
        if (!plot.isOwner(p.getUniqueId()) && !p.hasPermission("proshield.admin")) {
            deny(p); return;
        }
        ClaimRole role = roles.getRole(roleId);
        if (role == null) { warn(p, "Unknown role."); return; }
        plot.setDefaultRole(role.getId());
        plots.save(plot);
        msg(p, ChatColor.GREEN + "Default role set to " + role.getDisplayName() + ".");
        openRoles(p, plot);
    }

    private void onClickFlags(Player p, ViewState vs, String id) {
        if (id.equals("BACK")) { openMainMenu(p); return; }
        if (id.startsWith("PAGE:")) {
            int target = parseIntSafe(id.substring(5), vs.page);
            openFlags(p, vs.plot, target);
            return;
        }
        if (!id.startsWith("FLAG:")) return;

        Plot plot = vs.plot;
        if (plot == null) return;
        if (!plot.isOwner(p.getUniqueId()) && !p.hasPermission("proshield.admin")) {
            deny(p); return;
        }

        // FLAG:<key>:<toggle|cycle>
        String[] parts = id.split(":", 3);
        if (parts.length < 3) return;
        String key = parts[1];
        String action = parts[2];

        // Toggle boolean flag (or cycle tri-state if your implementation supports)
        boolean newVal = toggleFlag(plot, key, action);
        plots.save(plot);

        msg(p, ChatColor.AQUA + "Flag " + key + " → " + (newVal ? "ON" : "OFF"));
        openFlags(p, plot, vs.page);
    }

    private void onClickAdmin(Player p, ViewState vs, String id) {
        if (id.equals("BACK")) { openMainMenu(p); return; }
        switch (id) {
            case "PENDING" -> openPendingRequests(p, 0);
            case "TP_CLAIM" -> {
                Plot nearest = plots.findNearestClaim(p.getLocation(), 200);
                if (nearest == null) { warn(p, "No nearby claims."); return; }
                Location dest = nearest.getCenter();
                if (dest != null) {
                    p.teleport(dest);
                }
                msg(p, ChatColor.AQUA + "Teleported to claim " + nearest.getId());
                p.closeInventory();
            }
            case "WORLD_CTRL" -> warn(p, "World controls are coming soon.");
        }
    }

    private void onClickPending(Player p, ViewState vs, String id) {
        if (id.equals("BACK")) { openAdminTools(p); return; }
        if (id.startsWith("PAGE:")) {
            int target = parseIntSafe(id.substring(5), vs.page);
            openPendingRequests(p, target);
            return;
        }
        if (id.startsWith("REQ:")) {
            // REQ:<requestId>:<APPROVE|DENY>
            String[] parts = id.split(":", 3);
            if (parts.length < 3) return;
            String reqId = parts[1];
            String action = parts[2];
            ExpansionRequest req = expansionMgr.getById(reqId);
            if (req == null) { warn(p, "Request not found."); return; }

            switch (action) {
                case "APPROVE" -> {
                    boolean ok = expansionMgr.approve(reqId, p.getUniqueId(), "Approved via GUI");
                    if (ok) {
                        msg(p, ChatColor.GREEN + "Approved request " + shortId(reqId));
                        Player owner = Bukkit.getPlayer(req.getOwner());
                        if (owner != null) owner.sendMessage(ChatColor.GREEN + "Your expansion request was approved!");
                        openPendingRequests(p, vs.page);
                    } else warn(p, "Approve failed.");
                }
                case "DENY" -> {
                    boolean ok = expansionMgr.deny(reqId, p.getUniqueId(), "Denied via GUI");
                    if (ok) {
                        msg(p, ChatColor.RED + "Denied request " + shortId(reqId));
                        Player owner = Bukkit.getPlayer(req.getOwner());
                        if (owner != null) owner.sendMessage(ChatColor.RED + "Your expansion request was denied.");
                        openPendingRequests(p, vs.page);
                    } else warn(p, "Deny failed.");
                }
            }
        }
    }
    // ------------------------- Icons / Item Builders ----------------------

    private ItemStack iconClaim(Plot plotHere) {
        boolean canClaim = (plotHere == null);
        String name = color(canClaim ? "&aClaim Chunk" : "&7Claim Chunk");
        List<String> lore = List.of(
                color(canClaim ? "&7Create a new claim for this chunk." : "&7This chunk is already claimed."),
                idLine("CLAIM")
        );
        return item(Material.LIME_BED, name, lore, !canClaim);
    }

    private ItemStack iconUnclaim(Plot plotHere) {
        boolean can = (plotHere != null);
        String name = color(can ? "&cUnclaim Chunk" : "&7Unclaim Chunk");
        List<String> lore = List.of(
                color(can ? "&7Remove your claim from this chunk." : "&7No claim here."),
                idLine("UNCLAIM")
        );
        return item(Material.RED_BED, name, lore, !can);
    }

    private ItemStack iconClaimInfo(Plot plot) {
        String name = color("&bClaim Info");
        ArrayList<String> lore = new ArrayList<>();
        if (plot == null) {
            lore.add(color("&7You are not standing in a claim."));
        } else {
            lore.add(color("&7ID: &f" + plot.getId()));
            lore.add(color("&7Owner: &f" + nameOf(plot.getOwner())));
            lore.add(color("&7Chunks: &f" + plot.getChunkCount()));
        }
        lore.add(idLine("CLAIM_INFO"));
        return item(Material.BOOK, name, lore, false);
    }

    private ItemStack iconTrustedPlayers(Plot plot) {
        boolean enabled = plot != null;
        String name = color(enabled ? "&aTrusted Players" : "&7Trusted Players");
        List<String> lore = List.of(
                color(enabled ? "&7Manage who can build in this claim." : "&7No claim context."),
                idLine("TRUSTED")
        );
        return item(Material.PLAYER_HEAD, name, lore, !enabled);
    }

    private ItemStack iconRoles() {
        return item(Material.NAME_TAG, color("&eRoles"), List.of(
                color("&7Configure default permissions by role."),
                idLine("ROLES")
        ), false);
    }

    private ItemStack iconFlags() {
        return item(Material.LEVER, color("&6Flags"), List.of(
                color("&7Toggle claim behaviors (explosions, fire, pvp, etc)."),
                idLine("FLAGS")
        ), false);
    }

    private ItemStack iconAdminTools() {
        return item(Material.COMPASS, color("&dAdmin Tools"), List.of(
                color("&7Teleport, review requests, world controls."),
                idLine("ADMIN")
        ), false);
    }

    private ItemStack iconTeleportToClaim() {
        return item(Material.ENDER_PEARL, color("&bNearest Claim"), List.of(
                color("&7Teleport to the nearest claim (<=200 blocks)."),
                idLine("TP_CLAIM")
        ), false);
    }

    private ItemStack iconPendingRequests() {
        int pending = expansionMgr.getPendingRequests().size();
        return item(Material.PAPER, color("&aPending Requests &7(" + pending + ")"), List.of(
                color("&7Approve or deny player expansion requests."),
                idLine("PENDING")
        ), false);
    }

    private ItemStack iconWorldControls() {
        return item(Material.REDSTONE, color("&cWorld Controls"), List.of(
                color("&7(Coming soon) Global toggles for worlds."),
                idLine("WORLD_CTRL")
        ), false);
    }

    private ItemStack iconClaimOwner(Plot plot) {
        return item(Material.PLAYER_HEAD, color("&bOwner"), List.of(
                color("&7" + nameOf(plot.getOwner())),
                idLine("OWNER")
        ), false);
    }

    private ItemStack iconClaimChunks(Plot plot) {
        return item(Material.MAP, color("&eChunks"), List.of(
                color("&7" + plot.getChunkCount() + " claimed chunks."),
                idLine("CHUNKS")
        ), false);
    }

    private ItemStack iconExpansion(Plot plot) {
        return item(Material.EMERALD, color("&aRequest Expansion"), List.of(
                color("&7Submit a +1 chunk expansion request."),
                idLine("EXPAND")
        ), false);
    }

    private ItemStack iconRole(ClaimRole role, Plot plot) {
        boolean isDefault = role.getId().equalsIgnoreCase(plot.getDefaultRole());
        ArrayList<String> lore = new ArrayList<>();
        lore.add(color("&7" + role.getDescription()));
        lore.add(color("&7Build: &f" + bool(role.canBuild())));
        lore.add(color("&7Interact: &f" + bool(role.canInteract())));
        lore.add(color("&7Containers: &f" + bool(role.canContainers())));
        lore.add(color("&7Switches: &f" + bool(role.canSwitches())));
        lore.add(color("&7Damage Mobs: &f" + bool(role.canDamageMobs())));
        lore.add(color("&7Place Fluids: &f" + bool(role.canPlaceFluids())));
        lore.add("");
        lore.add(color(isDefault ? "&a(Default)" : "&7Set as default"));
        lore.add(idLine("ROLE:" + role.getId()));
        return item(Material.NAME_TAG, color((isDefault ? "&a" : "&e") + role.getDisplayName()), lore, false);
    }

    // ------------------------------- Flags --------------------------------

    private static class FlagSpec {
        final String key;
        final String display;
        final Material icon;

        FlagSpec(String key, String display, Material icon) {
            this.key = key; this.display = display; this.icon = icon;
        }
    }

    private List<FlagSpec> defaultFlagsFor(Plot plot) {
        // You can map these keys to your actual Plot flag store.
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

    private ItemStack iconFlag(Plot plot, FlagSpec spec) {
        boolean enabled = getFlag(plot, spec.key);
        ArrayList<String> lore = new ArrayList<>();
        lore.add(color("&7" + spec.display));
        lore.add(color("&7State: &f" + (enabled ? "ON" : "OFF")));
        lore.add("");
        lore.add(color("&aLeft-click: Toggle"));
        lore.add(idLine("FLAG:" + spec.key + ":toggle"));
        return item(spec.icon, color((enabled ? "&a" : "&c") + spec.display), lore, false);
    }

    // ----------------------- Inventory / UI Helpers -----------------------

    private static void border(Inventory inv) {
        int size = inv.getSize();
        for (int i = 0; i < size; i++) {
            int row = i / 9;
            int col = i % 9;
            if (row == 0 || row == (size / 9) - 1 || col == 0 || col == 8) {
                if (inv.getItem(i) == null) inv.setItem(i, FILLER.clone());
            }
        }
    }

    private void addPaginator(Inventory inv, int page, int maxPage) {
        ItemStack prev = item(Material.ARROW, color("&7« Previous"),
                List.of(idLine("PAGE:" + Math.max(0, page - 1))), page <= 0);
        ItemStack next = item(Material.ARROW, color("&7Next »"),
                List.of(idLine("PAGE:" + Math.min(maxPage, page + 1))), page >= maxPage);
        inv.setItem(inv.getSize() - 8, prev);
        inv.setItem(inv.getSize() - 2, next);
    }

    // Map index into inner 7x? grid (skip border)
    private static int slotGrid(int idx) {
        int row = idx / 7; // inner columns 1..7
        int col = (idx % 7) + 1;
        return row * 9 + col + 9; // start from row 1 (skip top border)
    }

    private static ItemStack item(Material mat, String name, List<String> lore, boolean disabled) {
        ItemStack it = new ItemStack(mat);
        ItemMeta im = it.getItemMeta();
        if (im != null) {
            im.setDisplayName(name);
            if (lore != null) im.setLore(lore);
            im.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_POTION_EFFECTS);
            it.setItemMeta(im);
        }
        return it;
    }

    private static ItemStack quickItem(Material mat, String name) {
        return item(mat, name, null, false);
    }

    private static String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    private static String bool(boolean b) { return b ? "Yes" : "No"; }

    private static String idLine(String id) { return ChatColor.DARK_GRAY + "#" + id; }

    private static String idOf(ItemStack item) {
        ItemMeta im = item.getItemMeta();
        if (im == null || im.getLore() == null) return null;
        for (String line : im.getLore()) {
            if (line != null && ChatColor.stripColor(line).startsWith("#")) {
                return ChatColor.stripColor(line).substring(1);
            }
        }
        return null;
    }

    private static String title(Menu menu, String name) {
        return ChatColor.DARK_GRAY + "[" + ChatColor.AQUA + menu.name() + ChatColor.DARK_GRAY + "] "
                + ChatColor.WHITE + name;
    }

    private void msg(Player p, String s) { p.sendMessage(ChatColor.GRAY + "[ProShield] " + s); }
    private void warn(Player p, String s) { p.sendMessage(ChatColor.GRAY + "[ProShield] " + ChatColor.RED + s); }
    private void ok(Player p) { p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.3f); }
    private void deny(Player p) {
        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.7f, 0.8f);
        warn(p, "You don't have permission.");
    }

    private String nameOf(UUID u) {
        OfflinePlayer op = Bukkit.getOfflinePlayer(u);
        String n = (op != null ? op.getName() : null);
        return (n == null || n.isBlank()) ? u.toString().substring(0, 8) : n;
    }

    private static String shortId(String id) {
        return (id != null && id.length() > 6) ? id.substring(0, 6) : (id == null ? "-" : id);
    }

    private int parseIntSafe(String s, int def) {
        try { return Integer.parseInt(s); } catch (Exception e) { return def; }
    }

    private UUID uuidSafe(String s) {
        try { return UUID.fromString(s); } catch (Exception e) { return null; }
    }

    // ----------------------------- View State -----------------------------

    private static class ViewState {
        final Menu menu;
        final Plot plot; // may be null for MAIN/ADMIN
        final int page;

        private ViewState(Menu m, Plot plot, int page) { this.menu = m; this.plot = plot; this.page = page; }
        static ViewState main(Plot plot) { return new ViewState(Menu.MAIN, plot, 0); }
        static ViewState claimInfo(Plot plot) { return new ViewState(Menu.CLAIM_INFO, plot, 0); }
        static ViewState trusted(Plot plot, int page) { return new ViewState(Menu.TRUSTED_PLAYERS, plot, page); }
        static ViewState roles(Plot plot) { return new ViewState(Menu.ROLES, plot, 0); }
        static ViewState flags(Plot plot, int page) { return new ViewState(Menu.FLAGS, plot, page); }
        static ViewState admin() { return new ViewState(Menu.ADMIN_TOOLS, null, 0); }
        static ViewState pending(int page) { return new ViewState(Menu.PENDING_REQUESTS, null, page); }
    }
    // -------------------------- Flag Getters/Setters ----------------------

    /**
     * Return current flag state for plot. Wire to your actual storage.
     * For now, we simulate via per-plot metadata or a simple map fallback.
     */
    @SuppressWarnings("unchecked")
    private boolean getFlag(Plot plot, String key) {
        Map<String, Boolean> m = ensureFlagMap(plot);
        return m.getOrDefault(key, defaultFlagValue(key));
    }

    /**
     * Toggle or cycle a flag. Returns new boolean value.
     */
    private boolean toggleFlag(Plot plot, String key, String action) {
        Map<String, Boolean> m = ensureFlagMap(plot);
        boolean current = m.getOrDefault(key, defaultFlagValue(key));
        boolean next = !current; // simple toggle for now
        m.put(key, next);
        putFlagMap(plot, m);
        return next;
    }

    private boolean defaultFlagValue(String key) {
        // Defaults: protections ON by default (true), pvp OFF by default (false)
        switch (key) {
            case "explosions": return false;
            case "fire_spread": return false;
            case "pvp": return false;
            case "mob_grief": return false;
            case "leaf_decay": return true;
            case "trample": return false;
            case "containers": return true;
            case "redstone": return true;
            case "entry": return true;
            case "exit": return true;
            default: return false;
        }
    }

    // --------------------------- Flag Map Bridge --------------------------

    // If your Plot has native flag getters/setters, replace these with your API.
    // Below we simulate by attaching a transient map to the Plot via PlotManager
    // save/load calls (or your persistent storage if available).

    private Map<String, Boolean> ensureFlagMap(Plot plot) {
        Map<String, Boolean> m = getFlagMap(plot);
        if (m == null) {
            m = new HashMap<>();
            // seed defaults
            for (FlagSpec fs : defaultFlagsFor(plot)) {
                m.putIfAbsent(fs.key, defaultFlagValue(fs.key));
            }
            putFlagMap(plot, m);
        }
        return m;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Boolean> getFlagMap(Plot plot) {
        // Hook into your real plot meta storage here.
        // Placeholder: use plugin-side transient store keyed by plotId.
        return FlagStore.INSTANCE.byPlotId.get(plot.getId());
    }

    private void putFlagMap(Plot plot, Map<String, Boolean> map) {
        FlagStore.INSTANCE.byPlotId.put(plot.getId(), map);
    }

    // Lightweight process-local store (replace with your persistence)
    private static final class FlagStore {
        static final FlagStore INSTANCE = new FlagStore();
        final Map<String, Map<String, Boolean>> byPlotId = new HashMap<>();
    }

    // ----------------------- Safe dependency accessors --------------------

    private static MessagesUtil safeMessages(ProShield pl) {
        try { return pl.getMessages(); } catch (Throwable t) { return new MessagesFallback(); }
    }

    private static PlotManager safePlotManager(ProShield pl) {
        try { return pl.getPlotManager(); } catch (Throwable t) {
            throw new IllegalStateException("PlotManager not available");
        }
    }

    private static ClaimRoleManager safeRoleManager(ProShield pl) {
        try { return pl.getRoleManager(); } catch (Throwable t) {
            throw new IllegalStateException("ClaimRoleManager not available");
        }
    }

    private static ExpansionRequestManager safeExpansionManager(ProShield pl) {
        try { return pl.getExpansionRequestManager(); } catch (Throwable t) {
            return new ExpansionFallback();
        }
    }

    // ------------------------------ Fallbacks -----------------------------

    private static class MessagesFallback extends MessagesUtil {
        @Override public String get(String path, Object... replacements) {
            return ChatColor.GRAY + "[" + path + "]";
        }
    }

    /**
     * Minimal fallback so dev servers can boot. Replace with your real manager.
     */
    private static class ExpansionFallback implements ExpansionRequestManager {
        private final Map<String, ExpansionRequest> fake = new LinkedHashMap<>();

        @Override public boolean createRequest(UUID owner, String plotId, int amount, String note) {
            String id = UUID.randomUUID().toString();
            fake.put(id, new SimpleReq(id, owner, plotId, amount, System.currentTimeMillis(), note, "PENDING"));
            return true;
        }

        @Override public Collection<ExpansionRequest> getPendingRequests() {
            return fake.values().stream().filter(r -> "PENDING".equals(r.getStatus())).collect(Collectors.toList());
        }

        @Override public ExpansionRequest getById(String id) { return fake.get(id); }

        @Override public boolean approve(String id, UUID admin, String note) {
            ExpansionRequest r = fake.get(id);
            if (r == null) return false;
            fake.put(id, ((SimpleReq) r).withStatus("APPROVED", note));
            return true;
        }

        @Override public boolean deny(String id, UUID admin, String note) {
            ExpansionRequest r = fake.get(id);
            if (r == null) return false;
            fake.put(id, ((SimpleReq) r).withStatus("DENIED", note));
            return true;
        }

        private static class SimpleReq implements ExpansionRequest {
            private final String id;
            private final UUID owner;
            private final String plotId;
            private final int amount;
            private final long createdAt;
            private final String note;
            private final String status;

            SimpleReq(String id, UUID owner, String plotId, int amount, long createdAt, String note, String status) {
                this.id = id; this.owner = owner; this.plotId = plotId; this.amount = amount;
                this.createdAt = createdAt; this.note = note; this.status = status;
            }

            SimpleReq withStatus(String st, String note) {
                return new SimpleReq(id, owner, plotId, amount, createdAt, note, st);
            }

            @Override public String getId() { return id; }
            @Override public UUID getOwner() { return owner; }
            @Override public String getPlotId() { return plotId; }
            @Override public int getAmount() { return amount; }
            @Override public long getCreatedAt() { return createdAt; }
            @Override public String getNote() { return note; }
            @Override public String getStatus() { return status; }
        }
    }
}
// =============================== EOF =========================================
