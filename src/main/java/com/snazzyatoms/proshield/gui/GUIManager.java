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
 * GUIManager
 * - Main menu (Claim/Info/Unclaim/Trusted/Flags/Request Expansion/Admin Tools*)
 * - Trusted players & role assignment
 * - Claim flags
 * - Admin tools (*only for users with "proshield.admin")
 * - Expansion review (approve/deny + deny reasons)
 * - Expansion history (paginated)
 * - Back/Exit buttons are present & functional in every menu
 * - Enforces one-claim-per-player (multi-claim planned for v2.0)
 */
public class GUIManager {

    private static final String HIDDEN_UUID_TAG = "§8#UUID:";
    private static final String HIDDEN_TS_TAG   = "§8#TS:";

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final ClaimRoleManager roleManager;
    private final ExpansionRequestManager expansionManager;
    private final MessagesUtil messages;

    // Transient GUI state
    private final Map<UUID, UUID> pendingRoleAssignments = new HashMap<>();
    private final Map<UUID, Integer> historyPages = new HashMap<>();
    private final Map<UUID, List<ExpansionRequest>> filteredHistory = new HashMap<>();
    private final Map<UUID, UUID> pendingDenyTarget = new HashMap<>();

    private static final int HISTORY_PER_PAGE = 18;

    public GUIManager(ProShield plugin) {
        this.plugin = plugin;
        this.plotManager = plugin.getPlotManager();
        this.roleManager = plugin.getRoleManager();
        this.expansionManager = plugin.getExpansionRequestManager();
        this.messages = plugin.getMessagesUtil();
    }

    /* ---------- Item helpers ---------- */
    private ItemStack backButton() { return simpleItem(Material.ARROW, "&eBack", "&7Return to previous menu"); }
    private ItemStack exitButton() { return simpleItem(Material.BARRIER, "&cExit", "&7Close this menu"); }

    private void placeNavButtons(Inventory inv) {
        int size = inv.getSize();
        inv.setItem(size - 9, backButton());
        inv.setItem(size - 1, exitButton());
    }

    private ItemStack simpleItem(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(messages.color(name));
            if (lore != null && lore.length > 0) {
                List<String> colored = new ArrayList<>(lore.length);
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

    public boolean isBack(ItemStack item) { return isNamed(item, "Back"); }
    public boolean isExit(ItemStack item) { return isNamed(item, "Exit"); }

    private UUID extractHiddenUuid(ItemStack item) {
        if (item == null || !item.hasItemMeta() || item.getItemMeta().getLore() == null) return null;
        for (String line : item.getItemMeta().getLore()) {
            String raw = ChatColor.stripColor(line);
            if (raw != null && raw.startsWith("#UUID:")) {
                try { return UUID.fromString(raw.substring("#UUID:".length()).trim()); }
                catch (Exception ignored) {}
            }
        }
        return null;
    }

    private Instant extractHiddenTimestamp(ItemStack item) {
        if (item == null || !item.hasItemMeta() || item.getItemMeta().getLore() == null) return null;
        for (String line : item.getItemMeta().getLore()) {
            String raw = ChatColor.stripColor(line);
            if (raw != null && raw.startsWith("#TS:")) {
                try { return Instant.parse(raw.substring("#TS:".length()).trim()); }
                catch (Exception ignored) {}
            }
        }
        return null;
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

        // Admin tools only for admins
        if (player.hasPermission("proshield.admin")) {
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
            // one-claim-per-player (v1.x)
            if (plotManager.getPlotByOwner(player.getUniqueId()) != null) {
                messages.send(player, "&cYou already own a claim. Multiple claims are not allowed.");
                return;
            }
            plotManager.createPlot(player.getUniqueId(), player.getLocation());
            player.closeInventory();

        } else if (name.contains("unclaim")) {
            Plot plot = plotManager.getPlotAt(player.getLocation());
            if (plot != null && (plot.getOwner().equals(player.getUniqueId()) || player.hasPermission("proshield.admin"))) {
                plotManager.deletePlot(plot.getId());
                messages.send(player, "&cYour claim has been unclaimed.");
            } else {
                messages.send(player, "&cYou are not the owner of this claim.");
            }
            player.closeInventory();

        } else if (name.contains("trusted players")) {
            openTrusted(player);

        } else if (name.contains("claim flags")) {
            openFlags(player);

        } else if (name.contains("request expansion")) {
            expansionManager.openPlayerRequestMenu(player);

        } else if (name.contains("admin tools")) {
            if (player.hasPermission("proshield.admin")) {
                openAdminTools(player);
            } else {
                messages.send(player, "&cYou don’t have permission to use Admin Tools.");
            }

        } else if (isBack(clicked)) {
            openMain(player);

        } else if (isExit(clicked)) {
            player.closeInventory();
        }
    }

    private ItemStack buildClaimInfoItem(Player player) {
        Plot plot = plotManager.getPlotAt(player.getLocation());
        List<String> lore = new ArrayList<>();
        if (plot == null) {
            lore.add("&7No claim here.");
        } else {
            OfflinePlayer owner = plugin.getServer().getOfflinePlayer(plot.getOwner());
            String ownerName = owner != null && owner.getName() != null ? owner.getName() : plot.getOwner().toString();
            lore.add("&7World: &f" + plot.getWorld());
            lore.add("&7Center: &f" + plot.getX() + ", " + plot.getZ());
            lore.add("&7Owner: &f" + ownerName);
            lore.add("&7Radius: &f" + plot.getRadius() + " blocks");
            lore.add("&7Flags: &f" + plot.getFlags().size());
        }
        return simpleItem(Material.PAPER, "&eClaim Info", lore.toArray(new String[0]));
    }

    /* ============================
     * TRUSTED PLAYERS + ASSIGN ROLE
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
            String display = (trusted != null && trusted.getName() != null)
                    ? trusted.getName()
                    : uuid.toString().substring(0, 8);

            ClaimRole role = roleManager.getRole(uuid, plot);
            List<String> lore = new ArrayList<>();
            lore.add(messages.color("&7Role: &b" + role.getDisplayName()));
            lore.add(messages.color("&aLeft-click: Assign new role"));
            lore.add(messages.color("&cRight-click: Untrust"));
            lore.add(HIDDEN_UUID_TAG + uuid);

            ItemStack head = simpleItem(Material.PLAYER_HEAD, "&f" + display, lore.toArray(new String[0]));
            inv.setItem(slot++, head);
            if (slot >= size - 9) break;
        }

        placeNavButtons(inv);
        player.openInventory(inv);
    }

    public void handleTrustedClick(Player player, InventoryClickEvent event) {
        ItemStack clicked = event.getCurrentItem();
        if (isBack(clicked)) { openMain(player); return; }
        if (isExit(clicked)) { player.closeInventory(); return; }
        if (clicked == null || !clicked.hasItemMeta()) return;

        Plot plot = plotManager.getPlotAt(player.getLocation());
        if (plot == null) return;

        UUID targetUuid = extractHiddenUuid(clicked);
        if (targetUuid == null) return;

        if (event.isLeftClick()) {
            openAssignRole(player, targetUuid);
        } else if (event.isRightClick()) {
            roleManager.setRole(plot, targetUuid, ClaimRole.NONE); // unified removal
            String name = Optional.ofNullable(Bukkit.getOfflinePlayer(targetUuid).getName())
                    .orElse(targetUuid.toString().substring(0, 8));
            messages.send(player, "&cUntrusted &f" + name);
            openTrusted(player);
        }
    }

    public void openAssignRole(Player actor, UUID targetUuid) {
        String title = plugin.getConfig().getString("gui.menus.assign-role.title", "&bAssign Role");
        int size = plugin.getConfig().getInt("gui.menus.assign-role.size", 45);
        Inventory inv = Bukkit.createInventory(actor, size, messages.color(title));

        int slot = 0;
        for (ClaimRole role : ClaimRole.values()) {
            if (role == ClaimRole.NONE || role == ClaimRole.OWNER) continue; // skip non-assignables
            List<String> lore = new ArrayList<>();
            lore.add(messages.color("&7Click to assign this role"));
            ItemStack item = simpleItem(Material.BOOK, role.getDisplayName(), lore.toArray(new String[0]));
            inv.setItem(slot++, item);
            if (slot >= size - 9) break;
        }

        placeNavButtons(inv);
        actor.openInventory(inv);
        pendingRoleAssignments.put(actor.getUniqueId(), targetUuid);
    }

    public void handleAssignRoleClick(Player player, InventoryClickEvent event) {
        ItemStack clicked = event.getCurrentItem();
        if (isBack(clicked)) { clearPendingRoleAssignment(player.getUniqueId()); openTrusted(player); return; }
        if (isExit(clicked)) { clearPendingRoleAssignment(player.getUniqueId()); player.closeInventory(); return; }

        UUID targetUuid = pendingRoleAssignments.remove(player.getUniqueId());
        if (targetUuid == null) return;

        if (clicked == null || !clicked.hasItemMeta()) return;
        String roleName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
        ClaimRole role = ClaimRole.fromName(roleName);

        if (role == ClaimRole.NONE || role == ClaimRole.OWNER || role == null) {
            messages.send(player, "&cInvalid role selected.");
            openTrusted(player);
            return;
        }

        Plot plot = plotManager.getPlotAt(player.getLocation());
        if (plot != null) {
            roleManager.setRole(plot, targetUuid, role);
            messages.send(player, "&aAssigned role &f" + role.getDisplayName() + " &ato target.");
        }
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
                boolean current = plot.getFlags().getOrDefault(key, plugin.getConfig().getBoolean(path + ".default", false));

                ItemStack item = new ItemStack(current ? Material.LIME_DYE : Material.GRAY_DYE);
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(messages.color(displayName));
                    meta.setLore(Arrays.asList(
                            messages.color("&7Click to toggle"),
                            messages.color("&fCurrent: " + (current ? "&aEnabled" : "&cDisabled"))
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
        if (isBack(clicked)) { openMain(player); return; }
        if (isExit(clicked)) { player.closeInventory(); return; }

        Plot plot = plotManager.getPlotAt(player.getLocation());
        if (plot == null) return;
        if (clicked == null || !clicked.hasItemMeta() || !clicked.getItemMeta().hasDisplayName()) return;

        String clickedName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
        if (clickedName == null) return;

        ConfigurationSection avail = plugin.getConfig().getConfigurationSection("flags.available");
        if (avail == null) return;

        for (String key : avail.getKeys(false)) {
            String display = plugin.getConfig().getString("flags.available." + key + ".name", key);
            if (ChatColor.stripColor(messages.color(display)).equalsIgnoreCase(clickedName)) {
                boolean current = plot.getFlags().getOrDefault(key,
                        plugin.getConfig().getBoolean("flags.available." + key + ".default", false));
                boolean newValue = !current;
                plot.getFlags().put(key, newValue);
                messages.send(player, "&eFlag &f" + key + " &eis now " + (newValue ? "&aEnabled" : "&cDisabled"));
                plotManager.saveAll();
                openFlags(player);
                break;
            }
        }
    }

    /* ============================
     * ADMIN TOOLS
     * ============================ */
    public void openAdminTools(Player player) {
        String title = plugin.getConfig().getString("gui.menus.admin-tools.title", "&cAdmin Tools");
        int size = plugin.getConfig().getInt("gui.menus.admin-tools.size", 45);
        Inventory inv = Bukkit.createInventory(player, size, messages.color(title));

        inv.setItem(10, simpleItem(Material.REPEATER, "&eReload Configs", "&7Reload ProShield configs."));
        inv.setItem(12, simpleItem(Material.ENDER_EYE, "&aToggle Debug", "&7Enable/disable debug logging."));
        inv.setItem(14, simpleItem(Material.BARRIER, "&cToggle Bypass", "&7Admin bypass for claims."));
        inv.setItem(16, simpleItem(Material.EMERALD, "&eExpansion Requests", "&7Review pending player requests."));
        inv.setItem(28, simpleItem(Material.CLOCK, "&eExpansion History", "&7View past requests."));

        placeNavButtons(inv);
        player.openInventory(inv);
    }

    public void handleAdminClick(Player player, InventoryClickEvent event) {
        ItemStack clicked = event.getCurrentItem();
        if (isBack(clicked)) { openMain(player); return; }
        if (isExit(clicked)) { player.closeInventory(); return; }
        if (clicked == null || !clicked.hasItemMeta()) return;

        String name = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
        if (name == null) return;

        if (name.equalsIgnoreCase("Reload Configs")) {
            plugin.reloadConfig();
            plugin.loadMessagesConfig();
            messages.send(player, "&aConfigs reloaded.");

        } else if (name.equalsIgnoreCase("Toggle Debug")) {
            plugin.toggleDebug();
            messages.send(player, "&eDebug mode: " + (plugin.isDebugEnabled() ? "&aENABLED" : "&cDISABLED"));

        } else if (name.equalsIgnoreCase("Toggle Bypass")) {
            UUID uuid = player.getUniqueId();
            if (plugin.isBypassing(uuid)) {
                plugin.getBypassing().remove(uuid);
                messages.send(player, "&cBypass disabled.");
            } else {
                plugin.getBypassing().add(uuid);
                messages.send(player, "&aBypass enabled.");
            }

        } else if (name.equalsIgnoreCase("Expansion Requests")) {
            openExpansionReview(player);

        } else if (name.equalsIgnoreCase("Expansion History")) {
            openFilteredHistory(player, new ArrayList<>(expansionManager.getAllRequests()));
        }
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

                List<String> lore = new ArrayList<>();
                lore.add("&7Blocks: &f" + req.getAmount());
                lore.add("&7When: &f" + fmt.format(req.getTimestamp()));
                lore.add("&aLeft-click to approve");
                lore.add("&cRight-click to deny");
                lore.add(HIDDEN_UUID_TAG + requester);
                lore.add(HIDDEN_TS_TAG + req.getTimestamp());

                inv.setItem(slot++, simpleItem(Material.LIME_WOOL, "&aApprove: " + name, lore.toArray(new String[0])));
                if (slot < size - 9) {
                    inv.setItem(slot++, simpleItem(Material.RED_WOOL, "&cDeny: " + name, lore.toArray(new String[0])));
                }
                if (slot >= size - 9) break;
            }
        }

        placeNavButtons(inv);
        admin.openInventory(inv);
    }

    public void handleExpansionReviewClick(Player admin, InventoryClickEvent event) {
        ItemStack clicked = event.getCurrentItem();
        if (isBack(clicked)) { openAdminTools(admin); return; }
        if (isExit(clicked)) { admin.closeInventory(); return; }
        if (clicked == null || !clicked.hasItemMeta() || !clicked.getItemMeta().hasDisplayName()) return;

        String dn = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
        if (dn == null || dn.equalsIgnoreCase("No Pending Requests")) return;

        UUID target = extractHiddenUuid(clicked);
        Instant ts = extractHiddenTimestamp(clicked);
        if (target == null || ts == null) return;

        if (dn.startsWith("Approve: ")) {
            expansionManager.approve(target, ts, admin.getUniqueId());
            String who = Optional.ofNullable(Bukkit.getOfflinePlayer(target).getName())
                    .orElse(target.toString().substring(0, 8));
            messages.send(admin, "&aApproved expansion for &f" + who);
            openExpansionReview(admin);

        } else if (dn.startsWith("Deny: ")) {
            openDenyReasons(admin, target, ts);
        }
    }

    public void openDenyReasons(Player admin, UUID target, Instant ts) {
        String title = plugin.getConfig().getString("gui.menus.deny-reasons.title", "&cDeny Reasons");
        int size = plugin.getConfig().getInt("gui.menus.deny-reasons.size", 27);
        Inventory inv = Bukkit.createInventory(admin, size, messages.color(title));

        ConfigurationSection sec = plugin.getConfig().getConfigurationSection("messages.deny-reasons");
        if (sec != null) {
            int slot = 0;
            for (String key : sec.getKeys(false)) {
                String reason = plugin.getConfig().getString("messages.deny-reasons." + key, "&c" + key);
                inv.setItem(slot++, simpleItem(
                        Material.PAPER,
                        "&fReason: " + key,
                        "&7" + ChatColor.stripColor(messages.color(reason)),
                        HIDDEN_UUID_TAG + target,
                        HIDDEN_TS_TAG + ts
                ));
                if (slot >= size - 9) break;
            }
        } else {
            inv.setItem(13, simpleItem(Material.BARRIER, "&7No reasons configured",
                    "&7Add messages.deny-reasons.* in config.yml"));
        }

        pendingDenyTarget.put(admin.getUniqueId(), target);
        placeNavButtons(inv);
        admin.openInventory(inv);
    }

    public void handleDenyReasonClick(Player admin, InventoryClickEvent event) {
        ItemStack clicked = event.getCurrentItem();
        if (isBack(clicked)) { openExpansionReview(admin); return; }
        if (isExit(clicked)) { admin.closeInventory(); return; }
        if (clicked == null || !clicked.hasItemMeta() || !clicked.getItemMeta().hasDisplayName()) return;

        String dn = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
        if (dn == null || !dn.startsWith("Reason: ")) return;

        String key = dn.substring("Reason: ".length()).trim();
        if (key.isEmpty()) return;

        UUID target = extractHiddenUuid(clicked);
        Instant ts = extractHiddenTimestamp(clicked);
        if (target == null || ts == null) return;

        expansionManager.deny(target, ts, admin.getUniqueId(), key);
        String who = Optional.ofNullable(Bukkit.getOfflinePlayer(target).getName())
                .orElse(target.toString().substring(0, 8));
        messages.send(admin, "&cDenied expansion for &f" + who + " &7(" + key + ")");
        openExpansionReview(admin);
    }

    public void clearPendingDenyTarget(UUID admin) {
        pendingDenyTarget.remove(admin);
    }

    /* ============================
     * EXPANSION HISTORY (pagination)
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
        String title = plugin.getConfig().getString("gui.menus.history.title", "&7Expansion History");
        Inventory inv = Bukkit.createInventory(admin, size, messages.color(title) + messages.color(" &7(Page " + (page + 1) + ")"));

        int start = page * HISTORY_PER_PAGE;
        int end = Math.min(start + HISTORY_PER_PAGE, history.size());
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());

        for (int i = start; i < end; i++) {
            ExpansionRequest req = history.get(i);
            OfflinePlayer p = Bukkit.getOfflinePlayer(req.getRequester());
            String name = (p != null && p.getName() != null) ? p.getName() : req.getRequester().toString();

            List<String> lore = new ArrayList<>();
            lore.add("&7Blocks: &f" + req.getAmount());
            lore.add("&7When: &f" + fmt.format(req.getTimestamp()));

            String status;
            Material icon;

            if (req.isApproved()) {
                status = "APPROVED";
                icon = Material.LIME_DYE;
            } else if (req.getStatus() == ExpansionRequest.Status.DENIED) {
                String reason = req.getDenialReason();
                status = (reason != null && !reason.isEmpty()) ? "DENIED (" + reason + ")" : "DENIED";
                icon = Material.RED_DYE;
            } else if (req.getStatus() == ExpansionRequest.Status.EXPIRED) {
                status = "EXPIRED";
                icon = Material.YELLOW_DYE;
            } else {
                status = "PENDING";
                icon = Material.ORANGE_DYE;
            }

            lore.add("&7Status: &f" + status);
            inv.setItem(i - start, simpleItem(icon, "&f" + name, lore.toArray(new String[0])));
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
            openFilteredHistoryPage(player);
        } else if ("Next Page".equalsIgnoreCase(name)) {
            historyPages.put(uuid, historyPages.getOrDefault(uuid, 0) + 1);
            openFilteredHistoryPage(player);
        } else if (isBack(clicked)) {
            openAdminTools(player);
        } else if (isExit(clicked)) {
            player.closeInventory();
        }
    }
}
