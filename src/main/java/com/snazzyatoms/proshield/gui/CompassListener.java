package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class CompassListener implements Listener {

    private final ProShield plugin;
    private final GUIManager guiManager;

    public CompassListener(ProShield plugin, GUIManager guiManager) {
        this.plugin = plugin;
        this.guiManager = guiManager;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.COMPASS) return;
        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return;

        String display = ChatColor.stripColor(item.getItemMeta().getDisplayName());
        if (!display.equalsIgnoreCase("ProShield Compass")) return;

        event.setCancelled(true);
        guiManager.openMenu(player, "main");
    }
}
