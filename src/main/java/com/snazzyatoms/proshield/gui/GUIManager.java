package com.snazzyatoms.proshield.gui;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class GUIManager {

    private static final String COMPASS_NAME = ChatColor.AQUA + "ProShield Compass";
    private static final String[] COMPASS_LORE = new String[]{
            ChatColor.GRAY + "Right-click to open",
            ChatColor.GRAY + "the ProShield menu"
    };

    private final Object pluginRef; // keep if you need it later

    public GUIManager(Object plugin) {
        this.pluginRef = plugin;
    }

    /** Create the Admin/OP ProShield compass. */
    public static ItemStack createAdminCompass() {
        ItemStack compass = new ItemStack(Material.COMPASS);
        ItemMeta meta = compass.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(COMPASS_NAME);
            meta.setLore(Arrays.asList(COMPASS_LORE));
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            compass.setItemMeta(meta);
        }
        return compass;
    }

    /** True if the given item is specifically a ProShield compass. */
    public static boolean isProShieldCompass(ItemStack item) {
        if (item == null || item.getType() != Material.COMPASS || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return false;
        String name = ChatColor.stripColor(meta.getDisplayName());
        return name.equalsIgnoreCase(ChatColor.stripColor(COMPASS_NAME));
    }

    /** True if the player's inventory already contains a ProShield compass. */
    public static boolean hasProShieldCompass(Player p) {
        return Arrays.stream(p.getInventory().getContents())
                .anyMatch(GUIManager::isProShieldCompass);
    }

    /** Open the main GUI. */
    public void openMainGUI(Player player) {
        PlayerGUI.open(player);
    }
}
