package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/** Simple PvP gate: honors protection.pvp-in-claims + raid-protection shield */
public class PvpProtectionListener implements Listener {

    private final PlotManager plotManager;
    private boolean pvpInClaims;
    private boolean raidEnabled;
    private int raidSeconds;

    public PvpProtectionListener(PlotManager plotManager) {
        this.plotManager = plotManager;
        reloadPvpFlag();
    }

    public void reloadPvpFlag() {
        var cfg = ProShield.getInstance().getConfig();
        pvpInClaims = cfg.getBoolean("protection.pvp-in-claims", false);
        raidEnabled = cfg.getBoolean("raid-protection.enabled", true);
        raidSeconds = cfg.getInt("raid-protection.seconds", 86400);
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player victim)) return;
        if (!(e.getDamager() instanceof Player attacker)) return;

        // No restriction? Allow if pvp-in-claims is true.
        if (pvpInClaims) return;

        var loc = victim.getLocation();
        if (!plotManager.isClaimed(loc)) return;

        // Respect raid shield
        if (raidEnabled && plotManager.isWithinRaidShield(loc, raidSeconds)) {
            e.setCancelled(true);
            return;
        }

        // General pvp disabled in claims
        e.setCancelled(true);
    }
}
