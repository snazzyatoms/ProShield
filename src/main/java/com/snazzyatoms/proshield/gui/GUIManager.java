package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
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
 * Central manager for all ProShield menus.
 * Now includes Back + Exit buttons everywhere.
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

    // ============================
    // MAIN MENU
    // ============================
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
            setItem(inv, 30, Material.EMERALD, "&aRequest Expansion", "&7Request to expand your claim.");
        }
        setItem(inv, 32, Material.COMMAND_BLOCK, "&cAdmin Tools", "&7Admin-only controls.");
        setItem(inv, 40, Material.BARRIER, "&cExit", "&7Close this menu."); // Exit button

        player.openInventory(inv);
    }

    // ============================
    // TRUSTED PLAYERS MENU
    // ============================
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

        setItem(inv, 40, Material.ARROW, "&7Back", "&7Return to Main Menu");

        player.openInventory(inv);
    }

    public void handleTrustedClick(Player player, InventoryClickEvent event) {
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        String name = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
        if (name == null) return;

        if (name.equalsIgnoreCase("Back")) {
            openMain(player);
            return;
        }

        Plot plot = plotManager.getPlot(player.getLocation());
        if (plot == null) return;

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

    // ============================
    // ASSIGN ROLE MENU
    // ============================
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

        setItem(inv, 40, Material.ARROW, "&7Back", "&7Return to Trusted Players");

        actor.openInventory(inv);
        pendingRoleAssignments.put(actor.getUniqueId(), targetUuid);
    }

    public void handleAssignRoleClick(Player player, InventoryClickEvent event) {
        String displayName = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());
        if ("Back".equalsIgnoreCase(displayName)) {
            openTrusted(player);
            return;
        }

        UUID targetUuid = pendingRoleAssignments.remove(player.getUniqueId());
        if (targetUuid == null) return;

        roleManager.assignRoleViaChat(player, targetUuid, displayName);
        plotManager.saveAll();
        openTrusted(player);
    }

    private final Map<UUID, UUID> pendingRoleAssignments = new HashMap<>();

    // ============================
    // CLAIM FLAGS MENU
    // ============================
    public void openFlags(Player player) {
        Plot plot = plotManager.getPlot(player.getLocation());
        if (plot == null) {
            messages.send(player, "&cNo claim here.");
            return;
        }

        String title = plugin.getConfig().getString("gui.menus.flags.title", "&eClaim Flags");
        int size = plugin.getConfig().getInt("gui.menus.flags.size", 45);

        Inventory inv = Bukkit.createInventory(player, size, messages.color(title));

        if (plugin.getConfig().isConfigurationSection("flags.available")) {
            int slot = 0;
            for (String key : plugin.getConfig().getConfigurationSection("flags.available").getKeys(false)) {
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

        setItem(inv, 40, Material.ARROW, "&7Back", "&7Return to Main Menu");

        player.openInventory(inv);
    }

    public void handleFlagsClick(Player player, InventoryClickEvent event) {
        String displayName = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());
        if ("Back".equalsIgnoreCase(displayName)) {
            openMain(player);
            return;
        }

        Plot plot = plotManager.getPlot(player.getLocation());
        if (plot == null) return;

        for (String key : plugin.getConfig().getConfigurationSection("flags.available").getKeys(false)) {
            String display = plugin.getConfig().getString("flags.available." + key + ".name", key);
            if (ChatColor.stripColor(messages.color(display)).equalsIgnoreCase(displayName)) {
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

    // ============================
    // ADMIN TOOLS MENU
    // ============================
    public void openAdminTools(Player player) {
        String title = plugin.getConfig().getString("gui.menus.admin-tools.title", "&cAdmin Tools");
        int size = plugin.getConfig().getInt("gui.menus.admin-tools.size", 45);

        Inventory inv = Bukkit.createInventory(player, size, messages.color(title));

        setItem(inv, 10, Material.REPEATER, "&eReload Configs", "&7Reload ProShield configs.");
        setItem(inv, 12, Material.ENDER_EYE, "&aToggle Debug", "&7Enable/disable debug logging.");
        setItem(inv, 14, Material.BARRIER, "&cToggle Bypass", "&7Admin bypass for claims.");
        setItem(inv, 40, Material.ARROW, "&7Back", "&7Return to Main Menu");

        player.openInventory(inv);
    }

    public void handleAdminClick(Player player, InventoryClickEvent event) {
        String displayName = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());
        if ("Back".equalsIgnoreCase(displayName)) {
            openMain(player);
            return;
        }

        // existing admin actions...
    }

    // ============================
    // EXPANSION REVIEW + DENY
    // ============================
    public void handleExpansionReviewClick(Player player, InventoryClickEvent event) {
        String displayName = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());
        if ("Back".equalsIgnoreCase(displayName)) {
            openMain(player);
            return;
        }

        if (displayName.contains("Approve")) {
            plugin.getExpansionRequestManager().approveRequest(player.getUniqueId());
            messages.send(player, "&aExpansion request approved.");
            player.closeInventory();
        } else if (displayName.contains("Deny")) {
            openDenyReasons(player);
        }
    }

    public void handleDenyReasonClick(Player player, InventoryClickEvent event) {
        String displayName = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());
        if ("Back".equalsIgnoreCase(displayName)) {
            openMain(player);
            return;
        }

        for (String reasonKey : plugin.getConfig().getConfigurationSection("messages.deny-reasons").getKeys(false)) {
            String reasonDisplay = plugin.getConfig().getString("messages.deny-reasons." + reasonKey, reasonKey);
            if (ChatColor.stripColor(messages.color(reasonDisplay)).equalsIgnoreCase(displayName)) {
                plugin.getExpansionRequestManager().denyRequest(player.getUniqueId(), reasonKey);
                messages.send(player, "&cExpansion request denied: " + reasonDisplay);
                player.closeInventory();
                return;
            }
        }
    }

    private void openDenyReasons(Player player) {
        String title = plugin.getConfig().getString("gui.menus.deny-reasons.title", "&cDeny Reasons");
        int size = plugin.getConfig().getInt("gui.menus.deny-reasons.size", 27);

        Inventory inv = Bukkit.createInventory(player, size, messages.color(title));

        if (plugin.getConfig().isConfigurationSection("messages.deny-reasons")) {
            int slot = 0;
            for (String key : plugin.getConfig().getConfigurationSection("messages.deny-reasons").getKeys(false)) {
                String display = plugin.getConfig().getString("messages.deny-reasons." + key, key);
                setItem(inv, slot++, Material.PAPER, display, "&7Click to deny with this reason");
            }
        }

        setItem(inv, 26, Material.ARROW, "&7Back", "&7Return to Expansion Review");

        player.openInventory(inv);
    }

    // ============================
    // UTILS
    // ============================
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
}
