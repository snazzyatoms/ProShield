package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Handles claim entry/exit messages.
 *
 * ✅ Uses MessagesUtil for clean placeholder output
 * ✅ Config toggle for wilderness messages
 */
public class ClaimMessageListener implements Listener {

    private final PlotManager plotManager;
    private final MessagesUtil messages;

    public ClaimMessageListener(PlotManager plotManager, MessagesUtil messages) {
        this.plotManager = plotManager;
        this.messages = messages;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        String fromClaim = plotManager.getClaimName(event.getFrom());
        String toClaim   = plotManager.getClaimName(event.getTo());

        if (!fromClaim.equals(toClaim)) {
            // Leaving
            if (fromClaim != null && !fromClaim.isEmpty() && !"Wilderness".equalsIgnoreCase(fromClaim)) {
                messages.send(player, "claim.leaving", fromClaim);
            }

            // Entering
            if (toClaim != null && !toClaim.isEmpty()) {
                if ("Wilderness".equalsIgnoreCase(toClaim)) {
                    if (plotManager.getPlugin().getConfig().getBoolean("messages.wilderness.enabled", true)) {
                        messages.send(player, "claim.entering", toClaim);
                    }
                } else {
                    messages.send(player, "claim.entering", toClaim);
                }
            }
        }
    }
}
