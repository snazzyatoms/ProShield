// src/main/java/com/snazzyatoms/proshield/gui/listeners/TrustListener.java
package com.snazzyatoms.proshield.gui.listeners;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.roles.ClaimRole;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.UUID;

/**
 * TrustListener
 *
 * ✅ Handles trust GUI actions.
 * ✅ Sets trusted players to default role (e.g. MEMBER).
 */
public class TrustListener implements Listener {

    private final PlotManager plots;
    private final ClaimRoleManager roles;

    public TrustListener(PlotManager plots, ClaimRoleManager roles) {
        this.plots = plots;
        this.roles = roles;
    }

    @EventHandler(ignoreCancelled = true)
    public void onTrustClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        if (e.getCurrentItem() == null) return;

        e.setCancelled(true);

        Plot plot = plots.getPlot(player.getLocation());
        if (plot == null) return;

        UUID target = roles.getPendingTarget(player);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "⚠ No player selected to trust.");
            return;
        }

        roles.setRole(plot, target, ClaimRole.MEMBER);

        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.2f);
        player.sendMessage(ChatColor.GREEN + "✔ Trusted player as " + ClaimRole.MEMBER.getDisplayName());
    }
}
