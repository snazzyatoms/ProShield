package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.util.MessagesUtil;
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
 * - Integrates with MessagesUtil
 */
public class KeepDropsListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final MessagesUtil messages;

    public KeepDropsListener(ProShield plugin, PlotManager plotManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
        this.messages = plugin.getMessagesUtil();
    }

    /**
     * Prevent dropped items from despawning inside claims.
     */
    @EventHandler(ignoreCancelled = true)
    public void onItemSpawn(ItemSpawnEvent event) {
        Item item = event.getEntity();
        Chunk chunk = item.getLocation().getChunk();

        Plot plot = plotManager.getPlot(chunk);
        FileConfiguration config = plugin.getConfig();

        boolean globalKeep = config.getBoolean("claims.keep-items.enabled", false);
        boolean perClaimKeep = plot != null && plot.getSettings().isKeepItemsEnabled();

        if (!globalKeep && !perClaimKeep) return;

        int despawnSeconds = config.getInt("claims.keep-items.despawn-seconds", 900);

        // Mark item as persistent
        item.setUnlimitedLifetime(true);
        item.setTicksLived(0);

        // Optional manual despawn
        if (despawnSeconds > 0) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (!item.isDead() && item.isValid()) {
                    item.remove();
                }
            }, despawnSeconds * 20L);
        }

        // Notify nearby players (debug/feedback)
        if (plot != null) {
            for (Player p : chunk.getWorld().getPlayers()) {
                if (p.getLocation().distance(item.getLocation()) < 8) {
                    messages.send(p, "keepdrops.item-persistent");
                }
            }
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

        // Respect vanilla keep rules but re-spawn items with persistence
        event.setKeepInventory(false);
        event.setKeepLevel(false);

        Iterator<org.bukkit.inventory.ItemStack> it = event.getDrops().iterator();
        while (it.hasNext()) {
            org.bukkit.inventory.ItemStack stack = it.next();
            if (stack == null) continue;

            Item drop = player.getWorld().dropItemNaturally(player.getLocation(), stack);
            drop.setUnlimitedLifetime(true);
            drop.setTicksLived(0);
        }

        event.getDrops().clear();

        // Send confirmation message
        messages.send(player, "keepdrops.death-items-persistent");
    }
}
