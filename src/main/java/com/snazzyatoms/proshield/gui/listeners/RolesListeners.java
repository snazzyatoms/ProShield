// src/main/java/com/snazzyatoms/proshield/gui/listeners/RolesListener.java
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

import java.util.List;
import java.util.UUID;

public class RolesListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plots;
    private final ClaimRoleManager roleManager;

    private final List<ClaimRole> roleCycle = List.of(
            ClaimRole.VISITOR,
            ClaimRole.MEMBER,
            ClaimRole.TRUSTED,
            ClaimRole.BUILDER,
            ClaimRole.CONTAINER,
            ClaimRole.MODERATOR,
            ClaimRole.MANAGER
    );

    public RolesListener(ProShield plugin, PlotManager plots, ClaimRoleManager roleManager) {
        this.plugin = plugin;
        this.plots = plots;
        this.roleManager = roleManager;
    }

    @EventHandler(ignoreCancelled = true)
    public void onRolesMenuClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        if (e.getCurrentItem() == null) return;

        e.setCancelled(true); // prevent moving items
        ItemStack item = e.getCurrentItem();

        if (!(item.getItemMeta() instanceof SkullMeta skull)) return;
        if (!skull.hasOwner()) return;

        String targetName = skull.getOwnerProfile().getName();
        if (targetName == null) return;

        Plot plot = plots.getPlot(player.getLocation());
        if (plot == null) return;

        UUID targetUUID = plugin.getServer().getOfflinePlayer(targetName).getUniqueId();
        ClaimRole current = roleManager.getRole(plot, targetUUID);
        int idx = roleCycle.indexOf(current);
        ClaimRole nextRole = roleCycle.get((idx + 1) % roleCycle.size());

        roleManager.setRole(plot, targetUUID, nextRole);
        plot.saveAsync();

        player.sendMessage(ChatColor.YELLOW + "Updated " + targetName + " → " + nextRole.name());
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.2f);
    }
}
