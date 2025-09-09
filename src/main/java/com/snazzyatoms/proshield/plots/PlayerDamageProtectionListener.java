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
 * Cancels damage to claim owner/trusted players inside their own claim
 * according to config protection.damage settings.
 *
 * Config (defaults ON):
 * protection:
 *   damage:
 *     enabled: true
 *     protect-owner-and-trusted: true
 *     cancel-all: true
 *     # if cancel-all = false, use category toggles...
 */
public class PlayerDamageProtectionListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plots;

    public PlayerDamageProtectionListener(ProShield plugin, PlotManager plots) {
        this.plugin = plugin;
        this.plots  = plots;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        // Global toggle
        if (!plugin.getConfig().getBoolean("protection.damage.enabled", true)) return;

        Location loc = player.getLocation();
        if (!plots.isClaimed(loc)) return;

        // Only protect owner/trusted if configured
        boolean protectOwnerTrusted = plugin.getConfig().getBoolean("protection.damage.protect-owner-and-trusted", true);
        if (protectOwnerTrusted) {
            UUID u = player.getUniqueId();
            if (!(plots.isOwner(u, loc) || plots.isTrustedOrOwner(u, loc))) {
                return; // not protected here
            }
        }

        // cancel-all short-circuit
        if (plugin.getConfig().getBoolean("protection.damage.cancel-all", true)) {
            event.setCancelled(true);
            return;
        }

        // Otherwise use per-cause controls
        EntityDamageEvent.DamageCause cause = event.getCause();
        switch (cause) {
            case ENTITY_ATTACK: // melee mobs/players
                if (event instanceof EntityDamageByEntityEvent) {
                    if (plugin.getConfig().getBoolean("protection.damage.pve", true)) {
                        event.setCancelled(true);
                    }
                }
                break;

            case PROJECTILE:
                if (plugin.getConfig().getBoolean("protection.damage.projectiles", true)) {
                    event.setCancelled(true);
                }
                break;

            case CONTACT: // cactus, sweet berry, etc.
                if (plugin.getConfig().getBoolean("protection.damage.environment", true)) {
                    event.setCancelled(true);
                }
                break;

            case FIRE:
            case FIRE_TICK:
            case LAVA:
                if (plugin.getConfig().getBoolean("protection.damage.fire-lava", true)) {
                    event.setCancelled(true);
                }
                break;

            case FALL:
                if (plugin.getConfig().getBoolean("protection.damage.fall", true)) {
                    event.setCancelled(true);
                }
                break;

            case BLOCK_EXPLOSION:
            case ENTITY_EXPLOSION:
                if (plugin.getConfig().getBoolean("protection.damage.explosions", true)) {
                    event.setCancelled(true);
                }
                break;

            case DROWNING:
            case SUFFOCATION:
            case VOID:
                if (plugin.getConfig().getBoolean("protection.damage.drown-void-suffocate", true)) {
                    event.setCancelled(true);
                }
                break;

            case POISON:
            case WITHER:
            case MAGIC:
                if (plugin.getConfig().getBoolean("protection.damage.poison-wither", true)) {
                    event.setCancelled(true);
                }
                break;

            default:
                // leave as-is for other exotic causes unless cancel-all is true
                break;
        }
    }
}
