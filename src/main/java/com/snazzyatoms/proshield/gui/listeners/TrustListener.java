package com.snazzyatoms.proshield.gui.listeners;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

/**
 * TrustListener
 *
 * ✅ Handles trust GUI actions.
 * ✅ Back button works.
 * ⚠ Actual trust assignment postponed until v2.0.
 */
public class TrustListener implements Listener {

    private final PlotManager plots;
    private final GUIManager gui;

    public TrustListener(ProShield plugin, PlotManager plots, GUIManager gui) {
        this.plots = plots;
        this.gui = plugin.getGuiManager();
    }

    @EventHandler(ignoreCancelled = true)
    public void onTrustClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        if (e.getCurrentItem() == null || !e.getCurrentItem().hasItemMeta()) return;

        // ✅ Prevent item movement
        e.setCancelled(true);

        String name = ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName()).toLowerCase();
        Plot plot = plots.getPlot(player.getLocation());
        if (plot == null) return;

        // ✅ Handle back button
        if (name.equals("back")) {
            boolean fromAdmin = player.hasPermission("proshield.admin");
            if (fromAdmin) gui.openAdminMain(player);
            else gui.openMain(player);
            return;
        }

        // ⚠ Postponed trust assignment
        player.sendMessage(ChatColor.YELLOW + "ℹ Trust via GUI is coming in v2.0. Use /trust <player> instead.");
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 0.5f);
    }
}
