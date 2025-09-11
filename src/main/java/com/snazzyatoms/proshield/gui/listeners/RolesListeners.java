// src/main/java/com/snazzyatoms/proshield/gui/listeners/RolesListener.java
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.UUID;

/**
 * RolesListener
 *
 * ✅ Handles clicks in the Roles GUI.
 * ✅ Assigns ClaimRole from enum (Visitor → Manager).
 * ✅ Prevents moving items.
 * ✅ Feedback via sounds + updated GUI refresh.
 */
public class RolesListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final ClaimRoleManager roleManager;
    private final GUIManager gui;

    public RolesListener(ProShield plugin, PlotManager plotManager, ClaimRoleManager roleManager, GUIManager gui) {
        this.plugin = plugin;
        this.plotManager = plotManager;
        this.roleManager = roleManager;
        this.gui = gui;
    }

    @EventHandler(ignoreCancelled = true)
    public void onRolesClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        if (e.getCurrentItem() == null) return;

        // ✅ Prevent item movement
        e.setCancelled(true);

        ItemStack item = e.getCurrentItem();
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;

        String name = ChatColor.stripColor(meta.getDisplayName()).toLowerCase();
        Plot plot = plotManager.getPlot(player.getLocation());
        if (plot == null) return;

        // Only owner or admin can assign roles
        if (!plot.isOwner(player.getUniqueId()) && !player.hasPermission("proshield.admin")) {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return;
        }

        UUID target = roleManager.getPendingTarget(player);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "⚠ No player selected for role assignment.");
            return;
        }

        ClaimRole chosen = null;
        for (ClaimRole role : ClaimRole.values()) {
            if (name.contains(role.name().toLowerCase())) {
                chosen = role;
                break;
            }
        }

        if (chosen == null || chosen == ClaimRole.OWNER) return; // skip invalid or owner

        // ✅ Assign role
        roleManager.setRole(plot, target, chosen);

        // ✅ Feedback
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.2f);
        player.sendMessage(ChatColor.GREEN + "✔ Assigned " +
                ChatColor.AQUA + chosen.getDisplayName() +
                ChatColor.GREEN + " to player.");

        // ✅ Refresh GUI
        boolean fromAdmin = player.hasPermission("proshield.admin");
        gui.openRolesGUI(player, plot, fromAdmin);
    }
}
