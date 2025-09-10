// path: src/main/java/com/snazzyatoms/proshield/plots/KeepDropsListener.java
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
 * - Preserves original behavior, extended for per-claim + global rules
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

        Plot plot = plotManager.getPlot(chunk);
        boolean globalKeep = plugin.getConfig().getBoolean("claims.keep-items.enabled", false);

        if (plot == null && !globalKeep) return; // no claim, no global keep

        boolean perClaimKeep = plot != null && plot.getSettings().isKeepItemsEnabled();
        if (!globalKeep && !perClaimKeep) return;

        // Apply despawn settings
        FileConfiguration config = plugin.getConfig();
        int despawnSeconds = config.getInt("claims.keep-items.despawn-seconds", 900);

        item.setUnlimitedLifetime(true);
        item.setTicksLived(0);
        item.setCustomName(null);
        item.setCustomNameVisible(false);

        // Manual despawn scheduling
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
        Plot plot = plotManager.getPlot(chunk);

        boolean globalKeep = plugin.getConfig().getBoolean("claims.keep-items.enabled", false);
        boolean perClaimKeep = plot != null && plot.getSettings().isKeepItemsEnabled();

        if (!globalKeep && !perClaimKeep) return;

        // Respect Minecraft defaults but respawn items as persistent
        event.setKeepInventory(false);
        event.setKeepLevel(false);

        Iterator<org.bukkit.inventory.ItemStack> it = event.getDrops().iterator();
        while (it.hasNext()) {
            org.bukkit.inventory.ItemStack stack = it.next();
            if (stack == null) continue;

            Item drop = player.getWorld().dropItemNaturally(player.getLocation(), stack);
            drop.setUnlimitedLifetime(true);
            drop.setTicksLived(0);
            drop.setCustomName(null);
            drop.setCustomNameVisible(false);
        }

        // Clear original drops (we re-spawned them as persistent)
        event.getDrops().clear();
    }
}
