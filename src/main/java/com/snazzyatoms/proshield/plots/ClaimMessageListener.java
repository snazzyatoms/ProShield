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
 * ✅ Expanded: "leaving" + "entering" messages
 * ✅ Skips Wilderness entry unless enabled in config
 * ✅ Uses MessagesUtil placeholders
 */
public class ClaimMessageListener implements Listener {

    private final PlotManager plots;
    private final MessagesUtil messages;
    private final ProShield plugin;

    public ClaimMessageListener(ProShield plugin, PlotManager plots) {
        this.plugin = plugin;
        this.plots = plots;
        this.messages = plugin.getMessagesUtil();
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();

        String fromClaim = plots.getClaimName(e.getFrom());
        String toClaim = plots.getClaimName(e.getTo());

        if (!fromClaim.equals(toClaim)) {
            // Leaving message
            if (fromClaim != null && !fromClaim.isEmpty() && !"Wilderness".equalsIgnoreCase(fromClaim)) {
                messages.send(p, "claim.leaving", fromClaim);
            }

            // Entering message
            if (toClaim != null && !toClaim.isEmpty()) {
                if ("Wilderness".equalsIgnoreCase(toClaim)) {
                    if (plugin.getConfig().getBoolean("messages.show-wilderness", false)) {
                        messages.send(p, "claim.entering", toClaim);
                    }
                } else {
                    messages.send(p, "claim.entering", toClaim);
                }
            }
        }
    }
}
