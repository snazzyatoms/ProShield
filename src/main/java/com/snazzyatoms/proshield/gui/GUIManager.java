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
import org.bukkit.configuration.file.FileConfiguration;
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
        FileConfiguration cfg = plugin.getConfig();

        Plot current = plotManager.getPlot(p.getLocation());
        if (current == null || !current.getOwner().equals(p.getUniqueId())) {
            messages.send(p, "&cOnly claim owners can request expansions.");
            return;
        }

        // Cooldown check
        int cooldownHours = cfg.getInt("claims.expansion.cooldown-hours", 6);
        ExpansionRequest last = requestManager.getLastRequest(p.getUniqueId());
        if (last != null && last.getStatus() == ExpansionRequest.Status.PENDING) {
            messages.send(p, cfg.getString("messages.expansion-cooldown-active",
                    "&7You must wait before making another request."));
            return;
        }

        Inventory inv = makeInv("gui.menus.expansion-request.title", "&aRequest Expansion", 27);

        List<Integer> options = cfg.getIntegerList("claims.expansion.step-options");
        int[] slots = {10, 12, 14, 16};
        for (int i = 0; i < options.size() && i < slots.length; i++) {
            int blocks = options.get(i);
            inv.setItem(slots[i], button(Material.EMERALD_BLOCK, "&a+" + blocks + " Radius",
                    Arrays.asList("&7Request to expand claim by &f" + blocks + " &7chunks radius"),
                    "expansion-request-submit", String.valueOf(blocks)));
        }

        backExit(inv, "main");
        p.openInventory(inv);
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
                lore.add(color("&7Expansion: &f+" + req.getBlocks() + " radius chunks"));
                long age = (System.currentTimeMillis() - req.getTimestamp()) / 1000;
                lore.add(color("&7Requested &f" + age + "s &7ago"));
                lore.add(color("&aLeft-click: Approve and expand claim radius"));
                lore.add(color("&cRight-click: Deny with reason"));
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

    /* =========================== Click Handling =========================== */

    public void handleClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player p)) return;
        if (event.getCurrentItem() == null) return;

        ItemMeta meta = event.getCurrentItem().getItemMeta();
        if (meta == null) return;

        event.setCancelled(true);

        String action = meta.getPersistentDataContainer().getOrDefault(ACTION_KEY, PersistentDataType.STRING, "");
        String arg = meta.getPersistentDataContainer().getOrDefault(ARG_KEY, PersistentDataType.STRING, "");

        switch (action) {
            case "close":
                p.closeInventory();
                break;

            case "menu":
                openMenu(p, arg);
                break;

            case "cmd":
                p.closeInventory();
                p.performCommand(arg);
                break;

            case "expansion-request-submit":
                try {
                    int blocks = Integer.parseInt(arg);
                    ExpansionRequest req = new ExpansionRequest(p.getUniqueId(), blocks);
                    requestManager.addRequest(req);

                    messages.send(p, plugin.getConfig().getString("messages.expansion-request",
                            "&eYour expansion request for +{blocks} chunks was sent to admins.")
                            .replace("{blocks}", String.valueOf(blocks)));
                    sound(p, Sound.UI_BUTTON_CLICK);
                    p.closeInventory();
                } catch (Exception ex) {
                    messages.send(p, "&cFailed to create expansion request.");
                }
                break;

            case "expansion-manage":
                String[] parts = arg.split("\\|");
                if (parts.length != 2) return;
                UUID targetId = UUID.fromString(parts[0]);
                if (event.isLeftClick()) {
                    requestManager.approveRequest(targetId);
                    openExpansionRequests(p);
                } else if (event.isRightClick()) {
                    // simple deny with default reason (can extend to GUI reasons later)
                    requestManager.denyRequest(targetId, "custom-1");
                    openExpansionRequests(p);
                }
                break;
        }
    }

    /* =========================== Helpers =========================== */
    // makeInv, button, info, toggleButton, gridSlots, backExit, markAction, sound, color,
    // safeUUID, stateLineForCurrentClaim, hideAll, roleDisplay — remain unchanged from your existing code
}
