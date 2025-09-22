// src/main/java/com/snazzyatoms/proshield/plots/ClaimProtectionListener.java
package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.roles.ClaimRole;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;

/**
 * ClaimProtectionListener (v1.2.6 SAFEZONE ENHANCED)
 * - Handles block break/place, bucket use, and PvP inside claims
 * - Adds Safe Zone (mob protection + despawn logic) when enabled
 * - Uses ClaimRoleManager for role-based permissions
 * - Respects claim flags and world-control defaults
 * - Denial messages pulled from messages.yml
 */
public class ClaimProtectionListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final ClaimRoleManager roleManager;
    private final MessagesUtil messages;

    public ClaimProtectionListener(ProShield plugin, PlotManager plotManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
        this.roleManager = plugin.getRoleManager();
        this.messages = plugin.getMessagesUtil();
    }

    /* -------------------------
     * BLOCK INTERACTIONS
     * ------------------------- */
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        handleBlockAction(event.getPlayer(), event.getBlock().getLocation(),
                "block-break", messages.getOrDefault("messages.error.block-break", "&cYou cannot break blocks here."),
                event, true);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        handleBlockAction(event.getPlayer(), event.getBlock().getLocation(),
                "block-place", messages.getOrDefault("messages.error.block-place", "&cYou cannot place blocks here."),
                event, true);
    }

    @EventHandler
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        handleBlockAction(event.getPlayer(), event.getBlock().getLocation(),
                "bucket-use", messages.getOrDefault("messages.error.bucket-empty", "&cYou cannot empty buckets here."),
                event, false);
    }

    @EventHandler
    public void onBucketFill(PlayerBucketFillEvent event) {
        handleBlockAction(event.getPlayer(), event.getBlock().getLocation(),
                "bucket-use", messages.getOrDefault("messages.error.bucket-fill", "&cYou cannot fill buckets here."),
                event, false);
    }

    private void handleBlockAction(Player player, Location loc, String flag,
                                   String denyMessage, org.bukkit.event.Cancellable event, boolean requiresBuild) {
        Plot plot = plotManager.getPlotAt(loc);
        if (plot == null) return; // wilderness = vanilla rules

        ClaimRole role = roleManager.getRole(plot, player.getUniqueId());
        boolean allowed = requiresBuild ? role.canBuild() : role.canInteract();

        if (!allowed) {
            event.setCancelled(true);
            messages.send(player, denyMessage);
            debug("Denied " + flag + " for " + player.getName() + " (role=" + role + ")");
            return;
        }

        if (!plot.getFlag(flag)) {
            event.setCancelled(true);
            messages.send(player, denyMessage);
            debug("Denied " + flag + " for " + player.getName()
                    + " (flag=" + flag + " disabled, role=" + role + ", plot=" + plot.getId() + ")");
        }
    }

    /* -------------------------
     * PVP HANDLING
     * ------------------------- */
    @EventHandler
    public void onEntityDamageByPlayer(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!(event.getEntity() instanceof Player victim)) return;

        Plot plot = plotManager.getPlotAt(victim.getLocation());
        if (plot == null) return; // wilderness = PvP allowed

        // Inside claim → respect PvP flag (default false → safezone)
        if (!plot.getFlag("pvp")) {
            event.setCancelled(true);
            messages.send(attacker, messages.getOrDefault("messages.error.pvp", "&cPvP is disabled in this claim."));
            debug("Prevented PvP: " + attacker.getName() + " → " + victim.getName()
                    + " (flag=pvp=false, plot=" + plot.getId() + ")");
        }
    }

    /* -------------------------
     * SAFEZONE MOB HANDLING
     * ------------------------- */
    @EventHandler
    public void onMobTarget(EntityTargetEvent event) {
        if (!(event.getTarget() instanceof Player player)) return;
        Plot plot = plotManager.getPlotAt(player.getLocation());
        if (plot == null) return;

        if (plot.getFlag("safezone")) {
            event.setCancelled(true);
            debug("Mob prevented from targeting " + player.getName() + " in safezone (plot=" + plot.getId() + ")");
        }
    }

    @EventHandler
    public void onMobDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        Plot plot = plotManager.getPlotAt(player.getLocation());
        if (plot == null) return;

        if (plot.getFlag("safezone")) {
            if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK ||
                event.getCause() == EntityDamageEvent.DamageCause.PROJECTILE) {
                event.setCancelled(true);
                debug("Damage prevented to " + player.getName() + " in safezone (plot=" + plot.getId() + ")");
            }
        }
    }

    /* -------------------------
     * DEBUGGING
     * ------------------------- */
    private void debug(String msg) {
        if (plugin.isDebugEnabled()) {
            plugin.getLogger().info("[ClaimProtection] " + msg);
        }
    }
}
