package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.roles.ClaimRole;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Chunk;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.projectiles.ProjectileSource;

/**
 * Damage protection inside claims:
 *  - Protect owner & trusted if enabled
 *  - Cancel PvE, projectile, and environment causes according to PlotSettings
 *  - Defers PvP rules to PvpProtectionListener
 */
public class DamageProtectionListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final ClaimRoleManager roleManager;
    private final MessagesUtil messages;

    public DamageProtectionListener(ProShield plugin, PlotManager plotManager, ClaimRoleManager roleManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
        this.roleManager = roleManager;
        this.messages = plugin.getMessagesUtil();
    }

    /* -------------------------------------------------------
     * Entity vs Entity damage (handles PvE & projectiles here)
     * PvP is handled by PvpProtectionListener to keep concerns split.
     * ------------------------------------------------------- */
    @EventHandler(ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity victim = event.getEntity();
        if (!(victim instanceof Player player)) return;

        Chunk chunk = player.getLocation().getChunk();
        Plot plot = plotManager.getPlot(chunk);
        if (plot == null) return; // wilderness

        // Admin bypass: if attacker is a player and bypassing, let pass.
        Entity rawDamager = event.getDamager();
        if (rawDamager instanceof Player damager && plugin.isBypassing(damager)) {
            return;
        }
        // Projectiles may be fired by players or mobs. If shooter is a player in bypass, allow.
        if (rawDamager instanceof Projectile proj) {
            ProjectileSource src = proj.getShooter();
            if (src instanceof Player shooter && plugin.isBypassing((Player) src)) {
                return;
            }
        }

        PlotSettings s = plot.getSettings();

        // Owner/trusted blanket protection (non-PvP — PvP is separate listener)
        if (s.isDamageProtectOwnerAndTrusted()) {
            ClaimRole role = roleManager.getRole(plot, player);
            boolean trusted = (role != ClaimRole.VISITOR); // treat any non-visitor as trusted
            if (trusted) {
                // If the damager is a player, let PvpProtectionListener decide. Otherwise cancel.
                if (!(rawDamager instanceof Player)) {
                    event.setCancelled(true);
                    // Avoid chat spam—only light feedback when a mob tries to damage you
                    messages.debug("Cancelled damage to trusted player inside claim (owner/trusted protection).");
                    return;
                }
                // attacker is a player; PvP handled elsewhere
            }
        }

        // Cancel-all inside claim (non-PvP)
        if (s.isDamageCancelAll()) {
            if (!(rawDamager instanceof Player)) {
                event.setCancelled(true);
                messages.debug("Cancelled ALL non-PvP damage inside claim (cancel-all).");
                return;
            }
        }

        // PvE toggle: cancel damage from non-player entities if enabled
        if (s.isDamagePveEnabled()) {
            // If damager is not a player AND not a player-shot projectile, cancel
            if (!(rawDamager instanceof Player)) {
                if (rawDamager instanceof Projectile proj) {
                    ProjectileSource shooter = proj.getShooter();
                    if (shooter instanceof Player) {
                        // player projectile -> PvP; handled by PvP listener
                    } else {
                        event.setCancelled(true);
                        messages.debug("Cancelled PvE damage by non-player projectile inside claim.");
                        return;
                    }
                } else {
                    event.setCancelled(true);
                    messages.debug("Cancelled PvE damage inside claim.");
                    return;
                }
            }
        }

        // Projectiles toggle: cancel ANY projectile damage (non-player shooter) if enabled
        if (s.isDamageProjectilesEnabled()) {
            if (rawDamager instanceof Projectile proj) {
                ProjectileSource shooter = proj.getShooter();
                // Only cancel if shooter is not a player; PvP is separate
                if (!(shooter instanceof Player)) {
                    event.setCancelled(true);
                    messages.debug("Cancelled projectile damage inside claim (projectiles).");
                }
            }
        }
    }

    /* -------------------------------------------------------
     * Environment damage inside claims
     * ------------------------------------------------------- */
    @EventHandler(ignoreCancelled = true)
    public void onEnvironmentDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        Chunk chunk = player.getLocation().getChunk();
        Plot plot = plotManager.getPlot(chunk);
        if (plot == null) return; // wilderness

        PlotSettings s = plot.getSettings();

        // Owner/trusted blanket protection for environment damage
        if (s.isDamageProtectOwnerAndTrusted()) {
            ClaimRole role = roleManager.getRole(plot, player);
            boolean trusted = (role != ClaimRole.VISITOR);
            if (trusted) {
                event.setCancelled(true);
                messages.debug("Cancelled environment damage to trusted player inside claim.");
                return;
            }
        }

        // Cancel-all damage (environment included)
        if (s.isDamageCancelAll()) {
            event.setCancelled(true);
            messages.debug("Cancelled ALL environment damage inside claim (cancel-all).");
            return;
        }

        // Fine-grained environment toggles
        switch (event.getCause()) {
            case FIRE, LAVA, FIRE_TICK, HOT_FLOOR, LIGHTNING -> {
                if (s.isDamageFireLavaEnabled()) {
                    event.setCancelled(true);
                    messages.debug("Cancelled fire/lava damage inside claim.");
                }
            }
            case FALL -> {
                if (s.isDamageFallEnabled()) {
                    event.setCancelled(true);
                    messages.debug("Cancelled fall damage inside claim.");
                }
            }
            case BLOCK_EXPLOSION, ENTITY_EXPLOSION -> {
                if (s.isDamageExplosionsEnabled()) {
                    event.setCancelled(true);
                    messages.debug("Cancelled explosion damage inside claim.");
                }
            }
            case DROWNING, SUFFOCATION, VOID -> {
                if (s.isDamageDrownVoidSuffocateEnabled()) {
                    event.setCancelled(true);
                    messages.debug("Cancelled drowning/void/suffocation damage inside claim.");
                }
            }
            case WITHER, POISON, MAGIC -> {
                if (s.isDamagePoisonWitherEnabled()) {
                    event.setCancelled(true);
                    messages.debug("Cancelled poison/wither/magic damage inside claim.");
                }
            }
            case CONTACT, CRAMMING, DRYOUT, THORNS, STARVATION, FREEZE, SONIC_BOOM, CUSTOM, MELTING, DRAGON_BREATH, SUICIDE, FLY_INTO_WALL, FALLING_BLOCK, STALAGMITE, STALACTITE -> {
                // Generic environment toggle if present
                if (s.isDamageEnvironmentEnabled()) {
                    event.setCancelled(true);
                    messages.debug("Cancelled generic environment damage inside claim.");
                }
            }
            default -> {
                // No action
            }
        }
    }
}
