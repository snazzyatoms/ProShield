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
            case "expansion-request": openExpansionRequestMenu(player); return; // Player submission
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

        // Player Expansion Request (modular toggle)
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

    private void openRoles(Player p) {
        Inventory inv = makeInv("gui.menus.roles.title", "&bTrusted Players", 45);
        inv.setItem(10, button(Material.PLAYER_HEAD, "&aTrust Nearby Player",
                Arrays.asList("&7Shows players within 10 blocks", "&7Click a head to assign a role"),
                "menu", "roles-nearby"));
        inv.setItem(12, button(Material.BOOK, "&eManage Trusted",
                Arrays.asList("&7Change role or remove access"), "menu", "manage-trusted"));
        backExit(inv, "main");
        p.openInventory(inv);
    }

    private void openRolesNearby(Player p) {
        Inventory inv = makeInv("gui.menus.roles-nearby.title", "&bNearby Players", 45);
        Plot plot = plotManager.getPlot(p.getLocation());
        if (plot == null) {
            inv.setItem(22, info(Material.BARRIER, "&cNo Claim Here",
                    Collections.singletonList("&7Stand inside your claim to manage trust")));
            backExit(inv, "roles");
            p.openInventory(inv);
            return;
        }

        List<Player> near = p.getWorld().getPlayers().stream()
                .filter(other -> !other.equals(p))
                .filter(other -> other.getLocation().distanceSquared(p.getLocation()) <= (10 * 10))
                .sorted(Comparator.comparingDouble(a -> a.getLocation().distanceSquared(p.getLocation())))
                .limit(21)
                .collect(Collectors.toList());

        int[] slots = gridSlots();
        int i = 0;
        for (Player target : near) {
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta sm = (SkullMeta) head.getItemMeta();
            if (sm != null) {
                sm.setOwningPlayer(target);
                sm.setDisplayName(color("&b" + target.getName()));
                sm.setLore(Arrays.asList(color("&7Click to assign role"),
                        color("&7Distance: &f" + (int) target.getLocation().distance(p.getLocation()) + " &7blocks")));
                hideAll(sm);
                sm.getPersistentDataContainer().set(ACTION_KEY, PersistentDataType.STRING, "assign-role");
                sm.getPersistentDataContainer().set(ARG_KEY, PersistentDataType.STRING, target.getUniqueId().toString());
                head.setItemMeta(sm);
            }
            if (i < slots.length) inv.setItem(slots[i++], head);
        }

        if (near.isEmpty()) {
            inv.setItem(22, info(Material.PAPER, "&eNo nearby players",
                    Arrays.asList("&7Ask your friend to stand within &f10 blocks&7.")));
        }

        backExit(inv, "roles");
        p.openInventory(inv);
    }

    private void openAssignRoleMenu(Player owner, UUID targetId) {
        Inventory inv = makeInv("gui.menus.assign-role.title", "&bAssign Role", 45);

        FileConfiguration cfg = plugin.getConfig();
        ConfigurationSection roles = cfg.getConfigurationSection("roles.available");
        if (roles == null || roles.getKeys(false).isEmpty()) {
            inv.setItem(22, info(Material.BARRIER, "&cNo roles configured",
                    Collections.singletonList("&7Define roles in config.yml → roles.available")));
            backExit(inv, "roles-nearby");
            owner.openInventory(inv);
            return;
        }

        Plot plot = plotManager.getPlot(owner.getLocation());
        if (plot == null) {
            inv.setItem(22, info(Material.BARRIER, "&cNo Claim Here",
                    Collections.singletonList("&7Stand inside your claim to manage trust")));
            backExit(inv, "roles-nearby");
            owner.openInventory(inv);
            return;
        }

        int[] slots = gridSlots();
        int i = 0;
        for (String roleKey : roles.getKeys(false)) {
            String name = roles.getString(roleKey + ".name", roleKey);
            List<String> lore = roles.getStringList(roleKey + ".lore").stream().map(this::color).collect(Collectors.toList());
            lore.add(color("&7Target: &f" + (Bukkit.getOfflinePlayer(targetId).getName() == null
                    ? targetId : Bukkit.getOfflinePlayer(targetId).getName())));
            ItemStack item = button(Material.PAPER, name, lore, "role-assign", roleKey + "|" + targetId);
            if (i < slots.length) inv.setItem(slots[i++], item);
        }

        backExit(inv, "roles-nearby");
        owner.openInventory(inv);
    }

    private void openManageTrusted(Player p) {
        Inventory inv = makeInv("gui.menus.manage-trusted.title", "&bManage Trusted", 45);
        Plot plot = plotManager.getPlot(p.getLocation());
        if (plot == null) {
            inv.setItem(22, info(Material.BARRIER, "&cNo Claim Here",
                    Collections.singletonList("&7Stand inside your claim")));
            backExit(inv, "roles");
            p.openInventory(inv);
            return;
        }

        Map<UUID, String> trusted = plot.getTrusted();
        if (trusted.isEmpty()) {
            inv.setItem(22, info(Material.PAPER, "&eNo trusted players",
                    Collections.singletonList("&7Use &fTrusted Players → Nearby&7 to add")));
            backExit(inv, "roles");
            p.openInventory(inv);
            return;
        }

        int[] slots = gridSlots();
        int i = 0;
        for (Map.Entry<UUID, String> e : trusted.entrySet()) {
            UUID uuid = e.getKey();
            String roleKey = e.getValue();
            OfflinePlayer op = Bukkit.getOfflinePlayer(uuid);

            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta sm = (SkullMeta) head.getItemMeta();
            if (sm != null) {
                sm.setOwningPlayer(op);
                sm.setDisplayName(color("&b" + (op.getName() == null ? uuid.toString() : op.getName())));
                List<String> lore = new ArrayList<>();
                lore.add(color("&7Role: &f" + roleDisplay(roleKey)));
                lore.add(color("&7Left-click: &aChange Role"));
                lore.add(color("&7Right-click: &cRemove from claim"));
                sm.setLore(lore);
                hideAll(sm);
                sm.getPersistentDataContainer().set(ACTION_KEY, PersistentDataType.STRING, "trusted-edit");
                sm.getPersistentDataContainer().set(ARG_KEY, PersistentDataType.STRING, uuid + "|" + roleKey);
                head.setItemMeta(sm);
            }
            if (i < slots.length) inv.setItem(slots[i++], head);
        }

        backExit(inv, "roles");
        p.openInventory(inv);
    }

    private void openFlags(Player p) {
        Inventory inv = makeInv("gui.menus.flags.title", "&eClaim Flags", 45);
        Plot plot = plotManager.getPlot(p.getLocation());
        if (plot == null) {
            inv.setItem(22, info(Material.BARRIER, "&cNo Claim Here",
                    Collections.singletonList("&7Stand inside your claim")));
            backExit(inv, "main");
            p.openInventory(inv);
            return;
        }

        FileConfiguration cfg = plugin.getConfig();
        ConfigurationSection flags = cfg.getConfigurationSection("flags.available");
        if (flags == null) {
            inv.setItem(22, info(Material.BARRIER, "&cNo flags configured",
                    Collections.singletonList("&7Define flags in config.yml → flags.available")));
            backExit(inv, "main");
            p.openInventory(inv);
            return;
        }

        int[] slots = gridSlots();
        int i = 0;
        for (String key : flags.getKeys(false)) {
            String display = flags.getString(key + ".name", key);
            List<String> desc = flags.getStringList(key + ".description");
            boolean state = plot.getFlag(key, cfg);

            List<String> lore = new ArrayList<>();
            for (String line : desc) lore.add(color(line));
            lore.add(" ");
            lore.add(color("&7Current: " + (state ? "&aON" : "&cOFF")));

            ItemStack item = toggleButton(state, display, lore);
            markAction(item, "flag-toggle", key);

            if (i < slots.length) inv.setItem(slots[i++], item);
        }

        backExit(inv, "main");
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
        inv.setItem(16, button(Material.REPEATER, "&dWorld Controls",
                Collections.singletonList("&7Manage protections for this world"), "cmd", "proshield worldcontrols"));
        inv.setItem(22, button(Material.PAPER, "&ePending Expansion Requests",
                Arrays.asList("&7Review player expansion requests", "&7Approve or Deny with reasons"),
                "menu", "expansion-requests"));
        backExit(inv, "main");
        p.openInventory(inv);
    }

    /* =========================== Player Expansion =========================== */

    private void openExpansionRequestMenu(Player p) {
        Inventory inv = makeInv("gui.menus.expansion-request.title", "&aRequest Expansion", 45);

        // Feature toggle
        if (!plugin.getConfig().getBoolean("claims.expansion.enabled", true)) {
            String disabled = plugin.getConfig().getString("messages.expansion-disabled",
                    "&cExpansion requests are disabled by the server.");
            inv.setItem(22, info(Material.BARRIER, "&cDisabled", Collections.singletonList(color(disabled))));
            backExit(inv, "main");
            p.openInventory(inv);
            return;
        }

        Plot plot = plotManager.getPlot(p.getLocation());
        if (plot == null || !p.getUniqueId().equals(plot.getOwner())) {
            inv.setItem(22, info(Material.BARRIER, "&cNot Your Claim",
                    Arrays.asList("&7You must be inside",
                                  "&7your own claim to request expansion.")));
            backExit(inv, "main");
            p.openInventory(inv);
            return;
        }

        // Cooldown check
        int cooldownHours = plugin.getConfig().getInt("claims.expansion.cooldown-hours", 6);
        ExpansionRequest last = requestManager.getLastRequest(p.getUniqueId());
        if (last != null) {
            long elapsed = System.currentTimeMillis() - last.getTimestamp();
            long required = Math.max(0, cooldownHours) * 3600_000L;
            if (elapsed < required) {
                long remainingMs = required - elapsed;
                long hours = remainingMs / 3_600_000L;
                long minutes = (remainingMs % 3_600_000L) / 60_000L;

                String title = plugin.getConfig().getString("messages.expansion-cooldown-title", "&cCooldown Active");
                String line  = plugin.getConfig().getString("messages.expansion-cooldown-active",
                        "&7You can request again in &f{hours}h {minutes}m&7.")
                        .replace("{hours}", String.valueOf(hours))
                        .replace("{minutes}", String.valueOf(minutes));

                inv.setItem(22, info(Material.CLOCK, color(title), Collections.singletonList(color(line))));
                backExit(inv, "main");
                p.openInventory(inv);
                return;
            }
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

                    // Re-check cooldown at click time (prevents race)
                    int cooldownHours = plugin.getConfig().getInt("claims.expansion.cooldown-hours", 6);
                    ExpansionRequest last = requestManager.getLastRequest(p.getUniqueId());
                    if (last != null) {
                        long elapsed = System.currentTimeMillis() - last.getTimestamp();
                        long required = Math.max(0, cooldownHours) * 3600_000L;
                        if (elapsed < required) {
                            long remainingMs = required - elapsed;
                            long hours = remainingMs / 3_600_000L;
                            long minutes = (remainingMs % 3_600_000L) / 60_000L;
                            String line = plugin.getConfig().getString("messages.expansion-cooldown-active",
                                    "&7You can request again in &f{hours}h {minutes}m&7.")
                                    .replace("{hours}", String.valueOf(hours))
                                    .replace("{minutes}", String.valueOf(minutes));
                            messages.send(p, line);
                            break;
                        }
                    }

                    ExpansionRequest req = new ExpansionRequest(p.getUniqueId(), blocks);
                    requestManager.addRequest(req);

                    String ok = plugin.getConfig().getString("messages.expansion-request",
                            "&eYour expansion request for +{blocks} blocks has been sent to admins.")
                            .replace("{blocks}", String.valueOf(blocks));
                    messages.send(p, ok);
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
                int blocks = Integer.parseInt(parts[1]); // kept for context; we don't need it here
                String reasonKey = parts[2];
                requestManager.denyRequest(targetId, reasonKey);
                messages.send(p, "&cDenied expansion for " + Bukkit.getOfflinePlayer(targetId).getName());
                openExpansionRequests(p);
                break;
            }

            // keep your other existing cases (assign-role, role-assign, trusted-edit, flag-toggle, etc.)
        }
    }

    /* ====================================================================== */
    /* Helpers (use your existing implementations)                             */
    /* ====================================================================== */
    // makeInv, button, info, toggleButton, gridSlots, backExit, markAction, sound, color, safeUUID,
    // stateLineForCurrentClaim, hideAll, roleDisplay → unchanged from your codebase.
}
