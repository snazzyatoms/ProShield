// src/main/java/com/snazzyatoms/proshield/gui/listeners/RolesListener.java
package com.snazzyatoms.proshield.gui.listeners;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

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
        if (e.getCurrentItem() == null || !e.getCurrentItem().hasItemMeta()) return;

        String title = ChatColor.stripColor(e.getView().getTitle()).toLowerCase();
        if (!title.contains("assign role")) return;

        e.setCancelled(true);
        String label = ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName());

        if (label.equalsIgnoreCase("Back")) {
            if (player.hasPermission("proshield.admin")) gui.openAdminMain(player); else gui.openMain(player);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            return;
        }

        String target = gui.getRememberedTarget(player);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Select a player in the Trust Menu first.");
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 0.7f);
            return;
        }

        // Map GUI labels to role ids used by your command
        String roleId;
        if (label.equalsIgnoreCase("Builder"))      roleId = "builder";
        else if (label.equalsIgnoreCase("Moderator")) roleId = "moderator";
        else if (label.equalsIgnoreCase("Clear Role")) roleId = ""; // clears to default
        else return;

        if (roleId.isEmpty()) {
            // clear role by re-trusting without a role (or you could add a dedicated /roles clear if you have one)
            gui.runPlayerCommand(player, "/trust " + target);
            player.sendMessage(ChatColor.YELLOW + "Cleared role for " + ChatColor.WHITE + target + ChatColor.YELLOW + ".");
        } else {
            gui.runPlayerCommand(player, "/trust " + target + " " + roleId);
            player.sendMessage(ChatColor.GREEN + "Set role " + ChatColor.WHITE + roleId + ChatColor.GREEN + " for " + ChatColor.WHITE + target + ChatColor.GREEN + ".");
        }

        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.2f);
        // Stay in roles menu (so user can continue adjusting), or go back:
        gui.openRolesGUI(player, plots.getPlot(player.getLocation()), player.hasPermission("proshield.admin"));
    }
}
