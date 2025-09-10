package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.roles.ClaimRole;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import org.bukkit.Chunk;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.ItemDespawnEvent;

/**
 * Handles item drops inside claims & wilderness.
 * - Claim owners can choose whether items despawn inside claims.
 * - Wilderness follows config rules for item drops.
 */
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
     * Prevent item drops if wilderness is restricted.
     */
    @EventHandler(ignoreCancelled = true)
    public void onPlayerDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("proshield.bypass")) return;

        Item item = event.getItemDrop();
        Chunk chunk = item.getLocation().getChunk();
        Plot plot = plotManager.getPlot(chunk);
        FileConfiguration config = plugin.getConfig();

        // === Wilderness ===
        if (plot == null) {
            if (!config.getBoolean("protection.wilderness.allow-drops", true)) {
                event.setCancelled(true);
                player.sendMessage(plugin.getPrefix() + "§cYou cannot drop items in the wilderness.");
            }
            return;
        }

        // === Inside claims ===
        ClaimRole role = roleManager.getRole(plot, player);
        if (!roleManager.canContainer(role)) {
            event.setCancelled(true);
            player.sendMessage(plugin.getPrefix() + "§cYou cannot drop items in this claim.");
        }
    }

    /**
     * Prevent natural item spawns (e.g., mob drops) if disabled.
     */
    @EventHandler(ignoreCancelled = true)
    public void onItemSpawn(ItemSpawnEvent event) {
        Chunk chunk = event.getLocation().getChunk();
        Plot plot = plotManager.getPlot(chunk);
        FileConfiguration config = plugin.getConfig();

        if (plot == null) {
            if (!config.getBoolean("protection.wilderness.allow-drops", true)) {
                event.setCancelled(true);
            }
        }
    }

    /**
     * Handle claim keep-items rule.
     */
    @EventHandler(ignoreCancelled = true)
    public void onItemDespawn(ItemDespawnEvent event) {
        Chunk chunk = event.getLocation().getChunk();
        Plot plot = plotManager.getPlot(chunk);

        if (plot == null) return; // wilderness uses global despawn rules

        FileConfiguration config = plugin.getConfig();
        if (config.getBoolean("claims.keep-items.enabled", false)) {
            event.setCancelled(true); // item stays until manually removed
        }
    }
}
