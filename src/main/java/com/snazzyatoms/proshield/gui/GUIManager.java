// src/main/java/com/snazzyatoms/proshield/gui/GUIManager.java
package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
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
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

public class GUIManager {

    private final ProShield plugin;

    public GUIManager(ProShield plugin) {
        this.plugin = plugin;
    }

    /**
     * Opens a GUI menu defined in config.yml under gui.menus.<menuKey>
     * Some menus (trust/untrust/roles/flags) are dynamic.
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
        Inventory inv = Bukkit.createInventory(null, size, title);

        // Handle dynamic menus
        switch (menuKey.toLowerCase(Locale.ROOT)) {
            case "trust" -> populateTrustMenu(player, inv, menu);
            case "untrust" -> populateUntrustMenu(player, inv, menu);
            case "roles" -> populateRolesMenu(player, inv, menu);
            case "flags" -> populateFlagsMenu(player, inv, menu);
            default -> populateStaticMenu(player, inv, menu);
        }

        player.openInventory(inv);
    }

    /* ======================================================
     * STATIC MENUS
     * ====================================================== */
    private void populateStaticMenu(Player player, Inventory inv, ConfigurationSection menu) {
        ConfigurationSection items = menu.getConfigurationSection("items");
        if (items == null) return;

        for (String slotKey : items.getKeys(false)) {
            try {
                int slot = Integer.parseInt(slotKey);
                ConfigurationSection itemSec = items.getConfigurationSection(slotKey);
                if (itemSec == null) continue;

                String perm = itemSec.getString("permission", "");
                if (!perm.isEmpty() && !player.hasPermission(perm) && !player.isOp()) continue;

                ItemStack item = buildItem(itemSec);
                if (item != null) inv.setItem(slot, item);
            } catch (NumberFormatException ignored) {}
        }
    }

    /* ======================================================
     * TRUST MENU (dynamic: nearby players)
     * ====================================================== */
    private void populateTrustMenu(Player player, Inventory inv, ConfigurationSection menu) {
        int slot = 10;
        for (Player nearby : player.getWorld().getPlayers()) {
            if (nearby.equals(player)) continue;
            if (nearby.getLocation().distance(player.getLocation()) > 10) continue; // within 10 blocks

            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            if (meta != null) {
                meta.setOwningPlayer(nearby);
                meta.setDisplayName("§aTrust " + nearby.getName());
                head.setItemMeta(meta);
            }
            inv.setItem(slot++, head);
            if (slot >= inv.getSize() - 1) break;
        }
        populateStaticMenu(player, inv, menu); // add back button
    }

    /* ======================================================
     * UNTRUST MENU (dynamic: trusted players)
     * ====================================================== */
    private void populateUntrustMenu(Player player, Inventory inv, ConfigurationSection menu) {
        PlotManager plotManager = plugin.getPlotManager();
        Plot plot = plotManager.getPlotAt(player.getLocation());
        if (plot != null) {
            int slot = 10;
            for (UUID trusted : plotManager.getTrusted(plot)) {
                OfflinePlayer op = Bukkit.getOfflinePlayer(trusted);
                ItemStack head = new ItemStack(Material.PLAYER_HEAD);
                SkullMeta meta = (SkullMeta) head.getItemMeta();
                if (meta != null) {
                    meta.setOwningPlayer(op);
                    meta.setDisplayName("§cUntrust " + op.getName());
                    head.setItemMeta(meta);
                }
                inv.setItem(slot++, head);
                if (slot >= inv.getSize() - 1) break;
            }
        }
        populateStaticMenu(player, inv, menu); // add back button
    }

    /* ======================================================
     * ROLES MENU (dynamic: trusted players list)
     * ====================================================== */
    private void populateRolesMenu(Player player, Inventory inv, ConfigurationSection menu) {
        PlotManager plotManager = plugin.getPlotManager();
        Plot plot = plotManager.getPlotAt(player.getLocation());
        if (plot != null) {
            int slot = 10;
            for (UUID trusted : plotManager.getTrusted(plot)) {
                OfflinePlayer op = Bukkit.getOfflinePlayer(trusted);
                ItemStack head = new ItemStack(Material.PLAYER_HEAD);
                SkullMeta meta = (SkullMeta) head.getItemMeta();
                if (meta != null) {
                    meta.setOwningPlayer(op);
                    meta.setDisplayName("§6Role: " + op.getName());
                    head.setItemMeta(meta);
                }
                inv.setItem(slot++, head);
                if (slot >= inv.getSize() - 1) break;
            }
        }
        populateStaticMenu(player, inv, menu); // add back button
    }

    /* ======================================================
     * FLAGS MENU (dynamic toggles)
     * ====================================================== */
    private void populateFlagsMenu(Player player, Inventory inv, ConfigurationSection menu) {
        PlotManager plotManager = plugin.getPlotManager();
        Plot plot = plotManager.getPlotAt(player.getLocation());
        ConfigurationSection items = menu.getConfigurationSection("items");
        if (items == null) return;

        for (String slotKey : items.getKeys(false)) {
            try {
                int slot = Integer.parseInt(slotKey);
                ConfigurationSection itemSec = items.getConfigurationSection(slotKey);
                if (itemSec == null) continue;

                String flag = itemSec.getString("action", "").replace("command:proshield flag ", "");
                boolean enabled = (plot != null) && plotManager.getFlag(plot, flag);

                ItemStack item = new ItemStack(Material.matchMaterial(itemSec.getString("material", "BARRIER")));
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    String baseName = ChatColor.translateAlternateColorCodes('&', itemSec.getString("name", flag));
                    meta.setDisplayName((enabled ? "§a" : "§c") + baseName);
                    meta.setLore(List.of("§7Click to toggle", enabled ? "§aEnabled" : "§cDisabled"));
                    item.setItemMeta(meta);
                }
                inv.setItem(slot, item);
            } catch (NumberFormatException ignored) {}
        }
        populateStaticMenu(player, inv, menu); // back button
    }

    /* ======================================================
     * HELPERS
     * ====================================================== */
    private ItemStack buildItem(ConfigurationSection itemSec) {
        Material mat = Material.matchMaterial(itemSec.getString("material", "BARRIER"));
        if (mat == null) mat = Material.BARRIER;

        String name = ChatColor.translateAlternateColorCodes('&', itemSec.getString("name", ""));
        List<String> lore = formatLore(itemSec.getStringList("lore"));

        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(lore);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            item.setItemMeta(meta);
        }
        return item;
    }

    private List<String> formatLore(List<String> input) {
        List<String> out = new ArrayList<>();
        for (String line : input) {
            out.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        return out;
    }
}
