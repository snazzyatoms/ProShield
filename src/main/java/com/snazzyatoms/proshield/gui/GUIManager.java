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
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.stream.Collectors;

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
            case "expansion-request": openExpansionRequestMenu(player); return; // NEW
            default: openMain(player); return;
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

        // NEW: Request Expansion
        if (plugin.getConfig().getBoolean("claims.expansion.enabled", true)) {
            inv.setItem(31, button(Material.EMERALD, "&aRequest Expansion",
                    Arrays.asList("&7Expand your claim size",
                            "&7Only claim owners may request"),
                    "menu", "expansion-request"));
        }

        if (p.isOp() || p.hasPermission("proshield.admin")) {
            inv.setItem(26, button(Material.REDSTONE, "&cAdmin Tools",
                    Arrays.asList("&7Reload, Debug, Bypass, World Controls", "&7Manage expansion requests"),
                    "menu", "admin-tools"));
        }

        inv.setItem(40, button(Material.BARRIER, "&cExit", Collections.singletonList("&7Close this menu"), "close", ""));
        p.openInventory(inv);
    }

    // ======================================================================
    // Player Expansion Request Menu (NEW)
    // ======================================================================
    private void openExpansionRequestMenu(Player p) {
        Inventory inv = makeInv("gui.menus.expansion-request.title", "&aRequest Expansion", 45);

        Plot plot = plotManager.getPlot(p.getLocation());
        if (plot == null || !p.getUniqueId().equals(plot.getOwner())) {
            inv.setItem(22, info(Material.BARRIER, "&cNot Your Claim",
                    Arrays.asList("&7You must be inside",
                                  "&7your own claim to request expansion.")));
            backExit(inv, "main");
            p.openInventory(inv);
            return;
        }

        List<Integer> steps = plugin.getConfig().getIntegerList("claims.expansion.step-options");
        if (steps.isEmpty()) {
            inv.setItem(22, info(Material.BARRIER, "&cNo options configured",
                    Collections.singletonList("&7Set claims.expansion.step-options in config.yml")));
            backExit(inv, "main");
            p.openInventory(inv);
            return;
        }

        int[] slots = gridSlots();
        int i = 0;
        for (int step : steps) {
            ItemStack item = button(Material.EMERALD_BLOCK, "&a+" + step + " blocks",
                    Arrays.asList("&7Request to expand your claim",
                            "&7by &f" + step + " &7blocks"),
                    "expansion-request-submit", String.valueOf(step));
            if (i < slots.length) inv.setItem(slots[i++], item);
        }

        backExit(inv, "main");
        p.openInventory(inv);
    }

    // ======================================================================
    // Admin Expansion Requests (existing)
    // ======================================================================
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
                lore.add(color("&aLeft-click: Approve"));
                lore.add(color("&cRight-click: Deny"));
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

    private void openDenyReasons(Player admin, UUID target, int amount) {
        Inventory inv = makeInv("gui.menus.deny-reasons.title", "&cDeny Reasons", 45);
        ConfigurationSection reasons = plugin.getConfig().getConfigurationSection("messages.deny-reasons");

        int[] slots = gridSlots();
        int i = 0;
        if (reasons != null) {
            for (String key : reasons.getKeys(false)) {
                String msg = reasons.getString(key, "&cDenied");
                ItemStack item = button(Material.RED_WOOL, "&c" + key,
                        Collections.singletonList(color(msg)),
                        "expansion-deny", target + "|" + amount + "|" + key);
                if (i < slots.length) inv.setItem(slots[i++], item);
            }
        }

        backExit(inv, "expansion-requests");
        admin.openInventory(inv);
    }

    /* ====================================================================== */
    /* Click Handling                                                          */
    /* ====================================================================== */
    public void handleClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player p)) return;
        if (event.getCurrentItem() == null) return;
        ItemMeta meta = event.getCurrentItem().getItemMeta();
        if (meta == null) return;
        event.setCancelled(true);

        String action = meta.getPersistentDataContainer().getOrDefault(ACTION_KEY, PersistentDataType.STRING, "");
        String arg = meta.getPersistentDataContainer().getOrDefault(ARG_KEY, PersistentDataType.STRING, "");

        switch (action) {
            case "close": p.closeInventory(); break;
            case "menu": openMenu(p, arg); break;
            case "cmd": p.closeInventory(); p.performCommand(arg); break;

            case "expansion-request-submit": {
                try {
                    int blocks = Integer.parseInt(arg);
                    Plot plot = plotManager.getPlot(p.getLocation());
                    if (plot == null || !p.getUniqueId().equals(plot.getOwner())) {
                        messages.send(p, "&cYou must be inside your own claim to request an expansion.");
                        break;
                    }
                    ExpansionRequest req = new ExpansionRequest(p.getUniqueId(), blocks);
                    requestManager.addRequest(req);
                    messages.send(p, plugin.getConfig().getString("messages.expansion-request")
                            .replace("{blocks}", String.valueOf(blocks)));
                    sound(p, Sound.UI_BUTTON_CLICK);
                    p.closeInventory();
                } catch (Exception ex) {
                    messages.send(p, "&cFailed to create expansion request.");
                }
                break;
            }

            case "expansion-manage": {
                String[] parts = arg.split("\\|");
                if (parts.length != 2) break;
                UUID targetId = safeUUID(parts[0]);
                int blocks = Integer.parseInt(parts[1]);
                if (event.isLeftClick()) {
                    requestManager.approveRequest(targetId);
                    messages.send(p, "&aApproved expansion for " + Bukkit.getOfflinePlayer(targetId).getName());
                    openExpansionRequests(p);
                } else if (event.isRightClick()) {
                    openDenyReasons(p, targetId, blocks);
                }
                break;
            }

            case "expansion-deny": {
                String[] parts = arg.split("\\|");
                if (parts.length != 3) break;
                UUID targetId = safeUUID(parts[0]);
                int blocks = Integer.parseInt(parts[1]);
                String reasonKey = parts[2];
                requestManager.denyRequest(targetId, reasonKey);
                messages.send(p, "&cDenied expansion for " + Bukkit.getOfflinePlayer(targetId).getName());
                openExpansionRequests(p);
                break;
            }

            // keep your other cases: assign-role, role-assign, trusted-edit, flag-toggle, etc.
        }
    }

    /* ====================================================================== */
    /* Helpers (color, markAction, toggleButton, gridSlots, backExit, etc.)   */
    /* ====================================================================== */
    // ... keep all your existing helper methods here
}
