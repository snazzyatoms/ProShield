package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Explosive;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityTargetEvent;

/**
 * MobProtectionListener
 * - Handles mob spawning, targeting, explosions, griefing
 * - Safezones protect only owners + trusted players
 * - Explosions & grief respect claim flags
 */
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
        Location loc = event.getLocation();
        if (loc == null) return;

        Plot plot = plotManager.getPlotAt(loc);
        if (plot != null && !plot.getFlag("mob-spawn")) {
            event.setCancelled(true);
            messages.debug("Blocked mob spawn at " + loc + " (Plot: " + plot.getId() + ")");
        }
    }

    @EventHandler
    public void onMobTarget(EntityTargetEvent event) {
        Entity target = event.getTarget();
        if (!(target instanceof Player player)) return;

        Plot plot = plotManager.getPlotAt(player.getLocation());
        if (plot != null && plot.getFlag("safezone")) {
            // Only protect owners/trusted players
            if (plotListener.isProtected(player, plot) && event.getEntity() instanceof Monster) {
                event.setCancelled(true);
                messages.debug("Prevented " + event.getEntity().getType() + " from targeting " + player.getName());
            }
        }
    }

    @EventHandler
    public void onExplosion(EntityExplodeEvent event) {
        Location loc = event.getLocation();
        if (loc == null) return;

        Plot plot = plotManager.getPlotAt(loc);
        if (plot != null && !plot.getFlag("explosions")) {
            event.blockList().clear(); // Prevent block damage
            messages.debug("Blocked explosion block damage at " + loc);
        }
    }

    @EventHandler
    public void onEndermanMoveBlock(EntityChangeBlockEvent event) {
        Location loc = event.getBlock().getLocation();
        if (loc == null) return;

        Plot plot = plotManager.getPlotAt(loc);
        // Currently tied to mob-spawn flag (could be separated into 'enderman-grief')
        if (plot != null && !plot.getFlag("mob-spawn")) {
            event.setCancelled(true);
            messages.debug("Prevented Enderman block move at " + loc);
        }
    }

    // --- Extra: Catch fireball & TNT entities if needed ---
    @EventHandler
    public void onProjectileExplosion(EntityExplodeEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Projectile || entity instanceof Explosive)) return;

        Location loc = entity.getLocation();
        Plot plot = plotManager.getPlotAt(loc);
        if (plot != null && !plot.getFlag("explosions")) {
            event.blockList().clear();
            messages.debug("Prevented projectile/explosive damage at " + loc);
        }
    }
}
