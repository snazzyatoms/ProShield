package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityTargetEvent;

public class MobProtectionListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final PlotListener plotListener;
    private final MessagesUtil messages;

    public MobProtectionListener(ProShield plugin, PlotManager plotManager, PlotListener plotListener) {
        this.plugin = plugin;
        this.plotManager = plotManager;
        this.plotListener = plotListener;
        this.messages = plugin.getMessagesUtil();
    }

    @EventHandler
    public void onMobSpawn(CreatureSpawnEvent event) {
        Plot plot = plotManager.getPlotAt(event.getLocation());
        if (plot != null && !plot.getFlag("mob-spawn")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onMobTarget(EntityTargetEvent event) {
        Entity target = event.getTarget();
        if (!(target instanceof Player player)) return;

        Plot plot = plotManager.getPlotAt(player.getLocation());
        if (plot != null && plot.getFlag("safezone")) {
            // Only protect owners/trusted players
            if (plotListener.isProtected(player, plot)) {
                if (event.getEntity() instanceof Monster) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onCreeperExplosion(EntityExplodeEvent event) {
        Plot plot = plotManager.getPlotAt(event.getLocation());
        if (plot != null && !plot.getFlag("explosions")) {
            event.blockList().clear(); // Prevent block damage
        }
    }

    @EventHandler
    public void onTntExplosion(EntityExplodeEvent event) {
        Plot plot = plotManager.getPlotAt(event.getLocation());
        if (plot != null && !plot.getFlag("explosions")) {
            event.blockList().clear();
        }
    }

    @EventHandler
    public void onEndermanMoveBlock(EntityChangeBlockEvent event) {
        Plot plot = plotManager.getPlotAt(event.getBlock().getLocation());
        if (plot != null && !plot.getFlag("mob-spawn")) {
            event.setCancelled(true);
        }
    }
}
