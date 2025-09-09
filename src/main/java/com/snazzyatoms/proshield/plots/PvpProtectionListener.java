package com.snazzyatoms.proshield.plots;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class PvpProtectionListener implements Listener {

    private final PlotManager plots;

    public PvpProtectionListener(PlotManager plots) {
        this.plots = plots;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPvp(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player victim)) return;
        if (!(e.getDamager() instanceof Player attacker)) return;

        Location l = victim.getLocation();
        if (plots.getClaim(l).isEmpty()) return;

        // pvp-in-claims false => block PvP in claims
        boolean pvpInClaims = plots.getPlugin().getConfig().getBoolean("protection.pvp-in-claims", false);
        if (!pvpInClaims) e.setCancelled(true);
    }
}
