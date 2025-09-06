package com.snazzyatoms.proshield.listeners;

import com.snazzyatoms.proshield.managers.GUIManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class CompassListener implements Listener {

    private final GUIManager gui;

    public CompassListener(GUIManager gui) {
        this.gui = gui;
    }

    @EventHandler
    public void onCompassUse(PlayerInteractEvent e) {
        if (!(e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK)) return;

        Player p = e.getPlayer();
        ItemStack item = e.getItem();
        if (item == null || item.getType() != Material.COMPASS) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;

        String name = ChatColor.stripColor(meta.getDisplayName());
        if (!name.equalsIgnoreCase("ProShield Admin Compass")) return;

        if (!p.hasPermission("proshield.compass")) {
            p.sendMessage(ChatColor.RED + "You do not have permission to use the ProShield Compass.");
            return;
        }

        e.setCancelled(true);
        gui.openClaimGUI(p);
    }
}
