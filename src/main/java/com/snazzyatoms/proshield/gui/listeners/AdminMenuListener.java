// src/main/java/com/snazzyatoms/proshield/gui/listeners/AdminMenuListener.java
package com.snazzyatoms.proshield.gui.listeners;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.GUIManager;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class AdminMenuListener implements Listener {

    private final ProShield plugin;
    private final GUIManager gui;

    public AdminMenuListener(ProShield plugin, GUIManager guiManager) {
        this.plugin = plugin;
        this.gui = guiManager;
    }

    @EventHandler(ignoreCancelled = true)
    public void onAdminMenuClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        if (e.getClickedInventory() == null || e.getClickedInventory() != e.getView().getTopInventory()) return;
        if (e.getCurrentItem() == null || !e.getCurrentItem().hasItemMeta()) return;

        String title = ChatColor.stripColor(e.getView().getTitle()).toLowerCase();
        if (!title.contains("proshield admin menu")) return;

        e.setCancelled(true);
        String label = ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName());

        switch (label.toLowerCase()) {
            case "force claim" -> gui.runPlayerCommand(player, "/proshield claim");
            case "force unclaim" -> gui.runPlayerCommand(player, "/proshield unclaim");
            case "claim info" -> {
                var plot = plugin.getPlotManager().getPlot(player.getLocation());
                if (plot != null) gui.openInfoMenu(player, plot);
                else player.sendMessage(ChatColor.RED + "No claim here.");
            }
            case "trust menu" -> gui.openTrustMenu(player, true);
            case "untrust menu" -> gui.openUntrustMenu(player, true);
            case "roles" -> gui.openRolesGUI(player, plugin.getPlotManager().getPlot(player.getLocation()), true);
            case "flags" -> gui.openFlagsMenu(player, true);
            case "back" -> player.closeInventory();
            default -> { return; }
        }

        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
    }
}
