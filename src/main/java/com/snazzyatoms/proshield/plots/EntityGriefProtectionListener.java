package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Chunk;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Ravager;
import org.bukkit.entity.Silverfish;
import org.bukkit.entity.Wither;
import org.bukkit.entity.EnderDragon;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

/**
 * Handles entity griefing protection:
 * - Endermen block movement
 * - Ravagers trampling
 * - Silverfish infestations
 * - Wither & Dragon destruction
 * - Uses both global + per-claim toggles
 * - Integrated with MessagesUtil
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

        boolean globalEnabled = config.getBoolean("protection.entity-grief.enabled", true);
        boolean perClaimEnabled = plot != null && plot.getSettings().isEntityGriefingAllowed();

        // If globally disabled and claim does not allow, cancel
        if (!globalEnabled && (plot == null || !perClaimEnabled)) return;

        // Endermen
        if (entity instanceof Enderman && !config.getBoolean("protection.entity-grief.enderman", true)) {
            event.setCancelled(true);
            messages.debug(plugin, "EntityGrief blocked: Enderman in " + chunk);
        }

        // Ravagers
        if (entity instanceof Ravager && !config.getBoolean("protection.entity-grief.ravager", true)) {
            event.setCancelled(true);
            messages.debug(plugin, "EntityGrief blocked: Ravager in " + chunk);
        }

        // Silverfish
        if (entity instanceof Silverfish && !config.getBoolean("protection.entity-grief.silverfish", true)) {
            event.setCancelled(true);
            messages.debug(plugin, "EntityGrief blocked: Silverfish in " + chunk);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityExplosion(EntityExplodeEvent event) {
        Entity entity = event.getEntity();
        Chunk chunk = event.getLocation().getChunk();

        Plot plot = plotManager.getPlot(chunk);
        FileConfiguration config = plugin.getConfig();

        boolean globalEnabled = config.getBoolean("protection.entity-grief.enabled", true);
        boolean perClaimEnabled = plot != null && plot.getSettings().isEntityGriefingAllowed();

        if (!globalEnabled && (plot == null || !perClaimEnabled)) return;

        // Wither explosion protection
        if (entity instanceof Wither && !config.getBoolean("protection.entity-grief.wither", true)) {
            event.blockList().clear();
            messages.debug(plugin, "EntityGrief blocked: Wither explosion in " + chunk);
        }

        // Ender Dragon destruction
        if (entity instanceof EnderDragon && !config.getBoolean("protection.entity-grief.ender-dragon", true)) {
            event.blockList().clear();
            messages.debug(plugin, "EntityGrief blocked: EnderDragon in " + chunk);
        }
    }
}
