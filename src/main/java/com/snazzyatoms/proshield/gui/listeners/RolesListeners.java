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

/**
 * RolesListener
 *
 * ✅ Handles clicks in the Roles GUI.
 * ✅ Prevents moving items.
 * ✅ Supports back button (returns to correct parent menu).
 * ⚠ Role assignment via GUI postponed until v2.0 (pending target system).
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

        // ✅ Handle back button
        if (name.equals("back")) {
            boolean fromAdmin = player.hasPermission("proshield.admin");
            gui.openMain(player);
            if (fromAdmin) gui.openAdminMain(player);
            return;
        }

        // ⚠ Postponed role assignment until v2.0
        player.sendMessage(ChatColor.YELLOW + "ℹ Role assignment via GUI is coming in v2.0. Use /trust <player> [role] for now.");
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 0.5f);
    }
}
