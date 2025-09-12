// src/main/java/com/snazzyatoms/proshield/gui/GUIManager.java
package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.cache.GUICache;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

/**
 * GUIManager
 * - Dynamically builds menus from config.yml (ui.menus section).
 * - Unified manager: handles main, admin, roles, flags, trust, untrust, info menus.
 * - No hardcoded slots/materials → all editable in config.yml.
 */
public class GUIManager {

    private final ProShield plugin;
    private final GUICache cache;
    private final ClaimRoleManager roles;

    // Store temporary targets for trust/untrust menus
    private final Map<UUID, String> pendingTargets = new HashMap<>();

    public GUIManager(ProShield plugin, GUICache cache, ClaimRoleManager roles) {
        this.plugin = plugin;
        this.cache = cache;
        this.roles = roles;
    }

    /* ====================================================
     * GENERIC MENU BUILDER
     * ==================================================== */

    private Inventory buildMenu(String menuKey) {
        ConfigurationSection menus = plugin.getConfig().getConfigurationSection("ui.menus");
        if (menus == null || !menus.isConfigurationSection(menuKey)) {
            plugin.getLogger().warning("Missing ui.menus." + menuKey + " in config.yml");
            return Bukkit.createInventory(null, 27, ChatColor.RED + "Missing Menu");
        }

        ConfigurationSection menu = menus.getConfigurationSection(menuKey);
        String title = ChatColor.translateAlternateColorCodes('&', menu.getString("title", "&cMissing Title"));

        Inventory inv = Bukkit.createInventory(null, 27, title);

        ConfigurationSection items = menu.getConfigurationSection("items");
        if (items != null) {
            for (String key : items.getKeys(false)) {
                ConfigurationSection item = items.getConfigurationSection(key);
                if (item == null) continue;

                int slot = item.getInt("slot", -1);
                String matName = item.getString("material", "BARRIER");
                Material mat = Material.matchMaterial(matName.toUpperCase(Locale.ROOT));
                if (mat == null) mat = Material.BARRIER;

                String displayName = ChatColor.translateAlternateColorCodes('&', item.getString("name", "&cMissing Name"));
                List<String> lore = new ArrayList<>();
                for (String line : item.getStringList("lore")) {
                    lore.add(ChatColor.translateAlternateColorCodes('&', line));
                }

                ItemStack stack = new ItemStack(mat);
                ItemMeta meta = stack.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(displayName);
                    if (!lore.isEmpty()) meta.setLore(lore);
                    stack.setItemMeta(meta);
                }

                if (slot >= 0 && slot < inv.getSize()) {
                    inv.setItem(slot, stack);
                }
            }
        }

        return inv;
    }

    /* ====================================================
     * PUBLIC OPEN METHODS
     * ==================================================== */

    public void openMain(Player player) {
        player.openInventory(buildMenu("main"));
    }

    public void openAdmin(Player player) {
        player.openInventory(buildMenu("admin"));
    }

    public void openRoles(Player player) {
        player.openInventory(buildMenu("roles"));
    }

    public void openFlags(Player player) {
        player.openInventory(buildMenu("flags"));
    }

    public void openTrust(Player player) {
        player.openInventory(buildMenu("trust"));
    }

    public void openUntrust(Player player) {
        player.openInventory(buildMenu("untrust"));
    }

    public void openInfo(Player player, Plot plot) {
        // For info menu, we inject dynamic lore
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.YELLOW + "Claim Info");

        if (plot == null) {
            inv.setItem(13, makeItem(Material.BARRIER, "&cNo Claim", Collections.singletonList("&7You are not inside a claim.")));
        } else {
            inv.setItem(11, makeItem(Material.PLAYER_HEAD, "&aOwner",
                    Collections.singletonList("&7Claim Owner: " + Bukkit.getOfflinePlayer(plot.getOwner()).getName())));

            inv.setItem(13, makeItem(Material.BOOK, "&bClaim Name",
                    Collections.singletonList("&7" + plot.getDisplayNameSafe())));

            inv.setItem(15, makeItem(Material.PAPER, "&eTrusted Players",
                    Collections.singletonList("&7" + String.join(", ", new ArrayList<>(plot.getTrustedNames())))));
        }

        player.openInventory(inv);
    }

    /* ====================================================
     * HELPER METHODS
     * ==================================================== */

    private ItemStack makeItem(Material mat, String name, List<String> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
            if (lore != null && !lore.isEmpty()) {
                List<String> coloredLore = new ArrayList<>();
                for (String l : lore) {
                    coloredLore.add(ChatColor.translateAlternateColorCodes('&', l));
                }
                meta.setLore(coloredLore);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack makeSkull(String playerName, String name, List<String> lore) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
            if (lore != null && !lore.isEmpty()) {
                List<String> coloredLore = new ArrayList<>();
                for (String l : lore) {
                    coloredLore.add(ChatColor.translateAlternateColorCodes('&', l));
                }
                meta.setLore(coloredLore);
            }
            OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);
            meta.setOwningPlayer(target);
            skull.setItemMeta(meta);
        }
        return skull;
    }

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
