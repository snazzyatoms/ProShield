package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.roles.ClaimRole;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import com.snazzyatoms.proshield.roles.RolePermissions;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

public class GUIManager {

    private final ProShield plugin;
    private final ClaimRoleManager roles;

    // store temporary targets for trust/untrust menus
    private final Map<UUID, String> pendingTargets = new HashMap<>();

    public GUIManager(ProShield plugin, ClaimRoleManager roles) {
        this.plugin = plugin;
        this.roles = roles;
    }

    private String getTitle(String path, Map<String, String> placeholders) {
        String raw = plugin.getConfig().getString("gui." + path + ".title", "&c[Missing Title]");
        String colored = ChatColor.translateAlternateColorCodes('&', raw);
        if (placeholders != null) {
            for (Map.Entry<String, String> e : placeholders.entrySet()) {
                colored = colored.replace("{" + e.getKey() + "}", e.getValue());
            }
        }
        return colored;
    }

    /* ====================================================
     * PLAYER MAIN MENU
     * ==================================================== */
    public void openMain(Player player) {
        String title = getTitle("main", null);
        Inventory inv = Bukkit.createInventory(null, 27, title);

        Map<String, Integer> slots = plugin.getConfig().getConfigurationSection("gui.main.slots").getValues(false)
                .entrySet().stream().collect(HashMap::new, (m,e)->m.put(e.getKey(), (Integer) e.getValue()), HashMap::putAll);

        inv.setItem(slots.get("claim"), makeMenuItem(Material.GRASS_BLOCK, ChatColor.GREEN, "Claim Chunk",
                Arrays.asList(ChatColor.GRAY + "Claim your current chunk", ChatColor.GRAY + "Protect your builds"), true));
        inv.setItem(slots.get("unclaim"), makeMenuItem(Material.BARRIER, ChatColor.RED, "Unclaim Chunk",
                Arrays.asList(ChatColor.GRAY + "Unclaim this chunk", ChatColor.GRAY + "Free it"), true));
        inv.setItem(slots.get("info"), makeMenuItem(Material.PAPER, ChatColor.YELLOW, "Claim Info",
                Arrays.asList(ChatColor.GRAY + "View owner & trusted players"), true));
        inv.setItem(slots.get("trust"), makeMenuItem(Material.PLAYER_HEAD, ChatColor.AQUA, "Trust Menu",
                Arrays.asList(ChatColor.GRAY + "Trust a player"), false));
        inv.setItem(slots.get("untrust"), makeMenuItem(Material.SKELETON_SKULL, ChatColor.RED, "Untrust Menu",
                Arrays.asList(ChatColor.GRAY + "Remove a player"), false));
        inv.setItem(slots.get("roles"), makeMenuItem(Material.BOOK, ChatColor.GOLD, "Roles",
                Arrays.asList(ChatColor.GRAY + "Assign roles"), false));
        inv.setItem(slots.get("flags"), makeMenuItem(Material.IRON_SWORD, ChatColor.DARK_RED, "Flags",
                Arrays.asList(ChatColor.GRAY + "Toggle protections"), false));
        inv.setItem(slots.get("back"), makeMenuItem(Material.BARRIER, ChatColor.DARK_RED, "Back",
                Collections.singletonList(ChatColor.GRAY + "Return"), false));

        player.openInventory(inv);
    }

    /* ====================================================
     * ADMIN MAIN MENU
     * ==================================================== */
    public void openAdminMain(Player player) {
        String title = getTitle("admin", null);
        Inventory inv = Bukkit.createInventory(null, 27, title);

        Map<String, Integer> slots = plugin.getConfig().getConfigurationSection("gui.admin.slots").getValues(false)
                .entrySet().stream().collect(HashMap::new, (m,e)->m.put(e.getKey(), (Integer) e.getValue()), HashMap::putAll);

        inv.setItem(slots.get("trust"), makeMenuItem(Material.PLAYER_HEAD, ChatColor.AQUA, "Trust Menu",
                Arrays.asList(ChatColor.GRAY + "Trust/untrust players"), false));
        inv.setItem(slots.get("roles"), makeMenuItem(Material.BOOK, ChatColor.GOLD, "Roles",
                Arrays.asList(ChatColor.GRAY + "Manage roles"), false));
        inv.setItem(slots.get("flags"), makeMenuItem(Material.IRON_SWORD, ChatColor.DARK_RED, "Flags",
                Arrays.asList(ChatColor.GRAY + "Manage protections"), false));
        inv.setItem(slots.get("back"), makeMenuItem(Material.BARRIER, ChatColor.DARK_RED, "Back",
                Collections.singletonList(ChatColor.GRAY + "Return"), false));

        player.openInventory(inv);
    }

    /* ====================================================
     * ROLES OVERVIEW
     * ==================================================== */
    public void openRolesGUI(Player player, Plot plot, boolean fromAdmin) {
        String title = getTitle("roles", null);
        Inventory inv = Bukkit.createInventory(null, 27, title);

        List<String> trusted = (plot != null) ? new ArrayList<>(plot.getTrustedNames()) : Collections.emptyList();
        int slot = 10;
        if (!trusted.isEmpty()) {
            for (String name : trusted.stream().limit(15).toList()) {
                UUID claimId = plot.getId();
                UUID targetId = Bukkit.getOfflinePlayer(name).getUniqueId();
                ClaimRole role = roles.getRole(claimId, targetId);
                String roleName = (role != null) ? role.getDisplayName() : "Trusted";

                inv.setItem(slot++, makeSkull(name, ChatColor.YELLOW, name,
                        Arrays.asList(ChatColor.GRAY + "Click to manage roles",
                                ChatColor.YELLOW + "Current Role: " + roleName)));
                if (slot == 17) slot = 19;
            }
        } else {
            inv.setItem(13, makeMenuItem(Material.BARRIER, ChatColor.GRAY, "No Trusted Players",
                    Collections.singletonList(ChatColor.DARK_GRAY + "Trust someone first"), false));
        }

        inv.setItem(26, makeMenuItem(Material.BARRIER, ChatColor.DARK_RED, "Back",
                Collections.singletonList(ChatColor.GRAY + "Return"), false));

        player.openInventory(inv);
    }

    /* ====================================================
     * INFO MENU
     * ==================================================== */
    public void openInfoMenu(Player player, Plot plot) {
        String title = getTitle("info", null);
        Inventory inv = Bukkit.createInventory(null, 27, title);

        if (plot == null) {
            inv.setItem(13, makeMenuItem(Material.BARRIER, ChatColor.RED, "No Claim",
                    Arrays.asList(ChatColor.GRAY + "You are not inside a claim."), false));
        } else {
            inv.setItem(11, makeMenuItem(Material.PLAYER_HEAD, ChatColor.GREEN, "Owner",
                    Arrays.asList(ChatColor.GRAY + "Owner: " + Bukkit.getOfflinePlayer(plot.getOwner()).getName()), false));
            inv.setItem(13, makeMenuItem(Material.BOOK, ChatColor.AQUA, "Claim Name",
                    Arrays.asList(ChatColor.GRAY + plot.getDisplayNameSafe()), false));
            inv.setItem(15, makeMenuItem(Material.PAPER, ChatColor.GOLD, "Trusted Players",
                    Arrays.asList(ChatColor.GRAY + String.join(", ", new ArrayList<>(plot.getTrustedNames()))), false));
        }

        inv.setItem(26, makeMenuItem(Material.BARRIER, ChatColor.DARK_RED, "Back",
                Collections.singletonList(ChatColor.GRAY + "Return"), false));

        player.openInventory(inv);
    }

    /* ====================================================
     * HELPERS
     * ==================================================== */
    private ItemStack makeMenuItem(Material mat, ChatColor color, String name, List<String> lore, boolean hideAttrs) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(color + name);
            meta.setLore(lore);
            if (hideAttrs) meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack makeSkull(String playerName, ChatColor color, String display, List<String> lore) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(color + display);
            meta.setLore(lore);
            OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);
            meta.setOwningPlayer(target);
            skull.setItemMeta(meta);
        }
        return skull;
    }

    public void rememberTarget(Player player, String targetName) {
        pendingTargets.put(player.getUniqueId(), targetName);
    }

    public String getRememberedTarget(Player player) {
        return pendingTargets.get(player.getUniqueId());
    }
}
