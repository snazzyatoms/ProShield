package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class SpawnClaimGuardListener implements Listener {

    private final PlotManager plots;

    public SpawnClaimGuardListener(PlotManager plots) {
        this.plots = plots;
    }

    private boolean isWithinSpawnGuard(Player p) {
        ProShield plugin = plots.getPlugin();
        boolean block = plugin.getConfig().getBoolean("spawn.block-claiming", true);
        if (!block) return false;

        int radius = plugin.getConfig().getInt("spawn.radius", 32);
        if (radius <= 0) return false;

        World w = p.getWorld();
        Location spawn = w.getSpawnLocation();
        return p.getLocation().distanceSquared(spawn) <= (radius * radius);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPreprocess(PlayerCommandPreprocessEvent e) {
        // Blocks claim attempts near spawn for regular players (GUI calls /proshield claim under the hood)
        String msg = e.getMessage().toLowerCase();
        if (!(msg.startsWith("/proshield claim") || msg.startsWith("/ps claim"))) return;

        Player p = e.getPlayer();
        // allow admins/bypass
        if (p.hasPermission("proshield.admin") || p.hasPermission("proshield.bypass")) return;

        if (isWithinSpawnGuard(p)) {
            ProShield plugin = plots.getPlugin();
            int r = plugin.getConfig().getInt("spawn.radius", 32);
            String prefix = plugin.getConfig().getString("messages.prefix", ChatColor.DARK_AQUA + "[ProShield] ");
            p.sendMessage(prefix + ChatColor.RED + "Claiming is disabled within " + r + " blocks of spawn.");
            e.setCancelled(true);
        }
    }
}
