package com.snazzyatoms.proshield.util;

import com.snazzyatoms.proshield.plots.PlotManager;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class ClaimPreviewTask {

    private final Player player;
    private final PlotManager plots;

    public ClaimPreviewTask(Player player, PlotManager plots) {
        this.player = player;
        this.plots = plots;
    }

    public void run(int seconds) {
        Location base = player.getLocation();
        World w = base.getWorld();
        int cx = base.getChunk().getX();
        int cz = base.getChunk().getZ();

        int minX = (cx << 4);
        int maxX = minX + 15;
        int minZ = (cz << 4);
        int maxZ = minZ + 15;

        new BukkitRunnable() {
            int t = seconds * 10; // 10 ticks per second

            @Override
            public void run() {
                if (t-- <= 0 || !player.isOnline()) {
                    cancel();
                    player.sendMessage("§7Preview ended.");
                    return;
                }
                int y = Math.max(w.getHighestBlockYAt(base), base.getBlockY()) + 1;
                // draw the rectangle border with particles
                for (int x = minX; x <= maxX; x += 2) {
                    w.spawnParticle(Particle.VILLAGER_HAPPY, x + 0.5, y, minZ + 0.5, 1, 0, 0, 0, 0);
                    w.spawnParticle(Particle.VILLAGER_HAPPY, x + 0.5, y, maxZ + 0.5, 1, 0, 0, 0, 0);
                }
                for (int z = minZ; z <= maxZ; z += 2) {
                    w.spawnParticle(Particle.VILLAGER_HAPPY, minX + 0.5, y, z + 0.5, 1, 0, 0, 0, 0);
                    w.spawnParticle(Particle.VILLAGER_HAPPY, maxX + 0.5, y, z + 0.5, 1, 0, 0, 0, 0);
                }
            }
        }.runTaskTimer(com.snazzyatoms.proshield.ProShield.getInstance(), 0L, 2L);
    }
}
