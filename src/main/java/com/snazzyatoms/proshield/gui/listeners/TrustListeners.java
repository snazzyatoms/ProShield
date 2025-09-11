// src/main/java/com/snazzyatoms/proshield/gui/listeners/TrustListener.java
package com.snazzyatoms.proshield.gui.listeners;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.GUIManager;
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
 * ✅ Assigns trusted players to the MEMBER role by default.
 * ✅ Back button returns to correct parent menu.
 */
public class TrustListener implements Listener {

    private final PlotManager plots;
    private final ClaimRoleManager roles;
    private final GUIManager gui;

    public TrustListener(ProShield plugin, PlotManager plots, ClaimRoleManager roles, GUIManager gui) {
        this.plots = plots;
        this.roles = roles;
        this.gui = plugin.getGuiManager();
    }

    @EventHandler(ignoreCancelled = true)
    public void onTrustClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        if (e.getCurrentItem() == null) return;

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

        // Only owner or admin can trust
        if (!plot.isOwner(player.getUniqueId()) && !player.hasPermission("proshield.admin")) {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return;
        }

        UUID target = roles.getPendingTarget(player);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "⚠ No player selected to trust.");
            return;
        }

        // ✅ Assign default role
        roles.setRole(plot, target, ClaimRole.MEMBER);

        // ✅ Feedback
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.2f);
        player.sendMessage(ChatColor.GREEN + "✔ Trusted player as " + ClaimRole.MEMBER.getDisplayName());

        // ✅ Refresh GUI
        boolean fromAdmin = player.hasPermission("proshield.admin");
        gui.openTrustMenu(player, fromAdmin);
    }
}
