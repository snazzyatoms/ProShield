package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import com.snazzyatoms.proshield.expansions.ExpansionRequest;
import com.snazzyatoms.proshield.expansions.ExpansionRequestManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class GUIManager {

    private final ProShield plugin;
    private final MessagesUtil messages;
    private final PlotManager plotManager;
    private final ExpansionRequestManager requestManager;

    private final NamespacedKey ACTION_KEY;
    private final NamespacedKey ARG_KEY;

    public GUIManager(ProShield plugin) {
        this.plugin = plugin;
        this.messages = plugin.getMessagesUtil();
        this.plotManager = plugin.getPlotManager();
        this.requestManager = plugin.getExpansionRequestManager();

        this.ACTION_KEY = new NamespacedKey(plugin, "ps_action");
        this.ARG_KEY = new NamespacedKey(plugin, "ps_arg");
    }

    /* ====================================================================== */
    /* Inventory Builders                                                      */
    /* ====================================================================== */

    public void openMenu(Player player, String menuKey) {
        switch (menuKey.toLowerCase()) {
            case "main": openMain(player); return;
            case "roles": openRoles(player); return;
            case "roles-nearby": openRolesNearby(player); return;
            case "flags": openFlags(player); return;
            case "admin-tools": openAdminTools(player); return;
            case "expansion-requests": openExpansionRequests(player); return;
            case "expansion-request": openExpansionRequestMenu(player); return;
            case "expansion-deny": 
                // should not be called directly; submenu opened internally
                openExpansionRequests(player); return;
            default: openMain(player);
        }
    }

    private void openMain(Player p) {
        Inventory inv = makeInv("gui.menus.main.title", "&6ProShield Menu", 45);

        inv.setItem(11, button(Material.GRASS_BLOCK, "&aClaim Land",
                Arrays.asList("&7Protect your land", "&7Radius: &f" + plugin.getConfig().getInt("claims.default-radius", 50) + " &7blocks"),
                "cmd", "proshield claim"));
        inv.setItem(13, button(Material.PAPER, "&eClaim Info",
                Arrays.asList("&7Shows your current claim details", stateLineForCurrentClaim(p)),
                "cmd", "proshield info"));
        inv.setItem(15, button(Material.BARRIER, "&cUnclaim Land",
                Collections.singletonList("&7Remove your claim"), "cmd", "proshield unclaim"));
        inv.setItem(21, button(Material.PLAYER_HEAD, "&bTrusted Players",
                Arrays.asList("&7Manage who can build in your claim", "&7Add/Remove roles"),
                "menu", "roles"));
        inv.setItem(23, button(Material.CHEST, "&eClaim Flags",
                Arrays.asList("&7Toggle special protections", stateLineForCurrentClaim(p)),
                "menu", "flags"));

        if (plugin.getConfig().getBoolean("claims.expansion.enabled", true)) {
            inv.setItem(31, button(Material.EMERALD, "&aRequest Expansion",
                    Arrays.asList("&7Expand your claim size", "&7Only claim owners may request"),
                    "menu", "expansion-request"));
        }

        if (p.isOp() || p.hasPermission("proshield.admin")) {
            inv.setItem(26, button(Material.REDSTONE, "&cAdmin Tools",
                    Arrays.asList("&7Reload, Debug, Bypass, World Controls", "&7Manage expansion requests"),
                    "menu", "admin-tools"));
        }

        inv.setItem(40, button(Material.BARRIER, "&cExit",
                Collections.singletonList("&7Close this menu"), "close", ""));
        p.openInventory(inv);
    }

    private void openAdminTools(Player p) {
        Inventory inv = makeInv("gui.menus.admin-tools.title", "&cAdmin Tools", 45);

        inv.setItem(10, button(Material.BOOK, "&bReload Config",
                Collections.singletonList("&7Reloads ProShield configuration"), "cmd", "proshield reload"));
        inv.setItem(12, button(Material.LEVER, "&dToggle Debug",
                Collections.singletonList("&7Turn debug mode on/off"), "cmd", "proshield debug"));
        inv.setItem(14, button(Material.TRIPWIRE_HOOK, "&6Toggle Bypass (You)",
                Collections.singletonList("&7Bypass all checks while enabled"), "cmd", "proshield bypass"));

        if (p.hasPermission("proshield.admin.worldcontrols")) {
            inv.setItem(16, button(Material.REPEATER, "&dWorld Controls",
                    Arrays.asList("&7Manage protections for this world", "&7Requires: proshield.admin.worldcontrols"),
                    "cmd", "proshield worldcontrols"));
        }

        if (p.hasPermission("proshield.admin.expansions")) {
            inv.setItem(22, button(Material.PAPER, "&ePending Expansion Requests",
                    Arrays.asList("&7Review player expansion requests",
                                  "&aLeft-click: Approve and expand claim radius",
                                  "&cRight-click: Deny with reason",
                                  "&7Requires: proshield.admin.expansions"),
                    "menu", "expansion-requests"));
        }

        backExit(inv, "main");
        p.openInventory(inv);
    }

    /* =========================== Player Expansion =========================== */

    private void openExpansionRequestMenu(Player p) {
        // TODO: implement cooldown + ownership check + request submission
        // intentionally kept as stub here (same as before)
    }

    /* =========================== Admin Expansion =========================== */

    private void openExpansionRequests(Player p) {
        Inventory inv = makeInv("gui.menus.expansion-requests.title", "&ePending Requests", 45);
        List<ExpansionRequest> requests = requestManager.getPendingRequests();

        if (requests.isEmpty()) {
            inv.setItem(22, info(Material.PAPER, "&7No Pending Requests",
                    Collections.singletonList("&7Players can request via GUI → Expansion")));
            backExit(inv, "admin-tools");
            p.openInventory(inv);
            return;
        }

        int[] slots = gridSlots();
        int i = 0;
        for (ExpansionRequest req : requests) {
            if (i >= slots.length) break;
            OfflinePlayer op = Bukkit.getOfflinePlayer(req.getRequester());
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta sm = (SkullMeta) head.getItemMeta();
            if (sm != null) {
                sm.setOwningPlayer(op);
                sm.setDisplayName(color("&b" + (op.getName() == null ? req.getRequester().toString() : op.getName())));
                List<String> lore = new ArrayList<>();
                lore.add(color("&7Expansion: &f+" + req.getBlocks() + " blocks"));
                long age = (System.currentTimeMillis() - req.getTimestamp()) / 1000;
                lore.add(color("&7Requested &f" + age + "s &7ago"));
                lore.add(color(plugin.getMessagesConfig().getString("messages.expansion-admin-lore.approve", "&aLeft-click: Approve")));
                lore.add(color(plugin.getMessagesConfig().getString("messages.expansion-admin-lore.deny", "&cRight-click: Deny")));
                sm.setLore(lore);
                hideAll(sm);
                sm.getPersistentDataContainer().set(ACTION_KEY, PersistentDataType.STRING, "expansion-manage");
                sm.getPersistentDataContainer().set(ARG_KEY, PersistentDataType.STRING,
                        req.getRequester().toString() + "|" + req.getBlocks());
                head.setItemMeta(sm);
            }
            inv.setItem(slots[i++], head);
        }

        backExit(inv, "admin-tools");
        p.openInventory(inv);
    }

    private void openExpansionDenyMenu(Player p, UUID targetId) {
        Inventory inv = makeInv("gui.menus.expansion-deny.title", "&cSelect Deny Reason", 45);

        ConfigurationSection sec = plugin.getMessagesConfig().getConfigurationSection("messages.deny-reasons");
        if (sec != null) {
            int[] slots = gridSlots();
            int i = 0;
            for (String key : sec.getKeys(false)) {
                if (i >= slots.length) break;
                String reason = sec.getString(key, "&c" + key);
                ItemStack paper = new ItemStack(Material.PAPER);
                ItemMeta meta = paper.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(color(reason));
                    meta.getPersistentDataContainer().set(ACTION_KEY, PersistentDataType.STRING, "expansion-deny");
                    meta.getPersistentDataContainer().set(ARG_KEY, PersistentDataType.STRING, targetId.toString() + "|" + key);
                    paper.setItemMeta(meta);
                }
                inv.setItem(slots[i++], paper);
            }
        }

        backExit(inv, "expansion-requests");
        p.openInventory(inv);
    }

    /* =========================== Click Handling =========================== */

    public void handleClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player p = (Player) event.getWhoClicked();
        ItemStack item = event.getCurrentItem();
        if (item == null || !item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        String action = meta.getPersistentDataContainer().get(ACTION_KEY, PersistentDataType.STRING);
        String arg = meta.getPersistentDataContainer().get(ARG_KEY, PersistentDataType.STRING);
        if (action == null) return;

        String[] parts = arg != null ? arg.split("\\|") : new String[0];

        switch (action) {
            case "cmd":
                if (parts.length > 0) {
                    p.performCommand(parts[0]);
                    sound(p, Sound.UI_BUTTON_CLICK);
                    p.closeInventory();
                }
                break;

            case "menu":
                if (parts.length > 0) {
                    openMenu(p, parts[0]);
                    sound(p, Sound.UI_BUTTON_CLICK);
                }
                break;

            case "close":
                p.closeInventory();
                sound(p, Sound.BLOCK_CHEST_CLOSE);
                break;

            case "expansion-manage": {
                UUID targetId = safeUUID(parts[0]);
                if (event.isLeftClick()) {
                    requestManager.approveRequest(targetId);
                    messages.send(p, "&aExpansion request approved.");
                    Bukkit.getScheduler().runTask(plugin, () -> openExpansionRequests(p));
                } else if (event.isRightClick()) {
                    openExpansionDenyMenu(p, targetId);
                }
                break;
            }

            case "expansion-deny": {
                UUID targetId = safeUUID(parts[0]);
                String reasonKey = parts.length > 1 ? parts[1] : "custom-1";
                requestManager.denyRequest(targetId, reasonKey);
                messages.send(p, "&cExpansion request denied.");
                Bukkit.getScheduler().runTask(plugin, () -> openExpansionRequests(p));
                break;
            }
        }
    }

    /* =========================== Helpers =========================== */
    private Inventory makeInv(String path, String def, int size) {
        String title = plugin.getConfig().getString(path, def);
        return Bukkit.createInventory(null, size, color(title));
    }

    private ItemStack button(Material mat, String name, List<String> lore, String action, String arg) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(color(name));
            List<String> loreCol = new ArrayList<>();
            if (lore != null) lore.forEach(s -> loreCol.add(color(s)));
            meta.setLore(loreCol);
            hideAll(meta);
            meta.getPersistentDataContainer().set(ACTION_KEY, PersistentDataType.STRING, action);
            meta.getPersistentDataContainer().set(ARG_KEY, PersistentDataType.STRING, arg);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack info(Material mat, String name, List<String> lore) {
        return button(mat, name, lore, "none", "");
    }

    private void backExit(Inventory inv, String backMenu) {
        inv.setItem(inv.getSize() - 9, button(Material.ARROW, "&7Back", Collections.singletonList("&7Return"), "menu", backMenu));
        inv.setItem(inv.getSize() - 1, button(Material.BARRIER, "&cExit", Collections.singletonList("&7Close this menu"), "close", ""));
    }

    private int[] gridSlots() {
        return new int[]{10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34};
    }

    private void sound(Player p, Sound s) { p.playSound(p.getLocation(), s, 1f, 1f); }
    private String color(String s) { return org.bukkit.ChatColor.translateAlternateColorCodes('&', s); }
    private void hideAll(ItemMeta meta) { meta.addItemFlags(org.bukkit.inventory.ItemFlag.values()); }

    private UUID safeUUID(String s) {
        try { return UUID.fromString(s); } catch (Exception e) { return null; }
    }

    private String stateLineForCurrentClaim(Player p) {
        Plot plot = plotManager.getPlot(p.getLocation());
        if (plot == null) return "&7You are not in a claim.";
        return "&7Owned by: &f" + Bukkit.getOfflinePlayer(plot.getOwner()).getName();
    }

    // placeholder stubs for role menus / flags (already in your base code)
    private void openRoles(Player p) {}
    private void openRolesNearby(Player p) {}
    private void openFlags(Player p) {}
}
