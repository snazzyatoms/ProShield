package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.roles.ClaimRole;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import org.bukkit.Chunk;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

/**
 * Handles rules for item drops & pickups inside claims.
 * - Complements ItemProtectionListener (which handles despawns)
 * - Enforces role-based permissions on dropping/picking up items
 * - Prevents grief (e.g., visitors littering or stealing)
 */
@SuppressWarnings("deprecation") // PlayerPickupItemEvent is still functional for supported versions
public class KeepDropsListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final ClaimRoleManager roleManager;

    public KeepDropsListener(ProShield plugin, PlotManager plotManager, ClaimRoleManager roleManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
        this.roleManager = roleManager;
    }

    /**
     * Prevent certain roles from dropping items in claims.
     */
    @EventHandler(ignoreCancelled = true)
    public void onItemDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        Item item = event.getItemDrop();
        Chunk chunk = player.getLocation().getChunk();

        Plot plot = plotManager.getPlot(chunk);
        if (plot == null) return; // wilderness → allow drops

        ClaimRole role = roleManager.getRole(plot, player);

        // Example restriction: Visitors cannot drop items inside claims
        if (role == ClaimRole.VISITOR) {
            event.setCancelled(true);
            item.remove();
            player.sendMessage(plugin.getPrefix() + "§cYou cannot drop items inside this claim.");
        }
    }

    /**
     * Prevent certain roles from picking up items in claims.
     */
    @EventHandler(ignoreCancelled = true)
    public void onItemPickup(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        Item item = event.getItem();
        Chunk chunk = item.getLocation().getChunk();

        Plot plot = plotManager.getPlot(chunk);
        if (plot == null) return; // wilderness → allow pickup

        ClaimRole role = roleManager.getRole(plot, player);

        // Example restriction: Visitors cannot pick up items inside claims
        if (role == ClaimRole.VISITOR) {
            event.setCancelled(true);
            player.sendMessage(plugin.getPrefix() + "§cYou cannot pick up items inside this claim.");
        }
    }
}
