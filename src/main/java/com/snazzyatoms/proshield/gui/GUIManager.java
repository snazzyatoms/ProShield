package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class GUIManager {

    private final ProShield plugin;
    private final PlayerGUI playerGUI;

    public GUIManager(ProShield plugin) {
        this.plugin = plugin;
        this.playerGUI = new PlayerGUI(plugin);
    }

    public PlayerGUI getPlayerGUI() {
        return playerGUI;
    }

    public static ItemStack createAdminCompass() {
        ItemStack compass = new ItemStack(Material.COMPASS);
        ItemMeta meta = compass.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6ProShield Admin Compass");
            compass.setItemMeta(meta);
        }
        return compass;
    }
}
