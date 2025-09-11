// src/main/java/com/snazzyatoms/proshield/gui/listeners/AdminMenuListener.java
package com.snazzyatoms.proshield.gui.listeners;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class AdminMenuListener implements Listener {

    private final ProShield plugin;
    private final GUIManager gui;
    private final PlotManager plots;

    public AdminMenuListener(ProShield plugin, GUIManager guiManager) {
        this.plugin = plugin;
        this.gui = guiManager;
        this.plots = plugin.getPlotManager();
    }

    @EventHandler(ignoreCancelled = true)
    public void onAdminMenuClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        if (e.getClickedInventory() == null || e.getClickedInventory() != e.getView().getTopInventory()) return;
        if (e.getCurrentItem() == null) return;

        String title = ChatColor.stripColor(e.getView().getTitle()).toLowerCase();
        if (!title.contains("proshield admin menu")) return;

        e.setCancelled(true);

        ItemStack item = e.getCurrentItem();
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;

        String name = ChatColor.stripColor(meta.getDisplayName()).toLowerCase();
        Location loc = player.getLocation();

        switch (name) {
            case "claim chunk", "claim" -> {
                if (plots.adminClaim(loc, player.getUniqueId())) {
                    player.sendMessage(ChatColor.GREEN + "Admin-claimed this chunk.");
                    gui.openAdminMain(player);
                } else {
                    player.sendMessage(ChatColor.RED + "Cannot admin-claim this chunk.");
                }
            }
            case "unclaim chunk", "unclaim" -> {
                if (plots.adminUnclaim(loc)) {
                    player.sendMessage(ChatColor.YELLOW + "Chunk forcibly unclaimed.");
                    gui.openAdminMain(player);
                } else {
                    player.sendMessage(ChatColor.RED + "Cannot unclaim here.");
                }
            }
            case "claim info", "info" -> {
                Plot plot = plots.getPlot(loc);
                if (plot == null) {
                    player.sendMessage(ChatColor.GRAY + "No claim here.");
                } else {
                    gui.openInfoMenu(player, plot);
                }
            }
            case "trust menu" -> gui.openTrustMenu(player, true);
            case "untrust menu" -> gui.openUntrustMenu(player, true);
            case "roles" -> gui.openRolesGUI(player, null, true);
            case "flags" -> gui.openFlagsMenu(player, true);
            case "back" -> gui.openAdminMain(player);
            default -> { return; }
        }

        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
    }
}
