package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Chunk;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Explosive;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.Iterator;

/**
 * Handles explosion protection inside claims and wilderness.
 * - Respects global config and per-claim settings
 * - Uses MessagesUtil for player/admin debug feedback
 */
public class ExplosionProtectionListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final MessagesUtil messages;

    public ExplosionProtectionListener(ProShield plugin, PlotManager plotManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
        this.messages = plugin.getMessagesUtil();
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        Entity entity = event.getEntity();
        Chunk chunk = entity.getLocation().getChunk();
        Plot plot = plotManager.getPlot(chunk);

        String explosionType = entity.getType().name();

        // Wilderness explosions → global toggle
        if (plot == null) {
            boolean allowExplosions = plugin.getConfig().getBoolean("protection.explosions.enabled", true);
            if (!allowExplosions) {
                event.setCancelled(true);
                messages.debug(plugin, "&cExplosion cancelled in wilderness: " + explosionType);
            }
            return;
        }

        // Inside claim → per-claim flags
        if (!plot.getSettings().isExplosionsAllowed()) {
            event.setCancelled(true);
            messages.debug(plugin, "&cExplosion cancelled in claim: " + explosionType + " @ " + plot.getName());
            return;
        }

        // If explosions allowed, filter affected blocks
        Iterator<org.bukkit.block.Block> it = event.blockList().iterator();
        while (it.hasNext()) {
            org.bukkit.block.Block block = it.next();

            // Prevent claim grief
            if (plotManager.getPlot(block.getChunk()) != null) {
                it.remove();
            }
        }

        if (entity instanceof Explosive) {
            messages.debug(plugin, "&eExplosion processed: " + explosionType + " in " + plot.getName());
        }
    }
}
