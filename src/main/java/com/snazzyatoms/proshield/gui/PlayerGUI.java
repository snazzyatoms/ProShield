package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.PlotManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class PlayerGUI {

    private final ProShield plugin;
    private final PlotManager plotManager;

    public PlayerGUI(ProShield plugin) {
        this.plugin = plugin;
        this.plotManager = plugin.getPlotManager();
    }

    public void open(Player player) {
        Inventory gui = Bukkit.createInventory(player, 9, "ProShield Menu");

        // Example buttons
        gui.setItem(0, createButton(Material.GRASS_BLOCK, "§aClaim Chunk"));
        gui.setItem(1, createButton(Material.BARRIER, "§cUnclaim Chunk"));
        gui.setItem(8, createButton(Material.COMPASS, "§eAdmin Compass"));

        player.openInventory(gui);
    }

    public void handleClick(Player player, int slot) {
        switch (slot) {
            case 0 -> plotManager.claimCurrentChunk(player);
            case 1 -> plotManager.unclaimCurrentChunk(player);
            case 8 -> player.getInventory().addItem(GUIManager.createAdminCompass());
            default -> player.sendMessage("§7Nothing here.");
        }
    }

    private ItemStack createButton(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            item.setItemMeta(meta);
        }
        return item;
    }
}
