package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.stream.Collectors;

/**
 * GUI-only manager:
 * - Main menu (claim, unclaim, info, roles, flags, admin tools)
 * - Roles flow: Nearby -> Assign Role, Manage Trusted -> edit/remove
 * - Flags: toggle per-claim flags from config.flags.available
 * - Admin tools: reload/debug/bypass/world controls hooks (via commands/events)
 *
 * All buttons use PDC (localizedName fallback) to carry action payloads.
 * Items have NO attributes; lore explains purpose + current ON/OFF state.
 */
public class GUIManager {

    // ---- Core ----
    private final ProShield plugin;
    private final MessagesUtil messages;
    private final PlotManager plotManager;

    // PDC keys
    private final NamespacedKey ACTION_KEY;
    private final NamespacedKey ARG_KEY;

    public GUIManager(ProShield plugin) {
        this.plugin = plugin;
        this.messages = plugin.getMessagesUtil();
        this.plotManager = plugin.getPlotManager();

        this.ACTION_KEY = new NamespacedKey(plugin, "ps_action");
        this.ARG_KEY = new NamespacedKey(plugin, "ps_arg");
    }

    /* ====================================================================== */
    /* Inventory Builders                                                      */
    /* ====================================================================== */

    public void openMenu(Player player, String menuKey) {
        if ("main".equalsIgnoreCase(menuKey)) { openMain(player); return; }
        if ("roles".equalsIgnoreCase(menuKey)) { openRoles(player); return; }
        if ("roles-nearby".equalsIgnoreCase(menuKey)) { openRolesNearby(player); return; }
        if ("flags".equalsIgnoreCase(menuKey)) { openFlags(player); return; }
        if ("admin-tools".equalsIgnoreCase(menuKey)) { openAdminTools(player); return; }
        // default fallback
        openMain(player);
    }

    private void openMain(Player p) {
        Inventory inv = makeInv("gui.menus.main.title", "&6ProShield Menu", 45);
        // Claim
        inv.setItem(11, button(Material.GRASS_BLOCK, "&aClaim Land",
                Arrays.asList("&7Protect your land", "&7Radius: &f" + plugin.getConfig().getInt("claims.default-radius", 50) + " &7blocks"),
                "cmd", "proshield claim"));
        // Info
        inv.setItem(13, button(Material.PAPER, "&eClaim Info",
                Arrays.asList("&7Shows your current claim details",
                        stateLineForCurrentClaim(p)), "cmd", "proshield info"));
        // Unclaim
        inv.setItem(15, button(Material.BARRIER, "&cUnclaim Land",
                Arrays.asList("&7Remove your claim"), "cmd", "proshield unclaim"));

        // Roles
        inv.setItem(21, button(Material.PLAYER_HEAD, "&bTrusted Players",
                Arrays.asList("&7Manage who can build in your claim", "&7Add/Remove roles"),
                "menu", "roles"));

        // Flags
        inv.setItem(23, button(Material.CHEST, "&eClaim Flags",
                Arrays.asList("&7Toggle special protections",
                        stateLineForCurrentClaim(p)), "menu", "flags"));

        // Admin tools (visible if perm or op)
        if (p.isOp() || p.hasPermission("proshield.admin")) {
            inv.setItem(26, button(Material.REDSTONE, "&cAdmin Tools",
                    Arrays.asList("&7Reload, Debug, Bypass, World Controls"), "menu", "admin-tools"));
        }

        // Exit
        inv.setItem(40, button(Material.BARRIER, "&cExit",
                Collections.singletonList("&7Close this menu"), "close", ""));

        p.openInventory(inv);
    }

    private void openRoles(Player p) {
        Inventory inv = makeInv("gui.menus.roles.title", "&bTrusted Players", 45);

        // Nearby players (heads)
        inv.setItem(10, button(Material.PLAYER_HEAD, "&aTrust Nearby Player",
                Arrays.asList("&7Shows players within 10 blocks", "&7Click a head to assign a role"),
                "menu", "roles-nearby"));

        // Manage previously trusted
        inv.setItem(12, button(Material.BOOK, "&eManage Trusted",
                Arrays.asList("&7Change role or remove access"), "menu", "manage-trusted"));

        // Back / Exit
        backExit(inv, "main");

        p.openInventory(inv);
    }

    private void openRolesNearby(Player p) {
        Inventory inv = makeInv("gui.menus.roles-nearby.title", "&bNearby Players", 45);
        Plot plot = plotManager.getPlot(p.getLocation());
        if (plot == null) {
            inv.setItem(22, info(Material.BARRIER, "&cNo Claim Here", Collections.singletonList("&7Stand inside your claim to manage trust")));
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
                sm.setLore(Arrays.asList(
                        color("&7Click to assign role"),
                        color("&7Distance: &f" + (int) target.getLocation().distance(p.getLocation()) + " &7blocks")
                ));
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
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetId);
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
            inv.setItem(22, info(Material.BARRIER, "&cNo Claim Here", Collections.singletonList("&7Stand inside your claim to manage trust")));
            backExit(inv, "roles-nearby");
            owner.openInventory(inv);
            return;
        }

        int[] slots = gridSlots();
        int i = 0;
        for (String roleKey : roles.getKeys(false)) {
            String name = roles.getString(roleKey + ".name", roleKey);
            List<String> lore = roles.getStringList(roleKey + ".lore");
            List<String> formatted = lore.stream().map(this::color).collect(Collectors.toList());
            formatted.add(color("&7Target: &f" + (target.getName() == null ? target.getUniqueId() : target.getName())));
            ItemStack item = button(Material.PAPER, name, formatted, "role-assign", roleKey + "|" + targetId);
            if (i < slots.length) inv.setItem(slots[i++], item);
        }

        backExit(inv, "roles-nearby");
        owner.openInventory(inv);
    }

    private void openManageTrusted(Player p) {
        Inventory inv = makeInv("gui.menus.manage-trusted.title", "&bManage Trusted", 45);

        Plot plot = plotManager.getPlot(p.getLocation());
        if (plot == null) {
            inv.setItem(22, info(Material.BARRIER, "&cNo Claim Here", Collections.singletonList("&7Stand inside your claim")));
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
            inv.setItem(22, info(Material.BARRIER, "&cNo Claim Here", Collections.singletonList("&7Stand inside your claim")));
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

        // Reload
        inv.setItem(10, button(Material.BOOK, "&bReload Config",
                Arrays.asList("&7Reloads ProShield configuration"), "cmd", "proshield reload"));

        // Debug toggle
        inv.setItem(12, button(Material.LEVER, "&dToggle Debug",
                Arrays.asList("&7Turn debug mode on/off"), "cmd", "proshield debug"));

        // Bypass toggle
        inv.setItem(14, button(Material.TRIPWIRE_HOOK, "&6Toggle Bypass (You)",
                Arrays.asList("&7Bypass all checks while enabled"), "cmd", "proshield bypass"));

        // World controls hook (if you have a separate GUI later)
        inv.setItem(16, button(Material.REPEATER, "&dWorld Controls",
                Arrays.asList("&7Manage protections for this world"), "cmd", "proshield worldcontrols"));

        // Back/Exit
        backExit(inv, "main");
        p.openInventory(inv);
    }

    /* ====================================================================== */
    /* Click Handling                                                          */
    /* ====================================================================== */

    public void handleClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player p)) return;
        if (event.getCurrentItem() == null) return;
        ItemMeta meta = event.getCurrentItem().getItemMeta();
        if (meta == null) return;

        event.setCancelled(true); // always GUI-only

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
                // Execute via player to keep permission model
                p.performCommand(arg);
                break;

            case "assign-role": { // clicked a nearby player head
                try {
                    UUID targetId = UUID.fromString(arg);
                    openAssignRoleMenu(p, targetId);
                } catch (Exception ignored) {}
                break;
            }

            case "role-assign": { // picked a role for a target
                String[] parts = arg.split("\\|");
                if (parts.length != 2) break;
                String roleKey = parts[0];
                UUID targetId = safeUUID(parts[1]);
                if (targetId == null) break;
                Plot plot = plotManager.getPlot(p.getLocation());
                if (plot == null) {
                    messages.send(p, "&cNo claim here.");
                    break;
                }
                // Owner or admin-only enforcement is typically elsewhere; here we assume caller has rights.
                plot.setRole(targetId, roleKey);
                plugin.getPlotManager().saveAll();
                OfflinePlayer op = Bukkit.getOfflinePlayer(targetId);
                messages.send(p, "&aTrusted &f" + (op.getName() == null ? targetId : op.getName()) + " &awith role &f" + roleDisplay(roleKey) + "&a.");
                sound(p, Sound.UI_BUTTON_CLICK);
                // Back to roles main
                openRoles(p);
                break;
            }

            case "trusted-edit": { // left = change role, right = remove
                String[] parts = arg.split("\\|");
                if (parts.length < 1) break;
                UUID targetId = safeUUID(parts[0]);
                if (targetId == null) break;

                if (event.isRightClick()) {
                    Plot plot = plotManager.getPlot(p.getLocation());
                    if (plot == null) { messages.send(p, "&cNo claim here."); break; }
                    plot.untrust(targetId);
                    plugin.getPlotManager().saveAll();
                    OfflinePlayer op = Bukkit.getOfflinePlayer(targetId);
                    messages.send(p, "&cRemoved &f" + (op.getName() == null ? targetId : op.getName()) + " &cfrom your claim.");
                    sound(p, Sound.ENTITY_VILLAGER_NO);
                    openManageTrusted(p);
                } else {
                    // Left click: choose a new role
                    openAssignRoleMenu(p, targetId);
                }
                break;
            }

            case "flag-toggle": {
                Plot plot = plotManager.getPlot(p.getLocation());
                if (plot == null) { messages.send(p, "&cNo claim here."); break; }
                FileConfiguration cfg = plugin.getConfig();
                boolean cur = plot.getFlag(arg, cfg);
                plot.setFlag(arg, !cur);
                plugin.getPlotManager().saveAll();
                messages.send(p, "&eFlag &b" + arg + " &eis now " + (cur ? "&cOFF" : "&aON"));
                sound(p, Sound.UI_BUTTON_CLICK);
                // Refresh flags menu to reflect state
                openFlags(p);
                break;
            }

            default:
                break;
        }
    }

    /* ====================================================================== */
    /* Helpers                                                                 */
    /* ====================================================================== */

    private Inventory makeInv(String pathTitle, String fallback, int size) {
        String title = color(plugin.getConfig().getString(pathTitle, fallback));
        return Bukkit.createInventory(null, size, title);
    }

    private ItemStack button(Material mat, String name, List<String> lore,
                             String action, String arg) {
        ItemStack it = new ItemStack(mat);
        ItemMeta im = it.getItemMeta();
        if (im != null) {
            im.setDisplayName(color(name));
            im.setLore(lore.stream().map(this::color).collect(Collectors.toList()));
            hideAll(im);
            markAction(im, action, arg);
            it.setItemMeta(im);
        }
        return it;
    }

    private ItemStack toggleButton(boolean state, String name, List<String> lore) {
        Material mat = state ? Material.LIME_DYE : Material.RED_DYE;
        String titled = (state ? "&a" : "&c") + name + " " + (state ? "&a[ON]" : "&c[OFF]");
        return button(mat, titled, lore, "noop", "");
    }

    private ItemStack info(Material mat, String name, List<String> lore) {
        ItemStack it = new ItemStack(mat);
        ItemMeta im = it.getItemMeta();
        if (im != null) {
            im.setDisplayName(color(name));
            im.setLore(lore.stream().map(this::color).collect(Collectors.toList()));
            hideAll(im);
            it.setItemMeta(im);
        }
        return it;
    }

    private void markAction(ItemStack it, String action, String arg) {
        ItemMeta im = it.getItemMeta();
        if (im != null) {
            markAction(im, action, arg);
            it.setItemMeta(im);
        }
    }

    private void markAction(ItemMeta im, String action, String arg) {
        im.getPersistentDataContainer().set(ACTION_KEY, PersistentDataType.STRING, action);
        im.getPersistentDataContainer().set(ARG_KEY, PersistentDataType.STRING, arg == null ? "" : arg);
    }

    private void backExit(Inventory inv, String backMenu) {
        inv.setItem(37, button(Material.BARRIER, "&cBack", Collections.singletonList("&7Return to previous menu"), "menu", backMenu));
        inv.setItem(43, button(Material.BARRIER, "&cExit", Collections.singletonList("&7Close this menu"), "close", ""));
    }

    private void hideAll(ItemMeta meta) {
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_DYE, ItemFlag.HIDE_UNBREAKABLE);
    }

    private String color(String s) { return ChatColor.translateAlternateColorCodes('&', s == null ? "" : s); }

    private String roleDisplay(String roleKey) {
        String name = plugin.getConfig().getString("roles.available." + roleKey + ".name", roleKey);
        return ChatColor.stripColor(color(name));
    }

    private String stateLineForCurrentClaim(Player p) {
        Plot plot = plotManager.getPlot(p.getLocation());
        if (plot == null) return color("&7Current: &fWilderness");
        boolean safe = plot.getFlag("safezone", plugin.getConfig());
        boolean pvp = plot.getFlag("pvp", plugin.getConfig());
        return color("&7Safezone: " + (safe ? "&aON" : "&cOFF") + " &7| PvP: " + (pvp ? "&aON" : "&cOFF"));
    }

    private int[] gridSlots() {
        // A nice centered grid across rows 2–4
        return new int[]{10,11,12,13,14,15,16,19,20,21,22,23,24,25,28,29,30,31,32,33,34};
    }

    private void sound(Player p, Sound s) {
        try { p.playSound(p.getLocation(), s, 0.7f, 1.0f); } catch (Throwable ignored) {}
    }

    private UUID safeUUID(String s) {
        try { return UUID.fromString(s); } catch (Exception e) { return null; }
    }
}
