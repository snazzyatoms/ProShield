package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityTargetEvent;

public class MobProtectionListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final MessagesUtil messages;

    public MobProtectionListener(ProShield plugin, PlotManager plotManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
        this.messages = plugin.getMessagesUtil();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMobSpawn(CreatureSpawnEvent event) {
        Location loc = event.getLocation();
        Entity entity = event.getEntity();

        // Skip passive mobs (optional — adjust as needed)
        if (entity instanceof Animals || entity instanceof Ambient) return;

        Plot plot = plotManager.getPlotAt(loc);
        if (plot == null) return;

        if (!plot.getFlag("mob-spawn")) {
            event.setCancelled(true);
            debug("Blocked spawn of " + entity.getType() + " at " + locToString(loc));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMobTarget(EntityTargetEvent event) {
        Entity target = event.getTarget();
        if (!(target instanceof Player)) return;

        Plot plot = plotManager.getPlotAt(target.getLocation());
        if (plot == null) return;

        if (plot.getFlag("safezone") && event.getEntity() instanceof Monster) {
            event.setCancelled(true);
            debug("Prevented " + event.getEntity().getType() + " targeting player in safezone at " + locToString(target.getLocation()));
        }
    }

    private void debug(String msg) {
        if (plugin.isDebugEnabled()) {
            plugin.getLogger().info("[MobProtection] " + msg);
        }
    }

    private String locToString(Location loc) {
        return loc.getWorld().getName() + " (" + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ() + ")";
    }
}
