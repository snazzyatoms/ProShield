package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Chunk;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;

/**
 * EntityGriefProtectionListener
 *
 * ✅ Blocks griefing from:
 *    - Endermen (block pickup)
 *    - Ravagers (crop trampling)
 *    - Silverfish (infestation)
 *    - Wither (block destruction)
 *    - EnderDragon (block destruction)
 *
 * ✅ Global wilderness config + per-claim flags
 * ✅ Debug logging for admins
 */
public class EntityGriefProtectionListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plots;
    private final MessagesUtil messages;

    public EntityGriefProtectionListener(ProShield plugin, PlotManager plots, MessagesUtil messages) {
        this.plugin = plugin;
        this.plots = plots;
        this.messages = messages;
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        Entity entity = event.getEntity();
        Chunk chunk = event.getBlock().getChunk();
        Plot plot = plots.getPlot(chunk);

        FileConfiguration config = plugin.getConfig();

        boolean globalGrief = config.getBoolean("protection.entity-grief.enabled", false);
        boolean perClaimAllowed = plot != null && plot.getSettings().isEntityGriefingAllowed();

        // Wilderness check
        if (plot == null) {
            if (!globalGrief && isProtectedEntity(entity, config)) {
                event.setCancelled(true);
                messages.debug("&cEntity grief blocked in wilderness by: " + entity.getType());
            }
            return;
        }

        // Inside a claim → controlled by per-claim setting
        if (!perClaimAllowed && isProtectedEntity(entity, config)) {
            event.setCancelled(true);
            messages.debug("&cEntity grief blocked in claim [" + plot.getDisplayNameSafe() + "] by: " + entity.getType());
        }
    }

    /**
     * Checks if entity type is protected according to config.
     */
    private boolean isProtectedEntity(Entity entity, FileConfiguration config) {
        if (entity instanceof Enderman) {
            return config.getBoolean("protection.entity-grief.enderman", true);
        }
        if (entity instanceof Ravager) {
            return config.getBoolean("protection.entity-grief.ravager", true);
        }
        if (entity instanceof Silverfish) {
            return config.getBoolean("protection.entity-grief.silverfish", true);
        }
        if (entity instanceof Wither) {
            return config.getBoolean("protection.entity-grief.wither", true);
        }
        if (entity instanceof EnderDragon) {
            return config.getBoolean("protection.entity-grief.ender-dragon", true);
        }
        return false;
    }
}
