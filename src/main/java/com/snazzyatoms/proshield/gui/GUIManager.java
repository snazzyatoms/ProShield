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
import java.util.stream.Collectors;

public class GUIManager {
    private static final String TAG_UUID = "#UUID:";
    private static final String TAG_TS   = "#TS:";

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
    public boolean isBack(ItemStack item) { return isNamed(item, "Back"); }
    public boolean isExit(ItemStack item) { return isNamed(item, "Exit"); }

    private UUID extractHiddenUuid(ItemStack item) {
        if (item == null || !item.hasItemMeta() || item.getItemMeta().getLore() == null) return null;
        for (String line : item.getItemMeta().getLore()) {
            String raw = ChatColor.stripColor(line);
            if (raw != null && raw.startsWith(TAG_UUID)) {
                try { return UUID.fromString(raw.substring(TAG_UUID.length()).trim()); }
                catch (Exception ignored) {}
            }
        }
        return null;
    }

    private Instant extractHiddenTimestamp(ItemStack item) {
        if (item == null || !item.hasItemMeta() || item.getItemMeta().getLore() == null) return null;
        for (String line : item.getItemMeta().getLore()) {
            String raw = ChatColor.stripColor(line);
            if (raw != null && raw.startsWith(TAG_TS)) {
                try { return Instant.parse(raw.substring(TAG_TS.length()).trim()); }
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
            openPlayerExpansionRequest(player);

        } else if (name.contains("admin tools")) {
            openAdminTools(player);

        } else if (isBack(clicked)) {
            openMain(player);
        } else if (isExit(clicked)) {
            player.closeInventory();
        }
    }

    /* ============================
     * PLAYER EXPANSION REQUEST
     * ============================ */
    public void openPlayerExpansionRequest(Player player) {
        // ✅ Fixed: matches config.yml → gui.menus.expansion-request
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
        if (isBack(clicked)) { openMain(player); return; }
        if (isExit(clicked)) { player.closeInventory(); return; }
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
                expansionManager.createRequest(player.getUniqueId(), Math.max(1, current));
                // ✅ Fixed: uses messages.yml
                messages.send(player, messages.get("messages.expansion.request-sent")
                        .replace("{blocks}", String.valueOf(Math.max(1, current))));
                pendingPlayerExpansionAmount.remove(player.getUniqueId());
                openMain(player);
                return;
            }
        }
        pendingPlayerExpansionAmount.put(player.getUniqueId(), current);
        openPlayerExpansionRequest(player);
    }

    /* ============================
     * DENY REASONS (fix config→messages.yml)
     * ============================ */
    public void openDenyReasons(Player admin, UUID target, Instant ts) {
        String title = plugin.getConfig().getString("gui.menus.deny-reasons.title", "&cDeny Reasons");
        int size = plugin.getConfig().getInt("gui.menus.deny-reasons.size", 27);
        Inventory inv = Bukkit.createInventory(admin, size, messages.color(title));

        // ✅ Fixed: now pulls from messages.yml
        Set<String> keys = messages.getKeys("messages.deny-reasons");
        if (keys != null && !keys.isEmpty()) {
            int slot = 0;
            for (String key : keys) {
                String reason = messages.get("messages.deny-reasons." + key, key);
                inv.setItem(slot++, simpleItem(
                        Material.PAPER,
                        "&fReason: " + key,
                        "&7" + ChatColor.stripColor(messages.color(reason)),
                        TAG_UUID + target,
                        TAG_TS + ts
                ));
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

        // ✅ Fixed: use messages.yml for denial reason
        String reasonMsg = messages.get("messages.deny-reasons." + key, key);

        boolean ok = denyByKey(target, ts, admin.getUniqueId(), reasonMsg);
        String who = Optional.ofNullable(Bukkit.getOfflinePlayer(target).getName())
                .orElse(target.toString().substring(0, 8));
        if (ok) {
            messages.send(admin, "&cDenied expansion for &f" + who + " &7(" + key + ")");
        } else {
            messages.send(admin, "&cUnable to locate pending request for &f" + who);
        }
        openExpansionReview(admin);
    }
}
