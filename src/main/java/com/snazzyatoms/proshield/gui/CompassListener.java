package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class CompassListener implements Listener {

    private final ProShield plugin;

    public CompassListener(ProShield plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer(); // ✅ fix: no pattern matching

        ItemStack item = event.getItem();
        if (item == null) return;

        if (item.getType() == Material.COMPASS) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && ChatColor.stripColor(meta.getDisplayName()).equalsIgnoreCase("ProShield Compass")) {
                event.setCancelled(true);
                plugin.getGuiManager().openMenu(player, "main");
            }
        }
    }
}
