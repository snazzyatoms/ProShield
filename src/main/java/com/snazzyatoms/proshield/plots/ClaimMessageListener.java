package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Handles claim entry/exit messages.
 *
 * ✅ Simplified constructor (PlotManager + MessagesUtil)
 * ✅ Consistent with other listeners
 * ✅ Wilderness messages still controlled by config
 */
public class ClaimMessageListener implements Listener {

    private final PlotManager plots;
    private final MessagesUtil messages;

    public ClaimMessageListener(PlotManager plots, MessagesUtil messages) {
        this.plots = plots;
        this.messages = messages;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();

        String fromClaim = plots.getClaimName(e.getFrom());
        String toClaim   = plots.getClaimName(e.getTo());

        if (!fromClaim.equals(toClaim)) {
            // Leaving message
            if (fromClaim != null && !fromClaim.isEmpty() && !"Wilderness".equalsIgnoreCase(fromClaim)) {
                messages.send(p, "claim.leaving", fromClaim);
            }

            // Entering message
            if (toClaim != null && !toClaim.isEmpty()) {
                if ("Wilderness".equalsIgnoreCase(toClaim)) {
                    if (p.getServer().getPluginManager().getPlugin("ProShield")
                            .getConfig().getBoolean("messages.wilderness.enabled", true)) {
                        messages.send(p, "claim.entering", toClaim);
                    }
                } else {
                    messages.send(p, "claim.entering", toClaim);
                }
            }
        }
    }
}
