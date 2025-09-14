package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class GUIManager {
    private final ProShield plugin;
    private final PlotManager plotManager;
    private final MessagesUtil messages;

    public GUIManager(ProShield plugin, PlotManager plotManager, MessagesUtil messages) {
        this.plugin = plugin;
        this.plotManager = plotManager;
        this.messages = messages;
    }

    public void openMenu(Player player, String menuName) {
        switch (menuName.toLowerCase()) {
            case "main" -> openMainMenu(player);
            case "flags" -> openFlagsMenu(player);
            case "claiminfo" -> openClaimInfoMenu(player);
            default -> player.sendMessage(ChatColor.RED + "Unknown menu: " + menuName);
        }
    }

    private void openMainMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.translateAlternateColorCodes('&', "&6ProShield Menu"));

        inv.setItem(11, createItem(Material.GRASS_BLOCK, "&aClaim Land", List.of(
                ChatColor.GRAY + "Protect your land",
                ChatColor.WHITE + "Radius: " + plugin.getConfig().getInt("claims.default-radius", 50) + " blocks by default"
        )));

        inv.setItem(13, createItem(Material.PAPER, "&eClaim Info", List.of(
                ChatColor.GRAY + "Shows your current claim details",
                ChatColor.YELLOW + "Click to view details"
        )));

        inv.setItem(15, createItem(Material.BARRIER, "&cUnclaim Land", List.of(
                ChatColor.GRAY + "Remove your claim"
        )));

        player.openInventory(inv);
    }

    private void openFlagsMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.translateAlternateColorCodes('&', "&dClaim Flags"));

        // flags will be dynamically populated
        inv.setItem(26, createItem(Material.BARRIER, "&cBack", List.of("&7Return to main menu")));

        player.openInventory(inv);
    }

    private void openClaimInfoMenu(Player player) {
        int defaultRadius = plugin.getConfig().getInt("claims.default-radius", 50);
        int currentRadius = plotManager.getClaimRadius(player.getUniqueId());

        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.translateAlternateColorCodes('&', "&eClaim Info"));

        inv.setItem(11, createItem(Material.PAPER, "&aClaim Size", List.of(
                ChatColor.GRAY + "Default Radius: " + defaultRadius + " blocks",
                ChatColor.GRAY + "Current Radius: " + currentRadius + " blocks"
        )));

        inv.setItem(13, createItem(Material.SHIELD, "&aProtections", List.of(
                ChatColor.GRAY + "✔ Pets",
                ChatColor.GRAY + "✔ Containers",
                ChatColor.GRAY + "✔ Item Frames",
                ChatColor.GRAY + "✔ Armor Stands",
                ChatColor.GRAY + "✘ Explosions",
                ChatColor.GRAY + "✘ Fire"
        )));

        inv.setItem(15, createItem(Material.BARRIER, "&cBack", List.of("&7Return to main menu")));

        player.openInventory(inv);
    }

    private ItemStack createItem(Material mat, String name, List<String> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    // ✅ Handle Back button clicks
    public void handleClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getCurrentItem() == null) return;

        ItemStack clicked = event.getCurrentItem();
        if (!clicked.hasItemMeta() || !clicked.getItemMeta().hasDisplayName()) return;

        String name = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());

        if (name.equalsIgnoreCase("Back")) {
            event.setCancelled(true);
            openMainMenu(player);
        }
    }
}
