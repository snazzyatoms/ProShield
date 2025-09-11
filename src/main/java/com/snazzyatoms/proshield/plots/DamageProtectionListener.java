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
 * Handles player damage protection inside claims.
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

        Plot plot = plotManager.getPlot(player.getLocation().getChunk());
        if (plot == null) return;

        PlotSettings s = plot.getSettings();

        if (s.isDamageCancelAll()) {
            event.setCancelled(true);
            return;
        }

        switch (event.getCause()) {
            case ENTITY_ATTACK -> { if (!s.isDamagePveEnabled()) event.setCancelled(true); }
            case PROJECTILE -> { if (!s.isDamageProjectilesEnabled()) event.setCancelled(true); }
            case LAVA, FIRE, FIRE_TICK, HOT_FLOOR -> { if (!s.isDamageFireLavaEnabled()) event.setCancelled(true); }
            case FALL -> { if (!s.isDamageFallEnabled()) event.setCancelled(true); }
            case BLOCK_EXPLOSION, ENTITY_EXPLOSION -> { if (!s.isDamageExplosionsEnabled()) event.setCancelled(true); }
            case VOID, DROWNING, SUFFOCATION -> { if (!s.isDamageDrownVoidSuffocateEnabled()) event.setCancelled(true); }
            case POISON, WITHER -> { if (!s.isDamagePoisonWitherEnabled()) event.setCancelled(true); }
            case CONTACT, CRAMMING, DRAGON_BREATH, MAGIC, LIGHTNING,
                 STARVATION, THORNS, FLY_INTO_WALL, DRYOUT -> { if (!s.isDamageEnvironmentEnabled()) event.setCancelled(true); }
            default -> { }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;

        Plot plot = plotManager.getPlot(victim.getLocation().getChunk());
        if (plot == null) return;

        PlotSettings s = plot.getSettings();

        if (event.getDamager() instanceof Player attacker) {
            if (!s.isDamagePvpEnabled()) {
                event.setCancelled(true);
                return;
            }
            if (s.isDamageProtectOwnerAndTrusted()) {
                if (plot.isOwner(attacker.getUniqueId()) || plot.getTrusted().containsKey(attacker.getUniqueId())) {
                    event.setCancelled(true);
                }
                if (plot.isOwner(victim.getUniqueId()) || plot.getTrusted().containsKey(victim.getUniqueId())) {
                    event.setCancelled(true);
                }
            }
        }

        if (event.getDamager().getType() != EntityType.PLAYER && !s.isDamagePveEnabled()) {
            event.setCancelled(true);
        }
    }
}
