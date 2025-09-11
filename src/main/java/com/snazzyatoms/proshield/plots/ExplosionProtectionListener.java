package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Explosive;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.Iterator;

/**
 * ExplosionProtectionListener
 *
 * ✅ Global wilderness explosion toggle
 * ✅ Per-claim explosion flag
 * ✅ Filters block list → explosions never damage protected claims
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
            boolean allow = plugin.getConfig().getBoolean("protection.explosions.enabled", false);
            if (!allow) {
                event.setCancelled(true);
                messages.debug("&cExplosion blocked in wilderness: " + explosionType);
            }
            return;
        }

        // Inside claim → per-claim toggle
        if (!plot.getSettings().isExplosionsAllowed()) {
            event.setCancelled(true);
            messages.debug("&cExplosion blocked in claim: " + plot.getDisplayNameSafe() +
                    " (" + explosionType + ")");
            return;
        }

        // Explosions allowed → filter block list
        Iterator<Block> it = event.blockList().iterator();
        while (it.hasNext()) {
            Block block = it.next();
            Plot blockPlot = plots.getPlot(block.getChunk());
            if (blockPlot != null && !blockPlot.getSettings().isExplosionsAllowed()) {
                it.remove(); // don’t damage protected claim blocks
            }
        }

        if (entity instanceof Explosive) {
            messages.debug("&eExplosion processed in claim: " +
                    plot.getDisplayNameSafe() + " (" + explosionType + ")");
        }
    }
}
