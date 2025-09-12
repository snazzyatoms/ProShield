package com.snazzyatoms.proshield.gui.listeners;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class RolesListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plots;
    private final ClaimRoleManager roles;
    private final GUIManager gui;

    public RolesListener(ProShield plugin, PlotManager plots, ClaimRoleManager roles, GUIManager gui) {
        this.plugin = plugin;
        this.plots = plots;
        this.roles = roles;
        this.gui = gui;
    }

    @EventHandler(ignoreCancelled = true)
    public void onRolesClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        if (e.getClickedInventory() == null || e.getClickedInventory() != e.getView().getTopInventory()) return;
        if (e.getCurrentItem() == null || !e.getCurrentItem().hasItemMeta()) return;

        String title = ChatColor.stripColor(e.getView().getTitle()).toLowerCase();
        String label = ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName());
        Plot plot = plots.getPlot(player.getLocation());
        if (plot == null) return;

        e.setCancelled(true);

        if (label.equalsIgnoreCase("Back")) {
            if (title.startsWith("roles for")) {
                gui.openRolesGUI(player, plot, player.hasPermission("proshield.admin"));
            } else {
                if (player.hasPermission("proshield.admin")) gui.openAdminMain(player); else gui.openMain(player);
            }
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            return;
        }

        if (title.contains("manage roles")) {
            String targetName = label;
            gui.openRoleAssignmentMenu(player, plot, targetName, player.hasPermission("proshield.admin"));
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.1f);
            return;
        }

        if (title.startsWith("roles for ")) {
            String targetName = title.replace("roles for ", "").trim();
            OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

            switch (label.toLowerCase()) {
                case "builder" -> roles.assignRole(plot.getId(), target.getUniqueId(), "builder");
                case "moderator" -> roles.assignRole(plot.getId(), target.getUniqueId(), "moderator");
                case "clear role" -> roles.clearRole(plot.getId(), target.getUniqueId());
                case "role flags" -> {
                    String currentRole = roles.getRole(plot.getId(), target.getUniqueId());
                    if (currentRole == null || currentRole.isEmpty()) currentRole = "trusted";
                    gui.openRoleFlagsMenu(player, plot, currentRole);
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.05f);
                    return;
                }
            }

            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.2f);
            gui.openRoleAssignmentMenu(player, plot, targetName, player.hasPermission("proshield.admin"));
        }
    }
}
