// path: src/main/java/com/snazzyatoms/proshield/plots/ClaimMessageListener.java
package com.snazzyatoms.proshield.plots;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.UUID;

/**
 * Handles showing claim enter/leave messages to players.
 */
public class ClaimMessageListener implements Listener {

    private final PlotManager plotManager;
    private final ClaimRoleManager roleManager;

    public ClaimMessageListener(PlotManager plotManager, ClaimRoleManager roleManager) {
        this.plotManager = plotManager;
        this.roleManager = roleManager;
    }

    @EventHandler(ignoreCancelled = true)
    public void onMove(PlayerMoveEvent e) {
        if (e.getFrom().getChunk().equals(e.getTo().getChunk())) return; // only when chunk changes

        Player p = e.getPlayer();
        UUID uuid = p.getUniqueId();

        // Enter new claim
        plotManager.getClaim(e.getTo()).ifPresentOrElse(claim -> {
            String owner = plotManager.ownerName(claim.getOwner());
            String role = roleManager.getRoleName(uuid, claim.getOwner());
            p.sendMessage(prefix() + ChatColor.AQUA + "Now entering " + owner + "'s claim" +
                    (role != null ? " as " + role : "") + ".");
        }, () -> {
            // Left claims
            plotManager.getClaim(e.getFrom()).ifPresent(prev -> {
                p.sendMessage(prefix() + ChatColor.GRAY + "Now leaving " +
                        plotManager.ownerName(prev.getOwner()) + "'s claim.");
            });
        });
    }

    private String prefix() {
        return ChatColor.translateAlternateColorCodes('&',
                com.snazzyatoms.proshield.ProShield.getInstance().getConfig()
                        .getString("messages.prefix", "&3[ProShield]&r "));
    }
}
