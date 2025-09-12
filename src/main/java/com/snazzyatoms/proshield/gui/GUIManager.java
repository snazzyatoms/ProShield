// src/main/java/com/snazzyatoms/proshield/gui/GUIManager.java
package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.cache.GUICache;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.plots.PlotSettings;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.SkullType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class GUIManager {

    private final ProShield plugin;
    private final GUICache cache;

    // simple per-viewer selection memory (target for roles/trust flow)
    private final Map<UUID, String> selectedTarget = new ConcurrentHashMap<>();

    public GUIManager(ProShield plugin, GUICache cache) {
        this.plugin = plugin;
        this.cache = cache;
    }

    /* ===========================
     * Helpers
     * =========================== */
    private ItemStack makeMenuItem(Material mat, ChatColor color, String name, List<String> lore, boolean hideAttributes) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(color + name);
        if (lore != null && !lore.isEmpty()) meta.setLore(lore);
        if (hideAttributes) meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack makeSkull(String playerName, ChatColor color, String title, List<String> lore) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta sm = (SkullMeta) skull.getItemMeta();
        sm.setDisplayName(color + title);
        if (lore != null) sm.setLore(lore);
        // best-effort set (works for online/offline names on modern Paper)
        sm.setOwningPlayer(Bukkit.getOfflinePlayer(playerName));
        skull.setItemMeta(sm);
        return skull;
    }

    public void rememberTarget(Player viewer, String targetName) { selectedTarget.put(viewer.getUniqueId(), targetName); }
    public String getRememberedTarget(Player viewer) { return selectedTarget.get(viewer.getUniqueId()); }
    public void clearRememberedTarget(Player viewer) { selectedTarget.remove(viewer.getUniqueId()); }

    private PlotManager plots() { return plugin.getPlotManager(); }
    private ClaimRoleManager roles() { return plugin.getRoleManager(); }

    /* ===========================
     * MAIN MENUS
     * =========================== */
    public void openMain(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.AQUA + "ProShield Menu");

        inv.setItem(10, makeMenuItem(Material.GRASS_BLOCK, ChatColor.GREEN, "Claim Chunk",
            Arrays.asList(ChatColor.GRAY + "Claim your current chunk",
                          ChatColor.GRAY + "to protect your builds."), false));

        inv.setItem(11, makeMenuItem(Material.DIRT, ChatColor.RED, "Unclaim Chunk",
            Arrays.asList(ChatColor.GRAY + "Remove your claim",
                          ChatColor.GRAY + "and free the land."), false));

        inv.setItem(12, makeMenuItem(Material.BOOK, ChatColor.YELLOW, "Claim Info",
            Arrays.asList(ChatColor.GRAY + "View claim owner &",
                          ChatColor.GRAY + "trusted players."), false));

        inv.setItem(14, makeMenuItem(Material.WRITABLE_BOOK, ChatColor.GREEN, "Trust Menu",
            Arrays.asList(ChatColor.GRAY + "Add trusted players",
                          ChatColor.GRAY + "to your claim."), false));

        inv.setItem(15, makeMenuItem(Material.PAPER, ChatColor.RED, "Untrust Menu",
            Arrays.asList(ChatColor.GRAY + "Remove players",
                          ChatColor.GRAY + "from trusted list."), false));

        inv.setItem(16, makeMenuItem(Material.IRON_PICKAXE, ChatColor.GOLD, "Roles",
            Arrays.asList(ChatColor.GRAY + "Assign roles (via Trust)",
                          ChatColor.GRAY + "Builder / Moderator"), true));

        inv.setItem(22, makeMenuItem(Material.IRON_SWORD, ChatColor.AQUA, "Flags",
            Arrays.asList(ChatColor.GRAY + "Toggle protections like",
                          ChatColor.GRAY + "Explosions, Fire, PvP…"), true));

        inv.setItem(26, makeMenuItem(Material.BARRIER, ChatColor.DARK_RED, "Back",
            Collections.singletonList(ChatColor.GRAY + "Return to previous menu"), false));

        player.openInventory(inv);
    }

    public void openAdminMain(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.RED + "ProShield Admin Menu");

        inv.setItem(10, makeMenuItem(Material.GRASS_BLOCK, ChatColor.GREEN, "Force Claim",
            Arrays.asList(ChatColor.GRAY + "Claim current chunk",
                          ChatColor.GRAY + "on behalf of a player."), false));

        inv.setItem(11, makeMenuItem(Material.DIAMOND_SHOVEL, ChatColor.RED, "Force Unclaim",
            Arrays.asList(ChatColor.GRAY + "Remove any claim",
                          ChatColor.GRAY + "from this chunk."), true));

        inv.setItem(12, makeMenuItem(Material.BOOK, ChatColor.YELLOW, "Claim Info",
            Arrays.asList(ChatColor.GRAY + "Owner, trusted players,",
                          ChatColor.GRAY + "and settings."), false));

        inv.setItem(14, makeMenuItem(Material.WRITABLE_BOOK, ChatColor.GREEN, "Trust Menu",
            Arrays.asList(ChatColor.GRAY + "Admin-manage trusted players",
                          ChatColor.GRAY + "for this claim."), false));

        inv.setItem(15, makeMenuItem(Material.PAPER, ChatColor.RED, "Untrust Menu",
            Arrays.asList(ChatColor.GRAY + "Admin-remove players",
                          ChatColor.GRAY + "from trusted list."), false));

        inv.setItem(16, makeMenuItem(Material.IRON_PICKAXE, ChatColor.GOLD, "Roles",
            Arrays.asList(ChatColor.GRAY + "Assign roles to players",
                          ChatColor.GRAY + "in this claim."), true));

        inv.setItem(22, makeMenuItem(Material.IRON_SWORD, ChatColor.AQUA, "Flags",
            Arrays.asList(ChatColor.GRAY + "Admin toggle protections:",
                          ChatColor.GRAY + "Explosions, PvP, Containers…"), true));

        inv.setItem(26, makeMenuItem(Material.BARRIER, ChatColor.DARK_RED, "Back",
            Collections.singletonList(ChatColor.GRAY + "Return to previous menu"), false));

        player.openInventory(inv);
    }

    /* ===========================
     * INFO MENU
     * =========================== */
    public void openInfoMenu(Player player, Plot plot) {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.YELLOW + "Claim Info");

        String ownerName = plot.getOwnerName() != null ? plot.getOwnerName() : "Unknown";
        List<String> trusted = plot.getTrustedNames(); // assume you have; otherwise adapt
        if (trusted == null) trusted = Collections.emptyList();

        inv.setItem(11, makeMenuItem(Material.PLAYER_HEAD, ChatColor.AQUA, "Owner",
            Arrays.asList(ChatColor.GRAY + "Owner: " + ChatColor.WHITE + ownerName), false));

        inv.setItem(13, makeMenuItem(Material.PAPER, ChatColor.GREEN, "Trusted Players",
            trusted.isEmpty()
                ? Collections.singletonList(ChatColor.GRAY + "None")
                : trusted.stream().limit(10).map(n -> ChatColor.GRAY + "- " + n).collect(Collectors.toList()),
            false));

        PlotSettings s = plot.getSettings();
        inv.setItem(15, makeMenuItem(Material.REDSTONE_TORCH, ChatColor.GOLD, "Key Flags",
            Arrays.asList(
                ChatColor.GRAY + "Explosions: " + state(s.isExplosionsAllowed()),
                ChatColor.GRAY + "Fire: " + state(s.isFireAllowed()),
                ChatColor.GRAY + "PvP: " + state(s.isPvpEnabled()),
                ChatColor.GRAY + "Containers: " + state(s.isContainersAllowed())
            ), false));

        inv.setItem(26, makeMenuItem(Material.BARRIER, ChatColor.DARK_RED, "Back",
            Collections.singletonList(ChatColor.GRAY + "Return to previous menu"), false));

        player.openInventory(inv);
    }

    private String state(boolean on) {
        return on ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled";
    }

    /* ===========================
     * FLAGS MENU (dynamic lore)
     * =========================== */
    public void openFlagsMenu(Player player, boolean fromAdmin) {
        Plot plot = plots().getPlot(player.getLocation());
        PlotSettings settings = (plot != null) ? plot.getSettings() : new PlotSettings();

        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.AQUA + "Flags Menu");

        inv.setItem(10, makeMenuItem(Material.TNT, ChatColor.RED, "Explosions",
            Arrays.asList(ChatColor.GRAY + "Toggle TNT / Creeper damage",
                          ChatColor.YELLOW + "Current: " + state(settings.isExplosionsAllowed())), false));

        inv.setItem(11, makeMenuItem(Material.FLINT_AND_STEEL, ChatColor.GOLD, "Fire",
            Arrays.asList(ChatColor.GRAY + "Toggle fire spread & ignition",
                          ChatColor.YELLOW + "Current: " + state(settings.isFireAllowed())), false));

        inv.setItem(12, makeMenuItem(Material.CHEST, ChatColor.YELLOW, "Containers",
            Arrays.asList(ChatColor.GRAY + "Allow or block chest/furnace/hopper",
                          ChatColor.YELLOW + "Current: " + state(settings.isContainersAllowed())), false));

        inv.setItem(13, makeMenuItem(Material.IRON_SWORD, ChatColor.DARK_RED, "PvP",
            Arrays.asList(ChatColor.GRAY + "Enable or disable player combat",
                          ChatColor.YELLOW + "Current: " + state(settings.isPvpEnabled())), true));

        inv.setItem(14, makeMenuItem(Material.BONE, ChatColor.AQUA, "Pets",
            Arrays.asList(ChatColor.GRAY + "Protect tamed pets from harm",
                          ChatColor.YELLOW + "Current: " + state(settings.isPetAccessAllowed())), false));

        inv.setItem(15, makeMenuItem(Material.COW_SPAWN_EGG, ChatColor.GREEN, "Animals",
            Arrays.asList(ChatColor.GRAY + "Allow/Block animal interactions",
                          ChatColor.YELLOW + "Current: " + state(settings.isAnimalAccessAllowed())), false));

        inv.setItem(16, makeMenuItem(Material.ITEM_FRAME, ChatColor.BLUE, "Item Frames",
            Arrays.asList(ChatColor.GRAY + "Toggle access to item frames",
                          ChatColor.YELLOW + "Current: " + state(settings.isItemFramesAllowed())), false));

        inv.setItem(22, makeMenuItem(Material.BARRIER, ChatColor.DARK_RED, "Back",
            Collections.singletonList(ChatColor.GRAY + "Return to previous menu"), false));

        player.openInventory(inv);
    }

    /* ===========================
     * TRUST / UNTRUST (player selector with pagination)
     * =========================== */
    public void openTrustMenu(Player player, boolean fromAdmin) {
        openPlayerSelector(player, "Trust Menu", fromAdmin);
    }

    public void openUntrustMenu(Player player, boolean fromAdmin) {
        openPlayerSelector(player, "Untrust Menu", fromAdmin);
    }

    private void openPlayerSelector(Player viewer, String title, boolean fromAdmin) {
        // 6x9 if you want more, but 27 (3x9) is fine with paging; we’ll show up to 21 heads
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.GREEN + title);

        List<Player> candidates = Bukkit.getOnlinePlayers().stream()
            .filter(p -> !p.getUniqueId().equals(viewer.getUniqueId()))
            .sorted(Comparator.comparing(Player::getName, String.CASE_INSENSITIVE_ORDER))
            .collect(Collectors.toList());

        // simple page 0 only (extend with cache if you want more pages)
        int slot = 10;
        for (int i = 0; i < Math.min(21, candidates.size()); i++) {
            Player p = candidates.get(i);
            inv.setItem(slot++, makeSkull(p.getName(), ChatColor.GREEN, p.getName(),
                Arrays.asList(ChatColor.GRAY + "Click to select",
                              ChatColor.GRAY + (title.toLowerCase().contains("trust") ? "Trust this player" : "Untrust this player"))));
            if (slot == 17) slot = 19;
            if (slot == 26) break;
        }

        inv.setItem(26, makeMenuItem(Material.BARRIER, ChatColor.DARK_RED, "Back",
            Collections.singletonList(ChatColor.GRAY + "Return to previous menu"), false));

        viewer.openInventory(inv);
    }

    /* ===========================
     * ROLES (after a target is chosen)
     * =========================== */
    public void openRolesGUI(Player player, Plot plotOrNull, boolean fromAdmin) {
        String target = getRememberedTarget(player);
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.GOLD + "Assign Role");

        List<String> tipLore = target == null
            ? Arrays.asList(ChatColor.GRAY + "Select a player first",
                            ChatColor.GRAY + "via Trust Menu (pick skull)")
            : Arrays.asList(ChatColor.GRAY + "Assign a role to:",
                            ChatColor.WHITE + target);

        inv.setItem(10, makeMenuItem(Material.STONE_PICKAXE, ChatColor.YELLOW, "Builder",
            new ArrayList<>(tipLore){{
                add(ChatColor.GRAY + "Can build/break inside claim");
            }}, true));

        inv.setItem(11, makeMenuItem(Material.CROSSBOW, ChatColor.RED, "Moderator",
            new ArrayList<>(tipLore){{
                add(ChatColor.GRAY + "Manage trusted, toggle flags");
            }}, true));

        inv.setItem(12, makeMenuItem(Material.BOOK, ChatColor.GRAY, "Clear Role",
            new ArrayList<>(tipLore){{
                add(ChatColor.GRAY + "Revert to default permissions");
            }}, false));

        inv.setItem(26, makeMenuItem(Material.BARRIER, ChatColor.DARK_RED, "Back",
            Collections.singletonList(ChatColor.GRAY + "Return to previous menu"), false));

        player.openInventory(inv);
    }

    /* ===========================
     * Convenience for running commands
     * =========================== */
    public void runPlayerCommand(Player player, String raw) {
        // simulate as if player typed it (respects perms/messages)
        player.performCommand(raw.startsWith("/") ? raw.substring(1) : raw);
    }
}
