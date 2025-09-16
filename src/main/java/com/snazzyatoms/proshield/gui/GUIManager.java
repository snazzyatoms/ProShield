package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.expansions.ExpansionRequest;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * GUIManager
 * Central manager for all ProShield menus (main, flags, trusted, roles, admin, expansion).
 * Fully functional (v1.2.5) with Expansion Request menu driven by config step-options.
 */
public class GUIManager {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final ClaimRoleManager roleManager;
    private final MessagesUtil messages;

    public GUIManager(ProShield plugin) {
        this.plugin = plugin;
        this.plotManager = plugin.getPlotManager();
        this.roleManager = plugin.getRoleManager();
        this.messages = plugin.getMessagesUtil();
    }

    /* =========================================================
     * MAIN MENU
     * ========================================================= */
    public void openMain(Player player) {
        String title = plugin.getConfig().getString("gui.menus.main.title", "&6ProShield Menu");
        int size = plugin.getConfig().getInt("gui.menus.main.size", 45);

        Inventory inv = Bukkit.createInventory(player, size, messages.color(title));

        setItem(inv, 10, Material.GRASS_BLOCK, "&aClaim Land", "&7Claim the chunk you are standing in.");
        setItem(inv, 12, Material.PAPER, "&eClaim Info", "&7View details about this claim.");
        setItem(inv, 14, Material.BARRIER, "&cUnclaim Land", "&7Remove your current claim.");
        setItem(inv, 16, Material.PLAYER_HEAD, "&bTrusted Players", "&7Manage trusted players & roles.");
        setItem(inv, 28, Material.REDSTONE_TORCH, "&eClaim Flags", "&7Toggle protection flags.");

        if (plugin.getConfig().getBoolean("claims.expansion.enabled", true)) {
            String reqTitle = plugin.getConfig().getString("gui.menus.expansion-request.title", "&eExpansion Request");
            setItem(inv, 30, Material.EMERALD, "&aRequest Expansion", "&7Request to expand your claim.", "&7Opens: " + ChatColor.stripColor(messages.color(reqTitle)));
        }

        // Admin Tools entry
        setItem(inv, 32, Material.COMMAND_BLOCK, "&cAdmin Tools", "&7Admin-only controls.");

        // Exit button (bottom-right)
        addExit(inv);

        player.openInventory(inv);
    }

    /* =========================================================
     * TRUSTED PLAYERS
     * ========================================================= */
    public void openTrusted(Player player) {
        Plot plot = plotManager.getPlot(player.getLocation());
        if (plot == null) {
            messages.send(player, "&cNo claim here.");
            return;
        }

        String title = plugin.getConfig().getString("gui.menus.roles.title", "&bTrusted Players");
        int size = plugin.getConfig().getInt("gui.menus.roles.size", 45);

        Inventory inv = Bukkit.createInventory(player, size, messages.color(title));

        int slot = 0;
        for (UUID uuid : plot.getTrusted().keySet()) {
            OfflinePlayer trusted = plugin.getServer().getOfflinePlayer(uuid);
            String role = plot.getTrusted().get(uuid);

            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            ItemMeta meta = head.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(messages.color("&f" + (trusted.getName() != null ? trusted.getName() : uuid.toString())));
                meta.setLore(Arrays.asList(
                        messages.color("&7Role: &b" + role),
                        messages.color("&aLeft-click: Assign new role"),
                        messages.color("&cRight-click: Untrust")
                ));
                head.setItemMeta(meta);
            }
            inv.setItem(slot++, head);
        }

        addBackExit(inv, "main");
        player.openInventory(inv);
    }

    public void handleTrustedClick(Player player, InventoryClickEvent event) {
        Plot plot = plotManager.getPlot(player.getLocation());
        if (plot == null) return;

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        String name = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
        if (name == null) return;

        // Back/Exit are globally handled in GUIListener; only player heads reach here.
        OfflinePlayer target = Bukkit.getOfflinePlayer(name);

        if (event.isLeftClick()) {
            openAssignRole(player, target.getUniqueId());
        } else if (event.isRightClick()) {
            plot.untrust(target.getUniqueId());
            messages.send(player, "&cUntrusted &f" + name);
            plotManager.saveAll();
            openTrusted(player);
        }
    }

    /* =========================================================
     * ASSIGN ROLE
     * ========================================================= */
    private final Map<UUID, UUID> pendingRoleAssignments = new HashMap<>();

    private void openAssignRole(Player actor, UUID targetUuid) {
        String title = plugin.getConfig().getString("gui.menus.assign-role.title", "&bAssign Role");
        int size = plugin.getConfig().getInt("gui.menus.assign-role.size", 45);

        Inventory inv = Bukkit.createInventory(actor, size, messages.color(title));

        ConfigurationSection section = plugin.getConfig().getConfigurationSection("roles.available");
        if (section != null) {
            int slot = 0;
            for (String roleKey : section.getKeys(false)) {
                String name = section.getString(roleKey + ".name", roleKey);
                List<String> lore = section.getStringList(roleKey + ".lore");

                ItemStack item = new ItemStack(Material.BOOK);
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(messages.color(name));
                    List<String> coloredLore = new ArrayList<>();
                    for (String line : lore) coloredLore.add(messages.color(line));
                    coloredLore.add(messages.color("&7Click to assign this role"));
                    meta.setLore(coloredLore);
                    item.setItemMeta(meta);
                }
                inv.setItem(slot++, item);
            }
        }

        addBackExit(inv, "roles");
        actor.openInventory(inv);
        pendingRoleAssignments.put(actor.getUniqueId(), targetUuid);
    }

    public void handleAssignRoleClick(Player player, InventoryClickEvent event) {
        UUID targetUuid = pendingRoleAssignments.remove(player.getUniqueId());
        if (targetUuid == null) return;

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        String roleName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
        roleManager.assignRoleViaChat(player, targetUuid, roleName);
        plotManager.saveAll();
        openTrusted(player);
    }

    /* =========================================================
     * CLAIM FLAGS
     * ========================================================= */
    public void openFlags(Player player) {
        Plot plot = plotManager.getPlot(player.getLocation());
        if (plot == null) {
            messages.send(player, "&cNo claim here.");
            return;
        }

        String title = plugin.getConfig().getString("gui.menus.flags.title", "&eClaim Flags");
        int size = plugin.getConfig().getInt("gui.menus.flags.size", 45);

        Inventory inv = Bukkit.createInventory(player, size, messages.color(title));

        ConfigurationSection avail = plugin.getConfig().getConfigurationSection("flags.available");
        if (avail != null) {
            int slot = 0;
            for (String key : avail.getKeys(false)) {
                String path = "flags.available." + key;
                String name = plugin.getConfig().getString(path + ".name", key);
                boolean current = plot.getFlags().getOrDefault(key, plugin.getConfig().getBoolean(path + ".default", false));

                ItemStack item = new ItemStack(current ? Material.LIME_DYE : Material.GRAY_DYE);
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(messages.color(name));
                    meta.setLore(Arrays.asList(
                            messages.color("&7Click to toggle"),
                            messages.color("&fCurrent: " + (current ? "&aEnabled" : "&cDisabled"))
                    ));
                    item.setItemMeta(meta);
                }
                inv.setItem(slot++, item);
            }
        }

        addBackExit(inv, "main");
        player.openInventory(inv);
    }

    public void handleFlagsClick(Player player, InventoryClickEvent event) {
        Plot plot = plotManager.getPlot(player.getLocation());
        if (plot == null) return;

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        String name = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
        if (name == null) return;

        ConfigurationSection avail = plugin.getConfig().getConfigurationSection("flags.available");
        if (avail == null) return;

        for (String key : avail.getKeys(false)) {
            String display = plugin.getConfig().getString("flags.available." + key + ".name", key);
            if (ChatColor.stripColor(messages.color(display)).equalsIgnoreCase(name)) {
                boolean current = plot.getFlags().getOrDefault(key, plugin.getConfig().getBoolean("flags.available." + key + ".default", false));
                boolean newValue = !current;
                plot.setFlag(key, newValue);
                messages.send(player, "&eFlag &f" + key + " &eis now " + (newValue ? "&aEnabled" : "&cDisabled"));
                plotManager.saveAll();
                openFlags(player);
                break;
            }
        }
    }

    /* =========================================================
     * ADMIN TOOLS (basic)
     * ========================================================= */
    public void openAdminTools(Player player) {
        String title = plugin.getConfig().getString("gui.menus.admin-tools.title", "&cAdmin Tools");
        int size = plugin.getConfig().getInt("gui.menus.admin-tools.size", 45);

        Inventory inv = Bukkit.createInventory(player, size, messages.color(title));

        setItem(inv, 10, Material.REPEATER, "&eReload Configs", "&7Reload ProShield configs.");
        setItem(inv, 12, Material.ENDER_EYE, "&aToggle Debug", "&7Enable/disable debug logging.");
        setItem(inv, 14, Material.BARRIER, "&cToggle Bypass", "&7Admin bypass for claims.");

        // Entry to expansion review list (pending requests)
        setItem(inv, 22, Material.PAPER, "&eExpansion Requests", "&7Review pending player requests.");

        addBackExit(inv, "main");
        player.openInventory(inv);
    }

    public void handleAdminClick(Player player, InventoryClickEvent event) {
        ItemStack clicked = event.getCurrentItem();
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
        }
    }

    /* =========================================================
     * EXPANSION REQUEST (PLAYER) — fully functional
     * ========================================================= */
    public void openRequestMenu(Player player) {
        if (!plugin.getConfig().getBoolean("claims.expansion.enabled", true)) {
            messages.send(player, plugin.getMessagesConfig().getString("messages.expansion-disabled", "&cExpansion requests are disabled by the server."));
            return;
        }

        String title = plugin.getConfig().getString("gui.menus.expansion-request.title", "&eExpansion Request");
        int size = Math.max(27, plugin.getConfig().getInt("gui.menus.expansion-request.size", 27));
        Inventory inv = Bukkit.createInventory(player, size, messages.color(title));

        List<Integer> options = plugin.getConfig().getIntegerList("claims.expansion.step-options");
        if (options == null || options.isEmpty()) {
            options = Arrays.asList(25, 50, 100);
        }

        // lay out options starting at slot 10
        int[] slots = new int[]{10, 12, 14, 16, 20, 22, 24};
        for (int i = 0; i < options.size() && i < slots.length; i++) {
            int amount = options.get(i);
            ItemStack item = new ItemStack(amount >= 100 ? Material.NETHERITE_INGOT : (amount >= 50 ? Material.DIAMOND : Material.EMERALD));
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(messages.color("&aRequest +" + amount + " blocks"));
                meta.setLore(Collections.singletonList(messages.color("&7Click to submit this request")));
                item.setItemMeta(meta);
            }
            inv.setItem(slots[i], item);
        }

        addBackExit(inv, "main");
        player.openInventory(inv);
    }

    public void handleExpansionRequestClick(Player player, InventoryClickEvent event) {
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta() || !clicked.getItemMeta().hasDisplayName()) return;

        String name = ChatColor.stripColor(clicked.getItemMeta().getDisplayName()).toLowerCase(Locale.ROOT);

        // Back/Exit handled globally in GUIListener.
        // Parse a number from "Request +{n} blocks"
        int blocks = 0;
        try {
            String digits = name.replaceAll("[^0-9]", "");
            if (!digits.isEmpty()) blocks = Integer.parseInt(digits);
        } catch (Exception ignored) { }

        if (blocks <= 0) return;

        // Respect max-increase
        int maxIncrease = plugin.getConfig().getInt("claims.expansion.max-increase", 100);
        if (blocks > maxIncrease) blocks = maxIncrease;

        ExpansionRequest req = new ExpansionRequest(player.getUniqueId(), blocks, player.getLocation());
        plugin.getExpansionRequestManager().addRequest(req);
        player.closeInventory();
        plugin.getMessagesUtil().send(player, "&aYour expansion request for +" + blocks + " blocks has been submitted!");
    }

    /* =========================================================
     * EXPANSION REVIEW (ADMIN) + DENY REASONS
     * (These were previously provided; keeping them intact)
     * ========================================================= */
    public void openExpansionReview(Player p) {
        String title = plugin.getConfig().getString("gui.menus.expansion-requests.title", "&eExpansion Requests");
        int size = plugin.getConfig().getInt("gui.menus.expansion-requests.size", 45);
        Inventory inv = Bukkit.createInventory(p, size, messages.color(title));

        List<com.snazzyatoms.proshield.expansions.ExpansionRequest> requests =
                plugin.getExpansionRequestManager().getPendingRequests();

        if (requests.isEmpty()) {
            // center a "no requests" barrier
            ItemStack barrier = new ItemStack(Material.BARRIER);
            ItemMeta meta = barrier.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(messages.color("&7No Pending Requests"));
                barrier.setItemMeta(meta);
            }
            inv.setItem(22, barrier);
            addBackExit(inv, "admin-tools");
            p.openInventory(inv);
            return;
        }

        int[] grid = new int[]{10,11,12,13,14,15,16,19,20,21,22,23,24,25,28,29,30,31,32,33,34};
        int i = 0;
        for (com.snazzyatoms.proshield.expansions.ExpansionRequest req : requests) {
            if (i >= grid.length) break;
            OfflinePlayer op = Bukkit.getOfflinePlayer(req.getRequester());

            ItemStack paper = new ItemStack(Material.PAPER);
            ItemMeta meta = paper.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(messages.color("&f" + (op.getName() != null ? op.getName() : op.getUniqueId().toString())));
                List<String> lore = new ArrayList<>();
                lore.add(messages.color("&7Requested: &a+" + req.getBlocks() + " &7blocks"));
                long ageS = Math.max(1, (System.currentTimeMillis() - req.getTimestamp()) / 1000);
                lore.add(messages.color("&7Age: &f" + ageS + "s"));
                lore.add(messages.color("&aLeft-click: Approve"));
                lore.add(messages.color("&cRight-click: Deny"));
                meta.setLore(lore);
                paper.setItemMeta(meta);
            }
            // store player uuid + blocks in display name/lore parsing (kept simple)
            inv.setItem(grid[i++], paper);
        }

        addBackExit(inv, "admin-tools");
        p.openInventory(inv);
    }

    public void handleExpansionReviewClick(Player p, InventoryClickEvent event) {
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta() || !clicked.getItemMeta().hasDisplayName()) return;

        String name = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
        OfflinePlayer target = Bukkit.getOfflinePlayer(name);
        if (target == null || target.getUniqueId() == null) return;

        if (event.isLeftClick()) {
            plugin.getExpansionRequestManager().approveRequest(target.getUniqueId());
            messages.send(p, "&aExpansion request approved for &f" + name);
            openExpansionReview(p);
        } else if (event.isRightClick()) {
            openDenyReasons(p, target.getUniqueId());
        }
    }

    public void openDenyReasons(Player p, UUID targetId) {
        String title = plugin.getConfig().getString("gui.menus.deny-reasons.title", "&cDeny Reasons");
        int size = plugin.getConfig().getInt("gui.menus.deny-reasons.size", 27);
        Inventory inv = Bukkit.createInventory(p, size, messages.color(title));

        ConfigurationSection sec = plugin.getMessagesConfig().getConfigurationSection("messages.deny-reasons");
        if (sec != null) {
            int[] slots = new int[]{10, 11, 12, 13, 14, 15, 16};
            int i = 0;
            for (String key : sec.getKeys(false)) {
                if (i >= slots.length) break;
                String reason = plugin.getMessagesConfig().getString("messages.deny-reasons." + key, "&cDenied.");
                ItemStack item = new ItemStack(Material.PAPER);
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(messages.color(reason));
                    // store UUID|key in lore for quick parsing
                    meta.setLore(Collections.singletonList(targetId.toString() + "|" + key));
                    item.setItemMeta(meta);
                }
                inv.setItem(slots[i++], item);
            }
        }

        addBackExit(inv, "expansion-requests");
        p.openInventory(inv);
    }

    public void handleDenyReasonClick(Player p, InventoryClickEvent event) {
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta() || clicked.getItemMeta().getLore() == null) return;
        List<String> lore = clicked.getItemMeta().getLore();
        if (lore.isEmpty()) return;

        String token = lore.get(0); // UUID|key
        String[] parts = token.split("\\|");
        if (parts.length != 2) return;

        try {
            UUID targetId = UUID.fromString(parts[0]);
            String reasonKey = parts[1];
            plugin.getExpansionRequestManager().denyRequest(targetId, reasonKey);
            messages.send(p, "&cDenied expansion request.");
            openExpansionReview(p);
        } catch (Exception ignored) { }
    }

    /* =========================================================
     * UTILS
     * ========================================================= */
    private void setItem(Inventory inv, int slot, Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(messages.color(name));
            List<String> colored = new ArrayList<>();
            for (String l : lore) colored.add(messages.color(l));
            meta.setLore(colored);
            item.setItemMeta(meta);
        }
        inv.setItem(slot, item);
    }

    private void addBackExit(Inventory inv, String backTargetKey) {
        // Back
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName(messages.color("&eBack"));
            backMeta.setLore(Collections.singletonList(messages.color("&7Return to " + backTargetKey.replace('-', ' '))));
            back.setItemMeta(backMeta);
        }
        inv.setItem(inv.getSize() - 9, back);

        // Exit
        addExit(inv);
    }

    private void addExit(Inventory inv) {
        ItemStack exit = new ItemStack(Material.BARRIER);
        ItemMeta meta = exit.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(messages.color("&cExit"));
            meta.setLore(Collections.singletonList(messages.color("&7Close this menu")));
            exit.setItemMeta(meta);
        }
        inv.setItem(inv.getSize() - 1, exit);
    }
}
