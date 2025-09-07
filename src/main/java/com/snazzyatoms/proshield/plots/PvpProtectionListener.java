// path: src/main/java/com/snazzyatoms/proshield/plots/PvpProtectionListener.java
package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class PvpProtectionListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plots;
    private boolean pvpInClaims;

    public PvpProtectionListener(PlotManager plots) {
        this.plugin = ProShield.getInstance();
        this.plots  = plots;
        reloadPvpFlag();
    }

    public void reloadPvpFlag() {
        this.pvpInClaims = plugin.getConfig().getBoolean("protection.pvp-in-claims", false);
    }

    @EventHandler(ignoreCancelled = true)
    public void onHit(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player victim)) return;
        if (!(e.getDamager() instanceof Player attacker)) return;

        Location loc = victim.getLocation();
        if (!plots.isClaimed(loc)) return;
        if (!pvpInClaims && !attacker.hasPermission("proshield.bypass")) {
            e.setCancelled(true);
        }
    }
}
