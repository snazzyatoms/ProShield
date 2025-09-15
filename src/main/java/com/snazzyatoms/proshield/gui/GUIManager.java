package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.stream.Collectors;

public class GUIManager {

    private final ProShield plugin;
    private final PlotManager plotManager;

    // Track which menu each player is currently in
    private final Map<UUID, String> openMenus = new HashMap<>();

    public GUIManager(ProShield plugin, PlotManager plotManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
    }

    /* =================================
     * MENU OPENERS
     * ================================= */
    public void openMenu(Player player, String menuKey) {
        ConfigurationSection menuCfg = plugin.getConfig().getConfigurationSection("gui.menus." + menuKey);
        if (menuCfg == null) {
            player.sendMessage("§cMenu not found: " + menuKey);
            return;
        }

        String title = menuCfg.getString("title", "ProShield Menu");
        int size = menuCfg.getInt("size", 27);

        Inventory inv = Bukkit.createInventory(null, size, title);

        ConfigurationSection itemsCfg = menuCfg.getConfigurationSection("items");
        if (itemsCfg != null) {
            for (String key : itemsCfg.getKeys(false)) {
                ConfigurationSection itemCfg = itemsCfg.getConfigurationSection(key);
                if (itemCfg == null) continue;

                String mat = itemCfg.getString("material", "STONE");
                ItemStack stack = new ItemStack(Material.matchMaterial(mat) != null ? Material.valueOf(mat) : Material.STONE);
                ItemMeta meta = stack.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(itemCfg.getString("name", ""));
                    meta.setLore(itemCfg.getStringList("lore"));
                    stack.setItemMeta(meta);
                }
                int slot = Integer.parseInt(key);
                inv.setItem(slot, stack);
            }
        }

        player.openInventory(inv);
        openMenus.put(player.getUniqueId(), menuKey);
    }

    /* =================================
     * TRUSTED PLAYERS MENU
     * ================================= */
    public void openTrustedMenu(Player player) {
        Plot plot = plotManager.getPlot(player.getLocation());
        if (plot == null) {
            player.sendMessage("§cYou are not in a claim.");
            return;
        }

        Inventory inv = Bukkit.createInventory(null, 27, "Trusted Players");

        // Nearby untrusted players
        List<Player> nearby = player.getNearbyEntities(10, 10, 10).stream()
                .filter(e -> e instanceof Player && !e.getUniqueId().equals(player.getUniqueId()))
                .map(e -> (Player) e)
                .collect(Collectors.toList());

        int slot = 10;
        for (Player near : nearby) {
            if (!plot.isTrusted(near.getUniqueId())) {
                ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
                ItemMeta meta = skull.getItemMeta();
                meta.setDisplayName("§aTrust " + near.getName());
                skull.setItemMeta(meta);
                inv.setItem(slot++, skull);
            }
        }

        // Already trusted players
        for (UUID uuid : plot.getTrusted().keySet()) {
            OfflinePlayer trusted = Bukkit.getOfflinePlayer(uuid);
            ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
            ItemMeta meta = skull.getItemMeta();
            meta.setDisplayName("§e" + trusted.getName() + " (" + plot.getTrusted().get(uuid) + ")");
            skull.setItemMeta(meta);
            inv.setItem(slot++, skull);
        }

        // Back + Exit
        inv.setItem(25, backButton());
        inv.setItem(26, exitButton());

        player.openInventory(inv);
        openMenus.put(player.getUniqueId(), "trusted");
    }

    /* =================================
     * FLAGS MENU
     * ================================= */
    public void openFlagsMenu(Player player) {
        Plot plot = plotManager.getPlot(player.getLocation());
        if (plot == null) {
            player.sendMessage("§cYou are not in a claim.");
            return;
        }

        Inventory inv = Bukkit.createInventory(null, 45, "Claim Flags");

        int slot = 10;
        for (String flag : plot.getFlags().keySet()) {
            boolean state = plot.getFlags().get(flag);
            Material mat = state ? Material.LIME_WOOL : Material.RED_WOOL;
            ItemStack item = new ItemStack(mat);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("§f" + flag + ": " + (state ? "§aON" : "§cOFF"));
            item.setItemMeta(meta);
            inv.setItem(slot++, item);
        }

        // Back + Exit
        inv.setItem(40, backButton());
        inv.setItem(44, exitButton());

        player.openInventory(inv);
        openMenus.put(player.getUniqueId(), "flags");
    }

    /* =================================
     * HELPERS
     * ================================= */
    public ItemStack backButton() {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§cBack");
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack exitButton() {
        ItemStack item = new ItemStack(Material.REDSTONE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§cExit");
        item.setItemMeta(meta);
        return item;
    }

    public boolean isInMenu(Player player, String menu) {
        return menu.equalsIgnoreCase(openMenus.get(player.getUniqueId()));
    }

    public void close(Player player) {
        openMenus.remove(player.getUniqueId());
        player.closeInventory();
    }
}
