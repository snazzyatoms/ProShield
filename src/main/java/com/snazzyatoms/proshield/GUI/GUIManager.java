package com.snazzyatoms.proshield.GUI;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.PlotManager;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class GUIManager {
    private final ProShield plugin;
    private final PlotManager plotManager;

    public GUIManager(ProShield plugin, PlotManager plotManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
    }

    public void openClaimGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 9, ChatColor.GREEN + "Claim Management");

        gui.setItem(2, createItem(Material.GRASS_BLOCK, ChatColor.GREEN + "Create Claim"));
        gui.setItem(4, createItem(Material.PAPER, ChatColor.YELLOW + "Claim Info"));
        gui.setItem(6, createItem(Material.BARRIER, ChatColor.RED + "Remove Claim"));

        player.openInventory(gui);
    }

    private ItemStack createItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }

    public void handleCreateClaim(Player player) {
        plotManager.createClaim(player);
    }

    public void handleInfoClaim(Player player) {
        plotManager.getClaimInfo(player);
    }

    public void handleRemoveClaim(Player player) {
        plotManager.removeClaim(player);
    }
}
