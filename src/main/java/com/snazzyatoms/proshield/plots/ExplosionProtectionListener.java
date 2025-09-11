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
 * ✅ Handles explosions in claims & wilderness.
 * ✅ Global config toggle for wilderness explosions.
 * ✅ Per-claim flag: isExplosionsAllowed().
 * ✅ Filters block destruction to prevent grief even if explosion occurs nearby.
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

        String type = entity.getType().name();

        // Wilderness explosions → global toggle
        if (plot == null) {
            boolean allowExplosions = plugin.getConfig().getBoolean("protection.explosions.enabled", false);
            if (!allowExplosions) {
                event.setCancelled(true);
                messages.debug("&cExplosion cancelled in wilderness: " + type);
            }
            return;
        }

        // Inside a claim → respect claim settings
        if (!plot.getSettings().isExplosionsAllowed()) {
            event.setCancelled(true);
            messages.debug("&cExplosion cancelled in claim [" + plot.getDisplayNameSafe() + "]: " + type);
            return;
        }

        // If explosions are allowed, strip griefing effect (remove blocks inside claims)
        Iterator<org.bukkit.block.Block> it = event.blockList().iterator();
        while (it.hasNext()) {
            org.bukkit.block.Block block = it.next();
            Plot affectedPlot = plots.getPlot(block.getChunk());
            if (affectedPlot != null) {
                it.remove(); // don’t let explosions break claim blocks
            }
        }

        if (entity instanceof Explosive) {
            messages.debug("&eExplosion processed in claim [" + plot.getDisplayNameSafe() + "]: " + type);
        }
    }
}
