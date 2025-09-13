// src/main/java/com/snazzyatoms/proshield/gui/CompassManager.java
package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;

public class CompassManager {
    private final ProShield plugin;

    public CompassManager(ProShield plugin) {
        this.plugin = plugin;
    }

    public ItemStack createCompass() {
        ItemStack item = new ItemStack(Material.COMPASS);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.AQUA + "ProShield Compass");
            meta.setLore(Collections.singletonList(ChatColor.GRAY + "Right-click to open ProShield menu"));
            item.setItemMeta(meta);
        }
        return item;
    }

    public void giveCompass(Player player) {
        if (!plugin.getConfig().getBoolean("settings.give-compass-on-join", true)) return;
        if (!player.getInventory().contains(Material.COMPASS)) {
            player.getInventory().addItem(createCompass());
        }
    }
}
