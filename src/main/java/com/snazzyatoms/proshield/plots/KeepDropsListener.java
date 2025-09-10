package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Chunk;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Handles "keep items in claims" feature:
 * - Prevents despawn inside claims if enabled
 * - Applies custom despawn delay from config
 * - Fully integrated with PlotManager and role/bypass checks
 */
@SuppressWarnings("deprecation") // For PlayerPickupItemEvent (1.12-1.18 compatibility)
public class KeepDropsListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final FileConfiguration config;

    public KeepDropsListener(ProShield plugin, PlotManager plotManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
        this.config = plugin.getConfig();
    }

    /**
     * Handles despawn prevention inside claims.
     */
    @EventHandler(ignoreCancelled = true)
    public void onItemDespawn(ItemDespawnEvent event) {
        if (!config.getBoolean("claims.keep-items.enabled", false)) return;

        Chunk chunk = event.getLocation().getChunk();
        Plot plot = plotManager.getPlot(chunk);

        if (plot != null) {
            int customDelay = config.getInt("claims.keep-items.despawn-seconds", 900);
            if (customDelay < 300) customDelay = 300;
            if (customDelay > 900) customDelay = 900;

            event.setCancelled(true); // prevent natural despawn

            // Schedule custom despawn
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!event.getEntity().isDead() && event.getEntity().isValid()) {
                        event.getEntity().remove();
                    }
                }
            }.runTaskLater(plugin, customDelay * 20L);
        }
    }

    /**
     * Prevent untrusted players from picking up items inside claims.
     */
    @EventHandler(ignoreCancelled = true)
    public void onPickup(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        Chunk chunk = event.getItem().getLocation().getChunk();
        Plot plot = plotManager.getPlot(chunk);

        if (plot != null && !plotManager.hasAccess(plot, player, "container")) {
            if (!player.hasPermission("proshield.bypass")) {
                event.setCancelled(true);
                player.sendMessage(plugin.getPrefix() + "§cYou cannot pick up items in this claim.");
            }
        }
    }

    /**
     * Prevent untrusted players from dropping items inside claims.
     */
    @EventHandler(ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        Item item = event.getItemDrop();
        Chunk chunk = item.getLocation().getChunk();
        Plot plot = plotManager.getPlot(chunk);

        if (plot != null && !plotManager.hasAccess(plot, player, "container")) {
            if (!player.hasPermission("proshield.bypass")) {
                event.setCancelled(true);
                player.sendMessage(plugin.getPrefix() + "§cYou cannot drop items in this claim.");
            }
        }
    }
}
