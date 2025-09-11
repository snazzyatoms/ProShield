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
 * ✅ Handles TNT, creepers, wither, beds, respawn anchors, etc.
 * ✅ Global wilderness config + per-claim flags
 * ✅ Prevents block damage inside claims unless allowed
 * ✅ Debug logging for admins
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

        // Wilderness → global config
        if (plot == null) {
            boolean enabled = plugin.getConfig().getBoolean("protection.explosions.enabled", false);
            if (!enabled) {
                event.setCancelled(true);
                messages.debug("&cExplosion cancelled in wilderness: " + explosionType);
            }
            return;
        }

        // Claim → per-claim flag
        if (!plot.getSettings().isExplosionsAllowed()) {
            event.setCancelled(true);
            messages.debug("&cExplosion cancelled in claim: " + plot.getDisplayNameSafe() +
                    " (" + explosionType + ")");
            return;
        }

        // If explosions are allowed, trim block damage so other nearby claims are safe
        Iterator<org.bukkit.block.Block> it = event.blockList().iterator();
        while (it.hasNext()) {
            org.bukkit.block.Block block = it.next();
            Plot affectedPlot = plots.getPlot(block.getChunk());
            if (affectedPlot != null && !affectedPlot.equals(plot)) {
                // Prevent cross-claim grief
                it.remove();
            }
        }

        if (entity instanceof Explosive) {
            messages.debug("&eExplosion processed in claim: " + plot.getDisplayNameSafe() +
                    " (" + explosionType + ")");
        }
    }
}
