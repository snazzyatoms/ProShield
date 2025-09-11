package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Chunk;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;

/**
 * Handles entity griefing protection inside claims.
 * - Enderman block pickup
 * - Ravager trampling
 * - Silverfish infestation
 * - Wither & EnderDragon destruction
 */
public class EntityGriefProtectionListener implements Listener {

    private final PlotManager plotManager;
    private final MessagesUtil messages;

    public EntityGriefProtectionListener(PlotManager plotManager, MessagesUtil messages) {
        this.plotManager = plotManager;
        this.messages = messages;
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        Entity entity = event.getEntity();
        Chunk chunk = event.getBlock().getChunk();
        Plot plot = plotManager.getPlot(chunk);

        FileConfiguration config = plotManager.getPlugin().getConfig();

        boolean globalGriefEnabled = config.getBoolean("protection.entity-grief.enabled", true);
        boolean perClaimGriefAllowed = plot != null && plot.getSettings().isEntityGriefingAllowed();

        // Wilderness
        if (plot == null) {
            if (!globalGriefEnabled && isProtectedEntity(entity, config)) {
                event.setCancelled(true);
                messages.debug("&cEntity grief blocked in wilderness by " + entity.getType());
            }
            return;
        }

        // Claim
        if (!perClaimGriefAllowed && isProtectedEntity(entity, config)) {
            event.setCancelled(true);
            messages.debug("&cEntity grief blocked in claim [" + plot.getName() + "] by " + entity.getType());
        }
    }

    private boolean isProtectedEntity(Entity entity, FileConfiguration config) {
        if (entity instanceof Enderman) return config.getBoolean("protection.entity-grief.enderman", true);
        if (entity instanceof Ravager) return config.getBoolean("protection.entity-grief.ravager", true);
        if (entity instanceof Silverfish) return config.getBoolean("protection.entity-grief.silverfish", true);
        if (entity instanceof Wither) return config.getBoolean("protection.entity-grief.wither", true);
        if (entity instanceof EnderDragon) return config.getBoolean("protection.entity-grief.ender-dragon", true);
        return false;
    }
}
