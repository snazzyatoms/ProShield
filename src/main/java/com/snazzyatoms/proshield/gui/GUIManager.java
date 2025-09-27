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
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

/** Main entry for all ProShield GUIs. */
public class GUIManager {

    // ------------------------------ Constants ------------------------------

// Common inventory sizes
private static final int SIZE_27 = 27;
private static final int SIZE_36 = 36;
private static final int SIZE_45 = 45;
private static final int SIZE_54 = 54;

// UI filler (neutral background)
private static final ItemStack FILLER = quick(Material.GRAY_STAINED_GLASS_PANE, " ");

// Timestamp formatter for expansion/history GUIs
private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        .withZone(ZoneId.systemDefault());

// Default world-control keys (config + GUI sync)
private static final String WC_PVP      = "pvp";
private static final String WC_SAFEZONE = "safezone";


    // ----------------------------- Dependencies ----------------------------

// Core plugin instance
private final ProShield plugin;

// Utilities & managers
private final MessagesUtil messages;                 // Handles localized messages (via LanguageManager)
private final PlotManager plots;                     // Claim/plot management
private final ClaimRoleManager roles;                // Role assignments per claim
private final ExpansionRequestManager expansions;    // Expansion request lifecycle


    // ------------------------------ State ----------------------------------

// Each player has their own navigation stack (like a breadcrumb trail)
private final Map<UUID, Deque<View>> nav = new HashMap<>();

// Debug toggles (loaded from config.yml → set in constructor)
private final boolean debugEnabled;
private final boolean debugMenus;
private final boolean debugClicks;
private final boolean debugNav;


    // ---------------------------- Construction -----------------------------
public GUIManager(ProShield plugin) {
    this.plugin     = plugin;
    this.messages   = plugin.getMessagesUtil();
    this.plots      = plugin.getPlotManager();
    this.roles      = plugin.getRoleManager();
    this.expansions = plugin.getExpansionRequestManager();

    // Load debug flags from config.yml (safe defaults)
    this.debugEnabled = plugin.getConfig().getBoolean("debug.enabled", false);
    this.debugMenus   = plugin.getConfig().getBoolean("debug.menus", false);
    this.debugClicks  = plugin.getConfig().getBoolean("debug.clicks", false);
    this.debugNav     = plugin.getConfig().getBoolean("debug.nav", false);

    // Optional debug: confirm language manager active
    if (debugEnabled && debugMenus) {
        plugin.getLogger().info("[ProShield][DEBUG] GUIManager initialized with language="
                + plugin.getLanguageManager().getCurrentLanguage());
    }
}

// Compatibility alias for older callers
public void openMain(Player player) {
    openMainMenu(player);
}

    // ---------------------------- Title helper -----------------------------
private String title(String key, String fallback) {
    // Pull from messages.yml (or active localization) with safe fallback
    String raw = messages.getOrDefault("messages.gui.titles." + key, fallback);

    // Defensive: ensure we never return null
    if (raw == null || raw.isBlank()) {
        raw = fallback;
    }

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
    inv.setItem(25, exitButton());

    push(p, View.main());
    p.openInventory(inv);
    click(p);

    if (debugMenus) plugin.getLogger().info("[ProShield][DEBUG] Opened Main Menu for " + p.getName());
}

public void openTrusted(Player p, int pageIgnored) {
    Plot plot = plots.getPlotAt(p.getLocation());
    Inventory inv = Bukkit.createInventory(p, SIZE_54, title("trusted", "&8Trusted Players"));
    border(inv);

    if (plot == null) {
        inv.setItem(22, textItem(Material.PLAYER_HEAD,
                messages.getOrDefault("messages.gui.no-claim", "&7No claim here."),
                (List<String>) null));
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

    if (debugMenus) plugin.getLogger().info("[ProShield][DEBUG] Opened Trusted Menu for " + p.getName());
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
        inv.setItem(i, textItem(
                Material.PAPER,
                (roles.getRole(plot, peek.pendingTarget) == r
                        ? messages.getOrDefault("messages.gui.role.selected", "&a") 
                        : messages.getOrDefault("messages.gui.role.unselected", "&7")
                ) + r.getDisplayName(),
                lore,
                "ROLE:" + r.name()
        ));

        i += (i % 9 == 7) ? 3 : 1;
    }

    inv.setItem(40, backButton());
    inv.setItem(41, exitButton());

    push(p, View.assignRole(peek.pendingTarget));
    p.openInventory(inv);
    click(p);

    if (debugMenus) plugin.getLogger().info("[ProShield][DEBUG] Opened Assign Role for " + p.getName());
}

public void openFlags(Player p, int pageIgnored) {
    Plot plot = plots.getPlotAt(p.getLocation());
    Inventory inv = Bukkit.createInventory(p, SIZE_54, title("flags", "&8Claim Flags"));
    border(inv);

    if (plot == null) {
        inv.setItem(22, textItem(Material.LEVER,
                messages.getOrDefault("messages.gui.no-claim", "&7No claim here."),
                (List<String>) null));
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

    if (debugMenus) plugin.getLogger().info("[ProShield][DEBUG] Opened Flags Menu for " + p.getName());
}


    // --------------------------- Admin & World Controls ---------------------------

public void openAdmin(Player p) {
    Inventory inv = Bukkit.createInventory(p, SIZE_27, title("admin", "&8Admin Tools"));
    border(inv);

    inv.setItem(12, textItem(
            Material.PAPER,
            messages.getOrDefault("messages.gui.admin.pending", "&aPending Requests"),
            List.of(gray(messages.getOrDefault("messages.lore.admin.pending", "&7Approve or deny expansion requests."))),
            "ADMIN:PENDING"
    ));
    inv.setItem(14, textItem(
            Material.REDSTONE,
            messages.getOrDefault("messages.gui.admin.worldctrl", "&cWorld Controls"),
            List.of(gray(messages.getOrDefault("messages.lore.admin.worldctrl", "&7Toggle per-world settings (PvP, Safe Zone, Explosions, …)"))),
            "ADMIN:WORLD_CTRL"
    ));
    inv.setItem(16, textItem(
            Material.REPEATER,
            messages.getOrDefault("messages.gui.admin.reload", "&eReload Config & Language"),
            List.of(
                gray(messages.getOrDefault("messages.lore.admin.reload.1", "&7Reloads ProShield configuration &")),
                gray(messages.getOrDefault("messages.lore.admin.reload.2", "&7active language/message files."))
            ),
            "ADMIN:RELOAD"
    ));

    // Placeholder for future feature (no action)
    inv.setItem(22, textItem(Material.BEACON,
            messages.getOrDefault("messages.gui.admin.bungee", "&dBungee Support (Coming Soon)"),
            List.of(
                gray(messages.getOrDefault("messages.lore.admin.bungee.1", "&7Planned for version 2.0 or later.")),
                gray(messages.getOrDefault("messages.lore.admin.bungee.2", "&7Synchronize multiple servers/worlds via BungeeCord."))
            )
    ));

    inv.setItem(25, backButton());
    inv.setItem(26, exitButton());

    replaceTop(p, View.admin());
    p.openInventory(inv);
    click(p);

    if (debugMenus) plugin.getLogger().info("[ProShield][DEBUG] Opened Admin Tools for " + p.getName());
}

public void openWorldControls(Player p, int pageIgnored) {
    Inventory inv = Bukkit.createInventory(p, SIZE_54, title("world-controls", "&8World Controls"));
    border(inv);

    int slot = 10;
    for (World w : Bukkit.getWorlds()) {
        String name = w.getName();
        if (name.equalsIgnoreCase("world_nether") || name.equalsIgnoreCase("world_the_end")) continue;
        inv.setItem(slot++, iconWorld(name));
        if (slot >= 16) break;
    }

    // Placeholders (no actions)
    inv.setItem(slot++, textItem(Material.NETHERRACK,
            messages.getOrDefault("messages.gui.world.nether", "&cWorld_Nether (Coming Soon)"), null));
    inv.setItem(slot++, textItem(Material.END_STONE,
            messages.getOrDefault("messages.gui.world.end", "&cWorld_The_End (Coming Soon)"), null));

    inv.setItem(49, backButton());
    inv.setItem(50, exitButton());

    replaceTop(p, View.worlds(0));
    p.openInventory(inv);
    click(p);

    if (debugMenus) plugin.getLogger().info("[ProShield][DEBUG] Opened World Controls for " + p.getName());
}

/** Opens Expansion Request history (per-player). */
public void openHistory(Player p) {
    Inventory inv = Bukkit.createInventory(p, SIZE_54, title("expansion-history", "&8Expansion History"));
    border(inv);

    List<ExpansionRequest> list = new ArrayList<>(expansions.getAllByPlayer(p.getUniqueId()));
    int max = Math.min(21, list.size());

    for (int i = 0; i < max; i++) {
        ExpansionRequest r = list.get(i);
        inv.setItem(grid(i), iconRequest(r));
    }

    inv.setItem(49, backButton());
    inv.setItem(50, exitButton());

    replaceTop(p, View.history());
    p.openInventory(inv);
    click(p);

    if (debugMenus) plugin.getLogger().info("[ProShield][DEBUG] Opened Expansion History for " + p.getName());
}

/* ===== Refresh helpers (no stack ops) ===== */
private void openFlagsNoPush(Player p) {
    Plot plot = plots.getPlotAt(p.getLocation());
    Inventory inv = Bukkit.createInventory(p, SIZE_54, title("flags", "&8Claim Flags"));
    border(inv);

    if (plot == null) {
        inv.setItem(22, textItem(Material.LEVER,
                messages.getOrDefault("messages.gui.no-claim", "&7No claim here."),
                (List<String>) null));
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
    p.openInventory(inv);
    click(p);

    if (debugMenus) plugin.getLogger().info("[ProShield][DEBUG] Refreshed Flags (no stack push) for " + p.getName());
}

private void openWorldDetailNoPush(Player p, String worldName) {
    Inventory inv = Bukkit.createInventory(p, SIZE_54, title("world-controls", "&8World: " + worldName));
    border(inv);

    boolean pvp             = readWorldBool(worldName, WC_PVP, true);
    boolean safezone        = readWorldBool(worldName, WC_SAFEZONE, false);
    boolean explosions      = readWorldBool(worldName, "explosions", true);
    boolean fireSpread      = readWorldBool(worldName, "fire-spread", true);
    boolean fireBurn        = readWorldBool(worldName, "fire-burn", true);
    boolean blockBreak      = readWorldBool(worldName, "block-break", true);
    boolean blockPlace      = readWorldBool(worldName, "block-place", true);
    boolean containers      = readWorldBool(worldName, "containers", true);
    boolean bucketUse       = readWorldBool(worldName, "bucket-use", true);
    boolean mobSpawn        = readWorldBool(worldName, "mob-spawn", true);
    boolean mobDamage       = readWorldBool(worldName, "mob-damage", true);
    boolean igniteFlint     = readWorldBool(worldName, "ignite-flint", true);
    boolean igniteLava      = readWorldBool(worldName, "ignite-lava", true);
    boolean igniteLightning = readWorldBool(worldName, "ignite-lightning", true);
    boolean cropTrample     = readWorldBool(worldName, "crop-trample", true);

    inv.setItem(10, iconWorldToggle(worldName, WC_PVP, pvp, Material.IRON_SWORD, "&ePvP", "&7Global player-versus-player damage"));
    inv.setItem(11, iconWorldToggle(worldName, WC_SAFEZONE, safezone, Material.TOTEM_OF_UNDYING, "&dSafe Zone", "&7Disable combat & damage in world"));
    inv.setItem(31, iconWorldToggle(worldName, "crop-trample", cropTrample, Material.WHEAT, "&aCrop Trample", "&7Allow/Disallow trampling of farmland"));
    inv.setItem(12, iconWorldToggle(worldName, "explosions", explosions, Material.TNT, "&cExplosions", "&7Enable/Disable explosions in world"));
    inv.setItem(13, iconWorldToggle(worldName, "fire-spread", fireSpread, Material.FLINT_AND_STEEL, "&6Fire Spread", "&7Enable/Disable fire spread in world"));
    inv.setItem(14, iconWorldToggle(worldName, "fire-burn", fireBurn, Material.CAMPFIRE, "&6Fire Burn", "&7Allow blocks to burn when ignited"));
    inv.setItem(15, iconWorldToggle(worldName, "block-break", blockBreak, Material.IRON_PICKAXE, "&9Block Break", "&7Allow players to break blocks"));
    inv.setItem(16, iconWorldToggle(worldName, "block-place", blockPlace, Material.STONE, "&9Block Place", "&7Allow players to place blocks"));

    inv.setItem(19, iconWorldToggle(worldName, "containers", containers, Material.CHEST, "&bContainers", "&7Allow players to open/use containers"));
    inv.setItem(20, iconWorldToggle(worldName, "bucket-use", bucketUse, Material.BUCKET, "&9Bucket Use", "&7Allow players to use buckets"));
    inv.setItem(21, iconWorldToggle(worldName, "mob-spawn", mobSpawn, Material.SKELETON_SPAWN_EGG, "&cMob Spawning", "&7Enable/Disable hostile mob spawns"));
    inv.setItem(22, iconWorldToggle(worldName, "mob-damage", mobDamage, Material.ZOMBIE_HEAD, "&cMob Damage", "&7Enable/Disable mob damage to players"));

    inv.setItem(28, iconWorldToggle(worldName, "ignite-flint", igniteFlint, Material.FLINT_AND_STEEL, "&6Ignite (Flint)", "&7Allow fire starting with flint & steel"));
    inv.setItem(29, iconWorldToggle(worldName, "ignite-lava", igniteLava, Material.LAVA_BUCKET, "&6Ignite (Lava)", "&7Allow fire starting with lava"));
    inv.setItem(30, iconWorldToggle(worldName, "ignite-lightning", igniteLightning, Material.LIGHTNING_ROD, "&6Ignite (Lightning)", "&7Allow fire starting by lightning"));

    inv.setItem(53, textItem(Material.BOOK, "&e⚠ Reload Required?", List.of(
            gray(messages.getOrDefault("messages.lore.world.reload.1", "&7Most changes apply instantly.")),
            ChatColor.YELLOW + messages.getOrDefault("messages.lore.world.reload.2", "Use Reload Config in Admin Tools"),
            gray(messages.getOrDefault("messages.lore.world.reload.3", "&7if toggles don’t apply immediately."))
    )));

    inv.setItem(49, backButton());
    inv.setItem(50, exitButton());
    p.openInventory(inv);
    click(p);

    if (debugMenus) plugin.getLogger().info("[ProShield][DEBUG] Opened World Detail for " + worldName + " (" + p.getName() + ")");
}


    // ---------------------- World Detail Wrapper ----------------------
private void openWorldDetail(Player p, String worldName) {
    // Open without pushing first (avoids double-stack)
    openWorldDetailNoPush(p, worldName);

    // Push translated view onto nav stack
    push(p, View.worldDetail(worldName));

    if (debugMenus) {
        plugin.getLogger().info("[ProShield][DEBUG] Opened World Detail for world=" + worldName + " | player=" + p.getName());
    }
}


    // ---------------------------- Claim Info & Expansion -----------------------------

public void openClaimInfo(Player p) {
    Plot plot = plots.getPlotAt(p.getLocation());
    String titleText = messages.getOrDefault("messages.gui.titles.claim-info", "&8Claim Info");
    Inventory inv = Bukkit.createInventory(p, SIZE_36, title("claim-info", titleText));
    border(inv);

    if (plot == null) {
        inv.setItem(13, textItem(
                Material.BOOK,
                messages.getOrDefault("messages.gui.no-claim", "&7No claim here."),
                (List<String>) null
        ));
    } else {
        inv.setItem(11, iconOwner(plot.getOwner()));
        inv.setItem(13, iconClaimSummary(plot));
        inv.setItem(15, iconExpansionRequest());
    }

    inv.setItem(31, backButton());
    inv.setItem(32, exitButton());

    replaceTop(p, View.claimInfo());
    p.openInventory(inv);
    click(p);

    if (debugMenus) {
        plugin.getLogger().info("[ProShield][DEBUG] Opened Claim Info for " + p.getName());
    }
}

/** Opens a selectable expansion request menu (configurable step-options). */
public void openExpansionRequestMenu(Player p, Plot plot) {
    String titleText = messages.getOrDefault("messages.gui.titles.expansion-menu", "&8Request Expansion");
    Inventory inv = Bukkit.createInventory(p, SIZE_36, title("expansion-menu", titleText));
    border(inv);

    List<Integer> steps = plugin.getConfig().getIntegerList("claims.expansion.step-options");
    if (steps.isEmpty()) steps = List.of(5, 10, 15, 20, 25, 30);

    int slot = 10;
    for (int amt : steps) {
        String optName = messages.getOrDefault("messages.gui.expansion-menu.option", "&a+%blocks% Blocks")
                .replace("%blocks%", String.valueOf(amt));
        List<String> optLore = List.of(gray(
                messages.getOrDefault("messages.gui.expansion-menu.option-lore",
                        "&7Request expansion of +%blocks% blocks.")
                        .replace("%blocks%", String.valueOf(amt))
        ));
        inv.setItem(slot++, textItem(Material.EMERALD, optName, optLore, "EXPAND:" + amt));
        if (slot % 9 == 7) slot += 2;
    }

    inv.setItem(31, backButton());
    inv.setItem(32, exitButton());

    Deque<View> st = nav.computeIfAbsent(p.getUniqueId(), k -> new ArrayDeque<>());
    if (st.isEmpty() || !"CLAIMINFO".equals(st.peek().type)) {
        push(p, View.claimInfo());
    }
    push(p, View.expansionMenu());

    p.openInventory(inv);
    click(p);

    if (debugMenus) {
        plugin.getLogger().info("[ProShield][DEBUG] Opened Expansion Request Menu for " + p.getName()
                + " | Steps=" + steps);
    }
}

/** Opens the Pending Requests menu (Admin view). */
public void openPending(Player p, int page) {
    String titleText = messages.getOrDefault("messages.gui.titles.expansion-requests", "&8Pending Expansion Requests");
    Inventory inv = Bukkit.createInventory(p, SIZE_54, title("pending", titleText));
    border(inv);

    List<ExpansionRequest> pending = expansions.getAllPending();
    int start = page * 28;
    int end = Math.min(start + 28, pending.size());

    int slot = 10;
    for (int i = start; i < end; i++) {
        ExpansionRequest req = pending.get(i);
        inv.setItem(slot++, iconRequest(req));
        if (slot % 9 == 7) slot += 2;
    }

    inv.setItem(49, backButton());
    inv.setItem(50, exitButton());

    push(p, View.pending(page));
    p.openInventory(inv);
    click(p);

    if (debugMenus) {
        plugin.getLogger().info("[ProShield][DEBUG] Opened Pending Requests (page " + page + ") for " + p.getName());
    }
}

    // ----------------------------- Central Click Router -----------------------------

/** Routes clicks by tracked view type (language-independent). */
public void handleClick(InventoryClickEvent e) {
    if (!(e.getWhoClicked() instanceof Player p)) return;
    e.setCancelled(true); // always cancel vanilla behavior

    View current = peek(p);
    if (current == null) return;

    try {
        switch (current.type) {
            case "MAIN"           -> handleMainClick(p, e);
            case "CLAIMINFO"      -> handleMainClaimInfoClickOrPlayerRequest(p, e);
            case "EXPANSION_MENU" -> handleExpansionMenuClick(p, e);
            case "TRUSTED"        -> handleTrustedClick(p, e);
            case "ASSIGNROLE"     -> handleAssignRoleClick(p, e);
            case "FLAGS"          -> handleFlagsClick(p, e);
            case "ADMIN"          -> handleAdminClick(p, e);
            case "WORLDS"         -> handleWorldControlsClick(p, e);
            case "WORLDDETAIL"    -> handleWorldControlsClick(p, e);
            case "PENDING"        -> handleExpansionReviewClick(p, e);
            case "HISTORY"        -> handleHistoryClick(p, e);
            case "DENYREASON"     -> handleDenyReasonClick(p, e);

            default -> {
                if (debugClicks) {
                    plugin.getLogger().info(
                        "[ProShield][DEBUG] Unhandled click in view=" + current.type +
                        " | slot=" + e.getSlot() +
                        " | item=" + (e.getCurrentItem() != null ? e.getCurrentItem().getType() : "null")
                    );
                }
            }
        }
    } catch (Throwable ex) {
        plugin.getLogger().warning("[ProShield] Click routing error in view=" + current.type);
        ex.printStackTrace();
        // Fail-safe: close inventory so player isn’t stuck in a broken GUI
        p.closeInventory();
    }
}



    // ----------------------------- Click Handlers -----------------------------

public void handleMainClick(Player p, InventoryClickEvent e) {
    ItemStack it = e.getCurrentItem();
    if (!valid(it)) return;
    String id = extractId(it);
    if (id == null) return;

    if (id.startsWith("INFO:")) return; // Ignore info-only placeholders

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
        case "INFO"    -> openClaimInfo(p);
        case "TRUSTED" -> openTrusted(p, 0);
        case "ROLES"   -> msg(p, messages.getOrDefault("messages.info.roles-howto",
                                "&7Open &fTrusted&7, click a player, then choose a role."));
        case "FLAGS"   -> openFlags(p, 0);
        case "ADMIN"   -> {
            if (!p.hasPermission("proshield.admin")) { deny(p); return; }
            openAdmin(p);
        }
        case "BACK"    -> back(p);
        case "EXIT"    -> p.closeInventory();
    }
}

public void handleAssignRoleClick(Player p, InventoryClickEvent e) {
    ItemStack it = e.getCurrentItem();
    if (!valid(it)) return;
    String id = extractId(it);
    if (id == null) return;
    if (id.startsWith("INFO:")) return;

    Plot plot = plots.getPlotAt(p.getLocation());
    if (plot == null) { warn(p, messages.getOrDefault("messages.error.no-claim", "&cNo claim here.")); return; }

    View peek = peek(p);
    if (peek == null || peek.pendingTarget == null) {
        warn(p, messages.getOrDefault("messages.error.pick-player-first", "&cPick a player first."));
        return;
    }

    if (!plot.getOwner().equals(p.getUniqueId()) && !p.hasPermission("proshield.admin")) { deny(p); return; }

    if (id.startsWith("ROLE:")) {
        String roleName = id.substring("ROLE:".length());
        ClaimRole role = ClaimRole.fromName(roleName);
        if (role == ClaimRole.NONE) {
            warn(p, messages.getOrDefault("messages.error.unknown-role", "&cUnknown role.")); return;
        }
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
    if (id.startsWith("INFO:")) return;

    Plot plot = plots.getPlotAt(p.getLocation());
    if (plot == null) { warn(p, messages.getOrDefault("messages.error.no-claim", "&cNo claim here.")); return; }

    if (id.startsWith("TRUST:")) {
        UUID target = uuidSafe(id.substring("TRUST:".length()));
        if (target == null) return;

        boolean right = e.isRightClick();
        if (right) {
            if (!plot.getOwner().equals(p.getUniqueId()) && !p.hasPermission("proshield.admin")) { deny(p); return; }
            plot.getTrusted().remove(target);
            plots.save(plot);
            msg(p, messages.getOrDefault("messages.success.trust-removed", "&eRemoved &f%player% &efrom trusted.")
                    .replace("%player%", nameOrShort(target)));
            openTrusted(p, 0);
        } else {
            setPendingTarget(p, target);
            openAssignRole(p);
        }
    } else if (id.equals("BACK")) {
        back(p);
    } else if (id.equals("EXIT")) {
        p.closeInventory();
    }
}

public void handleMainClaimInfoClickOrPlayerRequest(Player p, InventoryClickEvent e) {
    ItemStack it = e.getCurrentItem();
    if (!valid(it)) return;
    String id = extractId(it);
    if (id == null) return;
    if (id.startsWith("INFO:")) return;

    if (id.equals("BACK")) { back(p); return; }
    if (id.equals("EXIT")) { p.closeInventory(); return; }

    if (id.equals("EXPAND:MENU")) {
        Plot plot = plots.getPlotAt(p.getLocation());
        if (plot == null) { warn(p, "&cNo claim here."); return; }
        openExpansionRequestMenu(p, plot);
    }
}

public void handleExpansionMenuClick(Player p, InventoryClickEvent e) {
    ItemStack it = e.getCurrentItem();
    if (!valid(it)) return;
    String id = extractId(it);
    if (id == null) return;
    if (id.startsWith("INFO:")) return;

    if (id.equals("BACK")) { back(p); return; }
    if (id.equals("EXIT")) { p.closeInventory(); return; }

    if (id.startsWith("EXPAND:")) {
        int amt = safeInt(id.substring("EXPAND:".length()), 1);
        expansions.submitRequest(p, amt);
        msg(p, "&aExpansion request submitted for +" + amt + " blocks.");
        openClaimInfo(p);
    }
}

public void handleFlagsClick(Player p, InventoryClickEvent e) {
    ItemStack it = e.getCurrentItem();
    if (!valid(it)) return;
    String id = extractId(it);
    if (id == null) return;
    if (id.startsWith("INFO:")) return;

    if (id.equals("BACK")) { back(p); return; }
    if (id.equals("EXIT")) { p.closeInventory(); return; }

    Plot plot = plots.getPlotAt(p.getLocation());
    if (plot == null) {
        warn(p, messages.getOrDefault("messages.error.no-claim", "&cNo claim here."));
        return;
    }

    if (!plot.getOwner().equals(p.getUniqueId()) && !p.hasPermission("proshield.admin")) {
        deny(p);
        return;
    }

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

        openFlagsNoPush(p);
    }
}

public void handleAdminClick(Player p, InventoryClickEvent e) {
    ItemStack it = e.getCurrentItem();
    if (!valid(it)) return;
    String id = extractId(it);
    if (id == null) return;

    switch (id) {
        case "ADMIN:PENDING"    -> openPending(p, 0);
        case "ADMIN:WORLD_CTRL" -> openWorldControls(p, 0);
        case "ADMIN:RELOAD"     -> {
            plugin.reloadConfig();
            plugin.getLanguageManager().reload();
            msg(p, messages.getOrDefault("messages.reloaded", "&aConfiguration and language reloaded."));
        }
        case "BACK"             -> back(p);
        case "EXIT"             -> p.closeInventory();
    }
}

public void handleWorldControlsClick(Player p, InventoryClickEvent e) {
    ItemStack it = e.getCurrentItem();
    if (!valid(it)) return;
    String id = extractId(it);
    if (id == null) return;

    if (id.startsWith("WORLD:OPEN:")) {
        String world = id.substring("WORLD:OPEN:".length());
        openWorldDetail(p, world);
    } else if (id.startsWith("WORLD:TOGGLE:")) {
        String[] parts = id.split(":");
        if (parts.length == 4) {
            String world = parts[2];
            String key   = parts[3];
            boolean cur  = readWorldBool(world, key, defaultWorld(key));
            writeWorldBool(world, key, !cur);
            msg(p, "&eWorld &f" + world + "&e: &f" + key + " &7→ " + (!cur ? "&aON" : "&cOFF"));
            openWorldDetailNoPush(p, world);
        }
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

    if (id.startsWith("REQ:")) {
        String reqId = id.substring("REQ:".length());
        ExpansionRequest req = expansions.getById(reqId);
        if (req == null) {
            warn(p, "&cRequest not found.");
            return;
        }
        if (e.isRightClick()) {
            expansions.deny(reqId, "Denied by admin");
            msg(p, "&cDenied expansion request from &f" + nameOrShort(req.getRequester()));
        } else {
            expansions.approve(reqId);
            msg(p, "&aApproved expansion request for &f" + nameOrShort(req.getRequester()));
        }
        openPending(p, 0);
    } else if (id.equals("BACK")) {
        back(p);
    } else if (id.equals("EXIT")) {
        p.closeInventory();
    }
}

public void handleHistoryClick(Player p, InventoryClickEvent e) {
    ItemStack it = e.getCurrentItem();
    if (!valid(it)) return;
    String id = extractId(it);
    if (id == null) return;

    if (id.equals("BACK")) {
        back(p);
    } else if (id.equals("EXIT")) {
        p.closeInventory();
    }
}

public void handleDenyReasonClick(Player p, InventoryClickEvent e) {
    ItemStack it = e.getCurrentItem();
    if (!valid(it)) return;
    String id = extractId(it);
    if (id == null) return;

    if (id.startsWith("DENY:")) {
        String reason = id.substring("DENY:".length());
        View peek = peek(p);
        if (peek == null || !"PENDING".equals(peek.type)) {
            warn(p, "&cNo request selected.");
            return;
        }
        // Get the last request shown to this admin (for simplicity, deny latest viewed)
        List<ExpansionRequest> pending = expansions.getAllPending();
        if (pending.isEmpty()) {
            warn(p, "&cNo pending requests to deny.");
            return;
        }
        ExpansionRequest req = pending.get(0); // or improve with tracked selection
        expansions.deny(req.getId(), reason);
        msg(p, "&cDenied request by &f" + nameOrShort(req.getRequester()) + " &7Reason: &f" + reason);
        openPending(p, peek.page);
    } else if (id.equals("BACK")) {
        back(p);
    } else if (id.equals("EXIT")) {
        p.closeInventory();
    }
}


    // ------------------------------ Icon Builder ------------------------------

private ItemStack iconClaimButton(boolean canClaim) {
    return textItem(
        canClaim ? Material.LIME_BED : Material.GRAY_BED,
        canClaim ? "&aClaim Chunk" : "&7Claim Chunk",
        List.of(gray(canClaim
                ? messages.getOrDefault("messages.lore.claim", "&7Create a new claim centered here.")
                : messages.getOrDefault("messages.lore.already-claimed", "&7Already claimed."))),
        canClaim ? "CLAIM" : "INFO:ALREADYCLAIMED"
    );
}

private ItemStack iconUnclaimButton(boolean canUnclaim) {
    return textItem(
        canUnclaim ? Material.RED_BED : Material.GRAY_BED,
        canUnclaim ? "&cUnclaim Chunk" : "&7Unclaim Chunk",
        List.of(gray(canUnclaim
                ? messages.getOrDefault("messages.lore.unclaim", "&7Remove your current claim.")
                : messages.getOrDefault("messages.lore.no-claim", "&7No claim here."))),
        canUnclaim ? "UNCLAIM" : "INFO:NOCLAIM"
    );
}

private ItemStack iconExpansionRequest() {
    return textItem(Material.DIAMOND, "&bRequest Expansion",
        List.of(gray("&7Open the expansion request menu.")),
        "EXPAND:MENU"
    );
}

private ItemStack iconClaimInfo(Plot plot) {
    List<String> lore = new ArrayList<>();
    if (plot == null) {
        lore.add(gray(messages.getOrDefault("messages.lore.no-claim", "&7You are not standing in a claim.")));
        return textItem(Material.BOOK, "&bClaim Info", lore, "INFO:NOCLAIM");
    } else {
        lore.add(gray("&7ID: &f" + plot.getId()));
        lore.add(gray("&7Owner: &f" + nameOrShort(plot.getOwner())));
        lore.add(gray("&7Center: &f" + plot.getWorld() + " " + plot.getX() + "," + plot.getZ()));
        lore.add(gray("&7Radius: &f" + plot.getRadius()));
        return textItem(Material.BOOK, "&bClaim Info", lore, "INFO:CLAIM");
    }
}

private ItemStack iconTrusted(boolean enabled) {
    return textItem(Material.PLAYER_HEAD,
        enabled ? "&aTrusted Players" : "&7Trusted Players",
        List.of(gray(enabled
                ? messages.getOrDefault("messages.lore.trusted", "&7Manage trusted players & roles.")
                : messages.getOrDefault("messages.lore.no-claim", "&7No claim here."))),
        enabled ? "TRUSTED" : "INFO:NOCLAIM"
    );
}

private ItemStack iconRoles(boolean enabled) {
    return textItem(Material.NAME_TAG,
        enabled ? "&eAssign Roles" : "&7Assign Roles",
        List.of(gray(enabled
                ? messages.getOrDefault("messages.lore.roles", "&7Choose a player in Trusted, then pick a role.")
                : messages.getOrDefault("messages.lore.no-claim", "&7No claim here."))),
        enabled ? "ROLES" : "INFO:NOCLAIM"
    );
}

private ItemStack iconFlags(boolean enabled) {
    return textItem(Material.LEVER,
        enabled ? "&6Flags" : "&7Flags",
        List.of(gray(enabled
                ? messages.getOrDefault("messages.lore.flags", "&7Toggle claim flags (explosions, fire, pvp, safezone…)")
                : messages.getOrDefault("messages.lore.no-claim", "&7No claim here."))),
        enabled ? "FLAGS" : "INFO:NOCLAIM"
    );
}

private ItemStack iconFlag(Plot plot, FlagSpec f) {
    boolean on = plot.getFlag(f.key);
    return textItem(f.icon, (on ? "&a" : "&c") + f.name,
        List.of(gray("&7State: &f" + (on ? "ON" : "OFF"))),
        "FLAG:" + f.key
    );
}

private ItemStack iconAdminTools() {
    return textItem(Material.COMPASS, "&dAdmin Tools",
        List.of(gray(messages.getOrDefault("messages.lore.admin", "&7Review expansion requests & world controls"))),
        "ADMIN"
    );
}

private ItemStack iconOwner(UUID owner) {
    return textItem(Material.PLAYER_HEAD, "&bOwner",
        List.of(gray("&7" + nameOrShort(owner))),
        "INFO:OWNER"
    );
}

private ItemStack iconClaimSummary(Plot plot) {
    return textItem(Material.MAP, "&eClaim Summary", List.of(
        gray("&7World: &f" + plot.getWorld()),
        gray("&7Center: &f" + plot.getX() + "," + plot.getZ()),
        gray("&7Radius: &f" + plot.getRadius())
    ), "INFO:SUMMARY");
}

private ItemStack iconTrustedEntry(Plot plot, UUID target) {
    ClaimRole r = roles.getRole(plot, target);
    return textItem(Material.PLAYER_HEAD,
        "&f" + nameOrShort(target) + " &7[" + r.getDisplayName() + "]",
        List.of(gray(messages.getOrDefault("messages.lore.trusted-entry", "&8Left: set role  •  Right: remove"))),
        "TRUST:" + target  // left: set role, right: remove
    );
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
        new FlagSpec("crop-trample", "Crop Trample", Material.WHEAT),
        new FlagSpec("ignite-flint", "Ignite (Flint & Steel)", Material.FLINT_AND_STEEL),
        new FlagSpec("ignite-lava", "Ignite (Lava)", Material.LAVA_BUCKET),
        new FlagSpec("ignite-lightning", "Ignite (Lightning)", Material.LIGHTNING_ROD)
    );
}

private ItemStack iconWorld(String world) {
    return textItem(Material.GRASS_BLOCK, "&a" + world,
        List.of(gray("&7View & toggle settings")),
        "WORLD:OPEN:" + world
    );
}

private ItemStack iconWorldToggle(String world, String key, boolean on, Material mat, String name, String desc) {
    return textItem(mat, (on ? "&a" : "&c") + name,
        List.of(gray("&7" + desc), gray("&7State: &f" + (on ? "ON" : "OFF"))),
        "WORLD:TOGGLE:" + world + ":" + key
    );
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
        gray("&8Left click: Approve  •  Right click: Deny")
    ), "REQ:" + r.getId());
}

private ItemStack backButton() {
    return textItem(Material.ARROW, "&7Back", List.of(gray("&7Return to previous menu")), "BACK");
}

private ItemStack exitButton() {
    return textItem(Material.OAK_DOOR, "&7Exit", List.of(gray("&7Close this menu")), "EXIT");
}

    // ------------------------------ UI Helpers ------------------------------

private static void border(Inventory inv) {
    int size = inv.getSize();
    for (int i = 0; i < size; i++) {
        int r = i / 9, c = i % 9;
        if (r == 0 || r == (size / 9) - 1 || c == 0 || c == 8) {
            if (inv.getItem(i) == null) {
                inv.setItem(i, quick(Material.GRAY_STAINED_GLASS_PANE, " "));
            }
        }
    }
}

private static int grid(int i) {
    int row = i / 7;
    int col = (i % 7) + 1;
    return 9 + row * 9 + col;
}

// === Quick name-only item (for borders/placeholders)
private static ItemStack quick(Material m, String name) {
    ItemStack it = new ItemStack(m);
    ItemMeta im = it.getItemMeta();
    if (im != null) {
        im.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        im.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_POTION_EFFECTS);
        it.setItemMeta(im);
    }
    return it;
}

// === Clean item builders (no hashtags), with optional action ID ===
private ItemStack textItem(Material m, String name) {
    return textItem(m, name, (List<String>) null, null);
}

private ItemStack textItem(Material m, String name, List<String> lore) {
    return textItem(m, name, lore, null);
}

private NamespacedKey actionKey() {
    return new NamespacedKey(plugin, "action");
}

private ItemStack textItem(Material m, String name, List<String> lore, String actionId) {
    ItemStack it = new ItemStack(m);
    ItemMeta im = it.getItemMeta();
    if (im != null) {
        im.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        if (lore != null) {
            List<String> colored = new ArrayList<>(lore.size());
            for (String s : lore) {
                colored.add(ChatColor.translateAlternateColorCodes('&', s));
            }
            im.setLore(colored);
        }
        if (actionId != null) {
            im.getPersistentDataContainer().set(actionKey(), PersistentDataType.STRING, actionId);
        }
        im.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_POTION_EFFECTS);
        it.setItemMeta(im);
    }
    return it;
}

// === Extract the action cleanly (from PDC)
private String extractId(ItemStack it) {
    if (it == null) return null;
    ItemMeta im = it.getItemMeta();
    if (im == null) return null;
    return im.getPersistentDataContainer().get(actionKey(), PersistentDataType.STRING);
}

private static boolean valid(ItemStack it) {
    return it != null && it.getType() != Material.AIR;
}

private static String gray(String s) {
    return ChatColor.translateAlternateColorCodes('&', s);
}

// === Messaging ===
private void msg(Player p, String coloredMsg) {
    String prefix = messages.getOrDefault("messages.prefix", "&3[ProShield]&r ");
    p.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + coloredMsg));
}

private void warn(Player p, String coloredMsg) {
    String prefix = messages.getOrDefault("messages.prefix", "&3[ProShield]&r ");
    p.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + coloredMsg));
}

// === Sounds ===
private void click(Player p) {
    p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.6f, 1.25f);
}

private void soundGood(Player p) {
    p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.9f, 1.2f);
}

private void deny(Player p) {
    warn(p, messages.getOrDefault("messages.error.no-permission",
            "&cYou don’t have permission for this action."));
    click(p);
}

// === Utilities ===
private String nameOrShort(UUID u) {
    OfflinePlayer op = Bukkit.getOfflinePlayer(u);
    String n = (op != null ? op.getName() : null);
    return (n == null || n.isBlank()) ? u.toString().substring(0, 8) : n;
}

private int safeInt(String s, int def) {
    try { return Integer.parseInt(s); } catch (Exception e) { return def; }
}

private UUID uuidSafe(String s) {
    try { return UUID.fromString(s); } catch (Exception e) { return null; }
}

    // ------------------------------ View Stack ------------------------------

/** Pushes a new view onto the player's navigation stack. */
private void push(Player p, View v) {
    nav.computeIfAbsent(p.getUniqueId(), k -> new ArrayDeque<>()).push(v);
}

/** Replaces the top view with a new one (used for refresh). */
private void replaceTop(Player p, View v) {
    Deque<View> st = nav.computeIfAbsent(p.getUniqueId(), k -> new ArrayDeque<>());
    if (!st.isEmpty()) st.pop();
    st.push(v);
}

/** Peeks at the current view without modifying the stack. */
private View peek(Player p) {
    Deque<View> st = nav.get(p.getUniqueId());
    return (st == null || st.isEmpty()) ? null : st.peek();
}

/** Assigns a target for role assignment and pushes the role view. */
public void setPendingTarget(Player p, UUID target) {
    push(p, View.assignRole(target));
}

/** Clears the player's navigation stack completely. */
public void clearNav(Player p) {
    Deque<View> st = nav.get(p.getUniqueId());
    if (st != null) st.clear();
}

/** Navigates back to the previous view (or Claim Info if none). */
private void back(Player p) {
    Deque<View> st = nav.get(p.getUniqueId());
    if (st == null || st.size() <= 1) {
        openClaimInfo(p);
        return;
    }

    st.pop(); // discard current view
    View prev = st.peek();

    if (prev == null) {
        openClaimInfo(p);
        return;
    }

    switch (prev.type) {
        case "MAIN"           -> openMainMenu(p);
        case "CLAIMINFO"      -> openClaimInfo(p);
        case "TRUSTED"        -> openTrusted(p, prev.page);
        case "ASSIGNROLE"     -> openAssignRole(p);
        case "FLAGS"          -> openFlags(p, prev.page);
        case "ADMIN"          -> openAdmin(p);
        case "WORLDS"         -> openWorldControls(p, prev.page);
        case "WORLDDETAIL"    -> openWorldDetail(p, prev.world);
        case "PENDING"        -> openPending(p, prev.page);
        case "HISTORY"        -> openHistory(p);
        case "EXPANSION_MENU" -> openClaimInfo(p); // expansion menu always returns to claim info
        default               -> openClaimInfo(p);
    }
}

/** Wrapper class representing a UI view in the navigation stack. */
private static class View {
    final String type;
    final int page;
    final String world;
    final UUID pendingTarget;

    private View(String type, int page, String world, UUID target) {
        this.type = type;
        this.page = page;
        this.world = world;
        this.pendingTarget = target;
    }

    // Factory methods for readability
    static View main()                { return new View("MAIN", 0, null, null); }
    static View claimInfo()           { return new View("CLAIMINFO", 0, null, null); }
    static View trusted(int page)     { return new View("TRUSTED", page, null, null); }
    static View assignRole(UUID t)    { return new View("ASSIGNROLE", 0, null, t); }
    static View flags(int page)       { return new View("FLAGS", page, null, null); }
    static View admin()               { return new View("ADMIN", 0, null, null); }
    static View worlds(int page)      { return new View("WORLDS", page, null, null); }
    static View worldDetail(String w) { return new View("WORLDDETAIL", 0, w, null); }
    static View pending(int page)     { return new View("PENDING", page, null, null); }
    static View history()             { return new View("HISTORY", 0, null, null); }
    static View expansionMenu()       { return new View("EXPANSION_MENU", 0, null, null); }
}


    // --------------------------- World Controls I/O ---------------------------

/** Reads a boolean toggle for a specific world+key with a safe default. */
private boolean readWorldBool(String world, String key, boolean def) {
    return plugin.getConfig().getBoolean("worlds." + world + "." + key, def);
}

/** Writes a boolean toggle into config and persists it immediately. */
private void writeWorldBool(String world, String key, boolean val) {
    plugin.getConfig().set("worlds." + world + "." + key, val);
    plugin.saveConfig();
}

/** Reads all toggles for a given world (with defaults if missing). */
private Map<String, Boolean> readAllWorldBools(String world) {
    Map<String, Boolean> map = new LinkedHashMap<>();
    String basePath = "worlds." + world;

    if (plugin.getConfig().isConfigurationSection(basePath)) {
        for (String k : plugin.getConfig().getConfigurationSection(basePath).getKeys(false)) {
            map.put(k, plugin.getConfig().getBoolean(basePath + "." + k, defaultWorld(k)));
        }
    } else {
        // ensure at least core toggles exist
        map.put(WC_PVP, defaultWorld(WC_PVP));
        map.put(WC_SAFEZONE, defaultWorld(WC_SAFEZONE));
    }
    return map;
}

/** Provides default values for world-control keys (when not present in config). */
    private boolean defaultWorld(String key) {
        return switch (key.toLowerCase(Locale.ROOT)) {
            case "pvp"              -> true;
            case "safezone"         -> false;
            case "explosions"       -> true;
            case "fire-spread"      -> true;
            case "fire-burn"        -> true;
            case "block-break"      -> true;
            case "block-place"      -> true;
            case "containers"       -> true;
            case "bucket-use"       -> true;
            case "mob-spawn"        -> true;
            case "mob-damage"       -> true;
            case "ignite-flint"     -> true;
            case "ignite-lava"      -> true;
            case "ignite-lightning" -> true;
            case "crop-trample"     -> true;
            default                 -> false; // safe fallback
        };
    }
} // ← add this to close GUIManager

