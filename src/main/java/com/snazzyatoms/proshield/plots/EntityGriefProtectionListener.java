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
 * ✅ Blocks Enderman, Ravager, Silverfish, Wither, and EnderDragon grief
 * ✅ Global wilderness toggle (config.yml)
 * ✅ Per-claim toggle (PlotSettings.isEntityGriefingAllowed)
 * ✅ Debug feedback for admins
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

        FileConfiguration cfg = plugin.getConfig();
        boolean global = cfg.getBoolean("protection.entity-grief.enabled", true);

        // Wilderness
        if (plot == null) {
            if (!global && isProtectedEntity(entity, cfg)) {
                event.setCancelled(true);
                messages.debug("&cEntity grief blocked in wilderness: " + entity.getType());
            }
            return;
        }

        // Inside claim
        if (!plot.getSettings().isEntityGriefingAllowed()) {
            if (isProtectedEntity(entity, cfg)) {
                event.setCancelled(true);
                messages.debug("&cEntity grief blocked in claim [" + plot.getDisplayNameSafe() +
                        "] by " + entity.getType());
            }
        }
    }

    /**
     * Check if this entity type is configured to be blocked.
     */
    private boolean isProtectedEntity(Entity entity, FileConfiguration cfg) {
        if (entity instanceof Enderman) {
            return cfg.getBoolean("protection.entity-grief.enderman", true);
        }
        if (entity instanceof Ravager) {
            return cfg.getBoolean("protection.entity-grief.ravager", true);
        }
        if (entity instanceof Silverfish) {
            return cfg.getBoolean("protection.entity-grief.silverfish", true);
        }
        if (entity instanceof Wither) {
            return cfg.getBoolean("protection.entity-grief.wither", true);
        }
        if (entity instanceof EnderDragon) {
            return cfg.getBoolean("protection.entity-grief.ender-dragon", true);
        }
        return false;
    }
}
