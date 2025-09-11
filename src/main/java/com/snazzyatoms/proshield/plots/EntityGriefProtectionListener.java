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
 * Handles entity griefing protection inside claims.
 * - Enderman block pickup
 * - Ravager trampling
 * - Silverfish block infestation
 * - Wither + EnderDragon destruction
 * Supports both global config and per-claim settings.
 */
public class EntityGriefProtectionListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final MessagesUtil messages;

    public EntityGriefProtectionListener(ProShield plugin, PlotManager plotManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
        this.messages = plugin.getMessagesUtil();
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        Entity entity = event.getEntity();
        Chunk chunk = event.getBlock().getChunk();
        Plot plot = plotManager.getPlot(chunk);

        FileConfiguration config = plugin.getConfig();

        boolean globalGriefEnabled = config.getBoolean("protection.entity-grief.enabled", true);
        boolean perClaimGriefAllowed = plot != null && plot.getSettings().isEntityGriefingAllowed();

        // Wilderness check
        if (plot == null) {
            if (!globalGriefEnabled && isProtectedEntity(entity, config)) {
                event.setCancelled(true);
                messages.debug("&cEntity grief blocked in wilderness by " + entity.getType());
            }
            return;
        }

        // Inside claim check
        if (!perClaimGriefAllowed && isProtectedEntity(entity, config)) {
            event.setCancelled(true);
            messages.debug("&cEntity grief blocked in claim [" + plot.getName() + "] by " + entity.getType());
        }
    }

    /**
     * Determines if the entity type should be blocked based on config.
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
