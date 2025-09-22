// src/main/java/com/snazzyatoms/proshield/plots/ClaimProtectionListener.java
package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.roles.ClaimRole;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.projectiles.ProjectileSource;

/**
 * ClaimProtectionListener (v1.2.6)
 * - Handles block break/place, bucket use, PvP inside claims
 * - Adds SAFEZONE protections: cancel mob targeting & mob damage
 * - Uses ClaimRoleManager for role-based permissions
 * - Respects claim flags and world-control defaults
 * - Denial messages + PvP rules pulled from messages.yml
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

    /**
     * Unified block + bucket handling.
     *
     * @param requiresBuild true if action needs build perms, false if interaction-only
     */
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
    public void onEntityDamagePvP(EntityDamageByEntityEvent event) {
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
     * SAFEZONE: MOB PROTECTION
     * ------------------------- */

    // Cancel mob targeting players in safezone (immediate)
    @EventHandler
    public void onMobTargetPlayer(EntityTargetLivingEntityEvent event) {
        if (!(event.getTarget() instanceof Player player)) return;

        Plot plot = plotManager.getPlotAt(player.getLocation());
        if (plot == null) return;
        if (!plot.getFlag("safezone")) return;

        FileConfiguration cfg = plugin.getConfig();
        if (!cfg.getBoolean("protection.mobs.prevent-targeting", true)) return;

        // Clear target & cancel event
        if (event.getEntity() instanceof Mob mob) mob.setTarget(null);
        event.setCancelled(true);

        debug("Cleared mob target " + event.getEntity().getType()
                + " on " + player.getName() + " inside safezone plot=" + plot.getId());
    }

    // Cancel mob damage to players in safezone (covers melee & projectiles)
    @EventHandler
    public void onMobDamagePlayer(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        Plot plot = plotManager.getPlotAt(player.getLocation());
        if (plot == null) return;
        if (!plot.getFlag("safezone")) return;

        FileConfiguration cfg = plugin.getConfig();
        if (!cfg.getBoolean("protection.mobs.prevent-damage", true)) return;

        if (isHostileDamage(event.getDamager())) {
            event.setCancelled(true);
            debug("Blocked mob damage by " + event.getDamager().getType()
                    + " to " + player.getName() + " inside safezone plot=" + plot.getId());
        }
    }

    private boolean isHostileDamage(Entity damager) {
        if (damager instanceof Monster) return true;
        if (damager instanceof Projectile proj) {
            ProjectileSource src = proj.getShooter();
            return (src instanceof Monster);
        }
        // You can extend here if you want other hostile sources treated as "mob damage".
        return false;
    }

    private void debug(String msg) {
        if (plugin.isDebugEnabled()) {
            plugin.getLogger().info("[ClaimProtection] " + msg);
        }
    }
}
