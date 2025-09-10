package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.roles.ClaimRole;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import org.bukkit.Chunk;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

/**
 * Handles item dropping & picking up inside claims and wilderness
 * with role-based checks and configurable wilderness toggles.
 */
@SuppressWarnings("deprecation") // PlayerPickupItemEvent is deprecated but works up to 1.19
public class ItemProtectionListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final ClaimRoleManager roleManager;

    // Cached wilderness config values
    private boolean wildernessDropAllowed;
    private boolean wildernessPickupAllowed;

    public ItemProtectionListener(ProShield plugin, PlotManager plotManager, ClaimRoleManager roleManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
        this.roleManager = roleManager;
        reloadConfigValues();
    }

    /**
     * Reloads wilderness config values from config.yml
     */
    public void reloadConfigValues() {
        FileConfiguration config = plugin.getConfig();
        this.wildernessDropAllowed = config.getBoolean("protection.wilderness.allow-item-drop", true);
        this.wildernessPickupAllowed = config.getBoolean("protection.wilderness.allow-item-pickup", true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        Chunk chunk = player.getLocation().getChunk();

        Plot plot = plotManager.getPlot(chunk);

        if (plot == null) {
            // Wilderness
            if (!wildernessDropAllowed && !player.hasPermission("proshield.bypass")) {
                event.setCancelled(true);
                player.sendMessage(plugin.getPrefix() + "§cYou cannot drop items in the wilderness.");
            }
            return;
        }

        // Inside claim
        ClaimRole role = roleManager.getRole(plot, player);
        if (!roleManager.canDropItems(role)) {
            event.setCancelled(true);
            player.sendMessage(plugin.getPrefix() + "§cYou cannot drop items in this claim.");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemPickup(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        Chunk chunk = player.getLocation().getChunk();

        Plot plot = plotManager.getPlot(chunk);

        if (plot == null) {
            // Wilderness
            if (!wildernessPickupAllowed && !player.hasPermission("proshield.bypass")) {
                event.setCancelled(true);
                player.sendMessage(plugin.getPrefix() + "§cYou cannot pick up items in the wilderness.");
            }
            return;
        }

        // Inside claim
        ClaimRole role = roleManager.getRole(plot, player);
        if (!roleManager.canPickupItems(role)) {
            event.setCancelled(true);
            player.sendMessage(plugin.getPrefix() + "§cYou cannot pick up items in this claim.");
        }
    }
}
