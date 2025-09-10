package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Chunk;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.Iterator;

/**
 * Handles persistent item drops inside claims.
 * - Prevents despawn of items when enabled globally or per-claim
 * - Optional configurable despawn delay
 * - Now extended with PlotManager helper methods for consistency
 */
public class KeepDropsListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plotManager;

    public KeepDropsListener(ProShield plugin, PlotManager plotManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
    }

    /**
     * Prevent dropped items from despawning inside claims.
     */
    @EventHandler(ignoreCancelled = true)
    public void onItemSpawn(ItemSpawnEvent event) {
        Item item = event.getEntity();
        Chunk chunk = item.getLocation().getChunk();

        // Global + per-claim merged check
        if (!plotManager.isKeepItemsEnabled(chunk)) return;

        // Apply despawn settings
        FileConfiguration config = plugin.getConfig();
        int despawnSeconds = config.getInt("claims.keep-items.despawn-seconds", 900);

        item.setUnlimitedLifetime(true);
        item.setTicksLived(0);
        item.setCustomName(null);
        item.setCustomNameVisible(false);

        // Schedule manual despawn (if configured)
        if (despawnSeconds > 0) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (!item.isDead() && item.isValid()) {
                    item.remove();
                }
            }, despawnSeconds * 20L);
        }
    }

    /**
     * Handles death drops inside claims (keeps items if enabled).
     */
    @EventHandler(ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Chunk chunk = player.getLocation().getChunk();

        // Global + per-claim merged check
        if (!plotManager.isKeepItemsEnabled(chunk)) return;

        // Prevent default keepInventory from interfering
        event.setKeepInventory(false);
        event.setKeepLevel(false);

        // Respawn items with persistence
        Iterator<org.bukkit.inventory.ItemStack> it = event.getDrops().iterator();
        while (it.hasNext()) {
            org.bukkit.inventory.ItemStack stack = it.next();
            if (stack == null) continue;

            Item drop = player.getWorld().dropItemNaturally(player.getLocation(), stack);
            drop.setUnlimitedLifetime(true);
            drop.setTicksLived(0);
        }

        // Clear default drops since we respawned them
        event.getDrops().clear();

        player.sendMessage(plugin.getPrefix() + "§aYour items were preserved inside this claim.");
    }
}
