// path: src/main/java/com/snazzyatoms/proshield/gui/GUIListener.java
package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.plots.PlotManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.InventoryView;

public class GUIListener implements Listener {

    private final PlotManager plots;
    private final GUIManager gui;

    public GUIListener(PlotManager plots, GUIManager gui) {
        this.plots = plots;
        this.gui = gui;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        InventoryView view = p.getOpenInventory();
        if (!gui.isOurInventory(view)) return;

        e.setCancelled(true); // always cancel to prevent taking items
        if (e.getClickedInventory() == null) return;

        int slot = e.getSlot();
        ItemStack clicked = e.getCurrentItem();
        gui.handleInventoryClick(p, slot, clicked, view);
    }

    @EventHandler
    public void onCompassUse(PlayerInteractEvent e) {
        if (e.getHand() != EquipmentSlot.HAND) return; // main hand only
        ItemStack it = e.getItem();
        if (!gui.isProShieldCompass(it)) return;

        e.setCancelled(true);
        Player p = e.getPlayer();
        // Admin compass opens admin; player compass opens main
        String title = it.getItemMeta() != null ? ChatColor.stripColor(it.getItemMeta().getDisplayName()) : "";
        if (title.toLowerCase().contains("admin")) {
            gui.openAdmin(p);
        } else {
            gui.openMain(p);
        }
    }
}
