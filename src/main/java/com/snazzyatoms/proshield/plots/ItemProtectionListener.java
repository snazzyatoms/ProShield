package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import com.snazzyatoms.proshield.roles.ClaimRole;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;

/**
 * Handles item interactions inside claims:
 * - Pickup
 * - Drop
 * - Interact (containers/items)
 */
@SuppressWarnings("deprecation") // For legacy pickup event in older APIs
public class ItemProtectionListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final ClaimRoleManager roleManager;

    public ItemProtectionListener(ProShield plugin, PlotManager plotManager, ClaimRoleManager roleManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
        this.roleManager = roleManager;
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemPickup(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        Chunk chunk = event.getItem().getLocation().getChunk();

        Plot plot = plotManager.getPlot(chunk);
        if (plot == null) return;

        ClaimRole role = roleManager.getRole(plot, player);

        if (!roleManager.canPickupItems(role)) {
            event.setCancelled(true);
            player.sendMessage(plugin.getPrefix() + "§cYou cannot pick up items here.");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        Chunk chunk = event.getItemDrop().getLocation().getChunk();

        Plot plot = plotManager.getPlot(chunk);
        if (plot == null) return;

        ClaimRole role = roleManager.getRole(plot, player);

        if (!roleManager.canDropItems(role)) {
            event.setCancelled(true);
            player.sendMessage(plugin.getPrefix() + "§cYou cannot drop items here.");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!event.hasBlock()) return;

        Chunk chunk = event.getClickedBlock().getChunk();
        Plot plot = plotManager.getPlot(chunk);
        if (plot == null) return;

        ClaimRole role = roleManager.getRole(plot, player);

        if (!roleManager.canInteract(role)) {
            event.setCancelled(true);
            player.sendMessage(plugin.getPrefix() + "§cYou cannot interact with blocks here.");
        }
    }
}
