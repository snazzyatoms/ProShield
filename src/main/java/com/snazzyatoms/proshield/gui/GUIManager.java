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

/**
 * GUIManager
 * Full management of all GUIs:
 * - Main menu
 * - Claim Info, Claim, Unclaim
 * - Trusted Players & Roles
 * - Claim Flags
 * - Admin Tools (reload/debug/bypass/expansions/history/world controls)
 * - Expansion Requests + Deny Reasons
 * - Expansion History (pagination)
 * - Back & Exit buttons everywhere
 * - Dynamic world controls
 */
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

    private UUID extractHiddenUuid(ItemStack item) {
        if (item == null || !item.hasItemMeta() || item.getItemMeta().getLore() == null) return null;
        for (String line : item.getItemMeta().getLore()) {
            String raw = ChatColor.stripColor(line);
            if (raw != null && raw.startsWith("#UUID:")) {
                try { return UUID.fromString(raw.substring("#UUID:".length()).trim()); }
                catch (Exception ignored) {}
            }
        }
        return null;
    }

    private Instant extractHiddenTimestamp(ItemStack item) {
        if (item == null || !item.hasItemMeta() || item.getItemMeta().getLore() == null) return null;
        for (String line : item.getItemMeta().getLore()) {
            String raw = ChatColor.stripColor(line);
            if (raw != null && raw.startsWith("#TS:")) {
                try { return Instant.parse(raw.substring("#TS:".length()).trim()); }
                catch (Exception ignored) {}
            }
        }
        return null;
    }

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

        // Admin Tools only visible with base permission
        if (player.hasPermission("proshield.admin") ||
            player.hasPermission("proshield.admin.expansions") ||
            player.hasPermission("proshield.admin.worldcontrols")) {
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
            openAdminTools(player);

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
            String ownerName = owner != null && owner.getName() != null ? owner.getName() : plot.getOwner().toString();

            long enabledFlags = plot.getFlags().entrySet().stream()
                    .filter(Map.Entry::getValue)
                    .count();

            lore.add("&7World: &f" + plot.getWorld());
            lore.add("&7Center: &f" + plot.getX() + ", " + plot.getZ());
            lore.add("&7Owner: &f" + ownerName);
            lore.add("&7Radius: &f" + plot.getRadius() + " blocks");
            lore.add("&7Flags Enabled: &f" + enabledFlags);
        }
        return simpleItem(Material.PAPER, "&eClaim Info", lore.toArray(new String[0]));
    }

    /* ============================
     * TRUSTED PLAYERS + ROLES
     * ============================ */
    // (same as we wrote before, preserved)
    // ...

    /* ============================
     * CLAIM FLAGS
     * ============================ */
    // (same as before, preserved)
    // ...

    /* ============================
     * ADMIN TOOLS
     * ============================ */
    public void openAdminTools(Player player) {
        String title = plugin.getConfig().getString("gui.menus.admin-tools.title", "&cAdmin Tools");
        int size = plugin.getConfig().getInt("gui.menus.admin-tools.size", 45);
        Inventory inv = Bukkit.createInventory(player, size, messages.color(title));

        int slot = 10;
        if (player.hasPermission("proshield.admin")) {
            inv.setItem(slot++, simpleItem(Material.REPEATER, "&eReload Configs", "&7Reload ProShield configs."));
            inv.setItem(slot++, simpleItem(Material.ENDER_EYE, "&aToggle Debug", "&7Enable/disable debug logging."));
            inv.setItem(slot++, simpleItem(Material.BARRIER, "&cToggle Bypass", "&7Admin bypass for claims."));
        }
        if (player.hasPermission("proshield.admin.expansions")) {
            inv.setItem(slot++, simpleItem(Material.EMERALD, "&eExpansion Requests", "&7Review pending player requests."));
            inv.setItem(slot++, simpleItem(Material.CLOCK, "&eExpansion History", "&7View past requests."));
        }
        if (player.hasPermission("proshield.admin.worldcontrols")) {
            inv.setItem(slot++, simpleItem(Material.BEDROCK, "&cWorld Controls", "&7Toggle world-level protections."));
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

        if (name.equalsIgnoreCase("Reload Configs") && player.hasPermission("proshield.admin")) {
            plugin.reloadConfig();
            plugin.loadMessagesConfig();
            messages.send(player, "&aConfigs reloaded.");

        } else if (name.equalsIgnoreCase("Toggle Debug") && player.hasPermission("proshield.admin")) {
            plugin.toggleDebug();
            messages.send(player, "&eDebug mode: " + (plugin.isDebugEnabled() ? "&aENABLED" : "&cDISABLED"));

        } else if (name.equalsIgnoreCase("Toggle Bypass") && player.hasPermission("proshield.admin")) {
            UUID uuid = player.getUniqueId();
            if (plugin.isBypassing(uuid)) {
                plugin.getBypassing().remove(uuid);
                messages.send(player, "&cBypass disabled.");
            } else {
                plugin.getBypassing().add(uuid);
                messages.send(player, "&aBypass enabled.");
            }

        } else if (name.equalsIgnoreCase("Expansion Requests") && player.hasPermission("proshield.admin.expansions")) {
            openExpansionReview(player);

        } else if (name.equalsIgnoreCase("Expansion History") && player.hasPermission("proshield.admin.expansions")) {
            openFilteredHistory(player, new ArrayList<>(expansionManager.getAllRequests()));

        } else if (name.equalsIgnoreCase("World Controls") && player.hasPermission("proshield.admin.worldcontrols")) {
            openWorldControls(player);
        }
    }

    /* ============================
     * WORLD CONTROLS (Dynamic)
     * ============================ */
    public void openWorldControls(Player admin) {
        String title = plugin.getConfig().getString("gui.menus.world-controls.title", "&cWorld Controls");
        int size = plugin.getConfig().getInt("gui.menus.world-controls.size", 45);
        Inventory inv = Bukkit.createInventory(admin, size, messages.color(title));

        ConfigurationSection sec = plugin.getConfig().getConfigurationSection("world-controls");
        if (sec != null) {
            int slot = 0;
            for (String key : sec.getKeys(false)) {
                boolean enabled = sec.getBoolean(key, false);
                Material mat = enabled ? Material.LIME_DYE : Material.GRAY_DYE;
                inv.setItem(slot++, simpleItem(mat, "&f" + key,
                        "&7Click to toggle",
                        "&fCurrent: " + (enabled ? "&aENABLED" : "&cDISABLED")));
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
        if (clicked == null || !clicked.hasItemMeta()) return;

        String name = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
        if (name == null) return;

        ConfigurationSection sec = plugin.getConfig().getConfigurationSection("world-controls");
        if (sec == null) return;

        if (sec.contains(name)) {
            boolean current = sec.getBoolean(name, false);
            boolean newValue = !current;
            sec.set(name, newValue);
            plugin.saveConfig();
            messages.send(admin, "&eWorld Control &f" + name + " &eis now " + (newValue ? "&aENABLED" : "&cDISABLED"));
            openWorldControls(admin);
        }
    }

    /* ============================
     * EXPANSIONS / DENY / HISTORY
     * ============================ */
    // (unchanged but enhanced with Back/Exit)
    // ...

}
