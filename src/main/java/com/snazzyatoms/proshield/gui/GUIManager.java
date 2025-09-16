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

    /* … [Trusted, Assign Role, Flags, Admin Tools menus unchanged — as in your version] … */

    /* ============================
     * ADMIN EXPANSION REVIEW
     * ============================ */
    public void openExpansionReview(Player admin) {
        String title = plugin.getConfig().getString("gui.menus.expansion-requests.title", "&eExpansion Requests");
        int size = plugin.getConfig().getInt("gui.menus.expansion-requests.size", 45);
        Inventory inv = Bukkit.createInventory(admin, size, messages.color(title));

        List<ExpansionRequest> pending = expansionManager.getPendingRequests();
        if (pending.isEmpty()) {
            inv.setItem(22, simpleItem(Material.BARRIER, "&7No Pending Requests", "&7There are no requests to review."));
        } else {
            int slot = 0;
            for (ExpansionRequest req : pending) {
                OfflinePlayer p = Bukkit.getOfflinePlayer(req.getRequester());
                String name = (p != null && p.getName() != null) ? p.getName() : req.getRequester().toString();

                inv.setItem(slot++, simpleItem(Material.LIME_WOOL, "&aApprove: " + name,
                        "&7Blocks: &f" + req.getBlocks(),
                        "&7At: &f" + (req.getRequestedAt() != null ?
                                (req.getRequestedAt().getWorld().getName() + " " +
                                 req.getRequestedAt().getChunk().getX() + "," +
                                 req.getRequestedAt().getChunk().getZ()) : "unknown"),
                        "&7Left-click to approve"));
                inv.setItem(slot++, simpleItem(Material.RED_WOOL, "&cDeny: " + name,
                        "&7Blocks: &f" + req.getBlocks(),
                        "&7Right-click to choose reason"));
            }
        }

        placeNavButtons(inv);
        admin.openInventory(inv);
    }

    public void handleExpansionReviewClick(Player admin, InventoryClickEvent event) {
        ItemStack clicked = event.getCurrentItem();
        if (isBack(clicked)) { openAdminTools(admin); return; }
        if (isExit(clicked)) { admin.closeInventory(); return; }
        if (clicked == null || !clicked.hasItemMeta()) return;

        String dn = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
        if (dn == null) return;

        if (dn.equalsIgnoreCase("No Pending Requests")) return;

        if (dn.startsWith("Approve: ")) {
            String name = dn.substring("Approve: ".length());
            UUID target = resolveNameToUUID(name);
            if (target != null) {
                expansionManager.approveRequest(target);
                messages.send(admin, "&aApproved expansion for &f" + name);
            }
            openExpansionReview(admin);
        } else if (dn.startsWith("Deny: ")) {
            String name = dn.substring("Deny: ".length());
            UUID target = resolveNameToUUID(name);
            if (target != null) {
                pendingDenyTarget.put(admin.getUniqueId(), target);
                openDenyReasons(admin);
            }
        }
    }

    private UUID resolveNameToUUID(String nameOrUuid) {
        try { return UUID.fromString(nameOrUuid); }
        catch (IllegalArgumentException ignored) {}
        OfflinePlayer op = Bukkit.getOfflinePlayer(nameOrUuid);
        return (op != null) ? op.getUniqueId() : null;
    }

    /* ============================
     * DENY REASONS
     * ============================ */
    private final Map<UUID, UUID> pendingDenyTarget = new HashMap<>();

    private void openDenyReasons(Player admin) {
        String title = plugin.getConfig().getString("gui.menus.deny-reasons.title", "&cDeny Reasons");
        int size = plugin.getConfig().getInt("gui.menus.deny-reasons.size", 27);
        Inventory inv = Bukkit.createInventory(admin, size, messages.color(title));

        ConfigurationSection sec = plugin.getConfig().getConfigurationSection("messages.deny-reasons");
        if (sec != null) {
            int slot = 0;
            for (String key : sec.getKeys(false)) {
                inv.setItem(slot++, simpleItem(Material.PAPER, "&fReason: " + key,
                        "&7" + ChatColor.stripColor(messages.color(sec.getString(key)))));
            }
        } else {
            inv.setItem(13, simpleItem(Material.BARRIER, "&7No reasons configured",
                    "&7Add messages.deny-reasons.* in config.yml"));
        }

        placeNavButtons(inv);
        admin.openInventory(inv);
    }

    public void handleDenyReasonClick(Player admin, InventoryClickEvent event) {
        ItemStack clicked = event.getCurrentItem();
        if (isBack(clicked)) { openExpansionReview(admin); return; }
        if (isExit(clicked)) { admin.closeInventory(); return; }
        if (clicked == null || !clicked.hasItemMeta()) return;

        String dn = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
        if (dn == null || !dn.startsWith("Reason: ")) return;

        String key = dn.substring("Reason: ".length()).trim();
        UUID target = pendingDenyTarget.remove(admin.getUniqueId());
        if (target != null) {
            expansionManager.denyRequest(target, key);
            messages.send(admin, "&cDenied expansion for &f" +
                    Optional.ofNullable(Bukkit.getOfflinePlayer(target).getName())
                            .orElse(target.toString().substring(0, 8)) +
                    " &7(" + key + ")");
        }
        openExpansionReview(admin);
    }
}
