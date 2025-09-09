// path: src/main/java/com/snazzyatoms/proshield/plots/DamageProtectionListener.java
package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class DamageProtectionListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plots;

    // cached toggles
    private boolean enabled;                 // master switch
    private boolean protectOwnerAndTrusted;  // protect only owner/trusted inside claims
    private boolean cancelAll;               // if true, cancel any damage while in claim
    private boolean pve;                     // mobs/creatures damage
    private boolean projectiles;             // arrows, tridents, etc.
    private boolean environment;             // generic env (cactus, hot floor, etc.)
    private boolean fireLava;                // fire, fire_tick, lava
    private boolean fall;                    // fall damage
    private boolean explosions;              // entity/block explosions
    private boolean drownVoidSuffocate;      // drowning/void/suffocation
    private boolean poisonWither;            // potion/poison/wither (DoT)

    public DamageProtectionListener(PlotManager plots) {
        this.plugin = ProShield.getInstance();
        this.plots  = plots;
        reloadDamageConfig();
    }

    public void reloadDamageConfig() {
        var c = plugin.getConfig();

        enabled                = c.getBoolean("protection.damage.enabled", true);
        protectOwnerAndTrusted = c.getBoolean("protection.damage.protect-owner-and-trusted", true);
        cancelAll              = c.getBoolean("protection.damage.cancel-all", true);

        // fine-grained (used only if cancel-all = false)
        pve                    = c.getBoolean("protection.damage.pve", true);
        projectiles            = c.getBoolean("protection.damage.projectiles", true);
        environment            = c.getBoolean("protection.damage.environment", true);
        fireLava               = c.getBoolean("protection.damage.fire-lava", true);
        fall                   = c.getBoolean("protection.damage.fall", true);
        explosions             = c.getBoolean("protection.damage.explosions", true);
        drownVoidSuffocate     = c.getBoolean("protection.damage.drown-void-suffocate", true);
        poisonWither           = c.getBoolean("protection.damage.poison-wither", true);
    }

    private boolean isProtectedPlayerInClaim(Player p) {
        Location loc = p.getLocation();
        if (!plots.isClaimed(loc)) return false;

        if (!protectOwnerAndTrusted) return true;
        return plots.isTrustedOrOwner(p.getUniqueId(), loc);
    }

    /** Covers generic/environmental damage. */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onAnyDamage(EntityDamageEvent e) {
        if (!enabled) return;
        Entity victim = e.getEntity();
        if (!(victim instanceof Player p)) return;

        if (!isProtectedPlayerInClaim(p)) return;

        if (cancelAll) {
            e.setCancelled(true);
            return;
        }

        // granular handling
        switch (e.getCause()) {
            case ENTITY_ATTACK:
            case ENTITY_SWEEP_ATTACK:
                // handled in onDamageByEntity for finer PVE/PVP split
                break;

            case PROJECTILE:
                if (projectiles) e.setCancelled(true);
                break;

            case BLOCK_EXPLOSION:
            case ENTITY_EXPLOSION:
                if (explosions) e.setCancelled(true);
                break;

            case FIRE:
            case FIRE_TICK:
            case LAVA:
                if (fireLava) e.setCancelled(true);
                break;

            case FALL:
                if (fall) e.setCancelled(true);
                break;

            case DROWNING:
            case SUFFOCATION:
            case VOID:
                if (drownVoidSuffocate) e.setCancelled(true);
                break;

            case POISON:
            case WITHER:
                if (poisonWither) e.setCancelled(true);
                break;

            case CONTACT:        // cactus, sweet berries, etc.
            case HOT_FLOOR:      // magma blocks
            case CRAMMING:
            case FREEZE:
            case DRYOUT:
                if (environment) e.setCancelled(true);
                break;

            default:
                // Leave other exotic causes alone unless cancel-all = true
                break;
        }
    }

    /** Covers PVP/PVE and projectile owners */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onDamageByEntity(EntityDamageByEntityEvent e) {
        if (!enabled) return;
        if (!(e.getEntity() instanceof Player p)) return;
        if (!isProtectedPlayerInClaim(p)) return;

        if (cancelAll) {
            e.setCancelled(true);
            return;
        }

        // If damager is a Player -> PVP
        if (e.getDamager() instanceof Player) {
            // Your dedicated PvpProtectionListener already enforces pvp-in-claims false by default.
            // We still hard-block here if cancelAll is false but you want safety.
            // If you prefer to rely *only* on PvpProtectionListener, comment next line.
            e.setCancelled(true);
            return;
        }

        // Projectile owners (e.g., skeleton arrow, player arrow handled above)
        switch (e.getCause()) {
            case PROJECTILE:
                if (projectiles) e.setCancelled(true);
                return;
            case ENTITY_ATTACK:    // mobs melee
            case ENTITY_SWEEP_ATTACK:
                if (pve) e.setCancelled(true);
                return;
            default:
                // fall back to onAnyDamage for other causes
                break;
        }
    }
}
