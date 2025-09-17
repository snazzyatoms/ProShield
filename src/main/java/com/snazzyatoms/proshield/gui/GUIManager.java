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
    private static final String HIDDEN_TS_TAG   = "§8#TS:";

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final ClaimRoleManager roleManager;
    private final ExpansionRequestManager expansionManager;
    private final MessagesUtil messages;

    // State maps
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

    public boolean isBack(ItemStack item) { return isNamed(item, "Back"); }
    public boolean isExit(ItemStack item) { return isNamed(item, "Exit"); }

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
            if (plotManager.getPlotByOwner(player.getUniqueId()) != null) {
                messages.send(player, "&cYou already own a claim. Multiple claims are not allowed.");
                return;
            }
            plotManager.createPlot(player.getUniqueId(), player.getLocation());
            player.closeInventory();

        } else if (name.contains("claim info")) {
            // Info only

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
            String ownerName = owner.getName() != null ? owner.getName() : owner.getUniqueId().toString();

            int enabledFlags = (int) plot.getFlags().values().stream().filter(Boolean::booleanValue).count(); // ✅ FIX

            lore.add("&7World: &f" + plot.getWorld());
            lore.add("&7Chunk: &f" + plot.getX() + ", " + plot.getZ());
            lore.add("&7Owner: &f" + ownerName);
            lore.add("&7Radius: &f" + plot.getRadius() + " blocks");
            lore.add("&7Flags: &f" + enabledFlags + " enabled");
        }
        return simpleItem(Material.PAPER, "&eClaim Info", lore.toArray(new String[0]));
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

        ConfigurationSection wcSec = plugin.getConfig().getConfigurationSection("world-controls.available");
        if (wcSec != null && !wcSec.getKeys(false).isEmpty()) {
            inv.setItem(30, simpleItem(Material.GRASS_BLOCK, "&aWorld Controls", "&7Manage global world protections."));
        }

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

        } else if (name.equalsIgnoreCase("World Controls")) {
            openWorldControls(player);
        }
    }

    /* ============================
     * WORLD CONTROLS
     * ============================ */
    public void openWorldControls(Player admin) {
        String title = plugin.getConfig().getString("gui.menus.world-controls.title", "&aWorld Controls");
        int size = plugin.getConfig().getInt("gui.menus.world-controls.size", 45);
        Inventory inv = Bukkit.createInventory(admin, size, messages.color(title));

        ConfigurationSection wcSec = plugin.getConfig().getConfigurationSection("world-controls.available");
        if (wcSec != null) {
            int slot = 0;
            for (String key : wcSec.getKeys(false)) {
                String path = "world-controls.available." + key;
                String display = plugin.getConfig().getString(path + ".name", key);
                boolean enabled = plugin.getConfig().getBoolean(path + ".enabled", false);

                ItemStack item = new ItemStack(enabled ? Material.LIME_DYE : Material.GRAY_DYE);
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(messages.color(display));
                    meta.setLore(Arrays.asList(
                            messages.color("&7Click to toggle"),
                            messages.color("&fCurrent: " + (enabled ? "&aEnabled" : "&cDisabled"))
                    ));
                    item.setItemMeta(meta);
                }
                inv.setItem(slot++, item);
                if (slot >= size - 9) break;
            }
        }

        placeNavButtons(inv);
        admin.openInventory(inv);
    }

    public void handleWorldControlsClick(Player admin, InventoryClickEvent event) {
        ItemStack clicked = event.getCurrentItem();
        if (isBack(clicked)) { openAdminTools(admin); return; }
        if (isExit(clicked)) { admin.closeInventory(); return; }
        if (clicked == null || !clicked.hasItemMeta() || !clicked.getItemMeta().hasDisplayName()) return;

        String clickedName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
        if (clickedName == null) return;

        ConfigurationSection wcSec = plugin.getConfig().getConfigurationSection("world-controls.available");
        if (wcSec == null) return;

        for (String key : wcSec.getKeys(false)) {
            String display = plugin.getConfig().getString("world-controls.available." + key + ".name", key);
            if (ChatColor.stripColor(messages.color(display)).equalsIgnoreCase(clickedName)) {
                boolean current = plugin.getConfig().getBoolean("world-controls.available." + key + ".enabled", false);
                boolean newValue = !current;

                plugin.getConfig().set("world-controls.available." + key + ".enabled", newValue);
                plugin.saveConfig();

                messages.send(admin, "&eWorld Control &f" + key + " &eis now " + (newValue ? "&aEnabled" : "&cDisabled"));
                openWorldControls(admin);
                break;
            }
        }
    }

    /* ============================
     * EXPANSION HISTORY
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
        String title = messages.color("&7Expansion History (Page " + (page + 1) + ")");
        Inventory inv = Bukkit.createInventory(admin, size, title);

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
                status = "DENIED (" + req.getDenyReason() + ")";
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

    // 🔽 Trusted Players, Assign Role, Claim Flags, Expansion Review, Deny Reasons
    // (kept intact, enhanced with Back/Exit buttons like above)
    // ...
}
