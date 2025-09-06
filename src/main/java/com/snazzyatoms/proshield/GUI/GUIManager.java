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
    private final String title;

    public GUIManager(ProShield plugin, PlotManager plotManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
        this.title = ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("settings.gui-title", "&aClaim Management"));
    }

    public void openClaimGUI(Player player) {
        Inventory gui = Bukkit.createInventory(player, 27, title);

        // slots 11, 13, 15 for Create / Info / Remove
        gui.setItem(11, make(Material.LIME_DYE, ChatColor.GREEN + "Create Claim"));
        gui.setItem(13, make(Material.WRITABLE_BOOK, ChatColor.GOLD + "Claim Info"));
        gui.setItem(15, make(Material.BARRIER, ChatColor.RED + "Remove Claim"));

        player.openInventory(gui);
    }

    private ItemStack make(Material type, String name) {
        ItemStack it = new ItemStack(type);
        ItemMeta meta = it.getItemMeta();
        meta.setDisplayName(name);
        it.setItemMeta(meta);
        return it;
    }

    public void handleClick(Player player, String buttonName) {
        switch (ChatColor.stripColor(buttonName).toLowerCase()) {
            case "create claim" -> plotManager.createClaim(player);
            case "claim info" -> plotManager.sendClaimInfo(player);
            case "remove claim" -> plotManager.removeClaim(player);
        }
    }

    public String getTitle() {
        return title;
    }
}
