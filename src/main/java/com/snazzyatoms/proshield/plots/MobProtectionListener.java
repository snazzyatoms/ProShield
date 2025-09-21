// src/main/java/com/snazzyatoms/proshield/plots/MobProtectionListener.java
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
 * - Safezones (claims) protect all players from hostile mobs
 * - Wilderness follows vanilla unless world-controls override
 * - Explosions & grief respect claim flags
 */
public class MobProtectionListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final MessagesUtil messages;

    public MobProtectionListener(ProShield plugin, PlotManager plotManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
        this.messages = plugin.getMessagesUtil();
    }

    /* -------------------------
     * MOB SPAWNING
     * ------------------------- */
    @EventHandler
    public void onMobSpawn(CreatureSpawnEvent event) {
        Location loc = event.getLocation();
        if (loc == null) return;

        Plot plot = plotManager.getPlotAt(loc);
        if (plot != null && !plot.getFlag("mob-spawn")) {
            event.setCancelled(true);
            messages.debug("Blocked mob spawn at " + loc + " (Plot=" + plot.getId() + ")");
        }
    }

    /* -------------------------
     * MOB TARGETING (Safezones)
     * ------------------------- */
    @EventHandler
    public void onMobTarget(EntityTargetEvent event) {
        if (!(event.getTarget() instanceof Player player)) return;

        Plot plot = plotManager.getPlotAt(player.getLocation());
        if (plot != null && plot.getFlag("safezone") && event.getEntity() instanceof Monster) {
            event.setCancelled(true);
            messages.debug("Prevented " + event.getEntity().getType()
                    + " from targeting " + player.getName()
                    + " inside safezone (Plot=" + plot.getId() + ")");
        }
    }

    /* -------------------------
     * EXPLOSIONS (Creeper/TNT/etc.)
     * ------------------------- */
    @EventHandler
    public void onExplosion(EntityExplodeEvent event) {
        Location loc = event.getLocation();
        if (loc == null) return;

        Plot plot = plotManager.getPlotAt(loc);
        if (plot != null && !plot.getFlag("explosions")) {
            event.blockList().clear(); // Prevent block damage
            messages.debug("Blocked explosion block damage at " + loc
                    + " (Entity=" + event.getEntityType() + ", Plot=" + plot.getId() + ")");
        }
    }

    /* -------------------------
     * ENDERMAN GRIEFING
     * ------------------------- */
    @EventHandler
    public void onEndermanMoveBlock(EntityChangeBlockEvent event) {
        Location loc = event.getBlock().getLocation();
        if (loc == null) return;

        Plot plot = plotManager.getPlotAt(loc);
        // For now tied to mob-spawn flag (could add enderman-grief flag later)
        if (plot != null && !plot.getFlag("mob-spawn")) {
            event.setCancelled(true);
            messages.debug("Prevented Enderman block move at " + loc
                    + " (Plot=" + plot.getId() + ")");
        }
    }

    /* -------------------------
     * PROJECTILE / EXPLOSIVE ENTITIES
     * ------------------------- */
    @EventHandler
    public void onProjectileExplosion(EntityExplodeEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Projectile || entity instanceof Explosive)) return;

        Location loc = entity.getLocation();
        Plot plot = plotManager.getPlotAt(loc);
        if (plot != null && !plot.getFlag("explosions")) {
            event.blockList().clear();
            messages.debug("Prevented projectile/explosive damage at " + loc
                    + " (Entity=" + entity.getType() + ", Plot=" + plot.getId() + ")");
        }
    }
}
