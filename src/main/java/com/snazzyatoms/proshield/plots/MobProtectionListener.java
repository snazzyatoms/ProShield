package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.EntityChangeBlockEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityTargetEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class MobProtectionListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final MessagesUtil messages;

    // Players who have enabled per-player mob debug
    private final Set<UUID> debugPlayers = new HashSet<>();

    public MobProtectionListener(ProShield plugin, PlotManager plotManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
        this.messages = plugin.getMessagesUtil();
    }

    /* ===============================
     * SPAWNING CONTROL
     * =============================== */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMobSpawn(CreatureSpawnEvent event) {
        Location loc = event.getLocation();
        Entity entity = event.getEntity();

        // Optional: allow passive mob spawning
        if (entity instanceof Animals || entity instanceof Ambient) return;

        Plot plot = plotManager.getPlotAt(loc);
        if (plot == null) return;

        if (!plot.getFlag("mob-spawn")) {
            event.setCancelled(true);
            debug(loc, "Blocked spawn of " + entity.getType());
        }
    }

    /* ===============================
     * MOB TARGETING (SAFEZONE)
     * =============================== */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMobTarget(EntityTargetEvent event) {
        Entity target = event.getTarget();
        if (!(target instanceof Player)) return;

        Plot plot = plotManager.getPlotAt(target.getLocation());
        if (plot == null) return;

        if (plot.getFlag("safezone") && event.getEntity() instanceof Monster) {
            event.setCancelled(true);
            debug(target.getLocation(), "Prevented targeting by " + event.getEntity().getType());
        }
    }

    /* ===============================
     * EXPLOSION GRIEFING (CREEPER, TNT)
     * =============================== */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onExplosion(EntityExplodeEvent event) {
        Entity source = event.getEntity();
        Location loc = event.getLocation();
        Plot plot = plotManager.getPlotAt(loc);

        if (plot == null) return;
        if (plot.getFlag("mob-griefing")) return;

        if (source instanceof Creeper || source instanceof TNTPrimed || source instanceof Explosive) {
            event.blockList().clear();
            debug(loc, "Blocked explosion block damage from " + source.getType());
        }
    }

    /* ===============================
     * ENDERMAN GRIEF PREVENTION
     * =============================== */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEndermanBlockMove(EntityChangeBlockEvent event) {
        if (!(event.getEntity() instanceof Enderman)) return;

        Location loc = event.getBlock().getLocation();
        Plot plot = plotManager.getPlotAt(loc);
        if (plot == null) return;

        if (!plot.getFlag("mob-griefing")) {
            event.setCancelled(true);
            debug(loc, "Blocked Enderman block movement");
        }
    }

    /* ===============================
     * PER-PLAYER DEBUG TOGGLE
     * =============================== */
    public void toggleDebug(Player player) {
        UUID uuid = player.getUniqueId();
        if (debugPlayers.contains(uuid)) {
            debugPlayers.remove(uuid);
            messages.send(player, "&cMob protection debug disabled.");
        } else {
            debugPlayers.add(uuid);
            messages.send(player, "&aMob protection debug enabled.");
        }
    }

    /* ===============================
     * DEBUG HELPERS
     * =============================== */
    private void debug(Location loc, String msg) {
        if (plugin.isDebugEnabled()) {
            plugin.getLogger().info("[MobProtection] " + msg + " at " + locToString(loc));
        }

        for (UUID uuid : debugPlayers) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                messages.send(player, "&7[&aMobProtect&7] &f" + msg + " &7at &b" + locToString(loc));
            }
        }
    }

    private String locToString(Location loc) {
        return loc.getWorld().getName() + " (" + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ() + ")";
    }
}
