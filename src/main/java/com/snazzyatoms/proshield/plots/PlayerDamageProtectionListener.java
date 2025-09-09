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
 * Cancels damage to claim owners/trusted players inside the boundaries of their claim.
 * Notes:
 * - Honors config: protection.damage.enabled, cancel-all, protect-owner-and-trusted
 * - Ignores OP/bypass for VICTIM protection (bypass only affects building/interaction)
 */
public class PlayerDamageProtectionListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plots;

    public PlayerDamageProtectionListener(PlotManager plots, ProShield plugin) {
        this.plots = plots;
        this.plugin = plugin;
    }

    private boolean damageProtectionEnabled() {
        return plugin.getConfig().getConfigurationSection("protection") != null
                && plugin.getConfig().getBoolean("protection.damage.enabled", true);
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
        // Owner or trusted (via roles-aware helper)
        if (plots.isOwner(u, loc)) return true;
        // roles: treat any role >= VISITOR with explicit rule; default to roles helper when available
        try {
            return plots.hasRoleAtLeast(loc, u, ClaimRole.VISITOR); // VISITOR+ are protected from damage by default
        } catch (Throwable t) {
            // Fallback to legacy trusted check if roles not present
            return plots.isTrustedOrOwner(u, loc);
        }
    }

    private boolean shouldCancelSelective(EntityDamageEvent.DamageCause cause) {
        // Only used if cancel-all = false
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
            case CONTACT: // cactus, sweet berry
            case HOT_FLOOR: // magma block
                return plugin.getConfig().getBoolean("protection.damage.environment", true);
            default:
                // Unknown/rare causes → be safe and block if environment toggled on
                return plugin.getConfig().getBoolean("protection.damage.environment", true);
        }
    }

    /** Catch-all (environment, fall, lava, etc). */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!damageProtectionEnabled()) return;
        if (!(event.getEntity() instanceof Player)) return;

        Player victim = (Player) event.getEntity();

        // Do not treat bypass/admin as unprotected. Owners/trusted should still be safe.
        if (!isProtectedVictim(victim)) return;
        if (plots.getClaim(victim.getLocation()).isEmpty()) return;

        if (cancelAll()) {
            event.setCancelled(true);
            if (plugin.isDebug()) plugin.getLogger().info("[Damage] Cancelled ALL damage to " + victim.getName());
            return;
        }

        // Selective mode:
        if (shouldCancelSelective(event.getCause())) {
            event.setCancelled(true);
            if (plugin.isDebug()) plugin.getLogger().info("[Damage] Cancelled " + event.getCause() + " to " + victim.getName());
        }
    }

    /** PvP / Projectile / Mob-attacks (still route through generic handler above, but we keep this for clarity/logs). */
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
