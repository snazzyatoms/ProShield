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

import java.util.ArrayList;
import java.util.List;

public class GUIManager {

    public static final String TITLE_MAIN  = ChatColor.DARK_AQUA + "ProShield";
    public static final String TITLE_ADMIN = ChatColor.DARK_RED + "ProShield Admin";
    public static final String TITLE_HELP  = ChatColor.DARK_GREEN + "ProShield Help";

    private final ProShield plugin;
    private final PlotManager plotManager;

    // configurable slots
    private int SLOT_CLAIM;
    private int SLOT_INFO;
    private int SLOT_UNCLAIM;
    private int SLOT_HELP;
    private int SLOT_ADMIN;

    public GUIManager(ProShield plugin, PlotManager plotManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
        reloadSlotsFromConfig();
    }

    public void reloadSlotsFromConfig() {
        SLOT_CLAIM   = plugin.getConfig().getInt("gui.slots.claim", 11);
        SLOT_INFO    = plugin.getConfig().getInt("gui.slots.info", 13);
        SLOT_UNCLAIM = plugin.getConfig().getInt("gui.slots.unclaim", 15);
        SLOT_HELP    = plugin.getConfig().getInt("gui.slots.help", 31);
        SLOT_ADMIN   = plugin.getConfig().getInt("gui.slots.admin", 33);
    }

    public void openMain(org.bukkit.entity.Player p) {
        Inventory inv = Bukkit.createInventory(null, 45, TITLE_MAIN);

        inv.setItem(SLOT_CLAIM,   button(Material.OAK_SAPLING, ChatColor.GREEN + "Create Claim",
                lore("Claim the chunk you are standing in.")));
        inv.setItem(SLOT_INFO,    button(Material.PAPER, ChatColor.AQUA + "Claim Info",
                lore("View owner & trusted players.")));
        inv.setItem(SLOT_UNCLAIM, button(Material.BARRIER, ChatColor.RED + "Remove Claim",
                lore("Unclaim the current chunk.")));
        inv.setItem(SLOT_HELP,    button(Material.BOOK, ChatColor.GOLD + "Help",
                lore("Show commands you can use.")));
        inv.setItem(SLOT_ADMIN,   button(Material.REDSTONE, ChatColor.RED + "Admin",
                lore("Admin settings & toggles.")));

        p.openInventory(inv);
    }

    public void openAdmin(org.bukkit.entity.Player p) {
        Inventory inv = Bukkit.createInventory(null, 27, TITLE_ADMIN);

        boolean keepEnabled = plugin.getConfig().getBoolean("claims.keep-items.enabled", false);
        int retention = clampRetention(plugin.getConfig().getInt("claims.keep-items.retention-seconds", 600));

        inv.setItem(11, button(keepEnabled ? Material.LIME_DYE : Material.GRAY_DYE,
                ChatColor.YELLOW + "Keep Items in Claims: " + (keepEnabled ? ChatColor.GREEN + "ON" : ChatColor.RED + "OFF"),
                lore("When ON, dropped items inside claimed chunks",
                     "won't despawn until retention is reached.",
                     ChatColor.DARK_GRAY + "Retention: " + retention + "s",
                     "",
                     ChatColor.GRAY + "Click to toggle",
                     ChatColor.DARK_GRAY + "(Requires: proshield.admin.keepdrops)")));

        inv.setItem(15, button(Material.CLOCK, ChatColor.YELLOW + "Set Retention",
                lore("Retention window for claim item despawn.",
                     ChatColor.DARK_GRAY + "Min 300s, Max 900s",
                     ChatColor.GRAY + "Use config.yml to set exact value.")));

        p.openInventory(inv);
    }

    public void openHelp(org.bukkit.entity.Player p) {
        Inventory inv = Bukkit.createInventory(null, 45, TITLE_HELP);
        List<ItemStack> items = new ArrayList<>();

        // Only show commands the player can actually run
        items.add(helpLine("/proshield", "Main command + help", true));
        items.add(helpLine("/proshield claim", "Claim your current chunk", p.hasPermission("proshield.use")));
        items.add(helpLine("/proshield unclaim", "Remove your claim", p.hasPermission("proshield.use")));
        items.add(helpLine("/proshield info", "Show claim info", p.hasPermission("proshield.use")));
        items.add(helpLine("/proshield trust <player>", "Grant access to a player", p.hasPermission("proshield.use")));
        items.add(helpLine("/proshield untrust <player>", "Remove trust", p.hasPermission("proshield.use")));
        items.add(helpLine("/proshield trusted", "List trusted players", p.hasPermission("proshield.use")));
        items.add(helpLine("/proshield compass", "Give ProShield compass", p.hasPermission("proshield.compass")));
        items.add(helpLine("/proshield bypass <on|off|toggle>", "Toggle admin bypass", p.hasPermission("proshield.bypass")));
        items.add(helpLine("/proshield reload", "Reload configuration", p.hasPermission("proshield.admin.reload")));
        items.add(helpLine("/proshield purgeexpired", "Purge expired claims", p.hasPermission("proshield.admin.expired.purge")));

        int slot = 10;
        for (ItemStack is : items) {
            if (is == null) continue;
            if (slot % 9 == 8) slot += 3; // new row spacing
            if (slot >= inv.getSize()) break;
            inv.setItem(slot++, is);
        }
        p.openInventory(inv);
    }

    public static ItemStack createAdminCompass() {
        ItemStack it = new ItemStack(Material.COMPASS);
        ItemMeta meta = it.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + "ProShield Compass");
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        it.setItemMeta(meta);
        return it;
    }

    private ItemStack helpLine(String cmd, String desc, boolean show) {
        if (!show) return null;
        return button(Material.PAPER, ChatColor.AQUA + cmd, lore(ChatColor.GRAY + desc));
    }

    private ItemStack button(Material mat, String name, List<String> lore) {
        ItemStack it = new ItemStack(mat);
        ItemMeta meta = it.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        it.setItemMeta(meta);
        return it;
    }

    private List<String> lore(String... lines) {
        List<String> l = new ArrayList<>();
        for (String s : lines) l.add(s);
        return l;
    }

    private int clampRetention(int sec) {
        if (sec < 300) return 300;
        if (sec > 900) return 900;
        return sec;
    }
}
