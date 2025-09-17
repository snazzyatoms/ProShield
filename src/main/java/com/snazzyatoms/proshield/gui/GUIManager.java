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

public class GUIManager {

    private static final String HIDDEN_UUID_TAG = "§8#UUID:";
    private static final String HIDDEN_TS_TAG = "§8#TS:";

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final ClaimRoleManager roleManager;
    private final ExpansionRequestManager expansionManager;
    private final MessagesUtil messages;

    // State maps
    private final Map<UUID, UUID> pendingRoleAssignments = new HashMap<>();
    private final Map<UUID, Integer> historyPages = new HashMap<>();
    private final Map<UUID, List<ExpansionRequest>> filteredHistory = new HashMap<>();

    private static final int HISTORY_PER_PAGE = 18;

    public GUIManager(ProShield plugin) {
        this.plugin = plugin;
        this.plotManager = plugin.getPlotManager();
        this.roleManager = plugin.getRoleManager();
        this.expansionManager = plugin.getExpansionRequestManager();
        this.messages = plugin.getMessagesUtil();
    }

    /* ---------- Utility Buttons ---------- */
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
            List<String> colored = new ArrayList<>();
            for (String l : lore) colored.add(messages.color(l));
            meta.setLore(colored);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        return item;
    }

    private boolean isNamed(ItemStack item, String needle) {
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return false;
        return ChatColor.stripColor(item.getItemMeta().getDisplayName()).equalsIgnoreCase(needle);
    }

    private boolean isBack(ItemStack item) { return isNamed(item, "Back"); }
    private boolean isExit(ItemStack item) { return isNamed(item, "Exit"); }

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

        inv.setItem(32, simpleItem(Material.COMMAND_BLOCK, "&cAdmin Tools", "&7Admin-only controls."));

        placeNavButtons(inv);
        player.openInventory(inv);
    }

    private ItemStack buildClaimInfoItem(Player player) {
        Plot plot = plotManager.getPlotAt(player.getLocation());
        List<String> lore = new ArrayList<>();
        if (plot == null) {
            lore.add("&7No claim here.");
        } else {
            OfflinePlayer owner = plugin.getServer().getOfflinePlayer(plot.getOwner());
            String ownerName = owner.getName() != null ? owner.getName() : owner.getUniqueId().toString();
            lore.add("&7World: &f" + plot.getWorld());
            lore.add("&7Chunk: &f" + plot.getX() + ", " + plot.getZ());
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
        }

        placeNavButtons(inv);
        player.openInventory(inv);
    }

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
            roleManager.setRole(plot, targetUuid, ClaimRole.NONE);
            String name = Optional.ofNullable(Bukkit.getOfflinePlayer(targetUuid).getName())
                    .orElse(targetUuid.toString().substring(0, 8));
            messages.send(player, "&cUntrusted &f" + name);
            openTrusted(player);
        }
    }

    private void openAssignRole(Player actor, UUID targetUuid) {
        String title = plugin.getConfig().getString("gui.menus.assign-role.title", "&bAssign Role");
        int size = plugin.getConfig().getInt("gui.menus.assign-role.size", 45);
        Inventory inv = Bukkit.createInventory(actor, size, messages.color(title));

        int slot = 0;
        for (ClaimRole role : ClaimRole.values()) {
            if (role == ClaimRole.NONE || role == ClaimRole.OWNER) continue;
            List<String> lore = new ArrayList<>();
            lore.add(messages.color("&7Click to assign this role"));
            ItemStack item = simpleItem(Material.BOOK, role.getDisplayName(), lore.toArray(new String[0]));
            inv.setItem(slot++, item);
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

        if (role == ClaimRole.NONE || role == ClaimRole.OWNER) {
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
     * EXPANSION REVIEW
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

    private void openDenyReasons(Player admin, UUID target, Instant ts) {
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

    /* ============================
     * ADMIN TOOLS, FLAGS, HISTORY
     * (unchanged from your last version)
     * ============================ */
    // ... keep your Admin Tools, Flags, and History menus here (unchanged)
}
