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
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.projectiles.ProjectileSource;

/**
 * ClaimProtectionListener (v1.2.6 + Safezone Spawn Block)
 * - Handles block/bucket/PvP logic
 * - Cancels mob targeting & mob damage inside safezones
 * - Cancels hostile mob spawns inside safezones
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
        if (plot == null) return;

        ClaimRole role = roleManager.getRole(plot, player.getUniqueId());
        boolean allowed = requiresBuild ? role.canBuild() : role.canInteract();

        if (!allowed || !plot.getFlag(flag)) {
            event.setCancelled(true);
            messages.send(player, denyMessage);
            debug("Denied " + flag + " for " + player.getName()
                    + " (role=" + role + ", flag=" + flag + ", plot=" + plot.getId() + ")");
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
        if (plot == null) return;

        if (!plot.getFlag("pvp")) {
            event.setCancelled(true);
            messages.send(attacker, messages.getOrDefault("messages.error.pvp", "&cPvP is disabled in this claim."));
            debug("Prevented PvP: " + attacker.getName() + " → " + victim.getName()
                    + " (plot=" + plot.getId() + ")");
        }
    }

    /* -------------------------
     * SAFEZONE: MOB PROTECTION
     * ------------------------- */
    @EventHandler
    public void onMobTargetPlayer(EntityTargetLivingEntityEvent event) {
        if (!(event.getTarget() instanceof Player player)) return;

        Plot plot = plotManager.getPlotAt(player.getLocation());
        if (plot == null || !plot.getFlag("safezone")) return;

        FileConfiguration cfg = plugin.getConfig();
        if (!cfg.getBoolean("protection.mobs.prevent-targeting", true)) return;

        if (event.getEntity() instanceof Mob mob) mob.setTarget(null);
        event.setCancelled(true);

        debug("Cleared target " + event.getEntity().getType()
                + " on " + player.getName() + " in safezone " + plot.getId());
    }

    @EventHandler
    public void onMobDamagePlayer(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        Plot plot = plotManager.getPlotAt(player.getLocation());
        if (plot == null || !plot.getFlag("safezone")) return;

        FileConfiguration cfg = plugin.getConfig();
        if (!cfg.getBoolean("protection.mobs.prevent-damage", true)) return;

        if (isHostileDamage(event.getDamager())) {
            event.setCancelled(true);
            debug("Blocked mob damage by " + event.getDamager().getType()
                    + " to " + player.getName() + " in safezone " + plot.getId());
        }
    }

    private boolean isHostileDamage(Entity damager) {
        if (damager instanceof Monster) return true;
        if (damager instanceof Projectile proj) {
            ProjectileSource src = proj.getShooter();
            return (src instanceof Monster);
        }
        return false;
    }

    /* -------------------------
     * SAFEZONE: MOB SPAWN BLOCK
     * ------------------------- */
    @EventHandler
    public void onMobSpawn(CreatureSpawnEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Monster)) return; // only hostiles

        Plot plot = plotManager.getPlotAt(entity.getLocation());
        if (plot == null || !plot.getFlag("safezone")) return;

        FileConfiguration cfg = plugin.getConfig();
        if (!cfg.getBoolean("protection.mobs.prevent-spawn", true)) return;

        event.setCancelled(true);
        debug("Prevented hostile spawn " + entity.getType()
                + " inside safezone " + plot.getId());
    }

    private void debug(String msg) {
        if (plugin.isDebugEnabled()) {
            plugin.getLogger().info("[ClaimProtection] " + msg);
        }
    }
}
