package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.UUID;

/**
 * Cancels damage to claim owners/trusted players inside their claim.
 * - Respects config at protection.damage.*
 * - Ignores bypass/OP for VICTIM protection (bypass is for build/interaction).
 */
public class PlayerDamageProtectionListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plots;

    public PlayerDamageProtectionListener(PlotManager plots, ProShield plugin) {
        this.plots = plots;
        this.plugin = plugin;
    }

    private boolean damageProtectionEnabled() {
        return plugin.getConfig().getBoolean("protection.damage.enabled", true);
    }

    private boolean cancelAll() {
        return plugin.getConfig().getBoolean("protection.damage.cancel-all", true);
    }

    private boolean protectOwnerAndTrusted() {
        return plugin.getConfig().getBoolean("protection.damage.protect-owner-and-trusted", true);
    }

    private boolean isProtectedVictim(Player victim) {
        Location loc = victim.getLocation();
        if (plots.getClaim(loc).isEmpty()) return false;
        if (!protectOwnerAndTrusted()) return false;

        UUID u = victim.getUniqueId();
        if (plots.isOwner(u, loc)) return true;

        // Prefer roles if available; fall back to legacy trusted.
        try {
            return plots.hasRoleAtLeast(loc, u, ClaimRole.VISITOR);
        } catch (Throwable t) {
            return plots.isTrustedOrOwner(u, loc);
        }
    }

    private boolean shouldCancelSelective(EntityDamageEvent.DamageCause cause) {
        switch (cause) {
            case ENTITY_ATTACK:
            case ENTITY_SWEEP_ATTACK:
                return plugin.getConfig().getBoolean("protection.damage.pve", true);
            case PROJECTILE:
                return plugin.getConfig().getBoolean("protection.damage.projectiles", true);
            case BLOCK_EXPLOSION:
            case ENTITY_EXPLOSION:
                return plugin.getConfig().getBoolean("protection.damage.explosions", true);
            case FIRE:
            case FIRE_TICK:
            case LAVA:
                return plugin.getConfig().getBoolean("protection.damage.fire-lava", true);
            case FALL:
                return plugin.getConfig().getBoolean("protection.damage.fall", true);
            case DROWNING:
            case VOID:
            case SUFFOCATION:
                return plugin.getConfig().getBoolean("protection.damage.drown-void-suffocate", true);
            case MAGIC:
            case POISON:
            case WITHER:
                return plugin.getConfig().getBoolean("protection.damage.poison-wither", true);
            case CONTACT:   // cactus, sweet berries
            case HOT_FLOOR: // magma block
            default:
                return plugin.getConfig().getBoolean("protection.damage.environment", true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!damageProtectionEnabled()) return;
        if (!(event.getEntity() instanceof Player)) return;

        Player victim = (Player) event.getEntity();
        if (!isProtectedVictim(victim)) return;
        if (plots.getClaim(victim.getLocation()).isEmpty()) return;

        if (cancelAll()) {
            event.setCancelled(true);
            if (plugin.isDebug()) plugin.getLogger().info("[Damage] Cancelled ALL to " + victim.getName());
            return;
        }

        if (shouldCancelSelective(event.getCause())) {
            event.setCancelled(true);
            if (plugin.isDebug()) plugin.getLogger().info("[Damage] Cancelled " + event.getCause() + " to " + victim.getName());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamageByEntity(EntityDamageByEntityEvent event) {
        if (!damageProtectionEnabled()) return;
        if (!(event.getEntity() instanceof Player)) return;

        Player victim = (Player) event.getEntity();
        if (!isProtectedVictim(victim)) return;
        if (plots.getClaim(victim.getLocation()).isEmpty()) return;

        if (cancelAll()) {
            event.setCancelled(true);
            if (plugin.isDebug()) plugin.getLogger().info("[DamageByEntity] Cancelled ALL to " + victim.getName());
            return;
        }

        if (shouldCancelSelective(event.getCause())) {
            event.setCancelled(true);
            if (plugin.isDebug()) plugin.getLogger().info("[DamageByEntity] Cancelled " + event.getCause() + " to " + victim.getName());
        }
    }
}
