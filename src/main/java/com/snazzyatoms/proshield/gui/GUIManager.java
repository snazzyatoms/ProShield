package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
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

import java.util.*;

public class GUIManager {

    private final ProShield plugin;
    private final ClaimRoleManager roles;

    // store remembered targets (trust/untrust flows)
    private final Map<UUID, String> pendingTargets = new HashMap<>();

    public GUIManager(ProShield plugin, ClaimRoleManager roles) {
        this.plugin = plugin;
        this.roles = roles;
    }

    /* ====================================================
     * GENERIC MENU LOADER
     * ==================================================== */
    private Inventory buildMenu(String menuKey) {
        ConfigurationSection menu = plugin.getConfig().getConfigurationSection("ui.menus." + menuKey);
        if (menu == null) {
            plugin.getLogger().warning("Menu not found in config: " + menuKey);
            return Bukkit.createInventory(null, 27, ChatColor.RED + "Missing Menu");
        }

        String title = ChatColor.translateAlternateColorCodes('&', menu.getString("title", "&cMissing Title"));
        Inventory inv = Bukkit.createInventory(null, 27, title);

        ConfigurationSection items = menu.getConfigurationSection("items");
        if (items != null) {
            for (String key : items.getKeys(false)) {
                ConfigurationSection itemSec = items.getConfigurationSection(key);
                if (itemSec == null) continue;

                int slot = itemSec.getInt("slot", -1);
                if (slot < 0 || slot >= inv.getSize()) continue;

                String matName = itemSec.getString("material", "BARRIER");
                Material mat = Material.matchMaterial(matName);
                if (mat == null) mat = Material.BARRIER;

                String name = ChatColor.translateAlternateColorCodes('&', itemSec.getString("name", "&cMissing Name"));
                List<String> lore = new ArrayList<>();
                for (String line : itemSec.getStringList("lore")) {
                    lore.add(ChatColor.translateAlternateColorCodes('&', line));
                }

                ItemStack item = new ItemStack(mat);
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(name);
                    if (!lore.isEmpty()) meta.setLore(lore);
                    item.setItemMeta(meta);
                }

                inv.setItem(slot, item);
            }
        }

        return inv;
    }

    /* ====================================================
     * PUBLIC OPENERS
     * ==================================================== */
    public void openMain(Player player) {
        player.openInventory(buildMenu("main"));
    }

    public void openRolesMenu(Player player) {
        player.openInventory(buildMenu("roles"));
    }

    // Example: dynamic trust menu, can extend with more
    public void openTrustMenu(Player player) {
        player.openInventory(buildMenu("trust"));
    }

    public void openUntrustMenu(Player player) {
        player.openInventory(buildMenu("untrust"));
    }

    public void openFlagsMenu(Player player) {
        player.openInventory(buildMenu("flags"));
    }

    public void openAdminMenu(Player player) {
        player.openInventory(buildMenu("admin"));
    }

    public void openTransferMenu(Player player) {
        player.openInventory(buildMenu("transfer"));
    }

    public void openInfoMenu(Player player, Plot plot) {
        Inventory inv = buildMenu("info");

        if (plot != null) {
            OfflinePlayer owner = Bukkit.getOfflinePlayer(plot.getOwner());
            ItemStack info = new ItemStack(Material.PAPER);
            ItemMeta meta = info.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.YELLOW + "Claim Info");
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GRAY + "Owner: " + (owner != null ? owner.getName() : "Unknown"));
                lore.add(ChatColor.GRAY + "Trusted: " + String.join(", ", plot.getTrustedNames()));
                meta.setLore(lore);
                info.setItemMeta(meta);
            }
            inv.setItem(13, info);
        }

        player.openInventory(inv);
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
}
