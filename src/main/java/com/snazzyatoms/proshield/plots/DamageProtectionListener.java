// src/main/java/com/snazzyatoms/proshield/plots/DamageProtectionListener.java
package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.roles.ClaimRole;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Chunk;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

/**
 * Handles all forms of damage prevention inside claims.
 *
 * Preserves prior logic:
 * - Cancels PvP, PvE, projectiles, environment damage
 * - Honors PlotSettings flags
 * - Uses ClaimRoleManager for owner/trusted logic
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
     * Entity Damage Events
     * ------------------------------------------------------- */

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        Chunk chunk = event.getEntity().getLocation().getChunk();
        Plot plot = plotManager.getPlot(chunk);
        if (plot == null) return; // not in claim

        PlotSettings s = plot.getSettings();

        // Cancel all damage in claim
        if (s.isDamageCancelAll()) {
            event.setCancelled(true);
            return;
        }

        // Environment-based damage
        switch (event.getCause()) {
            case FIRE, FIRE_TICK, LAVA:
                if (!s.isDamageFireLavaEnabled()) event.setCancelled(true);
                break;
            case FALL:
                if (!s.isDamageFallEnabled()) event.setCancelled(true);
                break;
            case BLOCK_EXPLOSION, ENTITY_EXPLOSION:
                if (!s.isDamageExplosionsEnabled()) event.setCancelled(true);
                break;
            case DROWNING, VOID, SUFFOCATION:
                if (!s.isDamageDrownVoidSuffocateEnabled()) event.setCancelled(true);
                break;
            case POISON, WITHER:
                if (!s.isDamagePoisonWitherEnabled()) event.setCancelled(true);
                break;
            case ENTITY_ATTACK, PROJECTILE:
                // handled in EntityDamageByEntityEvent
                break;
            default:
                if (!s.isDamageEnvironmentEnabled()) event.setCancelled(true);
                break;
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player damager)) return;

        Chunk chunk = event.getEntity().getLocation().getChunk();
        Plot plot = plotManager.getPlot(chunk);
        if (plot == null) return;

        PlotSettings s = plot.getSettings();

        // Protect owner + trusted from damage
        if (s.isDamageProtectOwnerAndTrusted() && event.getEntity() instanceof Player target) {
            if (plotManager.isTrustedOrOwner(target.getUniqueId(), plot.getWorld().getChunkAt(plot.getX(), plot.getZ()).getBlock(0,0,0).getLocation())) {
                event.setCancelled(true);
                return;
            }
        }

        // PvP
        if (event.getEntityType() == EntityType.PLAYER && !s.isDamagePvpEnabled()) {
            event.setCancelled(true);
            messages.send(damager, "pvp-deny");
            return;
        }

        // PvE
        if (event.getEntityType() != EntityType.PLAYER && !s.isDamagePveEnabled()) {
            event.setCancelled(true);
            messages.send(damager, "pve-deny");
            return;
        }

        // Projectiles (arrows, snowballs, etc.)
        if (event.getCause() == EntityDamageEvent.DamageCause.PROJECTILE && !s.isDamageProjectilesEnabled()) {
            event.setCancelled(true);
            messages.send(damager, "projectiles-deny");
        }
    }
}
