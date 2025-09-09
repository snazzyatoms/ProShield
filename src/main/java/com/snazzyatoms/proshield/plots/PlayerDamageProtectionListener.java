package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.UUID;

public class PlayerDamageProtectionListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plots;

    public PlayerDamageProtectionListener(ProShield plugin, PlotManager plots) {
        this.plugin = plugin;
        this.plots = plots;
    }

    private boolean shouldProtect(Player p) {
        if (!plugin.getConfig().getBoolean("protection.damage.enabled", true)) return false;
        Location l = p.getLocation();
        var claimOpt = plots.getClaim(l);
        if (claimOpt.isEmpty()) return false;

        if (!plugin.getConfig().getBoolean("protection.damage.protect-owner-and-trusted", true))
            return false;

        UUID u = p.getUniqueId();
        return plots.isOwner(u, l) || plots.isTrustedOrOwner(u, l);
    }

    @EventHandler(ignoreCancelled = true)
    public void onAnyDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player p)) return;
        if (!shouldProtect(p)) return;

        if (plugin.getConfig().getBoolean("protection.damage.cancel-all", true)) {
            e.setCancelled(true);
            return;
        }
        switch (e.getCause()) {
            case FALL -> cancelIf("protection.damage.fall", e);
            case FIRE, FIRE_TICK, LAVA -> cancelIf("protection.damage.fire-lava", e);
            case BLOCK_EXPLOSION, ENTITY_EXPLOSION -> cancelIf("protection.damage.explosions", e);
            case DROWNING, SUFFOCATION, VOID -> cancelIf("protection.damage.drown-void-suffocate", e);
            case CONTACT, HOT_FLOOR, ENTITY_SWEEP_ATTACK, THORNS -> cancelIf("protection.damage.environment", e);
            case WITHER, POISON, MAGIC -> cancelIf("protection.damage.poison-wither", e);
            default -> {}
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPvEDamage(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player p)) return;
        if (!shouldProtect(p)) return;

        // projectiles & melee from mobs
        switch (e.getCause()) {
            case PROJECTILE -> cancelIf("protection.damage.projectiles", e);
            case ENTITY_ATTACK -> cancelIf("protection.damage.pve", e);
            default -> {}
        }
    }

    private void cancelIf(String path, EntityDamageEvent e) {
        if (plugin.getConfig().getBoolean(path, true)) e.setCancelled(true);
    }
}
