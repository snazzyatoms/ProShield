package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.plots.PlotManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class GUIListener implements Listener {

    private final PlotManager plots;
    private final GUIManager gui;

    public GUIListener(PlotManager plots, GUIManager gui) {
        this.plots = plots;
        this.gui = gui;
    }

    @EventHandler
    public void onCompassUse(PlayerInteractEvent e) {
        if (e.getHand() == EquipmentSlot.OFF_HAND) return;
        ItemStack it = e.getItem();
        if (!gui.isProShieldCompass(it)) return;
        e.setCancelled(true);
        Player p = e.getPlayer();
        boolean admin = p.hasPermission("proshield.admin.gui");
        gui.openMain(p, admin);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        if (e.getClickedInventory() == null) return;
        if (!gui.isOurInventory(e.getView())) return;

        e.setCancelled(true);
        gui.handleInventoryClick(p, e.getRawSlot(), e.getCurrentItem(), e.getView());
    }
}
