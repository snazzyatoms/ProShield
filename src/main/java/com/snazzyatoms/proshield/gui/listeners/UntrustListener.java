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
        if (e.getClickedInventory() == null || e.getClickedInventory() != e.getView().getTopInventory()) return;
        if (e.getCurrentItem() == null || !e.getCurrentItem().hasItemMeta()) return;

        String title = ChatColor.stripColor(e.getView().getTitle()).toLowerCase();
        if (!title.contains("untrust")) return;

        e.setCancelled(true);

        String name = ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName()).toLowerCase();
        Plot plot = plots.getPlot(player.getLocation());
        if (plot == null) return;

        if (name.equals("back")) {
            if (player.hasPermission("proshield.admin")) gui.openAdminMain(player);
            else gui.openMain(player);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            return;
        }

        player.sendMessage(ChatColor.YELLOW + "ℹ Untrust via GUI is coming in v2.0. Use /untrust <player> instead.");
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 0.5f);
    }
}
