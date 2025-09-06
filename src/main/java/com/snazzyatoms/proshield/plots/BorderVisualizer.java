package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class BorderVisualizer {

    private final ProShield plugin;

    public BorderVisualizer(ProShield plugin) { this.plugin = plugin; }

    public void showChunkBorder(Player p, Location loc) {
        if (!plugin.getConfig().getBoolean("visual.borders-enabled", true)) return;

        World w = loc.getWorld();
        int cx = loc.getChunk().getX();
        int cz = loc.getChunk().getZ();

        int minX = cx << 4;
        int minZ = cz << 4;
        int maxX = minX + 15;
        int maxZ = minZ + 15;

        int step = Math.max(1, plugin.getConfig().getInt("visual.step", 2));
        int ticks = Math.max(20, plugin.getConfig().getInt("visual.ticks", 60));

        new BukkitRunnable() {
            int t = 0;
            @Override public void run() {
                if (t++ >= ticks) { cancel(); return; }
                double y = p.getLocation().getY() + 0.2;
                Particle.DustOptions dust = new Particle.DustOptions(Color.AQUA, 1.2f);
                for (int x = minX; x <= maxX; x += step) {
                    w.spawnParticle(Particle.REDSTONE, new Location(w, x + 0.5, y, minZ + 0.5), 1, dust);
                    w.spawnParticle(Particle.REDSTONE, new Location(w, x + 0.5, y, maxZ + 0.5), 1, dust);
                }
                for (int z = minZ; z <= maxZ; z += step) {
                    w.spawnParticle(Particle.REDSTONE, new Location(w, minX + 0.5, y, z + 0.5), 1, dust);
                    w.spawnParticle(Particle.REDSTONE, new Location(w, maxX + 0.5, y, z + 0.5), 1, dust);
                }
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }
}
