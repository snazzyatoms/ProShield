// path: src/main/java/com/snazzyatoms/proshield/plots/PvpProtectionListener.java
package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class PvpProtectionListener implements Listener {

    private final PlotManager plotManager;
    private boolean pvpInClaims;

    public PvpProtectionListener(PlotManager plotManager) {
        this.plotManager = plotManager;
        reloadPvpFlag();
    }

    public void reloadPvpFlag() {
        this.pvpInClaims = ProShield.getInstance()
                .getConfig().getBoolean("protection.pvp-in-claims", false);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPvp(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player victim)) return;
        if (!(e.getDamager() instanceof Player attacker)) return;

        if (pvpInClaims) return; // allowed inside claims
        if (attacker.hasPermission("proshield.bypass")) return;

        if (plotManager.isClaimed(victim.getLocation())) {
            e.setCancelled(true);
        }
    }
}
