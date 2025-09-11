// src/main/java/com/snazzyatoms/proshield/gui/listeners/RolesListener.java
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class RolesListener implements Listener {

    private final PlotManager plots;
    private final ClaimRoleManager roles;
    private final GUIManager gui;

    public RolesListener(ProShield plugin, PlotManager plotManager, ClaimRoleManager roleManager, GUIManager gui) {
        this.plots = plotManager;
        this.roles = roleManager;
        this.gui = gui;
    }

    @EventHandler(ignoreCancelled = true)
    public void onRolesClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        if (e.getClickedInventory() == null || e.getClickedInventory() != e.getView().getTopInventory()) return;
        if (e.getCurrentItem() == null) return;

        String title = ChatColor.stripColor(e.getView().getTitle()).toLowerCase();
        if (!title.contains("role")) return;

        e.setCancelled(true);

        ItemStack item = e.getCurrentItem();
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;

        String name = ChatColor.stripColor(meta.getDisplayName()).toLowerCase();
        Plot plot = plots.getPlot(player.getLocation());
        if (plot == null) return;

        if (name.equals("back")) {
            if (player.hasPermission("proshield.admin")) gui.openAdminMain(player);
            else gui.openMain(player);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            return;
        }

        player.sendMessage(ChatColor.YELLOW + "ℹ Role assignment via GUI is coming in v2.0. Use /trust <player> [role] for now.");
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 0.5f);
    }
}
