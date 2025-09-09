package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.plots.PlotManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
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

    // Delegate all GUI inventory clicks to GUIManager
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        if (e.getClickedInventory() == null) return;

        // Let GUIManager decide if it's one of ours + what to do
        if (gui.isOurInventory(e.getView())) {
            e.setCancelled(true);
            gui.handleInventoryClick((Player) e.getWhoClicked(), e.getSlot(), e.getCurrentItem(), e.getView());
        }
    }

    // Open menu on compass right-click
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCompassUse(PlayerInteractEvent e) {
        // Only handle main hand interactions
        if (e.getHand() != EquipmentSlot.HAND) return;

        Player p = e.getPlayer();
        ItemStack item = p.getInventory().getItemInMainHand();
        if (item == null || item.getType() != Material.COMPASS) return;

        // Use GUIManager's detection to ensure it's *our* compass
        if (!gui.isProShieldCompass(item)) return;

        e.setCancelled(true);

        // Open the correct menu based on perms; admin gets admin menu directly if desired,
        // but we keep opening the main menu (which has Admin button at slot 33) for consistency.
        gui.openMain(p);
    }
}
