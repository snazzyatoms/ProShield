package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Chunk;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.ItemSpawnEvent;

/**
 * Handles item spawn/despawn protection inside claims.
 * - Prevents item despawn if enabled (globally or per-claim)
 * - Prevents unwanted drops in claims if configured
 * - Works alongside KeepDropsListener for death-drops
 */
public class ItemProtectionListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plotManager;

    public ItemProtectionListener(ProShield plugin, PlotManager plotManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
    }

    /**
     * Cancel despawn of items if keep-items is enabled (globally or per-claim).
     */
    @EventHandler(ignoreCancelled = true)
    public void onItemDespawn(ItemDespawnEvent event) {
        Item item = event.getEntity();
        Chunk chunk = item.getLocation().getChunk();

        if (plotManager.isKeepItemsEnabled(chunk)) {
            event.setCancelled(true);
            item.setTicksLived(0); // reset lifetime
        }
    }

    /**
     * Apply optional protection to item spawns (prevent unwanted spawns in claims).
     */
    @EventHandler(ignoreCancelled = true)
    public void onItemSpawn(ItemSpawnEvent event) {
        Item item = event.getEntity();
        Chunk chunk = item.getLocation().getChunk();

        Plot plot = plotManager.getPlot(chunk);
        FileConfiguration config = plugin.getConfig();

        // === Global keep-items check ===
        boolean globalKeep = config.getBoolean("claims.keep-items.enabled", false);

        // === Per-claim keep-items check ===
        boolean perClaimKeep = plot != null && plot.getSettings().isKeepItemsEnabled();

        // If neither enabled, allow normal item behavior
        if (!globalKeep && !perClaimKeep) return;

        // Reset despawn timer and make persistent
        int despawnSeconds = config.getInt("claims.keep-items.despawn-seconds", 900);
        item.setTicksLived(0);
        item.setUnlimitedLifetime(true);

        if (despawnSeconds > 0) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (!item.isDead() && item.isValid()) {
                    item.remove();
                }
            }, despawnSeconds * 20L);
        }
    }
}
