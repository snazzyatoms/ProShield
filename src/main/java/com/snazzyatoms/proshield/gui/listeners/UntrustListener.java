// src/main/java/com/snazzyatoms/proshield/gui/listeners/UntrustListener.java
package com.snazzyatoms.proshield.gui.listeners;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

/**
 * UntrustListener
 *
 * ✅ Handles untrust GUI actions.
 * ✅ Back button works.
 * ⚠ Actual untrust assignment postponed until v2.0.
 */
public class UntrustListener implements Listener {

    private final PlotManager plots;
    private final ClaimRoleManager roles;
    private final GUIManager gui;

    public UntrustListener(ProShield plugin, PlotManager plots, ClaimRoleManager roles, GUIManager gui) {
        this.plots = plots;
        this.roles = roles;
        this.gui = gui;
    }

    @EventHandler(ignoreCancelled = true)
    public void onUntrustClick(InventoryClickEvent e) {
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

        // ⚠ Postponed untrust assignment
        player.sendMessage(ChatColor.YELLOW + "ℹ Untrust via GUI is coming in v2.0. Use /untrust <player> instead.");
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 0.5f);
    }
}
