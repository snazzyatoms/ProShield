// path: src/main/java/com/snazzyatoms/proshield/gui/PlayerGUI.java
package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.plots.Claim;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
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
        Inventory gui = Bukkit.createInventory(null, 9, "Claim Management");

        gui.setItem(2, createItem(Material.GRASS_BLOCK, "§aCreate Claim"));
        gui.setItem(4, createItem(Material.PAPER, "§bClaim Info"));
        gui.setItem(6, createItem(Material.BARRIER, "§cRemove Claim"));

        player.openInventory(gui);
    }

    public void handleClick(InventoryClickEvent event, Player player) {
        event.setCancelled(true);

        if (event.getCurrentItem() == null) return;
        Material type = event.getCurrentItem().getType();

        if (type == Material.GRASS_BLOCK) {
            plotManager.createClaim(player.getUniqueId(), player.getLocation());
            player.sendMessage("§aClaim created at your current location.");
        } else if (type == Material.PAPER) {
            Claim claim = plotManager.getClaim(player.getUniqueId(), player.getLocation());
            if (claim != null) {
                player.sendMessage("§bClaim Info: Owner = " + claim.getOwner());
            } else {
                player.sendMessage("§cNo claim found at your location.");
            }
        } else if (type == Material.BARRIER) {
            plotManager.removeClaim(player.getUniqueId(), player.getLocation());
            player.sendMessage("§cClaim removed at your current location.");
        }
    }

    private ItemStack createItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            item.setItemMeta(meta);
        }
        return item;
    }
}
