// src/main/java/com/snazzyatoms/proshield/gui/GUIManager.java
package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import com.snazzyatoms.proshield.roles.ClaimRoleManager.PlayerRoleData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class GUIManager {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final ClaimRoleManager roleManager;

    // Default descriptions for flags
    private static final Map<String, String> FLAG_DESCRIPTIONS = new HashMap<>();
    static {
        FLAG_DESCRIPTIONS.put("explosions", "&7Toggle TNT and creeper damage inside claim");
        FLAG_DESCRIPTIONS.put("buckets", "&7Allow/disallow bucket use (water & lava)");
        FLAG_DESCRIPTIONS.put("item-frames", "&7Protect item frames from breaking/rotation");
        FLAG_DESCRIPTIONS.put("armor-stands", "&7Prevent others moving/destroying armor stands");
        FLAG_DESCRIPTIONS.put("containers", "&7Control access to chests, hoppers, furnaces, shulkers");
        FLAG_DESCRIPTIONS.put("pets", "&7Prevent damage to tamed pets (wolves, cats, etc.)");
        FLAG_DESCRIPTIONS.put("pvp", "&7Enable or disable PvP combat inside claim");
        FLAG_DESCRIPTIONS.put("safezone", "&7Turns your claim into a safe zone (blocks hostile spawns & damage)");
    }

    // Default per-player permissions (toggle keys -> description)
    private static final Map<String, String> ROLE_PERMISSIONS = new LinkedHashMap<>();
    static {
        ROLE_PERMISSIONS.put("build", "&7Allow building & block breaking");
        ROLE_PERMISSIONS.put("interact", "&7Allow doors, buttons, crops, redstone use");
        ROLE_PERMISSIONS.put("containers", "&7Allow access to chests, hoppers, furnaces");
        ROLE_PERMISSIONS.put("vehicles", "&7Allow using minecarts & boats");
        ROLE_PERMISSIONS.put("unclaim", "&7Allow unclaiming this land");
    }

    public GUIManager(ProShield plugin) {
        this.plugin = plugin;
        this.plotManager = plugin.getPlotManager();
        this.roleManager = plugin.getRoleManager();
    }

    /** ======================================================
     * Entry point for menus
     * ====================================================== */
    public void openMenu(Player player, String menuKey) {
        ConfigurationSection menus = plugin.getConfig().getConfigurationSection("gui.menus");
        if (menus == null) {
            plugin.getLogger().warning("No menus defined in config.yml (gui.menus missing).");
            return;
        }

        ConfigurationSection menu = menus.getConfigurationSection(menuKey);
        if (menu == null) {
            plugin.getLogger().warning("Menu not found in config.yml: " + menuKey);
            return;
        }

        String title = ChatColor.translateAlternateColorCodes('&', menu.getString("title", "&7Menu"));
        int size = menu.getInt("size", 27);
        if (size % 9 != 0) size = 27; // safeguard
        Inventory inv = Bukkit.createInventory(null, size, title);

        if (menuKey.equalsIgnoreCase("trust")) {
            buildTrustMenu(player, inv);
        } else if (menuKey.equalsIgnoreCase("untrust")) {
            buildUntrustMenu(player, inv);
        } else {
            buildStaticMenu(player, menu, inv, size);
        }

        player.openInventory(inv);
    }

    /** ======================================================
     * Role Editor GUI (per trusted player)
     * ====================================================== */
    public void openRoleEditor(Player owner, String targetName) {
        Plot plot = plotManager.getPlot(owner.getLocation());
        if (plot == null) {
            owner.sendMessage(ChatColor.RED + "You are not standing inside one of your claims.");
            return;
        }

        UUID plotId = plot.getId();
        Map<String, Boolean> perms = roleManager.getPermissions(plotId, targetName);
        String role = roleManager.getRole(plotId, targetName);

        String title = ChatColor.DARK_AQUA + "Role Editor: " + targetName;
        Inventory inv = Bukkit.createInventory(null, 27, title);

        int slot = 0;
        for (Map.Entry<String, String> entry : ROLE_PERMISSIONS.entrySet()) {
            String key = entry.getKey();
            String desc = entry.getValue();

            boolean enabled = perms.getOrDefault(key, false);

            ItemStack item = new ItemStack(enabled ? Material.LIME_DYE : Material.GRAY_DYE);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.YELLOW + key.toUpperCase());
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.translateAlternateColorCodes('&', desc));
                lore.add(ChatColor.GRAY + "Current: " + (enabled ? ChatColor.GREEN + "Allowed" : ChatColor.RED + "Denied"));
                lore.add(ChatColor.AQUA + "Click to toggle");
                meta.setLore(lore);
                item.setItemMeta(meta);
            }

            inv.setItem(slot++, item);
        }

        // Role name head (centerpiece)
        ItemStack roleItem = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta roleMeta = roleItem.getItemMeta();
        if (roleMeta != null) {
            roleMeta.setDisplayName(ChatColor.GOLD + "Role: " + (role == null ? "None" : role));
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Trusted player: " + targetName);
            lore.add(ChatColor.YELLOW + "Role defines defaults");
            lore.add(ChatColor.AQUA + "Custom toggles override defaults");
            roleMeta.setLore(lore);
            roleItem.setItemMeta(roleMeta);
        }
        inv.setItem(22, roleItem);

        // Close button
        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta cMeta = close.getItemMeta();
        if (cMeta != null) {
            cMeta.setDisplayName(ChatColor.RED + "Close");
            cMeta.setLore(Collections.singletonList(ChatColor.GRAY + "Click to close this menu"));
            close.setItemMeta(cMeta);
        }
        inv.setItem(26, close);

        owner.openInventory(inv);
    }

    /** ======================================================
     * Trust & Untrust Menus
     * ====================================================== */
    private void buildTrustMenu(Player player, Inventory inv) {
        Plot plot = plotManager.getPlot(player.getLocation());
        if (plot == null) return;

        int slot = 0;
        for (Player nearby : player.getWorld().getPlayers()) {
            if (nearby.equals(player)) continue;
            if (nearby.getLocation().distance(player.getLocation()) > 10) continue;

            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            ItemMeta meta = head.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.GREEN + nearby.getName());
                meta.setLore(Collections.singletonList(ChatColor.YELLOW + "Click to trust this player"));
                head.setItemMeta(meta);
            }
            inv.setItem(slot++, head);
            if (slot >= inv.getSize() - 1) break;
        }
    }

    private void buildUntrustMenu(Player player, Inventory inv) {
        Plot plot = plotManager.getPlot(player.getLocation());
        if (plot == null) return;

        Map<String, String> trusted = roleManager.getTrusted(plot.getId());
        if (trusted == null || trusted.isEmpty()) return;

        int slot = 0;
        for (String name : trusted.keySet()) {
            OfflinePlayer offline = Bukkit.getOfflinePlayer(name);
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            ItemMeta meta = head.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.RED + name);
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.YELLOW + "Click to manage this player");
                lore.add(ChatColor.GRAY + "→ Opens role editor");
                meta.setLore(lore);
                head.setItemMeta(meta);
            }
            inv.setItem(slot++, head);
            if (slot >= inv.getSize() - 1) break;
        }
    }

    /** ======================================================
     * Static Menus
     * ====================================================== */
    private void buildStaticMenu(Player player, ConfigurationSection menu, Inventory inv, int size) {
        ConfigurationSection items = menu.getConfigurationSection("items");
        if (items != null) {
            for (String slotKey : items.getKeys(false)) {
                try {
                    int slot = Integer.parseInt(slotKey);
                    if (slot < 0 || slot >= size) continue;

                    ConfigurationSection itemSec = items.getConfigurationSection(slotKey);
                    if (itemSec == null) continue;

                    String perm = itemSec.getString("permission");
                    if (perm != null && !perm.isBlank() &&
                        !player.hasPermission(perm) && !player.isOp()) {
                        continue;
                    }

                    Material mat = Material.matchMaterial(itemSec.getString("material", "BARRIER"));
                    if (mat == null) mat = Material.BARRIER;

                    String name = ChatColor.translateAlternateColorCodes('&', itemSec.getString("name", ""));
                    List<String> lore = formatLore(itemSec.getStringList("lore"), player, itemSec);

                    ItemStack item = new ItemStack(mat);
                    ItemMeta meta = item.getItemMeta();
                    if (meta != null) {
                        meta.setDisplayName(name);
                        meta.setLore(lore);
                        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                        item.setItemMeta(meta);
                    }

                    inv.setItem(slot, item);
                } catch (NumberFormatException ignored) {}
            }
        }
    }

    /** ======================================================
     * Utility
     * ====================================================== */
    private List<String> formatLore(List<String> input, Player player, ConfigurationSection itemSec) {
        List<String> out = new ArrayList<>();
        String action = itemSec.getString("action", "");
        String state = "";

        String flagKey = null;
        if (action.toLowerCase().startsWith("command:proshield flag ")) {
            String[] split = action.split(" ");
            if (split.length >= 3) {
                flagKey = split[2].toLowerCase();

                Plot plot = plotManager.getPlot(player.getLocation());
                boolean flagValue = plot != null
                        ? plot.getFlag(flagKey, plugin.getConfig().getBoolean("claims.default-flags." + flagKey, false))
                        : plugin.getConfig().getBoolean("claims.default-flags." + flagKey, false);

                state = flagValue ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled";
            }
        }

        if (input == null || input.isEmpty()) {
            if (flagKey != null && FLAG_DESCRIPTIONS.containsKey(flagKey)) {
                out.add(ChatColor.translateAlternateColorCodes('&', FLAG_DESCRIPTIONS.get(flagKey)));
                out.add(ChatColor.GRAY + "Current: " + state);
            }
        } else {
            for (String line : input) {
                out.add(ChatColor.translateAlternateColorCodes('&', line.replace("{state}", state)));
            }
        }
        return out;
    }
}
