package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

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
        Player p = e.getPlayer();

        String fromClaim = plotManager.getClaimName(e.getFrom());
        String toClaim   = plotManager.getClaimName(e.getTo());

        if (!fromClaim.equals(toClaim)) {
            if (fromClaim != null && !fromClaim.isEmpty() && !"Wilderness".equalsIgnoreCase(fromClaim)) {
                messages.send(p, "claim.leaving", fromClaim);
            }

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
