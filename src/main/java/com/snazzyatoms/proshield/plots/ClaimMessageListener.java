// ===========================================
// ClaimMessageListener.java
// ===========================================
package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class ClaimMessageListener implements Listener {
    private final PlotManager plots;

    public ClaimMessageListener(PlotManager plots) {
        this.plots = plots;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        String fromClaim = plots.getClaimName(e.getFrom());
        String toClaim = plots.getClaimName(e.getTo());
        if (!fromClaim.equals(toClaim)) {
            p.sendMessage(ProShield.PREFIX + "Entering " + toClaim);
        }
    }
}
