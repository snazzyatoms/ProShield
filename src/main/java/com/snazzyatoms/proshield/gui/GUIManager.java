package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.expansions.ExpansionRequest;
import com.snazzyatoms.proshield.expansions.ExpansionRequestManager;
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
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * GUIManager (v1.2.5-persist-mobs)
 * - Main, Trusted, Assign Role, Flags, Admin Tools, Expansion Request (player), Expansion Review (admin), Deny Reasons
 * - Claim Info is tooltip-only (no chat spam).
 * - Back/Exit are in every menu and are safe to click.
 */
public class GUIManager {

    private static final String HIDDEN_UUID_TAG = "\u00A78#UUID:"; // §8#UUID:

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final ClaimRoleManager roleManager;
    private final ExpansionRequestManager expansionManager;
    private final MessagesUtil messages;

    public GUIManager(ProShield plugin) {
        this.plugin = plugin;
        this.plotManager = plugin.getPlotManager();
        this.roleManager = plugin.getRoleManager();
        this.expansionManager = plugin.getExpansionRequestManager();
        this.messages = plugin.getMessagesUtil();
    }

    /* ---------- Buttons ---------- */
    private ItemStack backButton() {
        return simpleItem(Material.ARROW, "&eBack", "&7Return to previous menu");
    }
    private ItemStack exitButton() {
        return simpleItem(Material.BARRIER, "&cExit", "&7Close this menu");
    }
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
        String s = ChatColor.stripColor(item.getItemMeta().getDisplayName());
        if (s == null) return false;
        s = s.trim().toLowerCase(Locale.ROOT);
        String n = needle.trim().toLowerCase(Locale.ROOT);
        return s.equals(n);
    }
    private boolean isBack(ItemStack item) { return isNamed(item, "back"); }
    private boolean isExit(ItemStack item) { return isNamed(item, "exit"); }

    /* ============================
     * MAIN MENU
     * ============================ */
    public void openMain(Player player) {
        String title = plugin.getConfig().getString("gui.menus.main.title", "&6ProShield Menu");
        int size = plugin.getConfig().getInt("gui.menus.main.size", 45);
        Inventory inv = Bukkit.createInventory(player, size, messages.color(title));

        inv.setItem(10, simpleItem(Material.GRASS_BLOCK, "&aClaim Land", "&7Claim the chunk you are standing in."));
        inv.setItem(12, buildClaimInfoItem(player));
        inv.setItem(14, simpleItem(Material.BARRIER, "&cUnclaim Land", "&7Remove your current claim."));
        inv.setItem(16, simpleItem(Material.PLAYER_HEAD, "&bTrusted Players", "&7Manage trusted players & roles."));
        inv.setItem(28, simpleItem(Material.REDSTONE_TORCH, "&eClaim Flags", "&7Toggle protection flags."));

        if (plugin.getConfig().getBoolean("claims.expansion.enabled", true)) {
            inv.setItem(30, simpleItem(Material.EMERALD, "&aRequest Expansion", "&7Request to expand your claim."));
        }

        inv.setItem(32, simpleItem(Material.COMMAND_BLOCK, "&cAdmin Tools", "&7Admin-only controls."));

        placeNavButtons(inv);
        player.openInventory(inv);
    }

    private ItemStack buildClaimInfoItem(Player player) {
        Plot plot = plotManager.getPlot(player.getLocation());
        List<String> lore = new ArrayList<>();
        if (plot == null) {
            lore.add("&7No claim here.");
        } else {
            OfflinePlayer owner = plugin.getServer().getOfflinePlayer(plot.getOwner());
            String ownerName = owner.getName() != null ? owner.getName() : owner.getUniqueId().toString();
            lore.add("&7World: &f" + plot.getWorld());
            lore.add("&7Chunk: &f" + plot.getX() + ", " + plot.getZ());
            lore.add("&7Owner: &f" + ownerName);
            lore.add("&7Radius: &f" + plot.getRadius() + " blocks");
            lore.add("&7Y-range: &f" +
                    plugin.getConfig().getInt("claims.min-y", 0) + " - " +
                    plugin.getConfig().getInt("claims.max-y", 200));
            lore.add("&7Flags:");
            if (plugin.getConfig().isConfigurationSection("flags.available")) {
                for (String key : plugin.getConfig().getConfigurationSection("flags.available").getKeys(false)) {
                    boolean state = plot.getFlags().getOrDefault(
                            key, plugin.getConfig().getBoolean("flags.available." + key + ".default", false));
                    String nice = ChatColor.stripColor(messages.color(
                            plugin.getConfig().getString("flags.available." + key + ".name", key)));
                    lore.add((state ? "&a  ✔ " : "&c  ✖ ") + "&7" + nice);
                }
            }
            lore.add("&8(Click disabled – info only)");
        }
        return simpleItem(Material.PAPER, "&eClaim Info", lore.toArray(new String[0]));
    }

    /* ============================
     * TRUSTED PLAYERS MENU
     * ============================ */
    public void openTrusted(Player player) {
        String title = plugin.getConfig().getString("gui.menus.roles.title", "&bTrusted Players");
        int size = plugin.getConfig().getInt("gui.menus.roles.size", 45);
        Inventory inv = Bukkit.createInventory(player, size, messages.color(title));

        Plot plot = plotManager.getPlot(player.getLocation());
        if (plot == null) {
            inv.setItem(13, simpleItem(Material.BARRIER, "&cNo claim here", "&7Stand inside your claim to manage roles."));
            placeNavButtons(inv);
            player.openInventory(inv);
            return;
        }

        int slot = 0;
        for (Map.Entry<UUID,String> e : plot.getTrusted().entrySet()) {
            UUID uuid = e.getKey();
            if (uuid.equals(plot.getOwner()) || uuid.equals(player.getUniqueId())) continue;

            OfflinePlayer trusted = plugin.getServer().getOfflinePlayer(uuid);
            String display = (trusted != null && trusted.getName() != null)
                    ? trusted.getName()
                    : uuid.toString().substring(0, 8);

            List<String> lore = new ArrayList<>();
            lore.add(messages.color("&7Role: &b" + e.getValue()));
            lore.add(messages.color("&aLeft-click: Assign new role"));
            lore.add(messages.color("&cRight-click: Untrust"));
            lore.add(HIDDEN_UUID_TAG + uuid);

            ItemStack head = simpleItem(Material.PLAYER_HEAD, "&f" + display, lore.toArray(new String[0]));
            inv.setItem(slot++, head);
        }

        placeNavButtons(inv);
        player.openInventory(inv);
    }

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

    public void handleTrustedClick(Player player, InventoryClickEvent event) {
        ItemStack clicked = event.getCurrentItem();
        if (isBack(clicked)) { openMain(player); return; }
        if (isExit(clicked)) { player.closeInventory(); return; }
        if (clicked == null || !clicked.hasItemMeta()) return;

        Plot plot = plotManager.getPlot(player.getLocation());
        if (plot == null) return;

        UUID targetUuid = extractHiddenUuid(clicked);
        if (targetUuid == null) return;

        if (event.isLeftClick()) {
            openAssignRole(player, targetUuid);
        } else if (event.isRightClick()) {
            plot.untrust(targetUuid);
            String name = Optional.ofNullable(Bukkit.getOfflinePlayer(targetUuid).getName())
                    .orElse(targetUuid.toString().substring(0, 8));
            messages.send(player, "&cUntrusted &f" + name);
            plotManager.saveAll();
            openTrusted(player);
        }
    }

    /* ============================
     * ASSIGN ROLE MENU
     * ============================ */
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
                List<String> coloredLore = new ArrayList<>();
                for (String line : lore) coloredLore.add(messages.color(line));
                coloredLore.add(messages.color("&7Click to assign this role"));

                ItemStack item = simpleItem(Material.BOOK, name, coloredLore.toArray(new String[0]));
                inv.setItem(slot++, item);
            }
        }

        placeNavButtons(inv);
        actor.openInventory(inv);
        pendingRoleAssignments.put(actor.getUniqueId(), targetUuid);
    }

    public void clearPendingRoleAssignment(UUID actor) {
        pendingRoleAssignments.remove(actor);
    }

    public void handleAssignRoleClick(Player player, InventoryClickEvent event) {
        ItemStack clicked = event.getCurrentItem();
        if (isBack(clicked)) { clearPendingRoleAssignment(player.getUniqueId()); openTrusted(player); return; }
        if (isExit(clicked)) { clearPendingRoleAssignment(player.getUniqueId()); player.closeInventory(); return; }

        UUID targetUuid = pendingRoleAssignments.remove(player.getUniqueId());
        if (targetUuid == null) return;

        if (clicked == null || !clicked.hasItemMeta()) return;
        String roleName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
        if (roleName == null) return;

        roleManager.assignRoleViaChat(player, targetUuid, roleName);
        plotManager.saveAll();
        openTrusted(player);
    }

    /* ============================
     * CLAIM FLAGS MENU
     * ============================ */
    public void openFlags(Player player) {
        String title = plugin.getConfig().getString("gui.menus.flags.title", "&eClaim Flags");
        int size = plugin.getConfig().getInt("gui.menus.flags.size", 45);
        Inventory inv = Bukkit.createInventory(player, size, messages.color(title));

        Plot plot = plotManager.getPlot(player.getLocation());
        if (plot == null) {
            inv.setItem(13, simpleItem(Material.BARRIER, "&cNo claim here", "&7Stand inside your claim to manage flags."));
            placeNavButtons(inv);
            player.openInventory(inv);
            return;
        }

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

        placeNavButtons(inv);
        player.openInventory(inv);
    }

    public void handleFlagsClick(Player player, InventoryClickEvent event) {
        ItemStack clicked = event.getCurrentItem();
        if (isBack(clicked)) { openMain(player); return; }
        if (isExit(clicked)) { player.closeInventory(); return; }

        Plot plot = plotManager.getPlot(player.getLocation());
        if (plot == null) return;

        if (clicked == null || !clicked.hasItemMeta()) return;

        String name = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
        if (name == null) return;

        for (String key : plugin.getConfig().getConfigurationSection("flags.available").getKeys(false)) {
            String display = plugin.getConfig().getString("flags.available." + key + ".name", key);
            if (ChatColor.stripColor(messages.color(display)).equalsIgnoreCase(name)) {
                boolean current = plot.getFlags().getOrDefault(key,
                        plugin.getConfig().getBoolean("flags.available." + key + ".default", false));
                boolean newValue = !current;
                plot.setFlag(key, newValue);
                messages.send(player, "&eFlag &f" + key + " &eis now " + (newValue ? "&aEnabled" : "&cDisabled"));
                plotManager.saveAll();
                openFlags(player);
                break;
            }
        }
    }

    /* ============================
     * ADMIN TOOLS
     * ============================ */
    public void openAdminTools(Player player) {
        String title = plugin.getConfig().getString("gui.menus.admin-tools.title", "&cAdmin Tools");
        int size = plugin.getConfig().getInt("gui.menus.admin-tools.size", 45);
        Inventory inv = Bukkit.createInventory(player, size, messages.color(title));

        inv.setItem(10, simpleItem(Material.REPEATER, "&eReload Configs", "&7Reload ProShield configs."));
        inv.setItem(12, simpleItem(Material.ENDER_EYE, "&aToggle Debug", "&7Enable/disable debug logging."));
        inv.setItem(14, simpleItem(Material.BARRIER, "&cToggle Bypass", "&7Admin bypass for claims."));
        inv.setItem(16, simpleItem(Material.EMERALD, "&eExpansion Requests", "&7Review pending player requests."));

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

    /* ============================
     * PLAYER REQUEST EXPANSION
     * ============================ */
    public void openRequestMenu(Player player) {
        plugin.getExpansionRequestManager().openPlayerRequestMenu(player);
    }

    /* ============================
     * ADMIN EXPANSION REVIEW
     * ============================ */
   } else {
            int slot = 0;
            for (ExpansionRequest req : pending) {
                OfflinePlayer p = Bukkit.getOfflinePlayer(req.getRequester());
                String name = p.getName() != null ? p.getName() : p.getUniqueId().toString();

                List<String> lore = new ArrayList<>();
                lore.add(messages.color("&7Blocks: &f" + req.getBlocks()));
                lore.add(messages.color("&7Requested: &f" + new Date(req.getTimestamp())));
                lore.add(messages.color("&aLeft-click: Approve"));
                lore.add(messages.color("&cRight-click: Deny"));

                ItemStack paper = new ItemStack(Material.PAPER);
                ItemMeta meta = paper.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(messages.color("&f" + name));
                    meta.setLore(lore);
                    paper.setItemMeta(meta);
                }
                inv.setItem(slot++, paper);
            }
        }

        placeNavButtons(inv);
        admin.openInventory(inv);
    }
}
