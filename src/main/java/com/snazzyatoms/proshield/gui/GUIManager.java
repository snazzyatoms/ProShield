// path: src/main/java/com/snazzyatoms/proshield/gui/GUIManager.java
package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.PlotManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class GUIManager {

    public static final String MAIN_TITLE = ChatColor.DARK_GREEN + "ProShield Menu";
    public static final String ADMIN_TITLE = ChatColor.translateAlternateColorCodes('&',
            ProShield.getInstance().getAdminConfig().getString("admin-menu.title", "&3ProShield Admin"));
    public static final String CLAIMS_TITLE = ChatColor.translateAlternateColorCodes('&',
            ProShield.getInstance().getAdminConfig().getString("teleport.title", "&3Claims (click to TP)"));

    private final ProShield plugin;

    public GUIManager(ProShield plugin) {
        this.plugin = plugin;
    }

    // ===== Compass =====
    public static ItemStack createAdminCompass() {
        ItemStack compass = new ItemStack(Material.COMPASS);
        ItemMeta meta = compass.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.AQUA + "ProShield Compass");
            compass.setItemMeta(meta);
        }
        return compass;
    }

    // ===== Player Menu =====
    public void openMain(Player player) {
        Inventory gui = Bukkit.createInventory(null, 9, MAIN_TITLE);
        gui.setItem(0, named(Material.GRASS_BLOCK, ChatColor.GREEN + "Create Claim"));
        gui.setItem(4, named(Material.PAPER, ChatColor.YELLOW + "Claim Info"));
        gui.setItem(8, named(Material.BARRIER, ChatColor.RED + "Remove Claim"));
        player.openInventory(gui);
    }

    // ===== Admin Menu =====
    public void openAdminMenu(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, ADMIN_TITLE);
        gui.setItem(10, named(Material.LEVER, ChatColor.GOLD + "Toggle Bypass"));
        gui.setItem(12, named(Material.COMPASS, ChatColor.AQUA + "Give Compass"));
        gui.setItem(14, named(Material.ENDER_PEARL, ChatColor.GREEN + "List Claims"));
        gui.setItem(16, named(Material.REPEATER, ChatColor.YELLOW + "Reload Configs"));
        player.openInventory(gui);
    }

    public void openAdminClaims(Player player, List<String> claimKeys) {
        int size = Math.min(27, Math.max(9, (int) (Math.ceil(claimKeys.size() / 9.0) * 9)));
        Inventory gui = Bukkit.createInventory(null, size, CLAIMS_TITLE);
        for (int i = 0; i < size && i < claimKeys.size(); i++) {
            String key = claimKeys.get(i);
            gui.setItem(i, named(Material.MAP, ChatColor.AQUA + key));
        }
        player.openInventory(gui);
    }

    // ===== Utils =====
    public ItemStack named(Material mat, String name) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            item.setItemMeta(meta);
        }
        return item;
    }
}
