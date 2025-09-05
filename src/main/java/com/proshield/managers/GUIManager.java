package com.snazzyatoms.proshield.managers;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class GUIManager {

    private final PlotManager plotManager;

    public GUIManager(PlotManager plotManager) {
        this.plotManager = plotManager;
    }

    public void openClaimGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 9, "§aProShield Claims");

        gui.setItem(2, createButton(Material.GRASS_BLOCK, "§aCreate Claim"));
        gui.setItem(4, createButton(Material.BOOK, "§bClaim Info"));
        gui.setItem(6, createButton(Material.BARRIER, "§cRemove Claim"));

        player.openInventory(gui);
    }

    private ItemStack createButton(Material mat, String name) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }

    public void handleGUIClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!event.getView().getTitle().equals("§aProShield Claims")) return;

        event.setCancelled(true);

        switch (event.getSlot()) {
            case 2 -> plotManager.createClaim(player, player.getLocation(), 10);
            case 4 -> player.sendMessage(plotManager.getClaimInfo(player));
            case 6 -> plotManager.removeClaim(player);
        }
    }
}
