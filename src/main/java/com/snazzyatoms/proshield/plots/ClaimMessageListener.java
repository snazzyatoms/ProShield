// path: src/main/java/com/snazzyatoms/proshield/plots/ClaimMessageListener.java
package com.snazzyatoms.proshield.plots;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Shows enter/leave claim messages (very light implementation).
 * You can expand this later (cooldowns, per-player toggles, etc.).
 */
public class ClaimMessageListener implements Listener {

    private final PlotManager plots;
    private final ClaimRoleManager roleManager;

    public ClaimMessageListener(PlotManager plots, ClaimRoleManager roleManager) {
        this.plots = plots;
        this.roleManager = roleManager;
    }

    @EventHandler(ignoreCancelled = true)
    public void onMove(PlayerMoveEvent e) {
        if (e.getFrom().getChunk().equals(e.getTo().getChunk())) return;

        Player p = e.getPlayer();

        // Left-claim message
        plots.getClaim(e.getFrom()).ifPresent(fromClaim -> {
            if (!plots.isClaimed(e.getTo())) {
                p.sendMessage(ChatColor.DARK_GRAY + "You have left " +
                        ChatColor.AQUA + plots.ownerName(fromClaim.getOwner()) + ChatColor.DARK_GRAY + "'s claim.");
            }
        });

        // Enter-claim message
        plots.getClaim(e.getTo()).ifPresent(toClaim -> {
            String ownerName = plots.ownerName(toClaim.getOwner());
            String role = roleManager.getRoleName(toClaim.getOwner(), p.getUniqueId());
            p.sendMessage(ChatColor.GRAY + "You entered " + ChatColor.AQUA + ownerName +
                    ChatColor.GRAY + "'s claim " + ChatColor.DARK_GRAY + "(" + role + ")");
        });
    }
}
