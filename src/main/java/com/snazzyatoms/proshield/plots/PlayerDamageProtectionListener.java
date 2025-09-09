package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

/**
 * Cancels damage to claim owners (and trusted/role-based players) while they are inside
 * their own claim, according to protection.damage.* settings.
 *
 * NOTE: PvP between players is handled by PvpProtectionListener; this listener focuses
 * on PvE (mobs/projectiles/environment/etc.). It will still cancel player->player damage
 * if cancel-all is true, but PvP is already blocked by PvpProtectionListener when needed.
 */
public class PlayerDamageProtectionListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plots;

    private boolean enabled;
    private boolean protectOwnerTrusted;
    private boolean cancelAll;

    // granular flags are used only if cancelAll == false
    private boolean pve;
    private boolean projectiles;
    private boolean environment;
    private boolean fireLava;
    private boolean fall;
    private boolean explosions;
    private boolean drownVoidSuffocate;
    private boolean poisonWither;

    public PlayerDamageProtectionListener(PlotManager plots) {
        this.plugin = ProShield.getInstance();
        this.plots = plots;
        reloadFromConfig();
    }

    public void reloadFromConfig() {
        FileConfiguration cfg = plugin.getConfig();
        this.enabled             = cfg.getBoolean("protection.damage.enabled", true);
        this.protectOwnerTrusted = cfg.getBoolean("protection.damage.protect-owner-and-trusted", true);
        this.cancelAll           = cfg.getBoolean("protection.damage.cancel-all", true);

        this.pve                 = cfg.getBoolean("protection.damage.pve", true);
        this.projectiles         = cfg.getBoolean("protection.damage.projectiles", true);
        this.environment         = cfg.getBoolean("protection.damage.environment", true);
        this.fireLava            = cfg.getBoolean("protection.damage.fire-lava", true);
        this.fall                = cfg.getBoolean("protection.damage.fall", true);
        this.explosions          = cfg.getBoolean("protection.damage.explosions", true);
        this.drownVoidSuffocate  = cfg.getBoolean("protection.damage.drown-void-suffocate", true);
        this.poisonWither        = cfg.getBoolean("protection.damage.poison-wither", true);
    }

    /* -----------------------------------------------------------
       Master handler: catch-all damage
       ----------------------------------------------------------- */

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!enabled) return;
        if (!(event.getEntity() instanceof Player victim)) return;

        // Only protect if the player is inside a claimed chunk they own / are trusted in.
        if (!protectsVictimHere(victim)) return;

        // If we're cancel-all, we're done.
        if (cancelAll) {
            event.setCancelled(true);
            return;
        }

        // Otherwise evaluate by cause
        switch (event.getCause()) {
            case ENTITY_ATTACK:
            case ENTITY_SWEEP_ATTACK:
            case MAGIC:
            case THORNS:
                // handled by onDamageByEntity (below) to distinguish PvE/PvP.
                break;

            case PROJECTILE:
            case FIREBALL:
                if (projectiles) event.setCancelled(true);
                break;

            case ENTITY_EXPLOSION:
            case BLOCK_EXPLOSION:
                if (explosions) event.setCancelled(true);
                break;

            case LAVA:
            case FIRE:
            case FIRE_TICK:
            case HOT_FLOOR:
                if (fireLava) event.setCancelled(true);
                break;

            case CONTACT:  // cactus, sweet berries
            case SUFFOCATION:
            case CRAMMING:
            case DRYOUT:
            case FREEZE:
            case LIGHTNING:
            case MELTING:
            case POISON:
            case WITHER:
                if (environment) event.setCancelled(true);
                if ((event.getCause() == EntityDamageEvent.DamageCause.POISON
                        || event.getCause() == EntityDamageEvent.DamageCause.WITHER) && poisonWither) {
                    event.setCancelled(true);
                }
                break;

            case DROWNING:
            case VOID:
                if (drownVoidSuffocate) event.setCancelled(true);
                break;

            case FALL:
                if (fall) event.setCancelled(true);
                break;

            default:
                // Nothing
                break;
        }
    }

    /* -----------------------------------------------------------
       Refined: by-entity (distinguish PvP vs PvE)
       ----------------------------------------------------------- */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamageByEntity(EntityDamageByEntityEvent event) {
        if (!enabled) return;
        if (!(event.getEntity() instanceof Player victim)) return;
        if (!protectsVictimHere(victim)) return;

        // If cancel-all -> block everything
        if (cancelAll) {
            event.setCancelled(true);
            return;
        }

        // If damager is a player, let the PvP listener manage it. We'll only cancel here if cancelAll==false AND pve==true (not applicable),
        // so ignore unless we explicitly want to block all entity damage.
        if (event.getDamager() instanceof Player) {
            // PvPProtectionListener already blocks PvP in claims when configured.
            return;
        }

        // Non-player damager (mob or projectile source that Bukkit reports as entity)
        if (pve || projectiles) {
            event.setCancelled(true);
        }
    }

    /* -----------------------------------------------------------
       Optional sanity for block-based damage specifics
       ----------------------------------------------------------- */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamageByBlock(EntityDamageByBlockEvent event) {
        if (!enabled) return;
        if (!(event.getEntity() instanceof Player victim)) return;
        if (!protectsVictimHere(victim)) return;

        if (cancelAll) {
            event.setCancelled(true);
            return;
        }

        switch (event.getCause()) {
            case CONTACT: // cactus, berries
            case SUFFOCATION:
            case CRAMMING:
                if (environment) event.setCancelled(true);
                break;
            case LAVA:
            case FIRE:
            case FIRE_TICK:
            case HOT_FLOOR:
                if (fireLava) event.setCancelled(true);
                break;
            case BLOCK_EXPLOSION:
                if (explosions) event.setCancelled(true);
                break;
            default:
                // Nothing
        }
    }

    /* -----------------------------------------------------------
       Helpers
       ----------------------------------------------------------- */

    private boolean protectsVictimHere(Player victim) {
        // Spectators/creative? Still protected if config says so. No early returns here.
        // Only if they are in a claim and are owner/trusted (or role-satisfied).
        if (!plots.getClaim(victim.getLocation()).isPresent()) return false;

        if (!protectOwnerTrusted) return false;

        // Owner or trusted via classic trusted-list?
        if (plots.isOwner(victim.getUniqueId(), victim.getLocation())) return true;
        if (plots.isTrustedOrOwner(victim.getUniqueId(), victim.getLocation())) return true;

        // If roles are enabled in your implementation, prefer role check:
        // Example: treat MEMBER and above as "trusted".
        try {
            return plots.hasRoleAtLeast(victim.getLocation(), victim.getUniqueId(), ClaimRole.MEMBER);
        } catch (Throwable ignored) {
            // If roles not present, fall back on trusted-only.
        }
        return false;
    }

    @SuppressWarnings("unused")
    private String prefix() {
        return ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("messages.prefix", "&3[ProShield]&r "));
    }
}
