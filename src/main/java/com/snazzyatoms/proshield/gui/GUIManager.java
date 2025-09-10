package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.PlotManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class GUIManager {

    private final ProShield plugin;
    private final PlotManager plots;
    private final GUICache cache;

    private static final String COMPASS_NAME_PLAYER = ChatColor.GREEN + "ProShield Compass";
    private static final String COMPASS_NAME_ADMIN = ChatColor.RED + "ProShield Admin Compass";

    public GUIManager(ProShield plugin, PlotManager plots, GUICache cache) {
        this.plugin = plugin;
        this.plots = plots;
        this.cache = cache;
    }

    // =========================================
    // COMPASS CREATION & CHECKS
    // =========================================
    public ItemStack createCompass(boolean isAdmin) {
        ItemStack compass = new ItemStack(Material.COMPASS);
        ItemMeta meta = compass.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(isAdmin ? COMPASS_NAME_ADMIN : COMPASS_NAME_PLAYER);
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + (isAdmin ? "Admin tools for ProShield" : "Player land protection menu"));
            lore.add(ChatColor.YELLOW + "Right-click to open the ProShield GUI");
            meta.setLore(lore);
            compass.setItemMeta(meta);
        }
        return compass;
    }

    public boolean isProShieldCompass(ItemStack item) {
        if (item == null || item.getType() != Material.COMPASS) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return false;
        String name = ChatColor.stripColor(meta.getDisplayName());
        return name.equalsIgnoreCase("ProShield Compass") || name.equalsIgnoreCase("ProShield Admin Compass");
    }

    public boolean isAdminCompass(ItemStack item) {
        if (!isProShieldCompass(item)) return false;
        String name = ChatColor.stripColor(item.getItemMeta().getDisplayName());
        return name.equalsIgnoreCase("ProShield Admin Compass");
    }

    // =========================================
    // MAIN MENU HANDLERS
    // =========================================
    public void openMain(Player player, boolean isAdmin) {
        String title = ChatColor.DARK_GREEN + "ProShield Menu";
        Inventory inv = cache.getMenu(title);
        if (inv == null) {
            inv = Bukkit.createInventory(null, 54, title);

            // Load slots from config
            FileConfiguration config = plugin.getConfig();
            int claimSlot = config.getInt("gui.slots.main.create", 11);
            int infoSlot = config.getInt("gui.slots.main.info", 13);
            int unclaimSlot = config.getInt("gui.slots.main.remove", 15);
            int helpSlot = config.getInt("gui.slots.main.help", 31);
            int adminSlot = config.getInt("gui.slots.main.admin", 33);

            inv.setItem(claimSlot, createMenuItem(Material.GRASS_BLOCK, ChatColor.GREEN + "Claim Land"));
            inv.setItem(infoSlot, createMenuItem(Material.BOOK, ChatColor.AQUA + "Claim Info"));
            inv.setItem(unclaimSlot, createMenuItem(Material.BARRIER, ChatColor.RED + "Unclaim Land"));
            inv.setItem(helpSlot, createMenuItem(Material.PAPER, ChatColor.YELLOW + "Help"));

            if (isAdmin) {
                inv.setItem(adminSlot, createMenuItem(Material.NETHER_STAR, ChatColor.RED + "Admin Menu"));
            }

            cache.putMenu(title, inv);
        }
        player.openInventory(inv);
    }

    public void openAdmin(Player player) {
        String title = ChatColor.DARK_RED + "ProShield Admin Menu";
        Inventory inv = cache.getMenu(title);
        if (inv == null) {
            inv = Bukkit.createInventory(null, 54, title);

            FileConfiguration config = plugin.getConfig();
            int reloadSlot = config.getInt("gui.slots.admin.reload", 25);
            int purgeSlot = config.getInt("gui.slots.admin.purge-expired", 21);
            int tpToolsSlot = config.getInt("gui.slots.admin.tp-tools", 30);
            int backSlot = config.getInt("gui.slots.admin.back", 31);

            inv.setItem(reloadSlot, createMenuItem(Material.REDSTONE, ChatColor.RED + "Reload Configs"));
            inv.setItem(purgeSlot, createMenuItem(Material.LAVA_BUCKET, ChatColor.GOLD + "Purge Expired Claims"));
            inv.setItem(tpToolsSlot, createMenuItem(Material.ENDER_PEARL, ChatColor.LIGHT_PURPLE + "Teleport Tools"));
            inv.setItem(backSlot, createMenuItem(Material.ARROW, ChatColor.GRAY + "Back"));

            cache.putMenu(title, inv);
        }
        player.openInventory(inv);
    }

    // =========================================
    // ITEM CREATOR
    // =========================================
    private ItemStack createMenuItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            item.setItemMeta(meta);
        }
        return item;
    }

    // =========================================
    // CONFIG RELOAD SUPPORT
    // =========================================
    public void onConfigReload() {
        cache.clear(); // wipe cached GUIs so menus rebuild with new config
    }
}
