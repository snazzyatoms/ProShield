package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class EntityDamageProtectionListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plots;

    // cached flags
    private boolean enabled;
    private boolean ownerImmune;
    private boolean trustedImmune;

    private boolean blockPvpHere; // we still leave PvP to PvpProtectionListener, but we won't cancel here for players
    private boolean mobs;
    private boolean projectiles;
    private boolean explosions;
    private boolean fireLava;
    private boolean fall;
    private boolean otherEnv;

    public EntityDamageProtectionListener(PlotManager plots) {
        this.plugin = ProShield.getInstance();
        this.plots = plots;
        reloadConfig();
    }

    public final void reloadConfig() {
        // master switch for player damage protection inside claims
        enabled       = plugin.getConfig().getBoolean("protection.player.enabled", true);

        // who gets protected while inside the claim?
        ownerImmune   = plugin.getConfig().getBoolean("protection.player.damage.owner-immune", true);
        trustedImmune = plugin.getConfig().getBoolean("protection.player.damage.trusted-immune", true);

        // pvp toggle lives elsewhere; we read the default to be aware (but we won't handle PvP here)
        blockPvpHere  = !plugin.getConfig().getBoolean("protection.pvp-in-claims", false);

        // what types to cancel
        mobs          = plugin.getConfig().getBoolean("protection.player.damage.mobs", true);
        projectiles   = plugin.getConfig().getBoolean("protection.player.damage.projectiles", true);
        explosions    = plugin.getConfig().getBoolean("protection.player.damage.explosions", true);
        fireLava      = plugin.getConfig().getBoolean("protection.player.damage.fire-lava", true);
        fall          = plugin.getConfig().getBoolean("protection.player.damage.fall", true); // default ON per your “everything protected” request
        otherEnv      = plugin.getConfig().getBoolean("protection.player.damage.other-environment", true);
    }

    private boolean protectThisVictim(Player victim, Location loc) {
        if (!enabled) return false;
        if (!plots.isClaimed(loc)) return false;

        // owner always protected if ownerImmune
        if (ownerImmune && plots.isOwner(victim.getUniqueId(), loc)) return true;

        // trusted protected if trustedImmune
        if (trustedImmune && plots.isTrustedOrOwner(victim.getUniqueId(), loc)) return true;

        return false;
    }

    /** Handles entity-caused damage (mobs, projectiles, players). */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player victim)) return;
        if (!protectThisVictim(victim, victim.getLocation())) return;

        // Separate PvP (player vs player) — leave to PvpProtectionListener to honor pvp-in-claims config
        if (e.getDamager() instanceof Player) {
            // do not auto-cancel here; PvpProtectionListener already enforces pvp-in-claims
            return;
        }

        // Projectile (arrow, trident, fireball, etc.)
        if (e.getDamager() instanceof Projectile) {
            if (projectiles) {
                e.setCancelled(true);
            }
            return;
        }

        // Everything else is "mobs" (or non-player entities like armor stands shouldn’t damage anyway)
        if (mobs) {
            e.setCancelled(true);
        }
    }

    /** Handles environmental damage (explosions, fire/lava, fall, cactus, etc.). */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player victim)) return;
        if (!protectThisVictim(victim, victim.getLocation())) return;

        switch (e.getCause()) {
            case BLOCK_EXPLOSION:
            case ENTITY_EXPLOSION:
                if (explosions) e.setCancelled(true);
                break;

            case FIRE:
            case FIRE_TICK:
            case LAVA:
            case HOT_FLOOR: // magma block
                if (fireLava) e.setCancelled(true);
                break;

            case FALL:
                if (fall) e.setCancelled(true);
                break;

            // A sensible default set; you can extend as needed:
            case CONTACT:      // cactus, sweet berry, etc.
            case SUFFOCATION:
            case DROWNING:
            case LIGHTNING:
            case POISON:
            case WITHER:
            case CRAMMING:
            case DRYOUT:
            case STARVATION:
            case FLY_INTO_WALL: // elytra
            case VOID:
            case FREEZE:
                if (otherEnv) e.setCancelled(true);
                break;

            default:
                // leave other causes alone
                break;
        }
    }
}
