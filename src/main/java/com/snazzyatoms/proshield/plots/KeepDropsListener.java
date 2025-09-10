package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Chunk;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerDropItemEvent;

/**
 * Handles item drop & keep protection inside claims and wilderness.
 */
public class KeepDropsListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plotManager;

    public KeepDropsListener(ProShield plugin, PlotManager plotManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        Item item = event.getItemDrop();
        Chunk chunk = player.getLocation().getChunk();

        Plot plot = plotManager.getPlot(chunk);
        FileConfiguration config = plugin.getConfig();

        if (plot == null) {
            // Wilderness logic
            if (!config.getBoolean("protection.wilderness.allow-drops", true)) {
                event.setCancelled(true);
                player.sendMessage(plugin.getPrefix() + "§cYou cannot drop items in the wilderness.");
            }
            return;
        }

        // Inside claim logic
        boolean keepItems = config.getBoolean("claims.keep-items.enabled", false);
        if (keepItems) {
            int despawnSeconds = config.getInt("claims.keep-items.despawn-seconds", 900);
            item.setUnlimitedLifetime(true);
            item.setTicksLived(1); // reset life
            item.setPickupDelay(0);
            item.setCustomNameVisible(false);

            // Schedule forced removal after despawn time (safety)
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (!item.isDead() && item.isValid()) {
                    item.remove();
                }
            }, despawnSeconds * 20L);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemSpawn(ItemSpawnEvent event) {
        Item item = event.getEntity();
        Chunk chunk = item.getLocation().getChunk();

        Plot plot = plotManager.getPlot(chunk);
        FileConfiguration config = plugin.getConfig();

        if (plot == null) {
            // Wilderness logic
            if (!config.getBoolean("protection.wilderness.allow-drops", true)) {
                event.setCancelled(true);
            }
            return;
        }

        // Inside claim logic
        boolean keepItems = config.getBoolean("claims.keep-items.enabled", false);
        if (keepItems) {
            int despawnSeconds = config.getInt("claims.keep-items.despawn-seconds", 900);
            item.setUnlimitedLifetime(true);
            item.setTicksLived(1);
            item.setPickupDelay(0);

            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (!item.isDead() && item.isValid()) {
                    item.remove();
                }
            }, despawnSeconds * 20L);
        }
    }
}
