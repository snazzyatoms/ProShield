// ===========================================
// PvpProtectionListener.java
// ===========================================
package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class PvpProtectionListener implements Listener {
    private final PlotManager plots;

    public PvpProtectionListener(PlotManager plots) {
        this.plots = plots;
    }

    @EventHandler
    public void onPvp(EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof Player victim && e.getDamager() instanceof Player attacker) {
            if (!plots.canPvp(attacker, victim)) {
                e.setCancelled(true);
                attacker.sendMessage(ProShield.PREFIX + "PvP is disabled in this claim!");
            }
        }
    }
}
