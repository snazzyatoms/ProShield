package com.snazzyatoms.proshield.plots;

import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;
import java.util.Map;

public class ClaimMessageListener implements Listener {

    private final PlotManager plots;
    private final ClaimRoleManager roles;
    private final Map<Player, String> lastKey = new HashMap<>();

    public ClaimMessageListener(PlotManager plots, ClaimRoleManager roles) {
        this.plots = plots;
        this.roles = roles;
    }

    @EventHandler(ignoreCancelled = true)
    public void onMove(PlayerMoveEvent e) {
        if (e.getFrom().getChunk().equals(e.getTo().getChunk())) return;
        Player p = e.getPlayer();
        Chunk ch = e.getTo().getChunk();
        String key = ch.getWorld().getName() + ":" + ch.getX() + ":" + ch.getZ();

        String prev = lastKey.get(p);
        if (key.equals(prev)) return;
        lastKey.put(p, key);

        var c = plots.getClaim(e.getTo());
        if (c.isPresent()) {
            p.sendActionBar("§aEntering claim: §f" + plots.ownerName(c.get().getOwner()));
        } else {
            p.sendActionBar("§7Entering wilderness");
        }
    }
}
