package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.expansions.ExpansionRequest;
import com.snazzyatoms.proshield.expansions.ExpansionRequestManager;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
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

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final ClaimRoleManager roleManager;
    private final ExpansionRequestManager expansionManager;
    private final MessagesUtil messages;

    // State maps
    private final Map<UUID, UUID> pendingDenyTarget = new HashMap<>();
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
     * TRUSTED PLAYERS
     * ============================ */
    // (unchanged trusted player + assign role code – same as your version)

    /* ============================
     * CLAIM FLAGS
     * ============================ */
    // (unchanged flags code – same as your version)

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

    /* ============================
     * EXPANSION REVIEW + DENY REASONS
     * ============================ */
    public void openExpansionReview(Player admin) {
        String title = "&eExpansion Requests";
        Inventory inv = Bukkit.createInventory(admin, 45, messages.color(title));

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

                inv.setItem(slot++, simpleItem(Material.LIME_WOOL, "&aApprove: " + name, lore.toArray(new String[0])));
                if (slot < inv.getSize()) {
                    inv.setItem(slot++, simpleItem(Material.RED_WOOL, "&cDeny: " + name, lore.toArray(new String[0])));
                }
            }
        }

        placeNavButtons(inv);
        admin.openInventory(inv);
    }

    private void openDenyReasons(Player admin, UUID target) {
        String title = "&cDeny Reasons";
        Inventory inv = Bukkit.createInventory(admin, 27, messages.color(title));

        ConfigurationSection sec = plugin.getConfig().getConfigurationSection("messages.deny-reasons");
        if (sec != null) {
            int slot = 0;
            for (String key : sec.getKeys(false)) {
                String reason = plugin.getConfig().getString("messages.deny-reasons." + key, "&c" + key);
                inv.setItem(slot++, simpleItem(Material.PAPER, "&fReason: " + key, "&7" + reason));
                if (slot >= inv.getSize() - 9) break;
            }
        } else {
            inv.setItem(13, simpleItem(Material.BARRIER, "&7No reasons configured",
                    "&7Add messages.deny-reasons.* in config.yml"));
        }

        pendingDenyTarget.put(admin.getUniqueId(), target);
        placeNavButtons(inv);
        admin.openInventory(inv);
    }

    /* ============================
     * EXPANSION HISTORY (with pagination)
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

            // Status handling
            String status = req instanceof ExpansionRequest er && er.getStatus() != null
                    ? er.getStatus().name()
                    : (req.isApproved() ? "APPROVED" : "DENIED");

            lore.add("&7Status: &f" + status);
            Material icon = switch (status.toUpperCase(Locale.ROOT)) {
                case "APPROVED" -> Material.LIME_DYE;
                case "DENIED" -> Material.RED_DYE;
                case "EXPIRED" -> Material.GRAY_DYE;
                default -> Material.YELLOW_DYE;
            };

            inv.setItem(i - start, simpleItem(icon, "&f" + name, lore.toArray(new String[0])));
        }

        // Pagination buttons
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
