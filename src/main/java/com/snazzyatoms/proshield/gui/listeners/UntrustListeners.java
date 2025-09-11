// src/main/java/com/snazzyatoms/proshield/gui/listeners/UntrustListener.java
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.UUID;

public class UntrustListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plots;
    private final ClaimRoleManager roleManager;

    public UntrustListener(ProShield plugin, PlotManager plots, ClaimRoleManager roleManager) {
        this.plugin = plugin;
        this.plots = plots;
        this.roleManager = roleManager;
    }

    @EventHandler(ignoreCancelled = true)
    public void onUntrustMenuClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        if (e.getCurrentItem() == null) return;

        e.setCancelled(true);
        ItemStack item = e.getCurrentItem();

        if (!(item.getItemMeta() instanceof SkullMeta skull)) return;
        if (!skull.hasOwner()) return;

        String targetName = skull.getOwnerProfile().getName();
        if (targetName == null) return;

        Plot plot = plots.getPlot(player.getLocation());
        if (plot == null) return;

        UUID targetUUID = plugin.getServer().getOfflinePlayer(targetName).getUniqueId();

        roleManager.setRole(plot, targetUUID, ClaimRole.VISITOR); // reset to Visitor
        plot.saveAsync();

        player.sendMessage(ChatColor.RED + "Untrusted " + targetName + " from your claim.");
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
    }
}
