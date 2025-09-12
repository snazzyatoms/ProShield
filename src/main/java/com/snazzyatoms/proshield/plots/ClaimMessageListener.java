package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * ClaimMessageListener
 * - Sends enter/leave messages when players cross claim boundaries.
 * - Supports wilderness toggle from config.
 * - Uses named placeholders for consistency ({claim}).
 */
public class ClaimMessageListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final MessagesUtil messages;

    public ClaimMessageListener(ProShield plugin, PlotManager plotManager, MessagesUtil messages) {
        this.plugin = plugin;
        this.plotManager = plotManager;
        this.messages = messages;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();

        String fromClaim = plotManager.getClaimName(e.getFrom());
        String toClaim   = plotManager.getClaimName(e.getTo());

        if (!fromClaim.equals(toClaim)) {
            // Leaving claim
            if (fromClaim != null && !fromClaim.isEmpty() && !"Wilderness".equalsIgnoreCase(fromClaim)) {
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("claim", fromClaim);
                messages.send(player, "claim.leaving", placeholders);
            }

            // Entering claim / wilderness
            if (toClaim != null && !toClaim.isEmpty()) {
                if ("Wilderness".equalsIgnoreCase(toClaim)) {
                    if (plugin.getConfig().getBoolean("messages.show-wilderness", false)) {
                        messages.send(player, "claim.wilderness");
                    }
                } else {
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("claim", toClaim);
                    messages.send(player, "claim.entering", placeholders);
                }
            }
        }
    }
}
