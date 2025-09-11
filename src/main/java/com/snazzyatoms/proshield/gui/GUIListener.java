package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

/**
 * GUIListener handles clicks in ProShield GUIs.
 *
 * ✅ Fixed calls to PlotManager (getPlot instead of getClaim)
 * ✅ Fixed calls to GUIManager (only valid overloads used)
 * ✅ Preserves prior navigation logic
 */
public class GUIListener implements Listener {

    private final ProShield plugin;
    private final GUIManager gui;
    private final PlotManager plots;

    public GUIListener(ProShield plugin, GUIManager gui, PlotManager plots) {
        this.plugin = plugin;
        this.gui = gui;
        this.plots = plots;
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        String title = event.getView().getTitle();
        if (title == null) return;

        // Prevent taking items from GUIs
        event.setCancelled(true);

        Plot plot = plots.getPlot(player.getLocation());

        switch (title) {
            case "§dClaim Menu" -> {
                switch (event.getSlot()) {
                    case 11 -> gui.openFlagsMenu(player);
                    case 13 -> gui.openRolesGUI(player, plot);
                    case 15 -> gui.openTransferMenu(player);
                }
            }
            case "§dClaim Flags" -> {
                if (event.getSlot() == 22) {
                    gui.openMain(player);
                }
            }
            case "§dClaim Roles" -> {
                switch (event.getSlot()) {
                    case 11 -> gui.openTrustMenu(player);
                    case 13 -> gui.openUntrustMenu(player);
                    case 15 -> gui.openRolesGUI(player, plot); // re-open roles for assignments
                    case 22 -> gui.openMain(player);
                }
            }
            case "§dTrust Player" -> {
                if (event.getSlot() == 22) {
                    gui.openRolesGUI(player, plot);
                }
            }
            case "§dUntrust Player" -> {
                if (event.getSlot() == 22) {
                    gui.openRolesGUI(player, plot);
                }
            }
            case "§dTransfer Claim" -> {
                if (event.getSlot() == 22) {
                    gui.openMain(player);
                }
            }
            case "§dAdmin Menu" -> {
                if (event.getSlot() == 22) {
                    gui.openMain(player);
                }
            }
        }
    }
}
