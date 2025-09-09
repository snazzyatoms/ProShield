// path: src/main/java/com/snazzyatoms/proshield/gui/GUIManager.java
package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.PlotManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class GUIManager {

    public static final String TITLE = ChatColor.AQUA + "ProShield";
    public static final String ADMIN_TITLE = ChatColor.DARK_AQUA + "ProShield Admin";

    // Main menu slots (keep your existing layout)
    public static final int SLOT_CREATE = 11;
    public static final int SLOT_INFO   = 13;
    public static final int SLOT_REMOVE = 15;
    public static final int SLOT_ADMIN  = 33; // open Admin GUI from main

    // Admin menu slots
    public static final int SLOT_ADMIN_BACK           = 31;
    public static final int SLOT_TOGGLE_DROP_IF_FULL  = 20;

    private final ProShield plugin;
    private final PlotManager plotManager;

    public GUIManager(ProShield plugin, PlotManager plotManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
    }

    /* -------------------------
     * Public openers
     * ------------------------- */
    public void openMain(org.bukkit.entity.Player p) {
        Inventory inv = Bukkit.createInventory(null, 45, TITLE);

        inv.setItem(SLOT_CREATE, simpleItem(Material.LIME_BED, ChatColor.GREEN + "Create Claim",
                "Claim the chunk you are standing in."));
        inv.setItem(SLOT_INFO, simpleItem(Material.BOOK, ChatColor.AQUA + "Claim Info",
                "View owner and trusted players for this chunk."));
        inv.setItem(SLOT_REMOVE, simpleItem(Material.BARRIER, ChatColor.RED + "Remove Claim",
                "Unclaim this chunk (owner only)."));

        // Admin button (only visible/useful for admins)
        inv.setItem(SLOT_ADMIN, simpleItem(Material.COMPARATOR, ChatColor.GOLD + "Admin Menu",
                "Admin tools & settings."));

        p.openInventory(inv);
    }

    public void openAdmin(org.bukkit.entity.Player p) {
        Inventory inv = Bukkit.createInventory(null, 45, ADMIN_TITLE);

        // Toggle: compass.drop-if-full
        boolean dropIfFull = plugin.getConfig().getBoolean("compass.drop-if-full", true);
        ItemStack toggle = simpleItem(
                dropIfFull ? Material.LIME_DYE : Material.GRAY_DYE,
                (dropIfFull ? ChatColor.GREEN : ChatColor.GRAY) + "Compass: Drop if inventory full",
                "Current: " + (dropIfFull ? ChatColor.GREEN + "ON" : ChatColor.RED + "OFF"),
                ChatColor.GRAY + "Click to toggle."
        );
        inv.setItem(SLOT_TOGGLE_DROP_IF_FULL, toggle);

        // Back button
        inv.setItem(SLOT_ADMIN_BACK, simpleItem(Material.ARROW, ChatColor.YELLOW + "Back",
                "Return to the main ProShield menu."));

        p.openInventory(inv);
    }

    /* -------------------------
     * Actions used by GUIListener
     * ------------------------- */
    public void toggleDropIfFull(org.bukkit.entity.Player p) {
        boolean current = plugin.getConfig().getBoolean("compass.drop-if-full", true);
        plugin.getConfig().set("compass.drop-if-full", !current);
        plugin.saveConfig();
        p.sendMessage(prefix() + ChatColor.GREEN + "Compass drop-if-full is now " +
                (!current ? ChatColor.GREEN + "ON" : ChatColor.RED + "OFF") + ChatColor.RESET + ".");
        // Refresh the admin screen to reflect the new state
        openAdmin(p);
    }

    /* -------------------------
     * Utilities
     * ------------------------- */
    public static ItemStack createAdminCompass() {
        ItemStack it = new ItemStack(Material.COMPASS, 1);
        ItemMeta meta = it.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "ProShield Compass");
        meta.setLore(Arrays.asList(
                ChatColor.GRAY + "Right-click to open the ProShield menu."
        ));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        it.setItemMeta(meta);
        return it;
    }

    private ItemStack simpleItem(Material mat, String name, String... lore) {
        ItemStack it = new ItemStack(mat);
        ItemMeta meta = it.getItemMeta();
        meta.setDisplayName(name);
        if (lore != null && lore.length > 0) {
            java.util.List<String> lines = new java.util.ArrayList<>();
            for (String s : lore) lines.add(ChatColor.translateAlternateColorCodes('&', s));
            meta.setLore(lines);
        }
        it.setItemMeta(meta);
        return it;
    }

    private String prefix() {
        return ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("messages.prefix", "&3[ProShield]&r "));
    }
}
