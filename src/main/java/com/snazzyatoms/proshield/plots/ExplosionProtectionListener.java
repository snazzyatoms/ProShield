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
 * ExplosionProtectionListener
 *
 * ✅ Cancels or filters explosions in wilderness + claims
 * ✅ Uses both global config + per-claim flags
 * ✅ Prevents grief from TNT, creepers, wither, etc.
 * ✅ Debug messages included
 */
public class ExplosionProtectionListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plots;
    private final MessagesUtil messages;

    public ExplosionProtectionListener(ProShield plugin, PlotManager plots, MessagesUtil messages) {
        this.plugin = plugin;
        this.plots = plots;
        this.messages = messages;
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        Entity entity = event.getEntity();
        Chunk chunk = entity.getLocation().getChunk();
        Plot plot = plots.getPlot(chunk);

        String explosionType = entity.getType().name();

        /* -------------------------------------------------------
         * Wilderness explosions (no claim found)
         * ------------------------------------------------------- */
        if (plot == null) {
            boolean allowExplosions = plugin.getConfig().getBoolean("protection.explosions.enabled", false);
            if (!allowExplosions) {
                event.setCancelled(true);
                messages.debug("&cExplosion blocked in wilderness: " + explosionType);
            }
            return;
        }

        /* -------------------------------------------------------
         * Claimed land explosions
         * ------------------------------------------------------- */
        if (!plot.getSettings().isExplosionsAllowed()) {
            event.setCancelled(true);
            messages.debug("&cExplosion blocked in claim: " + explosionType + " @ " + plot.getDisplayNameSafe());
            return;
        }

        /* -------------------------------------------------------
         * If explosions are allowed, filter blocks to avoid grief
         * ------------------------------------------------------- */
        Iterator<org.bukkit.block.Block> it = event.blockList().iterator();
        while (it.hasNext()) {
            org.bukkit.block.Block block = it.next();
            Plot blockPlot = plots.getPlot(block.getChunk());
            if (blockPlot != null && !blockPlot.getSettings().isExplosionsAllowed()) {
                it.remove(); // block won’t be destroyed
            }
        }

        if (entity instanceof Explosive) {
            messages.debug("&eExplosion processed: " + explosionType + " in claim: " + plot.getDisplayNameSafe());
        }
    }
}
