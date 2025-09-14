// src/main/java/com/snazzyatoms/proshield/gui/GUIManager.java
package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
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

    /**
     * Opens the main ProShield GUI.
     */
    public void openMainMenu(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, "§6ProShield Menu");

        // Claim land button
        gui.setItem(11, createItem(Material.GRASS_BLOCK, "§aClaim Land",
                List.of("§7Protect your land", "§fRadius: " + plugin.getConfig().getInt("claims.default-radius", 50) + " blocks")));

        // Claim info button
        Plot plot = plotManager.getPlot(player.getLocation());
        if (plot != null) {
            int defaultRadius = plugin.getConfig().getInt("claims.default-radius", 50);
            int currentRadius = plotManager.getClaimRadius(plot.getId());

            gui.setItem(13, createItem(Material.PAPER, "§eClaim Info",
                    List.of(
                            "§7Owner: §f" + plugin.getPlotManager().getPlayerName(plot.getOwner()),
                            "§7Default Radius: §f" + defaultRadius + " blocks",
                            "§7Current Radius: §f" + currentRadius + " blocks",
                            "§7Protections:",
                            "§f✔ Pets safe",
                            "§f✔ Containers locked",
                            "§f✔ Item Frames protected",
                            "§f✔ Armor Stands protected",
                            "§f✔ Explosions disabled",
                            "§f✔ Fire spread disabled",
                            "§f✔ Hostile mobs blocked"
                    )));
        } else {
            gui.setItem(13, createItem(Material.PAPER, "§eClaim Info",
                    List.of("§7No claim found here.", "§fUse /claim to start.")));
        }

        // Unclaim button
        gui.setItem(15, createItem(Material.BARRIER, "§cUnclaim Land",
                List.of("§7Remove your claim")));

        player.openInventory(gui);
    }

    /**
     * Create an item with a custom name and lore.
     */
    private ItemStack createItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(lore);
            // Remove attack/damage attributes for clarity
            meta.setUnbreakable(true);
        }
        item.setItemMeta(meta);
        return item;
    }
}
