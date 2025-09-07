// path: src/main/java/com/snazzyatoms/proshield/plots/ClaimMessageListener.java
package com.snazzyatoms.proshield.plots;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class ClaimMessageListener implements Listener {

    private final PlotManager plots;
    private final ClaimRoleManager roleManager;

    public ClaimMessageListener(PlotManager plots, ClaimRoleManager roleManager) {
        this.plots = plots;
        this.roleManager = roleManager;
    }

    @EventHandler(ignoreCancelled = true)
    public void onMove(PlayerMoveEvent e) {
        Chunk from = e.getFrom().getChunk();
        Chunk to   = e.getTo().getChunk();
        if (from.getX() == to.getX() && from.getZ() == to.getZ() && from.getWorld().equals(to.getWorld())) return;

        Player p = e.getPlayer();
        var loc = e.getTo();

        plots.getClaim(loc).ifPresentOrElse(c -> {
            String owner = plots.ownerName(c.getOwner());
            p.sendMessage(ChatColor.DARK_AQUA + "[ProShield] " + ChatColor.AQUA + "Entering claim owned by " + owner);
        }, () -> {
            p.sendMessage(ChatColor.DARK_AQUA + "[ProShield] " + ChatColor.GRAY + "Entering wilderness");
        });
    }
}
