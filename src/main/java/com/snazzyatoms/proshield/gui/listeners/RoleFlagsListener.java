package com.snazzyatoms.proshield.gui.listeners;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import com.snazzyatoms.proshield.roles.RolePermissions;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class RoleFlagsListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plots;
    private final ClaimRoleManager roles;
    private final GUIManager gui;

    public RoleFlagsListener(ProShield plugin, PlotManager plots, ClaimRoleManager roles, GUIManager gui) {
        this.plugin = plugin;
        this.plots = plots;
        this.roles = roles;
        this.gui = gui;
    }

    @EventHandler(ignoreCancelled = true)
    public void onRoleFlagsClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        if (e.getClickedInventory() == null || e.getClickedInventory() != e.getView().getTopInventory()) return;
        if (e.getCurrentItem() == null || !e.getCurrentItem().hasItemMeta()) return;

        String title = ChatColor.stripColor(e.getView().getTitle()).toLowerCase();
        if (!title.startsWith("role flags: ")) return;

        e.setCancelled(true);
        Plot plot = plots.getPlot(player.getLocation());
        if (plot == null) return;

        String roleId = title.replace("role flags: ", "").trim();
        RolePermissions perms = roles.getRolePermissions(plot.getId(), roleId);

        String label = ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName()).toLowerCase();
        switch (label) {
            case "build blocks" -> perms.setCanBuild(!perms.canBuild());
            case "open containers" -> perms.setCanContainers(!perms.canContainers());
            case "manage trust" -> perms.setCanManageTrust(!perms.canManageTrust());
            case "unclaim land" -> perms.setCanUnclaim(!perms.canUnclaim());
            case "back" -> {
                gui.openRolesGUI(player, plot, player.hasPermission("proshield.admin"));
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                return;
            }
            default -> { return; }
        }

        roles.savePermissions(plot.getId(), roleId, perms);

        ItemMeta meta = e.getCurrentItem().getItemMeta();
        if (meta != null) {
            boolean on = switch (label) {
                case "build blocks" -> perms.canBuild();
                case "open containers" -> perms.canContainers();
                case "manage trust" -> perms.canManageTrust();
                case "unclaim land" -> perms.canUnclaim();
                default -> false;
            };
            meta.setLore(Arrays.asList(
                ChatColor.GRAY + "Toggle permission",
                on ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled"
            ));
            e.getCurrentItem().setItemMeta(meta);
        }

        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.2f);
    }
}
