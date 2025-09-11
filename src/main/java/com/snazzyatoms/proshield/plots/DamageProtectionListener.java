package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Chunk;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

/**
 * DamageProtectionListener
 *
 * ✅ Protects players inside claims from all types of damage
 * ✅ Configurable per-claim flags (PvP, PvE, fall, fire, lava, explosions, drowning, etc.)
 * ✅ Trusted + owner protection rules respected
 * ✅ Global + per-claim distinction
 */
public class DamageProtectionListener implements Listener {

    private final PlotManager plots;
    private final MessagesUtil messages;

    public DamageProtectionListener(PlotManager plots, MessagesUtil messages) {
        this.plots = plots;
        this.messages = messages;
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        Chunk chunk = player.getLocation().getChunk();
        Plot plot = plots.getPlot(chunk);
        if (plot == null) return;

        PlotSettings s = plot.getSettings();

        // Cancel all damage outright
        if (s.isDamageCancelAll()) {
            event.setCancelled(true);
            return;
        }

        switch (event.getCause()) {
            case ENTITY_ATTACK -> {
                if (!s.isDamagePveEnabled()) event.setCancelled(true);
            }
            case PROJECTILE -> {
                if (!s.isDamageProjectilesEnabled()) event.setCancelled(true);
            }
            case LAVA, FIRE, FIRE_TICK, HOT_FLOOR -> {
                if (!s.isDamageFireLavaEnabled()) event.setCancelled(true);
            }
            case FALL -> {
                if (!s.isDamageFallEnabled()) event.setCancelled(true);
            }
            case BLOCK_EXPLOSION, ENTITY_EXPLOSION -> {
                if (!s.isDamageExplosionsEnabled()) event.setCancelled(true);
            }
            case VOID, DROWNING, SUFFOCATION -> {
                if (!s.isDamageDrownVoidSuffocateEnabled()) event.setCancelled(true);
            }
            case POISON, WITHER -> {
                if (!s.isDamagePoisonWitherEnabled()) event.setCancelled(true);
            }
            case CONTACT, CRAMMING, DRAGON_BREATH, MAGIC, LIGHTNING,
                 STARVATION, THORNS, FLY_INTO_WALL, DRYOUT -> {
                if (!s.isDamageEnvironmentEnabled()) event.setCancelled(true);
            }
            default -> { /* allow other damage causes */ }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;

        Chunk chunk = victim.getLocation().getChunk();
        Plot plot = plots.getPlot(chunk);
        if (plot == null) return;

        PlotSettings s = plot.getSettings();

        if (event.getDamager() instanceof Player attacker) {
            // PvP
            if (!s.isDamagePvpEnabled()) {
                event.setCancelled(true);
                messages.debug("&cPvP prevented in claim: " + plot.getDisplayNameSafe());
                return;
            }

            // Protect owner & trusted players
            if (s.isDamageProtectOwnerAndTrusted()) {
                if (plot.isOwner(attacker.getUniqueId()) || plot.getTrusted().containsKey(attacker.getUniqueId())) {
                    event.setCancelled(true);
                    return;
                }
                if (plot.isOwner(victim.getUniqueId()) || plot.getTrusted().containsKey(victim.getUniqueId())) {
                    event.setCancelled(true);
                }
            }
        }

        // Non-player attackers (PvE)
        if (event.getDamager().getType() != EntityType.PLAYER && !s.isDamagePveEnabled()) {
            event.setCancelled(true);
        }
    }
}
