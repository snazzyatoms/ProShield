package com.snazzyatoms.proshield.gui.listeners;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.roles.ClaimRole;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.UUID;

/**
 * Handles clicks inside the Roles GUI.
 */
public class RolesListener implements Listener {

    private final ProShield plugin;
    private final ClaimRoleManager roles;
    private final PlotManager plotManager;

    public RolesListener(ProShield plugin, ClaimRoleManager roles, PlotManager plotManager) {
        this.plugin = plugin;
        this.roles = roles;
        this.plotManager = plotManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getCurrentItem() == null) return;

        String title = event.getView().getTitle();
        if (!title.contains("Roles")) return;

        event.setCancelled(true);

        // get the plot at the player's current location
        Plot plot = plotManager.getPlot(player.getLocation());
        if (plot == null) {
            player.sendMessage("§cYou are not inside a claim.");
            return;
        }

        UUID plotId = plot.getId();

        // determine target from the clicked skull, if any
        UUID targetId = null;
        ItemStack clicked = event.getCurrentItem();
        if (clicked.hasItemMeta() && clicked.getItemMeta() instanceof SkullMeta skullMeta) {
            OfflinePlayer target = skullMeta.getOwningPlayer();
            if (target != null) {
                targetId = target.getUniqueId();
            }
        }

        if (targetId == null) {
            player.sendMessage("§cCould not determine target player.");
            return;
        }

        // assign roles based on clicked slot
        int slot = event.getRawSlot();
        switch (slot) {
            case 11 -> {
                roles.assignRole(plotId, targetId, ClaimRole.MANAGER);
                player.sendMessage("§a" + Bukkit.getOfflinePlayer(targetId).getName() + " is now Manager in this claim!");
            }
            case 13 -> {
                roles.assignRole(plotId, targetId, ClaimRole.TRUSTED);
                player.sendMessage("§a" + Bukkit.getOfflinePlayer(targetId).getName() + " is now Trusted in this claim!");
            }
            case 15 -> {
                roles.clearRole(plotId, targetId);
                player.sendMessage("§cRole for " + Bukkit.getOfflinePlayer(targetId).getName() + " was cleared.");
            }
            default -> { }
        }
    }
}
