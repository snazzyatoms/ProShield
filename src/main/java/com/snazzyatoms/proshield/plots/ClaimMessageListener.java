package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Handles claim entry/exit messages.
 *
 * ✅ Preserves prior logic (notify when claim changes)
 * ✅ Expanded: shows "leaving" + "entering" messages
 * ✅ Uses MessagesUtil with placeholders: %claim%
 */
public class ClaimMessageListener implements Listener {

    private final PlotManager plots;
    private final MessagesUtil messages;

    public ClaimMessageListener(ProShield plugin, PlotManager plots) {
        this.plots = plots;
        this.messages = plugin.getMessagesUtil();
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();

        String fromClaim = plots.getClaimName(e.getFrom());
        String toClaim = plots.getClaimName(e.getTo());

        if (!fromClaim.equals(toClaim)) {
            // Leaving
            if (fromClaim != null && !fromClaim.isEmpty() && !"Wilderness".equalsIgnoreCase(fromClaim)) {
                messages.send(p, "claim.leaving", fromClaim);
            }

            // Entering
            if (toClaim != null && !toClaim.isEmpty()) {
                messages.send(p, "claim.entering", toClaim);
            }
        }
    }
}
