package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.cache.GUICache;
import com.snazzyatoms.proshield.plots.Plot;
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
    private final GUICache cache;
    private final ClaimRoleManager roles;

    // store temporary targets for trust/untrust menus
    private final Map<UUID, String> pendingTargets = new HashMap<>();

    public GUIManager(ProShield plugin, GUICache cache, ClaimRoleManager roles) {
        this.plugin = plugin;
        this.cache = cache;
        this.roles = roles;
    }

    /* ====================================================
     * PLAYER MAIN MENU
     * ==================================================== */
    public void openMain(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.GREEN + "ProShield Menu");

        inv.setItem(10, makeMenuItem(Material.GRASS_BLOCK, ChatColor.GREEN, "Claim Chunk",
                Arrays.asList(ChatColor.GRAY + "Claim your current chunk",
                        ChatColor.GRAY + "Protect your builds from griefers"), true));

        inv.setItem(11, makeMenuItem(Material.BARRIER, ChatColor.RED, "Unclaim Chunk",
                Arrays.asList(ChatColor.GRAY + "Unclaim your current chunk",
                        ChatColor.GRAY + "Free up space for others"), true));

        inv.setItem(12, makeMenuItem(Material.PAPER, ChatColor.YELLOW, "Claim Info",
                Arrays.asList(ChatColor.GRAY + "View claim owner and trusted players"), true));

        inv.setItem(13, makeMenuItem(Material.PLAYER_HEAD, ChatColor.AQUA, "Trust Menu",
                Arrays.asList(ChatColor.GRAY + "Trust a player into your claim"), false));

        inv.setItem(14, makeMenuItem(Material.SKELETON_SKULL, ChatColor.RED, "Untrust Menu",
                Arrays.asList(ChatColor.GRAY + "Remove a player from your claim"), false));

        inv.setItem(15, makeMenuItem(Material.BOOK, ChatColor.GOLD, "Roles",
                Arrays.asList(ChatColor.GRAY + "Assign roles to trusted players"), false));

        inv.setItem(16, makeMenuItem(Material.IRON_SWORD, ChatColor.DARK_RED, "Flags",
                Arrays.asList(ChatColor.GRAY + "Toggle protections like TNT, fire, PvP"), false));

        inv.setItem(26, makeMenuItem(Material.BARRIER, ChatColor.DARK_RED, "Back",
                Collections.singletonList(ChatColor.GRAY + "Return to previous menu"), false));

        player.openInventory(inv);
    }

    /* ====================================================
     * ADMIN MAIN MENU
     * ==================================================== */
    public void openAdminMain(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.RED + "ProShield Admin Menu");

        inv.setItem(11, makeMenuItem(Material.PLAYER_HEAD, ChatColor.AQUA, "Trust Menu",
                Arrays.asList(ChatColor.GRAY + "Trust/untrust players in any claim"), false));

        inv.setItem(13, makeMenuItem(Material.BOOK, ChatColor.GOLD, "Roles",
                Arrays.asList(ChatColor.GRAY + "Manage roles for any claim"), false));

        inv.setItem(15, makeMenuItem(Material.IRON_SWORD, ChatColor.DARK_RED, "Flags",
                Arrays.asList(ChatColor.GRAY + "Manage flags for any claim"), false));

        inv.setItem(26, makeMenuItem(Material.BARRIER, ChatColor.DARK_RED, "Back",
                Collections.singletonList(ChatColor.GRAY + "Return to previous menu"), false));

        player.openInventory(inv);
    }

    /* ====================================================
     * ROLES OVERVIEW (trusted skulls)
     * ==================================================== */
    public void openRolesGUI(Player player, Plot plot, boolean fromAdmin) {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.GOLD + "Manage Roles");

        List<String> trusted = (plot != null) ? plot.getTrustedNames() : Collections.emptyList();

        int slot = 10;
        if (!trusted.isEmpty()) {
            for (String name : trusted.stream().limit(15).toList()) {
                UUID claimId = plot.getId();
                UUID targetId = Bukkit.getOfflinePlayer(name).getUniqueId();
                String role = roles.getRole(claimId, targetId);
                if (role == null || role.isEmpty()) role = "Trusted";

                inv.setItem(slot++, makeSkull(name, ChatColor.YELLOW, name,
                        Arrays.asList(ChatColor.GRAY + "Click to manage roles",
                                ChatColor.YELLOW + "Current Role: " + role)));
                if (slot == 17) slot = 19;
            }
        } else {
            inv.setItem(13, makeMenuItem(Material.BARRIER, ChatColor.GRAY, "No Trusted Players",
                    Collections.singletonList(ChatColor.DARK_GRAY + "Trust someone first to manage roles"), false));
        }

        inv.setItem(26, makeMenuItem(Material.BARRIER, ChatColor.DARK_RED, "Back",
                Collections.singletonList(ChatColor.GRAY + "Return to previous menu"), false));

        player.openInventory(inv);
    }

    /* ====================================================
     * ROLE ASSIGNMENT MENU
     * ==================================================== */
    public void openRoleAssignmentMenu(Player player, Plot plot, String targetName, boolean fromAdmin) {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.AQUA + "Roles for " + targetName);

        UUID claimId = plot.getId();
        UUID targetId = Bukkit.getOfflinePlayer(targetName).getUniqueId();
        String current = roles.getRole(claimId, targetId);
        if (current == null || current.isEmpty()) current = "Trusted";

        inv.setItem(10, makeMenuItem(Material.STONE_PICKAXE, ChatColor.YELLOW, "Builder",
                Arrays.asList(ChatColor.GRAY + "Build & break blocks",
                        ChatColor.RED + "No flag/role management",
                        ChatColor.YELLOW + "Click to assign",
                        ChatColor.GRAY + "Current: " + current), true));

        inv.setItem(12, makeMenuItem(Material.CROSSBOW, ChatColor.RED, "Moderator",
                Arrays.asList(ChatColor.GRAY + "Manage trusted players",
                        ChatColor.GRAY + "Toggle claim flags (if allowed)",
                        ChatColor.RED + "Cannot unclaim land (unless allowed)",
                        ChatColor.YELLOW + "Click to assign",
                        ChatColor.GRAY + "Current: " + current), true));

        inv.setItem(14, makeMenuItem(Material.BOOK, ChatColor.GRAY, "Clear Role",
                Arrays.asList(ChatColor.GRAY + "Revert to default trusted",
                        ChatColor.YELLOW + "Click to clear role",
                        ChatColor.GRAY + "Current: " + current), false));

        inv.setItem(22, makeMenuItem(Material.LEVER, ChatColor.AQUA, "Role Flags",
                Arrays.asList(ChatColor.GRAY + "Customize permissions granted",
                        ChatColor.GRAY + "to this role in this claim."), false));

        inv.setItem(26, makeMenuItem(Material.BARRIER, ChatColor.DARK_RED, "Back",
                Collections.singletonList(ChatColor.GRAY + "Return to Roles Menu"), false));

        player.openInventory(inv);
    }

    /* ====================================================
     * ROLE FLAGS MENU
     * ==================================================== */
    public void openRoleFlagsMenu(Player owner, Plot plot, String roleId) {
        String key = (roleId == null || roleId.isEmpty() || roleId.equalsIgnoreCase("Trusted")) ? "trusted" : roleId.toLowerCase();
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.AQUA + "Role Flags: " + key);

        UUID claimId = plot.getId();
        RolePermissions perms = roles.getRolePermissions(claimId, key);

        inv.setItem(10, makeMenuItem(Material.STONE_PICKAXE, ChatColor.GREEN, "Build Blocks",
                Arrays.asList(ChatColor.GRAY + "Allow building & breaking",
                        state(perms.canBuild())), true));

        inv.setItem(11, makeMenuItem(Material.CHEST, ChatColor.YELLOW, "Open Containers",
                Arrays.asList(ChatColor.GRAY + "Access chests, furnaces, etc.",
                        state(perms.canContainers())), false));

        inv.setItem(12, makeMenuItem(Material.BOOK, ChatColor.AQUA, "Manage Trust",
                Arrays.asList(ChatColor.GRAY + "Allow trust/untrust players",
                        state(perms.canManageTrust())), false));

        inv.setItem(13, makeMenuItem(Material.BARRIER, ChatColor.RED, "Unclaim Land",
                Arrays.asList(ChatColor.GRAY + "Allow this role to unclaim",
                        state(perms.canUnclaim())), false));

        inv.setItem(26, makeMenuItem(Material.BARRIER, ChatColor.DARK_RED, "Back",
                Collections.singletonList(ChatColor.GRAY + "Return to Roles Menu"), false));

        owner.openInventory(inv);
    }

    /* ====================================================
     * TRUST / UNTRUST / FLAGS / INFO MENUS
     * ==================================================== */
    public void openTrustMenu(Player player, boolean fromAdmin) {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.AQUA + "Trust Player");
        inv.setItem(13, makeMenuItem(Material.PLAYER_HEAD, ChatColor.GREEN, "Enter Player",
                Arrays.asList(ChatColor.GRAY + "Type the player’s name in chat",
                        ChatColor.GRAY + "to trust them."), false));
        inv.setItem(26, makeMenuItem(Material.BARRIER, ChatColor.DARK_RED, "Back",
                Collections.singletonList(ChatColor.GRAY + "Return to previous menu"), false));
        player.openInventory(inv);
    }

    public void openUntrustMenu(Player player, boolean fromAdmin) {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.RED + "Untrust Player");
        inv.setItem(13, makeMenuItem(Material.SKELETON_SKULL, ChatColor.RED, "Enter Player",
                Arrays.asList(ChatColor.GRAY + "Type the player’s name in chat",
                        ChatColor.GRAY + "to untrust them."), false));
        inv.setItem(26, makeMenuItem(Material.BARRIER, ChatColor.DARK_RED, "Back",
                Collections.singletonList(ChatColor.GRAY + "Return to previous menu"), false));
        player.openInventory(inv);
    }

    public void openFlagsMenu(Player player, boolean fromAdmin) {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.DARK_RED + "Claim Flags");

        inv.setItem(10, makeMenuItem(Material.TNT, ChatColor.RED, "Explosions",
                Arrays.asList(ChatColor.GRAY + "Toggle TNT & creeper damage"), true));
        inv.setItem(11, makeMenuItem(Material.WATER_BUCKET, ChatColor.AQUA, "Buckets",
                Arrays.asList(ChatColor.GRAY + "Toggle water/lava placement"), true));
        inv.setItem(12, makeMenuItem(Material.ITEM_FRAME, ChatColor.GOLD, "Item Frames",
                Arrays.asList(ChatColor.GRAY + "Toggle item frame access"), true));
        inv.setItem(13, makeMenuItem(Material.ARMOR_STAND, ChatColor.YELLOW, "Armor Stands",
                Arrays.asList(ChatColor.GRAY + "Toggle armor stand interaction"), true));
        inv.setItem(14, makeMenuItem(Material.CHEST, ChatColor.GREEN, "Containers",
                Arrays.asList(ChatColor.GRAY + "Toggle chest/furnace/hopper access"), true));
        inv.setItem(15, makeMenuItem(Material.BONE, ChatColor.LIGHT_PURPLE, "Pets",
                Arrays.asList(ChatColor.GRAY + "Toggle pet interaction"), true));
        inv.setItem(16, makeMenuItem(Material.IRON_SWORD, ChatColor.DARK_RED, "PvP",
                Arrays.asList(ChatColor.GRAY + "Toggle player-vs-player combat"), true));

        inv.setItem(26, makeMenuItem(Material.BARRIER, ChatColor.DARK_RED, "Back",
                Collections.singletonList(ChatColor.GRAY + "Return to previous menu"), false));

        player.openInventory(inv);
    }

    public void openInfoMenu(Player player, Plot plot) {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.YELLOW + "Claim Info");

        if (plot == null) {
            inv.setItem(13, makeMenuItem(Material.BARRIER, ChatColor.RED, "No Claim",
                    Arrays.asList(ChatColor.GRAY + "You are not inside a claim."), false));
        } else {
            inv.setItem(11, makeMenuItem(Material.PLAYER_HEAD, ChatColor.GREEN, "Owner",
                    Arrays.asList(ChatColor.GRAY + "Claim Owner: " + Bukkit.getOfflinePlayer(plot.getOwner()).getName()), false));

            inv.setItem(13, makeMenuItem(Material.BOOK, ChatColor.AQUA, "Claim Name",
                    Arrays.asList(ChatColor.GRAY + plot.getDisplayNameSafe()), false));

            inv.setItem(15, makeMenuItem(Material.PAPER, ChatColor.GOLD, "Trusted Players",
                    Arrays.asList(ChatColor.GRAY + String.join(", ", plot.getTrustedNames())), false));
        }

        inv.setItem(26, makeMenuItem(Material.BARRIER, ChatColor.DARK_RED, "Back",
                Collections.singletonList(ChatColor.GRAY + "Return to previous menu"), false));

        player.openInventory(inv);
    }

    /* ====================================================
     * HELPER METHODS
     * ==================================================== */
    private ItemStack makeMenuItem(Material mat, ChatColor color, String name, List<String> lore, boolean hideAttrs) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(color + name);
        meta.setLore(lore);
        if (hideAttrs) meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack makeSkull(String playerName, ChatColor color, String display, List<String> lore) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        meta.setDisplayName(color + display);
        meta.setLore(lore);
        OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);
        meta.setOwningPlayer(target);
        skull.setItemMeta(meta);
        return skull;
    }

    private String state(boolean on) { return on ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled"; }

    /* ====================================================
     * UTILS
     * ==================================================== */
    public void rememberTarget(Player player, String targetName) {
        pendingTargets.put(player.getUniqueId(), targetName);
    }

    public String getRememberedTarget(Player player) {
        return pendingTargets.get(player.getUniqueId());
    }

    public void runPlayerCommand(Player player, String command) {
        Bukkit.dispatchCommand(player, command);
    }
}
