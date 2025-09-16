package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityTargetEvent;

public class MobProtectionListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plotManager;

    public MobProtectionListener(ProShield plugin, PlotManager plotManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
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
        if (target == null) return;

        Plot plot = plotManager.getPlotAt(target.getLocation());
        if (plot != null && plot.getFlag("safezone")) {
            if (event.getEntity() instanceof Monster) {
                event.setCancelled(true);
            }
        }
    }
}
