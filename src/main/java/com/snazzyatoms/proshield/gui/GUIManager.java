package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.expansions.ExpansionRequest;
import com.snazzyatoms.proshield.expansions.ExpansionRequestManager;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * GUIManager – v1.2.5 (fully functional)
 * - Main / Flags / Trusted / Assign Role / Admin Tools
 * - Expansion Request (player) + Expansion Review (admin) + Deny Reasons
 * - Back/Exit in ALL menus
 * - Clears and rebuilds menus to avoid duplicate items
 */
public class GUIManager {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final MessagesUtil messages;
    private final ExpansionRequestManager requestManager;

    // pending map used by Assign Role and Deny Reasons
    private final Map<UUID, UUID> pendingRoleAssignments = new HashMap<>();
    private final Map<UUID, UUID> pendingDenyTarget = new HashMap<>();

    public GUIManager(ProShield plugin) {
        this.plugin = plugin;
        this.plotManager = plugin.getPlotManager();
        this.messages = plugin.getMessagesUtil();
        this.requestManager = plugin.getExpansionRequestManager();
    }

    /* ====================================================================== */
    /* Main Menu                                                              */
    /* ====================================================================== */

    public void openMain(Player player) {
        String title = plugin.getConfig().getString("gui.menus.main.title", "&6ProShield Menu");
        int size = plugin.getConfig().getInt("gui.menus.main.size", 45);
        Inventory inv = Bukkit.createInventory(player, size, color(title));

        // Clear and fill
        clear(inv);

        setButton(inv, 10, Material.GRASS_BLOCK, "&aClaim Land",
                "&7Claim the chunk you are standing in.");
        setButton(inv, 12, Material.PAPER, "&eClaim Info",
                "&7View details about this claim.");
        setButton(inv, 14, Material.BARRIER, "&cUnclaim Land",
                "&7Remove your current claim.");
        setButton(inv, 16, Material.PLAYER_HEAD, "&bTrusted Players",
                "&7Manage trusted players & roles.");
        setButton(inv, 28, Material.REDSTONE_TORCH, "&eClaim Flags",
                "&7Toggle protection flags.");

        if (plugin.getConfig().getBoolean("claims.expansion.enabled", true)
                && plugin.getConfig().getBoolean("claims.expansion.show-in-gui", true)) {
            setButton(inv, 30, Material.EMERALD, "&aRequest Expansion",
                    "&7Request to expand your claim.");
        }

        setButton(inv, 32, Material.COMMAND_BLOCK, "&cAdmin Tools",
                "&7Admin-only controls (if permitted).");

        addExit(inv); // exit on main too
        player.openInventory(inv);
    }

    /* ====================================================================== */
    /* Trusted Players                                                        */
    /* ====================================================================== */

    public void openTrusted(Player player) {
        Plot plot = plotManager.getPlot(player.getLocation());
        if (plot == null) {
            messages.send(player, "&cNo claim here.");
            return;
        }

        String title = plugin.getConfig().getString("gui.menus.roles.title", "&bTrusted Players");
        int size = plugin.getConfig().getInt("gui.menus.roles.size", 45);
        Inventory inv = Bukkit.createInventory(player, size, color(title));
        clear(inv);

        int slot = 10;
        for (UUID uuid : plot.getTrusted().keySet()) {
            OfflinePlayer trusted = plugin.getServer().getOfflinePlayer(uuid);
            String role = plot.getTrusted().get(uuid);

            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            ItemMeta meta = head.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(color("&f" + (trusted.getName() != null ? trusted.getName() : uuid.toString())));
                meta.setLore(Arrays.asList(
                        color("&7Role: &b" + role),
                        color("&aLeft-click: Assign new role"),
                        color("&cRight-click: Untrust")
                ));
                head.setItemMeta(meta);
            }
            inv.setItem(slot++, head);
            if (slot % 9 == 8) slot += 2; // step across rows nicely
        }

        addBackExit(inv); // back→main, exit
        player.openInventory(inv);
    }

    public void handleTrustedClick(Player player, InventoryClickEvent event) {
        Plot plot = plotManager.getPlot(player.getLocation());
        if (plot == null) return;

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        String raw = strip(clicked.getItemMeta().getDisplayName());
        if (raw == null || raw.isBlank()) return;

        // handle Back/Exit already in listener; only process heads here
        OfflinePlayer target = Bukkit.getOfflinePlayer(raw);

        if (event.isLeftClick()) {
            openAssignRole(player, target.getUniqueId());
        } else if (event.isRightClick()) {
            plot.untrust(target.getUniqueId());
            messages.send(player, "&cUntrusted &f" + raw);
            plotManager.saveAll();
            openTrusted(player);
        }
    }

    /* ====================================================================== */
    /* Assign Role                                                            */
    /* ====================================================================== */

    private void openAssignRole(Player actor, UUID targetUuid) {
        String title = plugin.getConfig().getString("gui.menus.assign-role.title", "&bAssign Role");
        int size = plugin.getConfig().getInt("gui.menus.assign-role.size", 45);
        Inventory inv = Bukkit.createInventory(actor, size, color(title));
        clear(inv);

        ConfigurationSection section = plugin.getConfig().getConfigurationSection("roles.available");
        int slot = 10;
        if (section != null) {
            for (String roleKey : section.getKeys(false)) {
                String name = section.getString(roleKey + ".name", roleKey);
                List<String> lore = section.getStringList(roleKey + ".lore");

                ItemStack item = new ItemStack(Material.BOOK);
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(color(name));
                    List<String> coloredLore = new ArrayList<>();
                    for (String line : lore) coloredLore.add(color(line));
                    coloredLore.add(color("&7Click to assign this role"));
                    meta.setLore(coloredLore);
                    item.setItemMeta(meta);
                }
                inv.setItem(slot++, item);
                if (slot % 9 == 8) slot += 2;
            }
        }

        addBackExit(inv); // back→Trusted, exit
        pendingRoleAssignments.put(actor.getUniqueId(), targetUuid);
        actor.openInventory(inv);
    }

    public void handleAssignRoleClick(Player player, InventoryClickEvent event) {
        UUID targetUuid = pendingRoleAssignments.get(player.getUniqueId());
        if (targetUuid == null) return;

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        String roleName = strip(clicked.getItemMeta().getDisplayName());
        if (roleName == null) return;

        // normalize by stripping color and passing lower-cased to role manager
        plugin.getRoleManager().assignRoleViaChat(player, targetUuid, roleName);
        plotManager.saveAll();

        // clear pending and return to trusted list
        pendingRoleAssignments.remove(player.getUniqueId());
        openTrusted(player);
    }

    /* ====================================================================== */
    /* Claim Flags                                                            */
    /* ====================================================================== */

    public void openFlags(Player player) {
        Plot plot = plotManager.getPlot(player.getLocation());
        if (plot == null) {
            messages.send(player, "&cNo claim here.");
            return;
        }

        String title = plugin.getConfig().getString("gui.menus.flags.title", "&eClaim Flags");
        int size = plugin.getConfig().getInt("gui.menus.flags.size", 45);
        Inventory inv = Bukkit.createInventory(player, size, color(title));
        clear(inv);

        ConfigurationSection root = plugin.getConfig().getConfigurationSection("flags.available");
        int slot = 10;
        if (root != null) {
            for (String key : root.getKeys(false)) {
                String path = "flags.available." + key;
                String name = plugin.getConfig().getString(path + ".name", key);
                boolean current = plot.getFlags().getOrDefault(
                        key, plugin.getConfig().getBoolean(path + ".default", false));

                ItemStack item = new ItemStack(current ? Material.LIME_DYE : Material.GRAY_DYE);
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(color(name));
                    meta.setLore(Arrays.asList(
                            color("&7Click to toggle"),
                            color("&fCurrent: " + (current ? "&aEnabled" : "&cDisabled"))
                    ));
                    item.setItemMeta(meta);
                }
                inv.setItem(slot++, item);
                if (slot % 9 == 8) slot += 2;
            }
        }

        addBackExit(inv); // back→main, exit
        player.openInventory(inv);
    }

    public void handleFlagsClick(Player player, InventoryClickEvent event) {
        Plot plot = plotManager.getPlot(player.getLocation());
        if (plot == null) return;

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        String name = strip(clicked.getItemMeta().getDisplayName());
        if (name == null) return;

        ConfigurationSection root = plugin.getConfig().getConfigurationSection("flags.available");
        if (root == null) return;

        for (String key : root.getKeys(false)) {
            String display = plugin.getConfig().getString("flags.available." + key + ".name", key);
            if (strip(color(display)).equalsIgnoreCase(name)) {
                boolean current = plot.getFlags().getOrDefault(
                        key, plugin.getConfig().getBoolean("flags.available." + key + ".default", false));
                boolean newValue = !current;
                plot.setFlag(key, newValue);
                messages.send(player, "&eFlag &f" + key + " &eis now " + (newValue ? "&aEnabled" : "&cDisabled"));
                plotManager.saveAll();
                openFlags(player);
                break;
            }
        }
    }

    /* ====================================================================== */
    /* Admin Tools                                                            */
    /* ====================================================================== */

    public void openAdminTools(Player player) {
        String title = plugin.getConfig().getString("gui.menus.admin-tools.title", "&cAdmin Tools");
        int size = plugin.getConfig().getInt("gui.menus.admin-tools.size", 45);
        Inventory inv = Bukkit.createInventory(player, size, color(title));
        clear(inv);

        setButton(inv, 10, Material.REPEATER, "&eReload Configs", "&7Reload ProShield configs.");
        setButton(inv, 12, Material.ENDER_EYE, "&aToggle Debug", "&7Enable/disable debug logging.");
        setButton(inv, 14, Material.BARRIER, "&cToggle Bypass", "&7Admin bypass for claims.");

        if (player.hasPermission("proshield.admin.expansions")) {
            setButton(inv, 22, Material.PAPER, "&eExpansion Requests",
                    "&7Review pending player expansion requests.",
                    "&aLeft-click: Open review");
        }

        addBackExit(inv); // back→main, exit
        player.openInventory(inv);
    }

    public void handleAdminClick(Player player, InventoryClickEvent event) {
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        String name = strip(clicked.getItemMeta().getDisplayName());
        if (name == null) return;

        if (name.equalsIgnoreCase("reload configs")) {
            plugin.reloadConfig();
            plugin.loadMessagesConfig();
            messages.send(player, "&aConfigs reloaded.");
        } else if (name.equalsIgnoreCase("toggle debug")) {
            plugin.toggleDebug();
            messages.send(player, "&eDebug mode: " + (plugin.isDebugEnabled() ? "&aENABLED" : "&cDISABLED"));
        } else if (name.equalsIgnoreCase("toggle bypass")) {
            UUID uuid = player.getUniqueId();
            if (plugin.isBypassing(uuid)) {
                plugin.getBypassing().remove(uuid);
                messages.send(player, "&cBypass disabled.");
            } else {
                plugin.getBypassing().add(uuid);
                messages.send(player, "&aBypass enabled.");
            }
        } else if (name.equalsIgnoreCase("expansion requests")) {
            openExpansionReview(player);
        }
    }

    /* ====================================================================== */
    /* Player Expansion Request (functional)                                  */
    /* ====================================================================== */

    public void openExpansionRequestMenu(Player player) {
        if (!plugin.getConfig().getBoolean("claims.expansion.enabled", true)) {
            messages.send(player, plugin.getMessagesConfig().getString("messages.expansion-disabled",
                    "&cExpansion requests are disabled by the server."));
            return;
        }

        String title = "&aRequest Claim Expansion";
        Inventory inv = Bukkit.createInventory(player, 45, color(title));
        clear(inv);

        // build step options from config
        List<Integer> steps = plugin.getConfig().getIntegerList("claims.expansion.step-options");
        if (steps == null || steps.isEmpty()) {
            steps = Arrays.asList(10, 15, 20, 25);
        }
        int slot = 10;
        for (int amt : steps) {
            ItemStack item = new ItemStack(Material.EMERALD);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(color("&a+ " + amt + " blocks"));
                meta.setLore(Arrays.asList(
                        color("&7Click to request this expansion"),
                        color("&7Max per request: &f" + plugin.getConfig().getInt("claims.expansion.max-increase", 100))
                ));
                item.setItemMeta(meta);
            }
            inv.setItem(slot++, item);
            if (slot % 9 == 8) slot += 2;
        }

        addBackExit(inv); // back→main, exit
        player.openInventory(inv);
    }

    public void handleExpansionRequestClick(Player player, InventoryClickEvent event) {
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        String label = strip(clicked.getItemMeta().getDisplayName());
        if (label == null || !label.startsWith("+")) return;

        // parse amount
        int blocks = 0;
        try {
            blocks = Integer.parseInt(label.replace("+", "").replace("blocks", "").replace(" ", ""));
        } catch (Exception ignored) { }

        if (blocks <= 0) return;

        // record request at player location
        ExpansionRequest req = new ExpansionRequest(player.getUniqueId(), blocks, player.getLocation());
        requestManager.addRequest(req);

        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.2f);
        messages.send(player, plugin.getMessagesConfig().getString("messages.expansion-request",
                "&eYour expansion request for +{blocks} blocks has been sent to admins.")
                .replace("{blocks}", String.valueOf(blocks)));

        // return to main
        openMain(player);
    }

    /* ====================================================================== */
    /* Admin: Expansion Review + Deny Reasons                                 */
    /* ====================================================================== */

    public void openExpansionReview(Player player) {
        if (!player.hasPermission("proshield.admin.expansions")) {
            messages.send(player, "&cYou don’t have permission to review expansion requests.");
            return;
        }

        String title = plugin.getConfig().getString("gui.menus.expansion-requests.title", "&eExpansion Requests");
        int size = plugin.getConfig().getInt("gui.menus.expansion-requests.size", 45);
        Inventory inv = Bukkit.createInventory(player, size, color(title));
        clear(inv);

        List<ExpansionRequest> requests = requestManager.getPendingRequests();
        if (requests.isEmpty()) {
            ItemStack paper = new ItemStack(Material.PAPER);
            ItemMeta meta = paper.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(color("&7No Pending Requests"));
                meta.setLore(Collections.singletonList(color("&7Players can use Request Expansion in main menu")));
                paper.setItemMeta(meta);
            }
            inv.setItem(22, paper);
            addBackExit(inv); // back→Admin Tools, exit
            player.openInventory(inv);
            return;
        }

        int slot = 10;
        for (ExpansionRequest req : requests) {
            OfflinePlayer op = Bukkit.getOfflinePlayer(req.getRequester());
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            ItemMeta meta = head.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(color("&b" + (op.getName() == null ? req.getRequester().toString() : op.getName())));
                List<String> lore = new ArrayList<>();
                lore.add(color("&7Expansion: &f+" + req.getBlocks() + " blocks"));
                long age = (System.currentTimeMillis() - req.getTimestamp()) / 1000;
                lore.add(color("&7Requested &f" + age + "s &7ago"));
                lore.add(color("&aLeft-click: Approve"));
                lore.add(color("&cRight-click: Deny (choose reason)"));
                meta.setLore(lore);
                head.setItemMeta(meta);
            }
            inv.setItem(slot++, head);
            if (slot % 9 == 8) slot += 2;
        }

        addBackExit(inv); // back→Admin Tools, exit
        player.openInventory(inv);
    }

    public void handleExpansionReviewClick(Player player, InventoryClickEvent event) {
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        String who = strip(clicked.getItemMeta().getDisplayName());
        if (who == null || who.isBlank()) return;

        // resolve UUID by name or string
        UUID target = null;
        OfflinePlayer op = Bukkit.getOfflinePlayer(who);
        if (op != null && op.getUniqueId() != null) {
            target = op.getUniqueId();
        } else {
            try { target = UUID.fromString(who); } catch (Exception ignored) {}
        }
        if (target == null) return;

        if (event.isLeftClick()) {
            requestManager.approveRequest(target);
            messages.send(player, "&aExpansion request approved.");
            openExpansionReview(player);
        } else if (event.isRightClick()) {
            pendingDenyTarget.put(player.getUniqueId(), target);
            openDenyReasons(player);
        }
    }

    private void openDenyReasons(Player player) {
        String title = plugin.getConfig().getString("gui.menus.deny-reasons.title", "&cDeny Reasons");
        int size = plugin.getConfig().getInt("gui.menus.deny-reasons.size", 27);

        Inventory inv = Bukkit.createInventory(player, size, color(title));
        clear(inv);

        ConfigurationSection sec = plugin.getMessagesConfig().getConfigurationSection("messages.deny-reasons");
        int slot = 10;
        if (sec != null) {
            for (String key : sec.getKeys(false)) {
                String reason = plugin.getMessagesConfig().getString("messages.deny-reasons." + key, "&c" + key);
                ItemStack paper = new ItemStack(Material.PAPER);
                ItemMeta meta = paper.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(color(reason));
                    meta.setLore(Collections.singletonList(color("&7Click to deny with this reason")));
                    paper.setItemMeta(meta);
                }
                inv.setItem(slot++, paper);
            }
        }

        addBackExit(inv); // back→Expansion Review, exit
        player.openInventory(inv);
    }

    public void handleDenyReasonClick(Player player, InventoryClickEvent event) {
        UUID target = pendingDenyTarget.remove(player.getUniqueId());
        if (target == null) return;

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        // Find key by matching message text
        String display = strip(clicked.getItemMeta().getDisplayName());
        String chosenKey = "custom-1";

        ConfigurationSection sec = plugin.getMessagesConfig().getConfigurationSection("messages.deny-reasons");
        if (sec != null) {
            for (String key : sec.getKeys(false)) {
                String val = strip(color(plugin.getMessagesConfig().getString("messages.deny-reasons." + key, key)));
                if (val.equalsIgnoreCase(display)) {
                    chosenKey = key;
                    break;
                }
            }
        }

        requestManager.denyRequest(target, chosenKey);
        messages.send(player, "&cExpansion request denied.");
        openExpansionReview(player);
    }

    /* ====================================================================== */
    /* Click helpers / UI helpers                                             */
    /* ====================================================================== */

    private void setButton(Inventory inv, int slot, Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(color(name));
            if (lore != null && lore.length > 0) {
                List<String> lines = new ArrayList<>();
                for (String l : lore) lines.add(color(l));
                meta.setLore(lines);
            }
            item.setItemMeta(meta);
        }
        inv.setItem(slot, item);
    }

    private void addBackExit(Inventory inv) {
        int size = inv.getSize();
        setButton(inv, size - 9, Material.ARROW, "&7Back", "&7Return");
        setButton(inv, size - 1, Material.BARRIER, "&cExit", "&7Close this menu");
    }

    private void addExit(Inventory inv) {
        int size = inv.getSize();
        setButton(inv, size - 1, Material.BARRIER, "&cExit", "&7Close this menu");
    }

    private void clear(Inventory inv) {
        for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, null);
    }

    private String color(String s) { return ChatColor.translateAlternateColorCodes('&', s == null ? "" : s); }
    private String strip(String s) { return s == null ? "" : ChatColor.stripColor(s).trim(); }
}
