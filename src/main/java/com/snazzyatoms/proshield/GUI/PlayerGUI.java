package com.proshield.gui;

import com.proshield.ProShield;
import com.proshield.managers.PlotManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class PlayerGUI {

    private final ProShield plugin;
    private final Player player;
    private final Inventory inventory;

    public PlayerGUI(ProShield plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(null, 27, ChatColor.GREEN + "ProShield - Plot Menu");

        setupMenu();
    }

    private void setupMenu() {
        // Claim Plot
        inventory.setItem(10, createMenuItem(Material.GRASS_BLOCK,
                ChatColor.GREEN + "Claim Plot",
                "Protect your land from griefing.",
                "First-time claim is free."));

        // Expand Plot
        inventory.setItem(12, createMenuItem(Material.EMERALD,
                ChatColor.YELLOW + "Expand Plot",
                "Increase the radius of your claim.",
                "Costs scale with size."));

        // Claim Wizard (step-by-step guidance)
        inventory.setItem(14, createMenuItem(Material.BOOK,
                ChatColor.AQUA + "Claim Wizard",
                "Guided tutorial on claiming",
                "Perfect for new players."));

        // Player Quick Guide
        inventory.setItem(16, createMenuItem(Material.WRITABLE_BOOK,
                ChatColor.BLUE + "Quick Guide",
                "Learn how to manage plots.",
                "Tips & tricks for ProShield."));

        // Preview Plot
        inventory.setItem(22, createMenuItem(Material.MAP,
                ChatColor.LIGHT_PURPLE + "Preview Plot",
                "Visualize your current claim",
                "See what’s protected."));
    }

    private ItemStack createMenuItem(Material material, String name, String... loreLines) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(Arrays.asList(loreLines));
            item.setItemMeta(meta);
        }
        return item;
    }

    public Inventory getInventory() {
        return inventory;
    }
}
