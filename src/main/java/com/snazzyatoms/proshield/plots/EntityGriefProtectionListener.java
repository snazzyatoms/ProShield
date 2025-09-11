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
 * ✅ Protects against Enderman, Ravager, Silverfish, Wither, EnderDragon grief.
 * ✅ Global config + per-claim flag: isEntityGriefingAllowed().
 * ✅ Uses MessagesUtil for debug reporting.
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

        // Wilderness → respect global toggle
        if (plot == null) {
            if (!config.getBoolean("protection.entity-grief.enabled", false)) {
                if (isProtectedEntity(entity, config)) {
                    event.setCancelled(true);
                    messages.debug("&cEntity grief blocked in wilderness: " + entity.getType());
                }
            }
            return;
        }

        // Inside claim → respect per-claim toggle
        if (!plot.getSettings().isEntityGriefingAllowed()) {
            if (isProtectedEntity(entity, config)) {
                event.setCancelled(true);
                messages.debug("&cEntity grief blocked in claim [" + plot.getDisplayNameSafe() + "] by " + entity.getType());
            }
        }
    }

    /**
     * Determines if this entity type is blocked by config.
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
