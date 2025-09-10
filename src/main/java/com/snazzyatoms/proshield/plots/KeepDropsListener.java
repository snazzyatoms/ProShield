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
import org.bukkit.inventory.ItemStack;

import java.util.Iterator;

/**
 * Handles persistent item drops inside claims.
 * - Prevents despawn of items when enabled globally or per-claim
 * - Optional configurable despawn delay
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

        // Apply despawn settings
        int despawnSeconds = config.getInt("claims.keep-items.despawn-seconds", 900);
        item.setUnlimitedLifetime(true);
        item.setTicksLived(0);

        messages.debug(plugin, "&aKeepDrops applied to item spawn in "
                + (plot != null ? "claim: " + plot.getName() : "wilderness"));

        // Schedule manual despawn if configured
        if (despawnSeconds > 0) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (!item.isDead() && item.isValid()) {
                    item.remove();
                    messages.debug(plugin, "&cForced item despawn after " + despawnSeconds + "s");
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

        FileConfiguration config = plugin.getConfig();
        boolean globalKeep = config.getBoolean("claims.keep-items.enabled", false);
        boolean perClaimKeep = plot != null && plot.getSettings().isKeepItemsEnabled();

        if (!globalKeep && !perClaimKeep) return;

        // Preserve XP rules, but override item handling
        event.setKeepInventory(false);
        event.setKeepLevel(false);

        Iterator<ItemStack> it = event.getDrops().iterator();
        while (it.hasNext()) {
            ItemStack stack = it.next();
            if (stack == null) continue;

            Item drop = player.getWorld().dropItemNaturally(player.getLocation(), stack);
            drop.setUnlimitedLifetime(true);
            drop.setTicksLived(0);
        }

        // Clear default drops to avoid duplicates
        event.getDrops().clear();

        messages.debug(plugin, "&aKeepDrops applied to death items for " + player.getName()
                + (plot != null ? " in claim: " + plot.getName() : " (wilderness)"));
    }
}
