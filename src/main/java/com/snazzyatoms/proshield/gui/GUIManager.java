package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.plots.PlotManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class GUIManager {

    private final PlotManager plotManager;

    public static final String TITLE = ChatColor.DARK_AQUA + "ProShield Menu";

    public GUIManager(PlotManager plotManager) {
        this.plotManager = plotManager;
    }

    public static ItemStack createAdminCompass() {
        ItemStack it = new ItemStack(Material.COMPASS);
        ItemMeta meta = it.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + "ProShield Compass");
        meta.setLore(java.util.List.of(
                ChatColor.GRAY + "Right-click to open the ProShield menu."
        ));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        it.setItemMeta(meta);
        return it;
    }

    public void openMain(Player p) {
        Inventory inv = Bukkit.createInventory(p, 27, TITLE);

        inv.setItem(11, button(Material.GRASS_BLOCK, ChatColor.GREEN + "Create Claim",
                ChatColor.GRAY + "Claim the chunk you're standing in."));
        inv.setItem(13, button(Material.PAPER, ChatColor.YELLOW + "Claim Info",
                ChatColor.GRAY + "View owner and trusted players."));
        inv.setItem(15, button(Material.BARRIER, ChatColor.RED + "Remove Claim",
                ChatColor.GRAY + "Unclaim this chunk (if you are the owner)."));

        p.openInventory(inv);
    }

    private ItemStack button(Material mat, String name, String lore) {
        ItemStack it = new ItemStack(mat);
        ItemMeta meta = it.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(java.util.List.of(lore));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        it.setItemMeta(meta);
        return it;
    }

    public PlotManager getPlotManager() {
        return plotManager;
    }
}
