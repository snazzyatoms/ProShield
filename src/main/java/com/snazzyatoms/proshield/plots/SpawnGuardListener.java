package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class SpawnGuardListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plots;

    public SpawnGuardListener(ProShield plugin, PlotManager plots) {
        this.plugin = plugin;
        this.plots = plots;
    }

    @EventHandler(ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent e) {
        String msg = e.getMessage().toLowerCase();
        if (!msg.startsWith("/proshield claim")) return;
        if (!plugin.getConfig().getBoolean("spawn.block-claiming", true)) return;

        Player p = e.getPlayer();
        Location l = p.getLocation();
        World w = l.getWorld();
        Location spawn = w.getSpawnLocation();
        int r = plugin.getConfig().getInt("spawn.radius", 32);
        if (l.distanceSquared(spawn) <= (r * r)) {
            p.sendMessage("§cClaiming is disabled within " + r + " blocks of spawn.");
            e.setCancelled(true);
        }
    }
}
