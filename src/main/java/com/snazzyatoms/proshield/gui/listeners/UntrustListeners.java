// src/main/java/com/snazzyatoms/proshield/gui/listeners/UntrustListener.java
package com.snazzyatoms.proshield.gui.listeners;

import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.UUID;

/**
 * UntrustListener
 *
 * ✅ Removes a trusted player from the claim.
 */
public class UntrustListener implements Listener {

    private final PlotManager plots;
    private final ClaimRoleManager roles;

    public UntrustListener(PlotManager plots, ClaimRoleManager roles) {
        this.plots = plots;
        this.roles = roles;
    }

    @EventHandler(ignoreCancelled = true)
    public void onUntrustClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        if (e.getCurrentItem() == null) return;

        e.setCancelled(true);

        Plot plot = plots.getPlot(player.getLocation());
        if (plot == null) return;

        UUID target = roles.getPendingTarget(player);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "⚠ No player selected to untrust.");
            return;
        }

        roles.removeRole(plot, target);

        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 0.8f);
        player.sendMessage(ChatColor.YELLOW + "✖ Untrusted player.");
    }
}
