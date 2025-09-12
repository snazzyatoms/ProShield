// src/main/java/com/snazzyatoms/proshield/gui/listeners/TrustListener.java
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

public class TrustListener implements Listener {

    private final PlotManager plots;
    private final ClaimRoleManager roles;
    private final GUIManager gui;

    public TrustListener(ProShield plugin, PlotManager plots, ClaimRoleManager roles, GUIManager gui) {
        this.plots = plots;
        this.roles = roles;
        this.gui = gui;
    }

    @EventHandler(ignoreCancelled = true)
    public void onTrustClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        if (e.getClickedInventory() == null || e.getClickedInventory() != e.getView().getTopInventory()) return;
        if (e.getCurrentItem() == null || !e.getCurrentItem().hasItemMeta()) return;

        String title = ChatColor.stripColor(e.getView().getTitle()).toLowerCase();
        if (!title.contains("trust menu")) return;

        e.setCancelled(true);
        String name = ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName());

        if (name.equalsIgnoreCase("Back")) {
            if (player.hasPermission("proshield.admin")) gui.openAdminMain(player); else gui.openMain(player);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            return;
        }

        // Clicked a player head -> trust them (default role)
        String target = ChatColor.stripColor(name);
        gui.rememberTarget(player, target);
        gui.runPlayerCommand(player, "/trust " + target);

        player.sendMessage(ChatColor.GREEN + "Trusted " + ChatColor.WHITE + target + ChatColor.GREEN + ".");
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.2f);

        // offer role assignment right after trust
        gui.openRolesGUI(player, plots.getPlot(player.getLocation()), player.hasPermission("proshield.admin"));
    }
}
