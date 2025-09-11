package com.snazzyatoms.proshield.plots;

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
 * - Uses MessagesUtil for debug feedback
 */
public class ExplosionProtectionListener implements Listener {

    private final PlotManager plotManager;
    private final MessagesUtil messages;

    public ExplosionProtectionListener(PlotManager plotManager, MessagesUtil messages) {
        this.plotManager = plotManager;
        this.messages = messages;
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        Entity entity = event.getEntity();
        Chunk chunk = entity.getLocation().getChunk();
        Plot plot = plotManager.getPlot(chunk);

        String explosionType = entity.getType().name();

        // Wilderness explosions → global toggle
        if (plot == null) {
            // use global default wilderness protection
            boolean allowExplosions = plotManager.getPlugin().getConfig()
                    .getBoolean("protection.explosions.wilderness", true);
            if (!allowExplosions) {
                event.setCancelled(true);
                messages.debug("&cExplosion cancelled in wilderness: " + explosionType);
            }
            return;
        }

        // Inside claim → per-claim flag
        if (!plot.getSettings().isExplosionsAllowed()) {
            event.setCancelled(true);
            messages.debug("&cExplosion cancelled in claim: " + explosionType + " @ " + plot.getDisplayNameSafe());
            return;
        }

        // If explosions allowed, filter affected blocks (prevent griefing across claims)
        Iterator<org.bukkit.block.Block> it = event.blockList().iterator();
        while (it.hasNext()) {
            org.bukkit.block.Block block = it.next();
            if (plotManager.getPlot(block.getChunk()) != null) {
                it.remove();
            }
        }

        if (entity instanceof Explosive) {
            messages.debug("&eExplosion processed: " + explosionType + " in " + plot.getDisplayNameSafe());
        }
    }
}
