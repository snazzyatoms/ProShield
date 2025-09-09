// path: src/main/java/com/snazzyatoms/proshield/gui/GUIListener.java
package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.plots.PlotManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class GUIListener implements Listener {

    private final PlotManager plots;
    private final GUIManager gui;

    public GUIListener(PlotManager plots, GUIManager gui) {
        this.plots = plots;
        this.gui = gui;
    }

    /** Open main GUI when the tagged compass is right-clicked (air or block). */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onUseCompass(PlayerInteractEvent e) {
        Action a = e.getAction();
        if (a != Action.RIGHT_CLICK_AIR && a != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack it = e.getItem();
        if (!GUIManager.isProShieldCompass(it)) return;

        e.setCancelled(true); // stop other plugins from eating it
        gui.openMain(e.getPlayer());
    }

    /** Basic click handler (kept minimal—your existing menu code can expand from here). */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getView() == null || e.getView().getTitle() == null) return;
        if (!ChatColor.stripColor(e.getView().getTitle()).equalsIgnoreCase(ChatColor.stripColor(GUIManager.TITLE))) return;

        e.setCancelled(true);

        var p = (org.bukkit.entity.Player)e.getWhoClicked();
        var slot = e.getRawSlot();
        if (slot == 11) {
            p.performCommand("proshield claim");
            p.closeInventory();
        } else if (slot == 13) {
            p.performCommand("proshield info");
            p.closeInventory();
        } else if (slot == 15) {
            p.performCommand("proshield unclaim");
            p.closeInventory();
        } else if (slot == 31) {
            p.performCommand("proshield help"); // or gui.openHelp(p) if you have it
            p.closeInventory();
        } else if (slot == 33) {
            if (p.hasPermission("proshield.admin.gui")) {
                p.performCommand("proshield admin"); // or openAdmin(p)
            } else {
                p.sendMessage(ChatColor.RED + "You don’t have permission to open Admin tools.");
            }
        }
    }
}
