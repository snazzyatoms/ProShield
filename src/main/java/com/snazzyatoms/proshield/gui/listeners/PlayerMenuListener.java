// src/main/java/com/snazzyatoms/proshield/gui/listeners/PlayerMenuListener.java
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

public class PlayerMenuListener implements Listener {

    private final ProShield plugin;
    private final GUIManager gui;
    private final PlotManager plots;

    public PlayerMenuListener(ProShield plugin, GUIManager guiManager) {
        this.plugin = plugin;
        this.gui = guiManager;
        this.plots = plugin.getPlotManager();
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerMenuClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        if (e.getClickedInventory() == null || e.getClickedInventory() != e.getView().getTopInventory()) return;
        if (e.getCurrentItem() == null) return;

        String title = ChatColor.stripColor(e.getView().getTitle()).toLowerCase();
        if (!title.contains("proshield menu")) return;

        e.setCancelled(true);

        ItemStack item = e.getCurrentItem();
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;

        String name = ChatColor.stripColor(meta.getDisplayName()).toLowerCase();
        Location loc = player.getLocation();

        switch (name) {
            case "claim chunk", "claim" -> {
                if (plots.claim(loc, player.getUniqueId())) {
                    player.sendMessage(ChatColor.GREEN + "Chunk claimed.");
                    gui.openMain(player);
                } else {
                    player.sendMessage(ChatColor.RED + "This chunk is already claimed or cannot be claimed.");
                }
            }
            case "unclaim chunk", "unclaim" -> {
                if (plots.unclaim(loc, player.getUniqueId())) {
                    player.sendMessage(ChatColor.YELLOW + "Chunk unclaimed.");
                    gui.openMain(player);
                } else {
                    player.sendMessage(ChatColor.RED + "You can’t unclaim this chunk.");
                }
            }
            case "claim info", "info" -> {
                Plot plot = plots.getPlot(loc);
                if (plot == null) {
                    player.sendMessage(ChatColor.GRAY + "No claim here.");
                } else {
                    gui.openInfoMenu(player, plot); // assumes you have this; otherwise send text info here
                }
            }
            case "trust menu" -> gui.openTrustMenu(player, false);
            case "untrust menu" -> gui.openUntrustMenu(player, false);
            case "roles" -> gui.openRolesGUI(player, null, false);
            case "flags" -> gui.openFlagsMenu(player, false);
            case "back" -> gui.openMain(player);
            default -> { return; }
        }

        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
    }
}
