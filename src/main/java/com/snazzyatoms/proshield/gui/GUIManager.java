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

            String status = req.getStatus().name();
            lore.add("&7Status: &f" + status);

            Material icon;
            switch (status.toUpperCase(Locale.ROOT)) {
                case "APPROVED" -> icon = Material.LIME_DYE;
                case "DENIED" -> icon = Material.RED_DYE;
                case "EXPIRED" -> icon = Material.GRAY_DYE;
                default -> icon = Material.YELLOW_DYE; // PENDING
            }

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

    /* ============================
     * OTHER MENUS
     * ============================ */
    // Keep your Main, Trusted, Assign Role, Flags, Expansion Review, Admin Tools as-is.
}
