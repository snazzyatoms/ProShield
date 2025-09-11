// src/main/java/com/snazzyatoms/proshield/plots/DamageProtectionListener.java
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
 * Preserves all prior logic and fixes:
 * ✅ Aligned with new PlotSettings flags
 * ✅ UUID vs Player mismatch fixed
 * ✅ Enum switch cases cleaned up
 */
public class DamageProtectionListener implements Listener {

    private final PlotManager plotManager;
    private final MessagesUtil messages;

    public DamageProtectionListener(PlotManager plotManager, MessagesUtil messages) {
        this.plotManager = plotManager;
        this.messages = messages;
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        Chunk chunk = player.getLocation().getChunk();
        Plot plot = plotManager.getPlot(chunk);
        if (plot == null) return;

        PlotSettings s = plot.getSettings();

        // Cancel all damage if toggled
        if (s.isDamageCancelAll()) {
            event.setCancelled(true);
            return;
        }

        // Specific checks
        switch (event.getCause()) {
            case ENTITY_ATTACK -> {
                if (!s.isDamagePveEnabled()) {
                    event.setCancelled(true);
                }
            }
            case PROJECTILE -> {
                if (!s.isDamageProjectilesEnabled()) {
                    event.setCancelled(true);
                }
            }
            case LAVA, FIRE, FIRE_TICK, HOT_FLOOR -> {
                if (!s.isDamageFireLavaEnabled()) {
                    event.setCancelled(true);
                }
            }
            case FALL -> {
                if (!s.isDamageFallEnabled()) {
                    event.setCancelled(true);
                }
            }
            case BLOCK_EXPLOSION, ENTITY_EXPLOSION -> {
                if (!s.isDamageExplosionsEnabled()) {
                    event.setCancelled(true);
                }
            }
            case VOID, DROWNING, SUFFOCATION -> {
                if (!s.isDamageDrownVoidSuffocateEnabled()) {
                    event.setCancelled(true);
                }
            }
            case POISON, WITHER -> {
                if (!s.isDamagePoisonWitherEnabled()) {
                    event.setCancelled(true);
                }
            }
            case CONTACT, CRAMMING, DRAGON_BREATH, MAGIC, LIGHTNING, STARVATION, THORNS, FLY_INTO_WALL, DRYOUT -> {
                if (!s.isDamageEnvironmentEnabled()) {
                    event.setCancelled(true);
                }
            }
            default -> {
                // Allow other causes
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;

        Chunk chunk = victim.getLocation().getChunk();
        Plot plot = plotManager.getPlot(chunk);
        if (plot == null) return;

        PlotSettings s = plot.getSettings();

        if (event.getDamager() instanceof Player attacker) {
            // PvP protection
            if (!s.isDamagePvpEnabled()) {
                event.setCancelled(true);
                return;
            }

            // Protect trusted players if configured
            if (s.isDamageProtectOwnerAndTrusted()) {
                if (plot.isOwner(attacker.getUniqueId()) || plot.getTrusted().containsKey(attacker.getUniqueId())) {
                    event.setCancelled(true);
                }
                if (plot.isOwner(victim.getUniqueId()) || plot.getTrusted().containsKey(victim.getUniqueId())) {
                    event.setCancelled(true);
                }
            }
        }

        // Handle mob vs player damage
        if (event.getDamager().getType() != EntityType.PLAYER && !s.isDamagePveEnabled()) {
            event.setCancelled(true);
        }
    }
}
