// src/main/java/com/snazzyatoms/proshield/gui/GUIManager.java
package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
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

    public GUIManager(ProShield plugin) {
        this.plugin = plugin;
        this.plotManager = plugin.getPlotManager();
        this.roleManager = plugin.getRoleManager();
    }

    /**
     * Opens a GUI menu defined in config.yml under gui.menus.<menuKey>
     */
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

        // Special handling for dynamic menus
        if (menuKey.equalsIgnoreCase("trust")) {
            buildTrustMenu(player, inv);
        } else if (menuKey.equalsIgnoreCase("untrust")) {
            buildUntrustMenu(player, inv);
        } else {
            // Normal config-driven menus
            buildStaticMenu(player, menu, inv, size);
        }

        player.openInventory(inv);
    }

    /**
     * Build normal menus from config (main, flags, roles etc.)
     */
    private void buildStaticMenu(Player player, ConfigurationSection menu, Inventory inv, int size) {
        ConfigurationSection items = menu.getConfigurationSection("items");
        if (items != null) {
            for (String slotKey : items.getKeys(false)) {
                try {
                    int slot = Integer.parseInt(slotKey);
                    if (slot < 0 || slot >= size) continue;

                    ConfigurationSection itemSec = items.getConfigurationSection(slotKey);
                    if (itemSec == null) continue;

                    // Permission check: hide item if player lacks permission (operators always see it)
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
                } catch (NumberFormatException ignored) {
                }
            }
        }
    }

    /**
     * Build Trust menu dynamically (shows nearby players).
     */
    private void buildTrustMenu(Player player, Inventory inv) {
        Plot plot = plotManager.getPlot(player.getLocation());
        if (plot == null) return;

        int slot = 0;
        for (Player nearby : player.getWorld().getPlayers()) {
            if (nearby.equals(player)) continue;
            if (nearby.getLocation().distance(player.getLocation()) > 10) continue; // within 10 blocks

            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            ItemMeta meta = head.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.GREEN + nearby.getName());
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.YELLOW + "Click to trust this player");
                meta.setLore(lore);
                head.setItemMeta(meta);
            }
            inv.setItem(slot++, head);
            if (slot >= inv.getSize() - 1) break;
        }
    }

    /**
     * Build Untrust menu dynamically (shows currently trusted players).
     */
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
                lore.add(ChatColor.YELLOW + "Click to untrust this player");
                meta.setLore(lore);
                head.setItemMeta(meta);
            }
            inv.setItem(slot++, head);
            if (slot >= inv.getSize() - 1) break;
        }
    }

    /**
     * Format lore lines with color codes and replace placeholders like {state}.
     * Adds default flag descriptions if lore is empty or missing.
     */
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
                boolean flagValue;

                if (plot != null) {
                    flagValue = plot.getFlag(flagKey,
                            plugin.getConfig().getBoolean("claims.default-flags." + flagKey, false));
                } else {
                    // No plot: fall back to defaults
                    flagValue = plugin.getConfig().getBoolean("claims.default-flags." + flagKey, false);
                }

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
                if (line.contains("{state}")) {
                    out.add(ChatColor.translateAlternateColorCodes('&', line.replace("{state}", state)));
                } else {
                    out.add(ChatColor.translateAlternateColorCodes('&', line));
                }
            }
        }
        return out;
    }
}
