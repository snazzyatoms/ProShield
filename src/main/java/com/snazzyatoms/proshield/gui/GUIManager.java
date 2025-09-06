// path: src/main/java/com/snazzyatoms/proshield/gui/GUIManager.java
package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.PlotManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class GUIManager {

    private final ProShield plugin;

    public GUIManager(ProShield plugin) {
        this.plugin = plugin;
    }

    public void openClaimGUI(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.GREEN + "Claim Management");

        // Polish a bit: borders with gray panes
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fMeta = filler.getItemMeta();
        fMeta.setDisplayName(" ");
        filler.setItemMeta(fMeta);
        for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, filler);

        inv.setItem(11, makeBtn(Material.LIME_CONCRETE, ChatColor.GREEN + "Create Claim",
                "Claim the current chunk for yourself."));
        inv.setItem(13, makeBtn(Material.MAP, ChatColor.AQUA + "Claim Info",
                "Show info about the current chunk claim."));
        inv.setItem(15, makeBtn(Material.RED_CONCRETE, ChatColor.RED + "Remove Claim",
                "Remove your claim for the current chunk."));

        player.openInventory(inv);
    }

    public static ItemStack createAdminCompass() {
        ItemStack stack = new ItemStack(Material.COMPASS);
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "ProShield Admin Compass");
        meta.setLore(Arrays.asList(
                ChatColor.GRAY + "Right-click to open claim manager",
                ChatColor.DARK_GRAY + "Requires: proshield.compass (or OP)"));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        stack.setItemMeta(meta);
        return stack;
    }

    private ItemStack makeBtn(Material mat, String name, String lore) {
        ItemStack it = new ItemStack(mat);
        ItemMeta meta = it.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(ChatColor.DARK_GRAY + lore));
        it.setItemMeta(meta);
        return it;
    }

    /* Delegations used by GUIListener */
    public void handleCreate(Player p) {
        PlotManager pm = plugin.getPlotManager();
        pm.createClaim(p);
    }

    public void handleInfo(Player p) {
        PlotManager pm = plugin.getPlotManager();
        pm.getClaimInfo(p);
    }

    public void handleRemove(Player p) {
        PlotManager pm = plugin.getPlotManager();
        pm.removeClaim(p);
    }
}
